package ar.edu.utn.dds.k3003.Service;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.TemperaturaDTO;
import ar.edu.utn.dds.k3003.model.Heladera;
import ar.edu.utn.dds.k3003.model.Incidente;
import ar.edu.utn.dds.k3003.model.TipoIncidente;
import ar.edu.utn.dds.k3003.utils.utilsMetrics;
import ar.edu.utn.dds.k3003.utils.utilsNotifIncidentAndEvents;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

/***
 *
 * Servicio especializado en verificar y controlar desperfectos
 * en las heladeras, como exceso de temperatura, tiempo de espera
 * máximo de temperaturas y movimiento de heladera.
 * Ademas de guardar los incidentes en la base de datos.
 *
 * */

public class IncidenteService {

  private EntityManagerFactory entityManagerFactory;
  private final Fachada fachadaHeladera;

  public IncidenteService(EntityManagerFactory entityManagerFactory, Fachada fachadaHeladera){
    this.entityManagerFactory = entityManagerFactory;
    this.fachadaHeladera = fachadaHeladera;
  }

  public void incidenteEnHeladera(Incidente incidente){
      try{
        System.out.println("\nInhabilitando la heladera");
        fachadaHeladera.inhabilitarHeladera(incidente.getHeladeraId());
        if (!incidenteYaNotificado(incidente)){
          utilsMetrics.fallaHeladeras();
          System.out.println("Incidente sin notificar, procediendo a notificarlo..");
          //Aca le avisamos a todos los suscriptores de una falla
        fachadaHeladera.avisoIncidenteDesperfectoHeladera(incidente.getHeladeraId());
          //Aca nos encargamos de avisar a los topics en caso que sea necesario
        Incidente incidenteGuardado = guardadoNuevoIncidente(incidente);
        if (incidenteGuardado.getTipoIncidente().equals(TipoIncidente.FallaEnConexion)){
          utilsNotifIncidentAndEvents.notificarFallaEnConexionEnTopic(incidenteGuardado);
        }
        if (incidenteGuardado.getTipoIncidente().equals(TipoIncidente.ExcesoDeTemperatura)){
          utilsNotifIncidentAndEvents.notificarExcesoTiempoTemperaturaMaximaEnTopic(incidenteGuardado);
        }
        if (incidenteGuardado.getTipoIncidente().equals(TipoIncidente.Fraude)){
          utilsNotifIncidentAndEvents.notificarFraudeHeladeraEnTopic(incidenteGuardado);
        }
        if (incidenteGuardado.getTipoIncidente().equals(TipoIncidente.FallaDeHeladera)){
          utilsNotifIncidentAndEvents.notificarFallaEnHeladeraTopic(incidenteGuardado);
        }
        utilsMetrics.metricaIncidenteHeladera(true);
      }
      }catch (Exception e){
        e.printStackTrace();
        throw new RuntimeException("Error al realizar el proceso de incidenteEnHeladera: " + incidente + " " + e.getMessage());
      }
  }

  public void fallaEnHeladera(Integer heladeraId){
    Incidente incidente = new Incidente(TipoIncidente.FallaDeHeladera, heladeraId);
    incidenteEnHeladera(incidente);
  }

  public void reparacionHeladera(Integer heladeraId){
    fachadaHeladera.habilitarHeladera(heladeraId,true);
    utilsNotifIncidentAndEvents.notificarArregloDeHeladeraEnTopic(heladeraId);
    utilsMetrics.metricaIncidenteHeladera(false);
  }

  public Boolean verificarExcesoTemperatura(Heladera heladera, TemperaturaDTO temperaturaDTO) {
    Integer temperaturaActual = temperaturaDTO.getTemperatura();
    Integer temperaturaMaximaPermitida = heladera.getTemperaturaMaxima();

    if (temperaturaActual >= temperaturaMaximaPermitida) {
      // Si ha pasado el tiempo máximo desde la última temperatura máxima registrada
      if (heladera.superoTiempoMaximoTemperaturaMaxima() || heladera.superoTiempoMaximoTemperaturaMinima()) {
        Incidente incidente = new Incidente(TipoIncidente.ExcesoDeTemperatura, heladera.getHeladeraId());
        incidenteEnHeladera(incidente);
      }
      System.out.println("Temperatura máxima excedida en heladera ID: " + heladera.getHeladeraId());
     return true;
    }
    System.out.println("Temperatura máxima NO excedida en heladera ID: " + heladera.getHeladeraId());
    return false;
  }

  public Boolean verificarBajoTemperatura(Heladera heladera, TemperaturaDTO temperaturaDTO) {
    Integer temperaturaActual = temperaturaDTO.getTemperatura();
    Integer temperaturaMinimaPermitida = heladera.getTemperaturaMinima();

    // Si la temperatura actual está por debajo de la mínima permitida y ha pasado el tiempo máximo
    if (temperaturaActual < temperaturaMinimaPermitida){
        if(heladera.superoTiempoMaximoTemperaturaMinima()) {
          // Creación de incidente que deshabilita la heladera y notifica a los colaboradores
          Incidente incidente = new Incidente(TipoIncidente.ExcesoDeTemperatura, heladera.getHeladeraId());
          incidenteEnHeladera(incidente);
        }
      return true;
    }
    return false;
  }

  public void controlarTiempoDeEsperaMaximoTemperaturas() {
    try {
      List<Heladera> heladeras = fachadaHeladera.obtenerTodasLasHeladeras();
      for (Heladera heladera : heladeras) {
        if (heladera.estaActiva() != false) {
        System.out.println("\n Revisando heladera ID: " + heladera.getHeladeraId());
        System.out.println("Último tiempo " + heladera.getTiempoUltimaTemperaturaRecibida());

        // Si el tiempo de última temperatura máxima es diferente a nulo
        // y el tiempo máximo último recibido es menor al tiempo máximo
        if (heladera.getTiempoUltimaTemperaturaRecibida() != null &&
            heladera.superoTiempoUltimaTemperaturaRecibida()) {

          System.out.println("Se ha excedido el tiempo máximo para la heladera ID: " + heladera.getHeladeraId());

          // Creación de incidente que deshabilita la heladera y notifica a los colaboradores
          Incidente incidente = new Incidente(TipoIncidente.FallaEnConexion, heladera.getHeladeraId(), heladera.getTiempoUltimaTemperaturaRecibida());
          incidenteEnHeladera(incidente);
          System.out.println("Incidente creado para la heladera ID: " + heladera.getHeladeraId());
        } else {
          System.out.println("La heladera ID: " + heladera.getHeladeraId() + " está dentro del tiempo permitido.\n");
        }
      }}
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
      if (heladera.estaActiva() != false) {
        //Creacion de incidente que dehabilita la heladera y notifica a los colaboradores
        Incidente incidente = new Incidente(TipoIncidente.Fraude, heladeraID);
        incidenteEnHeladera(incidente);
      }
      else{ System.out.println("La Heladera "+ heladeraID + " Detectò movimiento, aun estando desactivada. \n");}
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error en movimiento de heladera " + heladeraID + ": " + e.getMessage());
    }
  }

  public Incidente guardadoNuevoIncidente(Incidente incidente) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();
    try {
      return entityManager.merge(incidente);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error al guardar el nuevo incidente: " + e.getMessage());
    }
    finally {
      entityManager.getTransaction().commit();
      entityManager.close();
    }
  }

  public Boolean incidenteYaNotificado(Incidente incidenteABuscar){
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();
    try {
      Long count = (Long) entityManager.createQuery(
              "SELECT COUNT(i) FROM Incidente i WHERE i.tipoIncidente = :tipoIncidente AND i.heladeraId = :heladeraId AND i.fechaIncidente = :fechaIncidente")
          .setParameter("tipoIncidente", incidenteABuscar.getTipoIncidente())
          .setParameter("heladeraId", incidenteABuscar.getHeladeraId())
          .setParameter("fechaIncidente", incidenteABuscar.getFechaIncidente())
          .getSingleResult();
      return count > 0;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error al guardar el nuevo incidente: " + e.getMessage());
    }
    finally {
      entityManager.getTransaction().commit();
      entityManager.close();
    }
  }

}
