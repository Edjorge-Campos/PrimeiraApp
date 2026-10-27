package com.example.primeiraapp.model;

import com.google.firebase.firestore.PropertyName;

public class Shift {
    @PropertyName("DiaDaSemana")
    private int diaDaSemana;
    @PropertyName("Inicio")
    private String inicio;
    @PropertyName("Status")
    private String status;
    private String id;

    public Shift() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isOpen() {
        return "open".equals(status);
    }

    @PropertyName("DiaDaSemana")
    public int getDiaDaSemana() {
        return diaDaSemana;
    }

    @PropertyName("Inicio")
    public String getInicio() {
        return inicio;
    }

    @PropertyName("Status")
    public String getStatus() {
        return status;
    }

    @PropertyName("DiaDaSemana")
    public void setDiaDaSemana(int diaDaSemana) {
        this.diaDaSemana = diaDaSemana;
    }

    @PropertyName("Inicio")
    public void setInicio(String inicio) {
        this.inicio = inicio;
    }

    @PropertyName("Status")
    public void setStatus(String status) {
        this.status = status;
    }

}
