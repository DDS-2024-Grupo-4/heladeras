package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import ar.edu.utn.dds.k3003.facades.dtos.EstadoViandaEnum;
import ar.edu.utn.dds.k3003.facades.dtos.RetiroDTO;
import ar.edu.utn.dds.k3003.model.Heladera;
import ar.edu.utn.dds.k3003.utils.utilsNotifIncidentAndEvents;
import ar.edu.utn.dds.k3003.facades.dtos.ViandaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

public class TESTretirar {

    private Fachada fachada;
    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;
    private utilsNotifIncidentAndEvents utilsNotifIncidentAndEvents;
    private FachadaViandas fachadaViandas;

    @BeforeEach
    public void setUp() {
        entityManagerFactory = mock(EntityManagerFactory.class);
        entityManager = mock(EntityManager.class);
        EntityTransaction transaction = mock(EntityTransaction.class);
        fachadaViandas = mock(FachadaViandas.class);
        ViandaDTO viandaMock = mock(ViandaDTO.class);
        when(viandaMock.getCodigoQR()).thenReturn("qrVianda1");

        when(fachadaViandas.buscarXQR("qrVianda1")).thenReturn(viandaMock);
        when(fachadaViandas.modificarEstado(anyString(), any(EstadoViandaEnum.class))).thenReturn(viandaMock) ;
        when(fachadaViandas.modificarHeladera(anyString(), anyInt())).thenReturn(viandaMock);

        fachada = new Fachada(entityManagerFactory);
        fachada.setViandasProxy(fachadaViandas);
        utilsNotifIncidentAndEvents = mock(utilsNotifIncidentAndEvents.class);

        when(entityManager.getTransaction()).thenReturn(transaction);
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
    }

    @Test
    public void testRetirar_yNotificarColaborador() throws Exception {
        Heladera heladera = mock(Heladera.class);
        ViandaDTO viandaDTO = mock(ViandaDTO.class);
        Integer nViandasDisponibles = 5;
        when(heladera.cantidadDeViandas()).thenReturn(5);
        when(entityManager.find(Heladera.class, 1)).thenReturn(heladera);
        when(heladera.getColaboradorIDsuscripcionNViandasDisponiblesFiltradoByN(anyInt())).thenReturn(createColaboradorMap(nViandasDisponibles));

        // Preparamos el RetiroDTO
        RetiroDTO retiroDTO = new RetiroDTO("qrVianda1","TarjetaNiidea",1);
        when(fachadaViandas.buscarXQR(retiroDTO.getQrVianda())).thenReturn(viandaDTO);

        // Ejecutamos el método
        fachada.retirar(retiroDTO);

        // Verificamos que se notificó a los colaboradores correctos
        verify(utilsNotifIncidentAndEvents, times(1)).notificarAColaboradorDeSuSuscripcion(argThat(suscripcionDTO ->
                suscripcionDTO.colaboradorId.equals(1L) && suscripcionDTO.cantidadN.equals(10)
        ));
        verify(utilsNotifIncidentAndEvents, times(1)).notificarAColaboradorDeSuSuscripcion(argThat(suscripcionDTO ->
                suscripcionDTO.colaboradorId.equals(2L) && suscripcionDTO.cantidadN.equals(5)
        ));
        verify(utilsNotifIncidentAndEvents, never()).notificarAColaboradorDeSuSuscripcion(argThat(suscripcionDTO ->
                suscripcionDTO.colaboradorId.equals(3L))
        ); // Este no debería recibir notificación
    }

    private Map<Long, Integer> createColaboradorMap(Integer nViandas) {
        Map<Long, Integer> colaboradores = new HashMap<>();
        colaboradores.put(1L, 10);  // Quiere ser avisado si hay 10 o menos
        colaboradores.put(2L, 5); // Quiere ser avisado si hay 5 o menos
        colaboradores.put(3L, 1); // Quiere ser avisado si hay 2 o menos

        return colaboradores.entrySet().stream()
                .filter(entry -> entry.getValue() >= nViandas) // Filtro los colaboradores que quieren saber si hay menos de nViandasDisponibles
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)); // Creo un nuevo Map

    }
}