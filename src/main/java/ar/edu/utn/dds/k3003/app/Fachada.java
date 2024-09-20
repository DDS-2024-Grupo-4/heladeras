package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import ar.edu.utn.dds.k3003.facades.dtos.*;
import ar.edu.utn.dds.k3003.model.Heladera;
import ar.edu.utn.dds.k3003.model.SensorTemperatura;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.*;

public class Fachada implements ar.edu.utn.dds.k3003.facades.FachadaHeladeras {
    private FachadaViandas fachadaViandas;
    private EntityManagerFactory entityManagerFactory;

    public Fachada() {
    }

    public Fachada(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public HeladeraDTO obtenerHeladera(Integer heladeraID) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {

            Heladera heladera = entityManager.find(Heladera.class, heladeraID);
            HeladeraDTO heladeraDTO = new HeladeraDTO(
                heladera.getHeladeraId(),
                heladera.getNombre(),
                heladera.cantidadDeViandas()
            );
            return heladeraDTO;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new RuntimeException("Error al obtener la Heladera con ID: " + heladeraID, e);
        } finally {
            entityManager.close();
            entityManagerFactory.close();
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
            entityManager.close();
            entityManagerFactory.close();
        }
    }

    public List<Heladera> obtenerTodasLasHeladeras() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            String jpql = "SELECT h FROM Heladera h";
            TypedQuery<Heladera> query = entityManager.createQuery(jpql, Heladera.class);
            List<Heladera> heladeras = query.getResultList();

            return heladeras;
        } finally {
            entityManager.close();
            entityManagerFactory.close();
        }
    }

    @Override
    public HeladeraDTO agregar(HeladeraDTO heladeraDTO) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Heladera heladera = new Heladera(heladeraDTO.getNombre());
            SensorTemperatura sensor = new SensorTemperatura(heladera);
            heladera.setSensor(sensor);
            entityManager.persist(heladera);
            entityManager.getTransaction().commit();
            entityManager.refresh(heladera);
            return new HeladeraDTO(heladera.getHeladeraId(), heladera.getNombre(), heladera.cantidadDeViandas());
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al agregar la heladera: " + e.getMessage());
        } finally {
            entityManager.close();
            entityManagerFactory.close();
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

            entityManager.merge(heladera);
            entityManager.getTransaction().commit();

        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al agregar la heladera: " + e.getMessage());
        } finally {
            entityManager.close();
            entityManagerFactory.close();
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
            entityManager.close();
            entityManagerFactory.close();
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

            entityManager.merge(heladera);
            entityManager.getTransaction().commit();

        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al procesar el retiro: " + e.getMessage());
        } finally {
            entityManager.close();
            entityManagerFactory.close();
        }
    }

    @Override
    public void temperatura(TemperaturaDTO temperaturaDTO) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try{
            Heladera heladera = entityManager.find(Heladera.class, temperaturaDTO.getHeladeraId());
            SensorTemperatura sensor = heladera.getSensor();
            if (sensor == null) {
                throw new NoSuchElementException("Sensor no encontrado para la Heladera con ID: " + temperaturaDTO.getHeladeraId());
            }

            sensor.setNuevaTemperatura(temperaturaDTO.getTemperatura(), temperaturaDTO.getFechaMedicion());

            entityManager.merge(sensor);
            entityManager.getTransaction().commit();

        }catch (Exception e){
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al  guardar la temperatura en la heladera: " + e.getMessage());
        }
        finally {
            entityManager.close();
            entityManagerFactory.close();
        }
    }

    @Override
    public List<TemperaturaDTO> obtenerTemperaturas(Integer heladeraID) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Heladera heladera = entityManager.find(Heladera.class, heladeraID);
            Map<Integer, LocalDateTime> temperaturas = heladera.obtenerTodasLasTemperaturas();
            List<TemperaturaDTO> temperaturasMapped = new ArrayList<>();
            temperaturas.forEach( (temperatura,tiempo) -> {
                TemperaturaDTO temperaturaDTO = new TemperaturaDTO(
                    temperatura,
                    heladera.getHeladeraId(),
                    tiempo
                );
                temperaturasMapped.add(temperaturaDTO);
            } );
            return temperaturasMapped;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Error al encontrar la Heladera " + heladeraID + " "+ e.getMessage());
        }
        finally {
            entityManager.close();
            entityManagerFactory.close();
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

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar la heladera: " + e.getMessage());
        } finally {
            entityManager.close();
            entityManagerFactory.close();
        }
    }

    @Override
    public void setViandasProxy(FachadaViandas fachadaViandas) {
        this.fachadaViandas = fachadaViandas;
    }
}
