package ar.edu.utn.dds.k3003.model.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidente")
public class RetiroDTODay {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String qrVianda;
  private String tarjeta;
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'"
  )
  private LocalDateTime fechaRetiro;
  private Integer heladeraId;

  public RetiroDTODay(String qrVianda, String tarjeta, Integer heladeraId) {
    this.qrVianda = qrVianda;
    this.tarjeta = tarjeta;
    this.heladeraId = heladeraId;
    this.fechaRetiro = LocalDateTime.now();
  }

  public RetiroDTODay() {
  }

  public Long getId() {
    return this.id;
  }

  public String getQrVianda() {
    return this.qrVianda;
  }

  public String getTarjeta() {
    return this.tarjeta;
  }

  public LocalDateTime getFechaRetiro() {
    return this.fechaRetiro;
  }

  public Integer getHeladeraId() {
    return this.heladeraId;
  }

}
