package ar.edu.utn.dds.k3003.model;

import javax.persistence.Embeddable;

@Embeddable
public class Coordenadas {
    Integer X,Y;
    public Coordenadas(){}
    public Coordenadas(Integer X,Integer Y){
        this.X = X;
        this.Y = Y;
    }

    public Integer getX() {
        return X;
    }

    public void setX(Integer x) {
        X = x;
    }

    public Integer getY() {
        return Y;
    }

    public void setY(Integer y) {
        Y = y;
    }
}
