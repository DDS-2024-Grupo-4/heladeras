package ar.edu.utn.dds.k3003.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
public class SensorTemperatura {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sensor_id")
    private Integer id;
    @OneToOne(mappedBy = "sensorTemperatura")
    private Heladera heladera;
    @OneToOne(mappedBy = "sensor", cascade = CascadeType.ALL)
    private Temperatura temperatura;
    private Integer ultimaTemperaRegistrada;

    public SensorTemperatura(){}

    public SensorTemperatura(Heladera heladera) {
        this.heladera = heladera;
    }

    public Integer getId() {
        return id;
    }

    public Map<Integer, LocalDateTime> obtenerTodasLasTemperaturas(){
        Map<Integer, LocalDateTime> temperaturasMap = new HashMap<>();
        if(this.temperatura != null){
            this.temperatura.gettiempo();
            temperaturasMap.put(this.temperatura.gettemperatura(), this.temperatura.gettiempo());
        }
        return temperaturasMap;
    }

    public Map.Entry<Integer, LocalDateTime> setNuevaTemperatura(Integer temperatura, LocalDateTime tiempo) {
        if (this.temperatura == null) {
            this.temperatura = new Temperatura(this, temperatura, tiempo);
        } else {
            this.temperatura.setTemperatura(temperatura);
            this.temperatura.setTiempo(tiempo);
        }
        this.ultimaTemperaRegistrada = temperatura;
        return new AbstractMap.SimpleEntry<>(temperatura, tiempo);
    }

    public Integer getUltimaTemperaRegistrada(){
        return this.ultimaTemperaRegistrada;
    }
}
