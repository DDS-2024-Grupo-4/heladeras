package ar.edu.utn.dds.k3003.model;

import ar.edu.utn.dds.k3003.facades.dtos.EstadoTrasladoEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class Traslado {
    private Integer id;
    private final String qrVianda;
    private final Ruta ruta;
    private EstadoTrasladoEnum estado;
    private final LocalDateTime fechaCreacion;
    private final LocalDateTime fechaTraslado;

    public Traslado(String qrVianda, Ruta ruta, EstadoTrasladoEnum estado, LocalDateTime fechaTraslado) {
        this.qrVianda = qrVianda;
        this.ruta = ruta;
        this.estado = estado;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaTraslado = fechaTraslado;
    }

}
