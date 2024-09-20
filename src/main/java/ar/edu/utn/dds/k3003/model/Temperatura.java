package ar.edu.utn.dds.k3003.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Temperatura {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    private SensorTemperatura sensor;
    private LocalDateTime tiempo;
    private Integer temperatura;

    public Temperatura(){}

    public Temperatura(SensorTemperatura sensor, Integer temperatura, LocalDateTime tiempo) {
        this.sensor = sensor;
        this.tiempo = tiempo;
        this.temperatura = temperatura;
    }

    public Integer getId() {
        return id;
    }

    public LocalDateTime gettiempo() {
        return tiempo;
    }

    public Integer gettemperatura() {
        return temperatura;
    }
}
