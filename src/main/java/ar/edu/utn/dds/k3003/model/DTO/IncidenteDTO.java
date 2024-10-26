package ar.edu.utn.dds.k3003.model.DTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class IncidenteDTO {
  public String incidente;
  public Integer heladeraId;
  public String fechaIncidente;
  public Integer cantidadNSeteada;

  public IncidenteDTO(String tipoIncidente, Integer heladeraId) {
    this.incidente = tipoIncidente;
    this.heladeraId = heladeraId;
    this.fechaIncidente = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
  }
  public IncidenteDTO(String tipoIncidente, Integer heladeraId, Integer cantidadNSeteada) {
    this.incidente = tipoIncidente;
    this.heladeraId = heladeraId;
    this.fechaIncidente = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
    this.cantidadNSeteada = cantidadNSeteada;
  }

  @Override
  public String toString() {
    return "IncidenteDTO{" +
        "incidente='" + incidente + '\'' +
        ", heladeraId=" + heladeraId +
        ", fechaIncidente='" + fechaIncidente + '\'' +
        ", cantidadNSeteada=" + cantidadNSeteada +
        '}';
  }
}
