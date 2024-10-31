package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import ar.edu.utn.dds.k3003.model.Heladera;
import ar.edu.utn.dds.k3003.utils.utilsNotifIncidentAndEvents;
import ar.edu.utn.dds.k3003.facades.dtos.ViandaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

public class TESTdepositar {

    private Fachada fachada;
    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;
    private utilsNotifIncidentAndEvents utilsNotifIncidentAndEvents;
    private FachadaViandas fachadaViandas; // Mock de FachadaViandas

    @BeforeEach
    public void setUp() {
        // Inicializa el EntityManagerFactory para pruebas
        entityManagerFactory = mock(EntityManagerFactory.class); // Crear el mock aquí
        entityManager = mock(EntityManager.class);
        EntityTransaction transaction = mock(EntityTransaction.class);
        fachadaViandas = mock(FachadaViandas.class);
        ViandaDTO viandaMock = mock(ViandaDTO.class);
        when(viandaMock.getCodigoQR()).thenReturn("qrVianda");

        // Configuramos el comportamiento de fachadaViandas
        when(fachadaViandas.buscarXQR("qrVianda")).thenReturn(viandaMock); // Devuelve el mock

        // Inicializa la clase bajo prueba
        fachada = new Fachada(entityManagerFactory);
        fachada.setViandasProxy(fachadaViandas);
        utilsNotifIncidentAndEvents = mock(utilsNotifIncidentAndEvents.class); // Inicializa el mock aquí

        // Configura el comportment de los mocks
        when(entityManager.getTransaction()).thenReturn(transaction);
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
    }

    @Test
    public void testDepositar_yNotificarColaboradorFaltanteViandas() {
        // Configuración de la heladera y su comportamiento
        Heladera heladera = mock(Heladera.class);
        when(entityManager.find(Heladera.class, 1)).thenReturn(heladera);

        int cantidadFaltante = 5;
        // Simulamos la cantidad de viandas en la heladera
        when(heladera.cantidadDeViandasQueQuedanHastaLlenar()).thenReturn(5);
        when(heladera.getColaboradorIDsuscripcionCantidadFaltantesViandasByNumber()).thenReturn(createColaboradorMap(cantidadFaltante));

        // Ejecutamos el método
        fachada.depositar(1, "qrVianda");

    }

    private List<Long> createColaboradorMap(Integer cantidadFaltante) {
        Map<Long, Integer> colaboradores = new HashMap<>();
        colaboradores.put(1L, 1); // Simula un colaborador con 1 vianda faltante o menos para llenar
        colaboradores.put(2L, 5); // Simula un colaborador con 5 vianda faltante o menos para llenar
        colaboradores.put(3L, 10); // Simula un colaborador con 10 vianda faltante o menos para llenar

        return colaboradores.entrySet().stream()
                .filter(entry -> entry.getValue() >= cantidadFaltante) // Filtra si la cantidad faltante del colaborador es 10 o menos
                .map(Map.Entry::getKey) // Extrae solo los IDs de los colaboradores
                .collect(Collectors.toList()); // Recoge en una lista
    }
}