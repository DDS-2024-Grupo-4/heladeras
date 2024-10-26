package ar.edu.utn.dds.k3003.Service;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.TemperaturaDTO;
import ar.edu.utn.dds.k3003.model.DTO.IncidenteDTO;
import ar.edu.utn.dds.k3003.model.Heladera;
import ar.edu.utn.dds.k3003.utils.utilsMetrics;
import ar.edu.utn.dds.k3003.utils.utilsNotifIncidentAndEvents;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.List;

public class IncidenteService {

  private EntityManagerFactory entityManagerFactory;
  private final Fachada fachadaHeladera;

  public IncidenteService(EntityManagerFactory entityManagerFactory, Fachada fachadaHeladera){
    this.entityManagerFactory = entityManagerFactory;
    this.fachadaHeladera = fachadaHeladera;
  }
  //DeshabilitaHeladera para repararla
  public void incidenteEnHeladera(Integer heladeraId){}
  //habilitaHeladera por la reparacion, recordar heladera.habilitar()
  public void reparacionHeladera(Integer heladeraId){}

  public Boolean verificarExcesoTemperatura(Heladera heladera, TemperaturaDTO temperaturaDTO) {
    LocalDateTime ultimaTemperaturaMaximaTiempo = heladera.getTiempoUltimaTemperaturaMaxima();
    Integer temperaturaActual = temperaturaDTO.getTemperatura();
    Integer temperaturaMaximaPermitida = heladera.getTemperaturaMaxima();

    // Si la temperatura actual supera la máxima permitida y ha pasado el tiempo máximo
    if (temperaturaActual > temperaturaMaximaPermitida &&
        ultimaTemperaturaMaximaTiempo.plusMinutes(heladera.tiempoMaximoTemperaturaMaxima()).isBefore(temperaturaDTO.getFechaMedicion())) {

      IncidenteDTO incidente = new IncidenteDTO("Exceso de temperatura", heladera.getHeladeraId());

      //inhabilitación de la heladera
      fachadaHeladera.inhabilitarHeladera(heladera.getHeladeraId());

      // Acciones interesadas en el incidente
      fachadaHeladera.avisoIncidenteDesperfectoHeladera(heladera.getHeladeraId());
      utilsNotifIncidentAndEvents.notificarExcesoTiempoTemperaturaMaximaEnTopic(incidente);
      utilsMetrics.enviarExcesoTemperaturaHeladera(heladera.getHeladeraId());

      return true;
    }
    return false;
  }

  public void controlarTiempoDeEsperaMaximoTemperaturas() {
    try {
      List<Heladera> heladeras = fachadaHeladera.obtenerTodasLasHeladeras();
      for (Heladera heladera : heladeras) {
        // Si el tiempo de última temperatura máxima es diferente a nulo
        // y el tiempo máximo último recibido es menor al tiempo máximo
        if (heladera.getTiempoUltimaTemperaturaMaxima() != null &&
            heladera.getTiempoUltimaTemperaturaMaxima().plusMinutes(heladera.tiempoMaximoUltimoReciboTemperatura()).isBefore(LocalDateTime.now())) {

          IncidenteDTO incidente = new IncidenteDTO("Exceso de tiempo de espera de temperaturas", heladera.getHeladeraId());

          // inhabilitación de la heladera
          fachadaHeladera.inhabilitarHeladera(heladera.getHeladeraId());
          // Acciones interesadas en el incidente
          fachadaHeladera.avisoIncidenteDesperfectoHeladera(heladera.getHeladeraId());
          utilsNotifIncidentAndEvents.notificarFallaEnConexionEnTopic(incidente);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error al controlar el tiempo de espera máximo de temperaturas: " + e.getMessage());
    }
  }

  public void movimientoHeladera(Integer heladeraID) {
    try {
      Heladera heladera = fachadaHeladera.obtenerHeladera(heladeraID);

      if (heladera == null) {
        throw new RuntimeException("Heladera no encontrada: " + heladeraID);
      }

      if (!heladera.getHeladeraAbierta()) {
        IncidenteDTO incidente = new IncidenteDTO("Fraude Heladera", heladeraID);
        // inhabilitación de la heladera
        fachadaHeladera.inhabilitarHeladera(heladera.getHeladeraId());
        // Acciones interesadas en el incidente
        fachadaHeladera.avisoIncidenteDesperfectoHeladera(heladera.getHeladeraId());
        utilsNotifIncidentAndEvents.notificarFraudeHeladeraEnTopic(incidente);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error en movimiento de heladera " + heladeraID + ": " + e.getMessage());
    }
  }


}
