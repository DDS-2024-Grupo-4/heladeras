package ar.edu.utn.dds.k3003.model.DTO;

public class DepositoDTO {
  private Integer heladeraId;
  private String codigoQR;

  public DepositoDTO(){}

  public DepositoDTO(Integer heladeraId, String codigoQR) {
    this.heladeraId = heladeraId;
    this.codigoQR = codigoQR;
  }

  public void setHeladeraId(Integer heladeraId) {
    this.heladeraId = heladeraId;
  }

  public void setCodigoQR(String codigoQR) {
    this.codigoQR = codigoQR;
  }

  public Integer getHeladeraId() {
    return heladeraId;
  }

  public String getCodigoQR() {
    return codigoQR;
  }
}
