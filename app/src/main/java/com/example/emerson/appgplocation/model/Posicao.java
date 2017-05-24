package com.example.emerson.appgplocation.model;

/**
 * Created by Emerson on 18/05/2017.
 */

public class Posicao {

    private Long idUsuario;
    private double lat;
    private double longi;

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLongi() {
        return longi;
    }

    public void setLongi(double longi) {
        this.longi = longi;
    }
}
