package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.Service.IncidenteService;
import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.model.DTO.SuscripcionDTO;
import ar.edu.utn.dds.k3003.model.Heladera;
import ar.edu.utn.dds.k3003.model.Incidente;
import ar.edu.utn.dds.k3003.model.TipoIncidente;
import ar.edu.utn.dds.k3003.utils.utilsNotifIncidentAndEvents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class TESTfalla {

    private IncidenteService incidenteService;
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private Fachada fachadaHeladera;
    private utilsNotifIncidentAndEvents utilsNotifIncidentAndEvents;

    @BeforeEach
    public void setUp() {
        entityManagerFactory = mock(EntityManagerFactory.class);
        entityManager = mock(EntityManager.class);
        EntityTransaction transaction = mock(EntityTransaction.class);
        fachadaHeladera = new Fachada(entityManagerFactory);
        utilsNotifIncidentAndEvents = mock(utilsNotifIncidentAndEvents.class);

        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        when(entityManager.getTransaction()).thenReturn(transaction);

        incidenteService = new IncidenteService(entityManagerFactory, fachadaHeladera);
    }

    @Test
    public void testFalla() {
        // Configurar incidente y mocks
        Incidente incidente = new Incidente(TipoIncidente.ExcesoDeTemperatura, 1);
        Heladera heladeraMock = mock(Heladera.class);
        when(entityManager.find(Heladera.class, 1)).thenReturn(heladeraMock);
        when(entityManager.merge(incidente)).thenReturn(incidente);
        when(heladeraMock.getColaboradorIDsuscripcionDesperfectoHeladera()).thenReturn(this.createColaboradorMap());

        // Mockear el EntityManager y la consulta de incidente Ya notificado
        Query queryMock = mock(Query.class);
        when(entityManager.createQuery(anyString())).thenReturn(queryMock);
        when(queryMock.setParameter(anyString(), any())).thenReturn(queryMock);
        when(queryMock.getSingleResult()).thenReturn(0L); // Simular que no hay incidentes notificados
        doNothing().when(entityManager).close();

        // Ejecutar el método bajo prueba
        incidenteService.incidenteEnHeladera(incidente);

        // Verificar interacciones
        verify(fachadaHeladera).inhabilitarHeladera(1);
        verify(fachadaHeladera).avisoIncidenteDesperfectoHeladera(1);
        verify(utilsNotifIncidentAndEvents, times(3)).notificarAColaboradorDeSuSuscripcion(any()); // tres notificaciones esperadas
    }

    private List<Long> createColaboradorMap() {
        List<Long> colaboradores = new ArrayList<>();
        colaboradores.add(1L);
        colaboradores.add(2L);
        colaboradores.add(3L);
        return colaboradores;
    }
}
