package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.facades.dtos.RetiroDTO;
import ar.edu.utn.dds.k3003.model.DTO.RetiroDTODay;

import javax.persistence.*;
import java.time.Duration;
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
    private Boolean estadoActivo;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "sensor_id", referencedColumnName = "sensor_id")
    private SensorTemperatura sensorTemperatura;
    private Integer temperaturaMaxima;
    private Integer temperaturaMinima;
    private Integer umbralTemperatura;
    private LocalDateTime tiempoUltimaTemperaturaMaxima;
    private LocalDateTime tiempoUltimaTemperaturaMinima;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "heladeraId", referencedColumnName = "heladeraId")
    private List<RetiroDTODay> retirosDelDia = new ArrayList<>();

    //Campos para FALLO DE DESCONEXION
    private Integer tiempoMaximoUltimoReciboTemperatura;
    private LocalDateTime tiempoUltimaTemperaturaRecibida;
    @ElementCollection(fetch = FetchType.EAGER)
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
        this.cantidadMaximaViandas = 4;
        this.estadoActivo = true;
        this.modelo = generarModeloAleatorio();
        this.direccion = generarDireccionAleatoria();
        this.temperaturaMaxima = randomNumberBetween(10,15);
        this.temperaturaMinima = randomNumberBetween(0,3);
        this.umbralTemperatura = randomNumberBetween(1,2);
        this.tiempoMaximoUltimoReciboTemperatura = randomNumberBetween(1,4);
    }

    public void setHeladeraId(Integer id){ this.heladeraId = id;};

    public Integer getHeladeraId(){
        return this.heladeraId;
    }

    public String getNombre(){return this.nombre;}

    public Integer ultimaTemperatura(){
        return this.sensorTemperatura.getUltimaTemperaRegistrada();
    }

    public Integer cantidadDeViandasQueQuedanHastaLlenar(){
        return this.cantidadMaximaViandas - this.viandas.size();
    }

    public Map<Integer, LocalDateTime> obtenerTemperaturaHeladera(){
        return this.sensorTemperatura.obtenerTodasLasTemperaturas();
    }

    public List<String> getViandas(){
        return this.viandas;
    }

    public Integer getCantidadMaximaViandas() {
        return cantidadMaximaViandas;
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

    public LocalDateTime getTiempoUltimaTemperaturaRecibida(){
        return this.tiempoUltimaTemperaturaRecibida;
    }

    public void setTiempoUltimaTemperaturaRecibida(LocalDateTime tiempoUltimaTemperaturaRecibida) {
        this.tiempoUltimaTemperaturaRecibida = tiempoUltimaTemperaturaRecibida;
    }

    public Boolean superoTiempoUltimaTemperaturaRecibida(){
        return this.tiempoUltimaTemperaturaRecibida.plusMinutes(tiempoMaximoUltimoReciboTemperatura).isBefore(LocalDateTime.now());
    }
    //////////////////////////////////////////
    public boolean superoTiempoMaximoTemperaturaMaxima() {
        if (tiempoUltimaTemperaturaMaxima == null) {
            return false;
        }
        long tiempoTranscurrido = Duration.between(tiempoUltimaTemperaturaMaxima, LocalDateTime.now()).toMinutes();
        return tiempoTranscurrido > umbralTemperatura;
    }
    public boolean superoTiempoMaximoTemperaturaMinima() {
        if (tiempoUltimaTemperaturaMinima == null) {
            return false;
        }
        long tiempoTranscurrido = Duration.between(tiempoUltimaTemperaturaMinima, LocalDateTime.now()).toMinutes();
        return tiempoTranscurrido > umbralTemperatura;
    }


    public Integer getTemperaturaMaxima() {
        return temperaturaMaxima;
    }

    public Integer getTemperaturaMinima() {
        return this.temperaturaMinima;
    }

    public void setTiempoUltimaTemperaturaMaxima(LocalDateTime tiempo) {
        this.tiempoUltimaTemperaturaMaxima = tiempo;
    }

    public void setTiempoUltimaTemperaturaMinima(LocalDateTime tiempo) {
        this.tiempoUltimaTemperaturaMinima = tiempo;
    }

    public Long tiempoRestanteHastaError() {
        LocalDateTime ahora = LocalDateTime.now();
        long tiempoRestante = Long.MAX_VALUE; // Por defecto, sin límite

        // Calculo de tiempo restante para temperatura máxima
        if (tiempoUltimaTemperaturaMaxima != null) {
            long minutosDesdeUltimaMaxima = calcularMinutos(tiempoUltimaTemperaturaMaxima, ahora);
            long tiempoMaximoRestante = umbralTemperatura - minutosDesdeUltimaMaxima;
            if (tiempoMaximoRestante <= 0) {
                return 0L;
            }
            tiempoRestante = Math.min(tiempoRestante, tiempoMaximoRestante);
        }

        // Calculo de tiempo restante para temperatura mínima
        if (tiempoUltimaTemperaturaMinima != null) {
            long minutosDesdeUltimaMinima = calcularMinutos(tiempoUltimaTemperaturaMinima, ahora);
            long tiempoMinimoRestante = umbralTemperatura - minutosDesdeUltimaMinima;
            if (tiempoMinimoRestante <= 0) {
                return 0L; // Ya ha excedido el tiempo
            }
            tiempoRestante = Math.min(tiempoRestante, tiempoMinimoRestante);
        }

        return tiempoRestante;
    }
    private long calcularMinutos(LocalDateTime desde, LocalDateTime hasta) {
        return java.time.Duration.between(desde, hasta).toMinutes();
    }

    public List<Long> getColaboradorIDsuscripcionNViandasDisponiblesFiltrado() {
        Integer cantidadDeViandasActual = this.cantidadDeViandas();
         return this.colaboradorIDsuscripcionNViandasDisponibles.entrySet().stream()
            .filter(entry -> entry.getValue() <= cantidadDeViandasActual)
            .map(Map.Entry::getKey)// Filtro los colaboradores que quieren saber si hay menos de nViandasDisponibles para retirar
            .collect(Collectors.toList()); // Creo un nuevo List
    }

    public void setColaboradorIDsuscripcionNViandasDisponibles(Long colaboradorId, Integer nViandasDisponibles) {
        this.colaboradorIDsuscripcionNViandasDisponibles.put(colaboradorId, nViandasDisponibles);
    }

    public List<Long> getColaboradorIDsuscripcionCantidadFaltantesViandasByNumber() {
        int cantidadFaltanteHastaLlenarse = this.cantidadDeViandasQueQuedanHastaLlenar();
        return colaboradorIDsuscripcionCantidadFaltantesViandas.entrySet().stream()
                .filter(entry -> entry.getValue() <= cantidadFaltanteHastaLlenarse) // Filtra si la cantidad faltante  de viandas hasta llenarse es menor
                .map(Map.Entry::getKey) // Extrae solo los IDs de los colaboradores
                .collect(Collectors.toList()); // Recoge en una lista
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
        tiempoUltimaTemperaturaMaxima = null;
        tiempoUltimaTemperaturaMinima = null;
    }
    public Boolean estaActiva(){
        return this.estadoActivo;
    }

    public void addRetiroDelDia(RetiroDTODay retiroDTODay) {
        this.retirosDelDia.add(retiroDTODay);
    }

    public void resetRetiroDelDia(){
        if (this.retirosDelDia != null) {
            this.retirosDelDia.clear();
        }
    }

    public List<RetiroDTODay> getRetirosDelDia(){
        return this.retirosDelDia;
    }
    @Override
    public String toString() {
        return "Heladera{" +
            "heladeraId=" + heladeraId +
            ", estadoActivo=" + estadoActivo +
            ", temperaturaMaxima=" + temperaturaMaxima +
            ", temperaturaMinima=" + temperaturaMinima +
            ", umbralTemperatura=" + umbralTemperatura +
            ", tiempoUltimaTemperaturaMaxima=" + tiempoUltimaTemperaturaMaxima +
            ", tiempoUltimaTemperaturaMinima=" + tiempoUltimaTemperaturaMinima +
            '}';
    }
}
