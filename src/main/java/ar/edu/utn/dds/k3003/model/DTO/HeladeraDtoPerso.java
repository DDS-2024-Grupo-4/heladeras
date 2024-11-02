package ar.edu.utn.dds.k3003.model.DTO;

public class HeladeraDtoPerso {
    public Integer id;
    public String nombre;
    public Integer cantidadDeViandas;
    public Boolean disponible;

    public HeladeraDtoPerso(Integer id, String nombre, Integer cantidadDeViandas, Boolean disponible){
        this.id = id;
        this.nombre = nombre;
        this.cantidadDeViandas = cantidadDeViandas;
        this.disponible = disponible;
    }
}
