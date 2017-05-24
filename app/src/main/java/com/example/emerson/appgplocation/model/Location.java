package com.example.emerson.appgplocation.model;

import java.io.Serializable;

/**
 * Created by Emerson on 16/04/2017.
 */

public class Location implements Serializable {

    private Long idpoint;
    private String lat;
    private String log;
    private String descricao;

    public Long getIdpoint() {
        return idpoint;
    }

    public void setIdpoint(Long idpoint) {
        this.idpoint = idpoint;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
