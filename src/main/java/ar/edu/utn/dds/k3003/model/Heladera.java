package ar.edu.utn.dds.k3003.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ar.edu.utn.dds.k3003.utils.utils.*;
@Entity
@Table(name = "heladera")
public class Heladera {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer heladeraId;
    private String nombre;
    private String modelo;
    @Embedded
    private Coordenadas coordenadas;
    private String direccion;
    private Integer cantidadMaximaViandas;
    private Float pesoMaximo;
    private Float pesoActual;
    private Boolean estadoActivo;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "sensor_id", referencedColumnName = "sensor_id")
    private SensorTemperatura sensorTemperatura;
    private Integer temperaturaMaxima;
    private Integer tiempoMaximoTemperaturaMaxima;
    private Integer tiempoMaximoUltimoReciboTemperatura;
    private LocalDateTime tiempoUltimaTemperaturaMaxima;
    private Boolean heladeraAbierta;
    @ElementCollection
    private List<String> viandas = new ArrayList<>();
    @ElementCollection
    @CollectionTable(name = "colaboradorIDsuscripcionNViandasDisponibles")
    @MapKeyColumn(name = "colaborador_id")
    @Column(name = "n_seteado")
    private Map<Long, Integer> colaboradorIDsuscripcionNViandasDisponibles = new HashMap<>();
    @ElementCollection
    @CollectionTable(name = "colaboradorIDsuscripcionCantidadFaltantesViandas")
    @MapKeyColumn(name = "colaborador_id")
    @Column(name = "n_seteado")
    private Map<Long, Integer> colaboradorIDsuscripcionCantidadFaltantesViandas = new HashMap<>();
    @ElementCollection
    private List<Long> colaboradorIDsuscripcionDesperfectoHeladera = new ArrayList<>();

    public Heladera(){}

    public Heladera(String nombre){
        this.nombre = nombre;
        this.coordenadas = new Coordenadas(randomNumberBetween(0,255), randomNumberBetween(0,255));
        this.cantidadMaximaViandas = randomNumberBetween(10,25);
        this.pesoMaximo = 100f;
        this.pesoActual = 0f;
        this.estadoActivo = true;
        this.modelo = generarModeloAleatorio();
        this.direccion = generarDireccionAleatoria();
        this.temperaturaMaxima = randomNumberBetween(5,10);
        this.tiempoMaximoTemperaturaMaxima = randomNumberBetween(1,5);
        this.tiempoMaximoUltimoReciboTemperatura = randomNumberBetween(1,5);
    }

    public void setHeladeraId(Integer id){ this.heladeraId = id;};

    public Integer getHeladeraId(){
        return this.heladeraId;
    }

    public String getNombre(){return this.nombre;}

    public Integer ultimaTemperatura(){
        return this.sensorTemperatura.getUltimaTemperaRegistrada();
    }

    public void setHeladeraAbierta(Boolean heladeraAbierta) {
        this.heladeraAbierta = heladeraAbierta;
    }

    public Map<Integer, LocalDateTime> obtenerTodasLasTemperaturas(){
        return this.sensorTemperatura.obtenerTodasLasTemperaturas();
    }

    public Map.Entry<Integer, LocalDateTime> reportarTemperatura(){
        return this.sensorTemperatura.obtenerTemperatura();
    }
    public void guardarVianda(String viandaQR) throws Exception {
        if (this.cantidadDeViandas() < cantidadMaximaViandas) {
            this.viandas.add(viandaQR);
        } else {
            throw new Exception("La cantidad de viandas en la heladera ha alcanzado el límite máximo");
        }
    }
    public void retirarVianda(String viandaQR) {
        viandas.removeIf(v -> v.equals(viandaQR));
    }

    public Integer cantidadDeViandas(){
        return this.viandas.size();
    }

    public SensorTemperatura getSensorTemperatura() {
        return sensorTemperatura;
    }

    public void setSensorTemperatura(SensorTemperatura sensorTemperatura) {
        this.sensorTemperatura = sensorTemperatura;
    }
    public Boolean getHeladeraAbierta() {
        return heladeraAbierta;
    }

    public void setTiempoUltimaTemperaturaMaxima(LocalDateTime tiempoUltimaTemperaturaMaxima) {
        this.tiempoUltimaTemperaturaMaxima = tiempoUltimaTemperaturaMaxima;
    }
    public LocalDateTime getTiempoUltimaTemperaturaMaxima() {
        return tiempoUltimaTemperaturaMaxima;
    }

    public Integer tiempoMaximoTemperaturaMaxima(){
        return this.tiempoMaximoTemperaturaMaxima;
    }

    public Integer tiempoMaximoUltimoReciboTemperatura(){
        return this.tiempoMaximoUltimoReciboTemperatura;
    }

    public Boolean superoTiempoMaximoTemperaturaMaxima(){
        return this.tiempoUltimaTemperaturaMaxima.plusHours(tiempoMaximoTemperaturaMaxima).isBefore(LocalDateTime.now());
    }

    public void setTiempoMaximoTemperaturaMaxima(Integer tiempoMaximoTemperaturaMaxima) {
        this.tiempoMaximoTemperaturaMaxima = tiempoMaximoTemperaturaMaxima;
    }

    public Integer getTemperaturaMaxima() {
        return temperaturaMaxima;
    }



    public Map<Long, Integer> getColaboradorIDsuscripcionNViandasDisponibles(Integer nViandasDisponibles) {
        Map<Long, Integer> colaboradoresAAvisar = this.colaboradorIDsuscripcionNViandasDisponibles.entrySet().stream()
            .filter(entry -> entry.getValue() <= nViandasDisponibles) // Filtro los colaboradores que quieren saber si hay menos de nViandasDisponibles
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)); // Creo un nuevo Map
        return colaboradoresAAvisar;
    }

    public void setColaboradorIDsuscripcionNViandasDisponibles(Long colaboradorId, Integer nViandasDisponibles) {
        this.colaboradorIDsuscripcionNViandasDisponibles.put(colaboradorId, nViandasDisponibles);
    }

    public Map<Long, Integer> getColaboradorIDsuscripcionCantidadFaltantesViandas(Integer nViandasDisponibles) {
        Map<Long, Integer> colaboradoresAAvisar = this.colaboradorIDsuscripcionNViandasDisponibles.entrySet().stream()
            .filter(entry -> entry.getValue() >= nViandasDisponibles) // Filtro los colaboradores que quieren saber cuantas quedan disponibles
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)); // Creo un nuevo Map
        return colaboradoresAAvisar;
    }

    public void setColaboradorIDsuscripcionCantidadFaltantesViandas(Long colaboradorId, Integer nViandasDisponibles) {
        this.colaboradorIDsuscripcionCantidadFaltantesViandas.put(colaboradorId, nViandasDisponibles);
    }

    public List<Long> getColaboradorIDsuscripcionDesperfectoHeladera() {
        return colaboradorIDsuscripcionDesperfectoHeladera;
    }

    public Map<Long, Integer> getColaboradorIDsuscripcionNViandasDisponibles() {
        return colaboradorIDsuscripcionNViandasDisponibles;
    }

    public Map<Long, Integer> getColaboradorIDsuscripcionCantidadFaltantesViandas() {
        return colaboradorIDsuscripcionCantidadFaltantesViandas;
    }

    public void setColaboradorIDsuscripcionDesperfectoHeladera(Long colaboradorID) {
        this.colaboradorIDsuscripcionDesperfectoHeladera.add(colaboradorID);
    }

    public void eliminarColaboradorIDsuscripcionNViandasDisponibles(Long colaboradorID){
        this.colaboradorIDsuscripcionNViandasDisponibles.remove(colaboradorID);
    }

    public void eliminarColaboradorIDsuscripcionCantidadFaltantesViandas(Long colaboradorID){
        this.colaboradorIDsuscripcionCantidadFaltantesViandas.remove(colaboradorID);
    }

    public void eliminarColaboradorIDsuscripcionDesperfectoHeladera(Long colaboradorID){
        this.colaboradorIDsuscripcionDesperfectoHeladera.remove(colaboradorID);
    }

    public void inhabilitar(){
        this.estadoActivo = false;
    }
    public void habilitar(){
        this.estadoActivo = true;
    }
    public Boolean estaActiva(){
        return this.estadoActivo;
    }
}
