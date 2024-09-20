package ar.edu.utn.dds.k3003.model.DTO;

public class GetErrorHeladeraDTO {
    Integer codigo;
    String descripcion;

    public GetErrorHeladeraDTO(){}

    public GetErrorHeladeraDTO(Integer codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }
}
