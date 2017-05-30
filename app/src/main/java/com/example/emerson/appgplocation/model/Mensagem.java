package com.example.emerson.appgplocation.model;

/**
 * Created by Emerson on 26/05/2017.
 */

public class Mensagem {

    private Long idMsg;
    private String msg;
    private Long idenviado;
    private Long idrecebido;
    private String status;

    public Long getIdMsg() {
        return idMsg;
    }

    public void setIdMsg(Long idMsg) {
        this.idMsg = idMsg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Long getIdenviado() {
        return idenviado;
    }

    public void setIdenviado(Long idenviado) {
        this.idenviado = idenviado;
    }

    public Long getIdrecebido() {
        return idrecebido;
    }

    public void setIdrecebido(Long idrecebido) {
        this.idrecebido = idrecebido;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
