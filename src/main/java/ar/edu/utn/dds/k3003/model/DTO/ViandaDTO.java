package ar.edu.utn.dds.k3003.model.DTO;

import ar.edu.utn.dds.k3003.facades.dtos.EstadoViandaEnum;
import ar.edu.utn.dds.k3003.model.Heladera;

public class ViandaDTO {

    Integer viandaId;
    String qrVianda;
    EstadoViandaEnum estadoVianda;
    Heladera heladera;

    public ViandaDTO(){}

    public ViandaDTO(String qrVianda) {
        this.qrVianda = qrVianda;
        this.estadoVianda = EstadoViandaEnum.PREPARADA;
        this.heladera = null;
    }

    public void setHeladera(Heladera heladera){
        this.heladera = heladera;
    }

    public Integer getViandaId() {
        return viandaId;
    }

    public String getQrVianda() {
        return qrVianda;
    }

    public EstadoViandaEnum getEstadoVianda() {
        return estadoVianda;
    }

    public void setEstadoVianda(EstadoViandaEnum estadoVianda) {
        this.estadoVianda = estadoVianda;
    }

}
