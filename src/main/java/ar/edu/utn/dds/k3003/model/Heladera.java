package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.facades.dtos.TemperaturaDTO;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
    private SensorTemperatura sensor;
    private Integer temperaturaMinima;
    private Integer temperaturaMaxima;
    @ElementCollection
    private List<String> viandas = new ArrayList<>();

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
        this.temperaturaMaxima = randomNumberBetween(95,120);
        this.temperaturaMinima = - 10;
    }

    public void setHeladeraId(Integer id){ this.heladeraId = id;};

    public Integer getHeladeraId(){
        return this.heladeraId;
    }

    public String getNombre(){return this.nombre;}

    public Integer ultimaTemperatura(){
        return this.sensor.getUltimaTemperaRegistrada();
    }

    public Map<Integer, LocalDateTime> obtenerTodasLasTemperaturas(){
        return this.sensor.obtenerTodasLasTemperaturas();
    }

    public Map.Entry<Integer, LocalDateTime> reportarTemperatura(){
        return this.sensor.obtenerTemperatura();
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

    public SensorTemperatura getSensor() {
        return sensor;
    }

    public void setSensor(SensorTemperatura sensor) {
        this.sensor = sensor;
    }
}
