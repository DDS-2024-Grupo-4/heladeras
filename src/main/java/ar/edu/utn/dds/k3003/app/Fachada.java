package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.model.TipoSuscripcion;
import ar.edu.utn.dds.k3003.utils.utilsNotifIncidentAndEvents;
import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import ar.edu.utn.dds.k3003.facades.dtos.*;
import ar.edu.utn.dds.k3003.model.DTO.SuscripcionDTO;
import ar.edu.utn.dds.k3003.model.Heladera;
import ar.edu.utn.dds.k3003.model.SensorTemperatura;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.*;
import static ar.edu.utn.dds.k3003.app.WebApp.incidenteService;

public class Fachada implements ar.edu.utn.dds.k3003.facades.FachadaHeladeras {
    private FachadaViandas fachadaViandas;
    private EntityManagerFactory entityManagerFactory;

    public Fachada() {
    }

    public Boolean heladeraHabilitada(Integer heladeraID){
        Heladera heladera = obtenerHeladera(heladeraID);
        return heladera.estaActiva();
    }
    public Fachada(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public Heladera obtenerHeladera(Integer heladeraID) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            return entityManager.find(Heladera.class, heladeraID);
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new RuntimeException("Error al obtener la Heladera con ID: " + heladeraID, e);
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }

    }

    public Boolean existeHeladera(Integer heladeraID) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Heladera heladera = entityManager.find(Heladera.class, heladeraID);
            return heladera != null;
        } catch (Exception e) {
            return false;
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }

    public List<Heladera> obtenerTodasLasHeladeras() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            String jpql = "SELECT h FROM Heladera h";
            TypedQuery<Heladera> query = entityManager.createQuery(jpql, Heladera.class);
            List<Heladera> heladeras = query.getResultList();
            return heladeras;
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }

    @Override
    public HeladeraDTO agregar(HeladeraDTO heladeraDTO) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Heladera heladera = new Heladera(heladeraDTO.getNombre());
            SensorTemperatura sensor = new SensorTemperatura(heladera);
            heladera.setSensorTemperatura(sensor);
            entityManager.persist(heladera);
            return new HeladeraDTO(heladera.getHeladeraId(), heladera.getNombre(), heladera.cantidadDeViandas());
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al agregar la heladera: " + e.getMessage());
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }

    @Override
    public void depositar(Integer heladeraID, String qrVianda) throws NoSuchElementException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Heladera heladera = entityManager.find(Heladera.class, heladeraID);
            if (heladera == null) {
                throw new NoSuchElementException("No se encontró la heladera con ID: " + heladeraID);
            }
            ViandaDTO vianda = fachadaViandas.buscarXQR(qrVianda);

            fachadaViandas.modificarEstado(vianda.getCodigoQR(), EstadoViandaEnum.DEPOSITADA);
            fachadaViandas.modificarHeladera(vianda.getCodigoQR(), heladeraID);

            heladera.guardarVianda(qrVianda);
            Integer cantidadDeViandasFaltantesPorRetirar = heladera.cantidadDeViandasQueQuedanHastaLlenar();
            List<Long> colaboradoresParaAvisar = heladera.getColaboradorIDsuscripcionCantidadFaltantesViandasByNumber();
            for (Long colaboradorID : colaboradoresParaAvisar) {
                Long colaboradorId = colaboradorID;
                SuscripcionDTO suscripcionDTO = new SuscripcionDTO( colaboradorId, heladera.getHeladeraId(), TipoSuscripcion.FaltanteViandas, cantidadDeViandasFaltantesPorRetirar);
                utilsNotifIncidentAndEvents.notificarAColaboradorDeSuSuscripcion(suscripcionDTO);
            }

            entityManager.merge(heladera);

        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al agregar la heladera: " + e.getMessage());
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }

    @Override
    public Integer cantidadViandas(Integer heladeraID) throws NoSuchElementException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Heladera heladera = entityManager.find(Heladera.class, heladeraID);
            return heladera.cantidadDeViandas();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar la heladera: " + e.getMessage());
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }
    //TODO: REVISAR ESTA FUNCION ME HACE RUIDO
    public List<SuscripcionDTO> obtenerSuscripciones(Integer heladeraID){
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Heladera heladera = entityManager.find(Heladera.class, heladeraID);
            List<SuscripcionDTO> suscripciones = new ArrayList<>();
            heladera.getColaboradorIDsuscripcionCantidadFaltantesViandas().forEach((colaboradorId, cantidadN) -> {
                SuscripcionDTO suscripcionDTO = new SuscripcionDTO();
                suscripcionDTO.colaboradorId = colaboradorId;
                suscripcionDTO.heladeraId = heladeraID;
                suscripcionDTO.tipoSuscripcion = TipoSuscripcion.FaltanteViandas;
                suscripcionDTO.cantidadN = cantidadN;
                suscripciones.add(suscripcionDTO);
            });
            heladera.getColaboradorIDsuscripcionDesperfectoHeladera().forEach(colaboradorId -> {
                SuscripcionDTO suscripcionDTO = new SuscripcionDTO();
                suscripcionDTO.colaboradorId = colaboradorId;
                suscripcionDTO.heladeraId = heladeraID;
                suscripcionDTO.tipoSuscripcion = TipoSuscripcion.HeladeraDesperfecto;
                suscripciones.add(suscripcionDTO);
            });
            heladera.getColaboradorIDsuscripcionNViandasDisponibles().forEach((colaboradorId, cantidadN) -> {
                SuscripcionDTO suscripcionDTO = new SuscripcionDTO();
                suscripcionDTO.colaboradorId = colaboradorId;
                suscripcionDTO.heladeraId = heladeraID;
                suscripcionDTO.tipoSuscripcion = TipoSuscripcion.FaltanteViandas;
                suscripcionDTO.cantidadN = cantidadN;
                suscripciones.add(suscripcionDTO);
            });

            return suscripciones;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar la heladera: " + e.getMessage());
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }

    @Override
    public void retirar(RetiroDTO retiroDTO) throws NoSuchElementException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {

            Heladera heladera = entityManager.find(Heladera.class, retiroDTO.getHeladeraId());
            if (heladera == null) {
                throw new NoSuchElementException("Heladera no encontrada");
            }
            ViandaDTO vianda = fachadaViandas.buscarXQR(retiroDTO.getQrVianda());
            if (vianda == null) {
                throw new NoSuchElementException("Vianda no encontrada");
            }

            fachadaViandas.modificarEstado(vianda.getCodigoQR(), EstadoViandaEnum.RETIRADA);
            fachadaViandas.modificarHeladera(vianda.getCodigoQR(), -1);  // -1 SIGNIFICA SET NULL

            heladera.retirarVianda(retiroDTO.getQrVianda());
            Integer cantidadDeViandasFaltantesPorRetirar = heladera.cantidadDeViandas();
            Map<Long, Integer> colaboradoresParaAvisar = heladera.getColaboradorIDsuscripcionNViandasDisponiblesFiltradoByN(cantidadDeViandasFaltantesPorRetirar);
            System.out.println(colaboradoresParaAvisar);
            for (Map.Entry<Long, Integer> entry : colaboradoresParaAvisar.entrySet()) {
                Long colaboradorId = entry.getKey();
                Integer viandasDisponibles = entry.getValue();
                SuscripcionDTO suscripcionDTO = new SuscripcionDTO( colaboradorId, heladera.getHeladeraId(), TipoSuscripcion.ViandasDisponibles, viandasDisponibles);
                utilsNotifIncidentAndEvents.notificarAColaboradorDeSuSuscripcion(suscripcionDTO);
            }
            entityManager.merge(heladera);

        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al procesar el retiro: " + e.getMessage());
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }

    @Override
    public void temperatura(TemperaturaDTO temperaturaDTO) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Heladera heladera = entityManager.find(Heladera.class, temperaturaDTO.getHeladeraId());
            SensorTemperatura sensor = heladera.getSensorTemperatura();

            if (sensor == null) {
                throw new NoSuchElementException("Sensor no encontrado para la Heladera con ID: " + temperaturaDTO.getHeladeraId());
            }

            System.out.println("\n Temperatura recibida para setear: " + temperaturaDTO.getTemperatura());
            sensor.setNuevaTemperatura(temperaturaDTO.getTemperatura(), temperaturaDTO.getFechaMedicion());
            heladera.setTiempoUltimaTemperaturaRecibida(temperaturaDTO.getFechaMedicion());

            System.out.println(heladera);
            System.out.printf("Tiempo restante hasta error -> " + heladera.tiempoRestanteHastaError());
            entityManager.merge(sensor);
            entityManager.merge(heladera);
            entityManager.getTransaction().commit();
            // Verificar exceso de temperatura
            if (incidenteService.verificarExcesoTemperatura(heladera, temperaturaDTO)) {
                System.out.println("Alerta: Alta temperatura detectada.\n");
                throw new RuntimeException("Alerta: Alta temperatura detectada.\n");
            }
            // Verificar bajo temperatura
            else if (incidenteService.verificarBajoTemperatura(heladera, temperaturaDTO)) {
                System.out.println("Alerta: Bajo temperatura detectada.\n");
                throw new RuntimeException("Alerta: Bajo temperatura detectada.\n");
            }
            entityManager.getTransaction().begin();

            // Actualizar tiempos de temperatura si no hay incidentes
            heladera.setTiempoUltimaTemperaturaMaxima(temperaturaDTO.getFechaMedicion());
            heladera.setTiempoUltimaTemperaturaMinima(temperaturaDTO.getFechaMedicion());


            entityManager.merge(sensor);
            entityManager.merge(heladera);
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al guardar la temperatura en la heladera: " + e.getMessage());
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().commit();
            }
            entityManager.close();
        }
    }

    @Override
    public List<TemperaturaDTO> obtenerTemperaturas(Integer heladeraID) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Heladera heladera = entityManager.find(Heladera.class, heladeraID);
            Map<Integer, LocalDateTime> temperaturas = heladera.obtenerTemperaturaHeladera();
            List<TemperaturaDTO> temperaturasMapped = new ArrayList<>();

            if (!temperaturas.isEmpty()) {
                temperaturas.forEach((temperatura, tiempo) -> {
                    TemperaturaDTO temperaturaDTO = new TemperaturaDTO(
                        temperatura,
                        heladera.getHeladeraId(),
                        tiempo
                    );
                    temperaturasMapped.add(temperaturaDTO);
                });
                return temperaturasMapped;
            } else {
                throw new RuntimeException("Error No dispone de Temperaturas actualmente");
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Error No dispone de Temperaturas actualmente");
        }
        finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }

    public void suscribirse(SuscripcionDTO suscripcionDTO) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Heladera heladera = entityManager.find(Heladera.class, suscripcionDTO.heladeraId);
            if (heladera == null) {
                throw new NoSuchElementException("No se encontró la heladera con ID: " + suscripcionDTO.heladeraId);
            }
            switch (suscripcionDTO.tipoSuscripcion) {
                case ViandasDisponibles:
                    heladera.setColaboradorIDsuscripcionNViandasDisponibles(suscripcionDTO.colaboradorId, suscripcionDTO.cantidadN);
                    break;
                case FaltanteViandas:
                    heladera.setColaboradorIDsuscripcionCantidadFaltantesViandas(suscripcionDTO.colaboradorId, suscripcionDTO.cantidadN);
                    break;
                case HeladeraDesperfecto:
                    heladera.setColaboradorIDsuscripcionDesperfectoHeladera(suscripcionDTO.colaboradorId);
                    break;
                default:
                    throw new RuntimeException("Tipo de suscripción no válido: " + suscripcionDTO.tipoSuscripcion);
            }
        entityManager.merge(heladera);
        }
        catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al suscribirse a la heladera: " + e.getMessage());
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }

    public void eliminarSuscripcion(SuscripcionDTO suscripcionDTO){
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Heladera heladera = entityManager.find(Heladera.class, suscripcionDTO.heladeraId);
            if (heladera == null) {
                throw new NoSuchElementException("No se encontró la heladera con ID: " + suscripcionDTO.heladeraId);
            }
            switch (suscripcionDTO.tipoSuscripcion) {
                case ViandasDisponibles:
                    heladera.eliminarColaboradorIDsuscripcionNViandasDisponibles(suscripcionDTO.colaboradorId);
                    break;
                case FaltanteViandas:
                    heladera.eliminarColaboradorIDsuscripcionCantidadFaltantesViandas(suscripcionDTO.colaboradorId);
                    break;
                case HeladeraDesperfecto:
                    heladera.eliminarColaboradorIDsuscripcionDesperfectoHeladera(suscripcionDTO.colaboradorId);
                    break;
                default:
                    throw new RuntimeException("Tipo de suscripción no válido: " + suscripcionDTO.tipoSuscripcion);
            }
            entityManager.merge(heladera);
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar la suscripción a la heladera: " + e.getMessage());
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }

    public void avisoIncidenteDesperfectoHeladera(Integer heladeraID) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Heladera heladera = entityManager.find(Heladera.class, heladeraID);
            if (heladera == null) {
                throw new NoSuchElementException("No se encontró la heladera con ID: " + heladeraID);
            }
            // Manejo del aviso a los suscriptores del evento
            for (Long colaboradorId : heladera.getColaboradorIDsuscripcionDesperfectoHeladera()) {
                SuscripcionDTO suscripcionDTO = new SuscripcionDTO(colaboradorId, heladeraID, TipoSuscripcion.HeladeraDesperfecto);
                utilsNotifIncidentAndEvents.notificarAColaboradorDeSuSuscripcion(suscripcionDTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al avisar del Incidente a los Suscriptores: " + e.getMessage());
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close(); // Cerrar en finally
        }
    }

    public void inhabilitarHeladera(Integer heladeraID){this.habilitarHeladera(heladeraID,false);
    }
    public void habilitarHeladera(Integer heladeraID,Boolean habilitar){
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Heladera heladera = entityManager.find(Heladera.class, heladeraID);
            if (heladera == null) {
                throw new NoSuchElementException("No se encontró la heladera con ID: " + heladeraID);
            }
            //DEPENDIENDO SI HABILITAR ESTA EN TRUE O FALSE, HABILITO O INHABILITO LA HELADERA
            System.out.println("Estado Actual de la Heladera: " + heladera.estaActiva());
            if(habilitar) heladera.habilitar(); else heladera.inhabilitar();
            System.out.println("Pasando al estado ->: " + heladera.estaActiva());
            entityManager.merge(heladera);
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al habilitar la heladera: " + e.getMessage());
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }

    public void eliminarHeladera(Integer heladeraId){
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Heladera heladera = entityManager.find(Heladera.class, heladeraId);

                if (heladera != null) {
                    entityManager.remove(heladera);
                } else {
                    throw new RuntimeException("No se encontró la heladera con ID: " + heladeraId);
                }
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar la heladera: " + e.getMessage());
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }

    @Override
    public void setViandasProxy(FachadaViandas fachadaViandas) {
        this.fachadaViandas = fachadaViandas;
    }
}
