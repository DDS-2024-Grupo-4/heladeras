package ar.edu.utn.dds.k3003.model.DTO;

import ar.edu.utn.dds.k3003.model.TipoSuscripcion;

public class SuscripcionDTO {
  public Long colaboradorId;
  public Integer heladeraId;
  public TipoSuscripcion tipoSuscripcion;
  public Integer cantidadN;

  public SuscripcionDTO(){}

  public SuscripcionDTO(Long colaboradorId, Integer heladeraId, TipoSuscripcion tipoSuscripcion, Integer cantidadN) {
    this.colaboradorId = colaboradorId;
    this.heladeraId = heladeraId;
    this.tipoSuscripcion = tipoSuscripcion;
    this.cantidadN = cantidadN;
  }

  public SuscripcionDTO(Long colaboradorId, Integer heladeraId, TipoSuscripcion tipoSuscripcion) {
    this.colaboradorId = colaboradorId;
    this.heladeraId = heladeraId;
    this.tipoSuscripcion = tipoSuscripcion;
  }

  @Override
  public String toString() {
    return "===========================\n" +
        "      SuscripcionDTO      \n" +
        "===========================\n" +
        "Colaborador ID: " + colaboradorId + "\n" +
        "Heladera ID: " + heladeraId + "\n" +
        "Tipo de Suscripción: " + tipoSuscripcion + "\n" +
        "Cantidad N: " + cantidadN + "\n" +
        "===========================\n";
  }
}
