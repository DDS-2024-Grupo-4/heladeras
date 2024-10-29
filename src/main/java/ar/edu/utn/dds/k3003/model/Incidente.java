package ar.edu.utn.dds.k3003.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "incidente")
public class Incidente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  @Enumerated(EnumType.STRING)
  private TipoIncidente tipoIncidente;
  private Integer heladeraId;
  private String fechaIncidente;

  public Incidente(TipoIncidente tipoIncidente, Integer heladeraId) {
    this.tipoIncidente = tipoIncidente;
    this.heladeraId = heladeraId;
    this.fechaIncidente = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
  }

  public Incidente(TipoIncidente tipoIncidente, Integer heladeraId, LocalDateTime fechaIncidente) {
    this.tipoIncidente = tipoIncidente;
    this.heladeraId = heladeraId;
    this.fechaIncidente = String.valueOf(fechaIncidente);
  }


  public Incidente() {
  }

  public TipoIncidente getTipoIncidente() {
    return tipoIncidente;
  }

  public Integer getHeladeraId() {
    return heladeraId;
  }

  public String getFechaIncidente() {
    return fechaIncidente;
  }

  @Override
  public String toString() {
    return "===========================\n" +
        "         Incidente        \n" +
        "===========================\n" +
        "ID: " + id + "\n" +
        "Tipo de Incidente: " + tipoIncidente + "\n" +
        "ID de Heladera: " + heladeraId + "\n" +
        "Fecha de Incidente: " + fechaIncidente + "\n" +
        "===========================\n";
  }

}
