package com.academia.modelo;

import java.time.LocalDate;

public class Assinatura {

    private LocalDate dtAssinatura;
    private int nroMatric;
    private String alunoNome;
    private int codPlano;
    private String planoNome;
    private LocalDate dtInicio;
    private LocalDate dtFim;
    private String status;

    public Assinatura() {
    }

    public LocalDate getDtAssinatura() {
        return dtAssinatura;
    }

    public void setDtAssinatura(LocalDate dtAssinatura) {
        this.dtAssinatura = dtAssinatura;
    }

    public int getNroMatric() {
        return nroMatric;
    }

    public void setNroMatric(int nroMatric) {
        this.nroMatric = nroMatric;
    }

    public String getAlunoNome() {
        return alunoNome;
    }

    public void setAlunoNome(String alunoNome) {
        this.alunoNome = alunoNome;
    }

    public int getCodPlano() {
        return codPlano;
    }

    public void setCodPlano(int codPlano) {
        this.codPlano = codPlano;
    }

    public String getPlanoNome() {
        return planoNome;
    }

    public void setPlanoNome(String planoNome) {
        this.planoNome = planoNome;
    }

    public LocalDate getDtInicio() {
        return dtInicio;
    }

    public void setDtInicio(LocalDate dtInicio) {
        this.dtInicio = dtInicio;
    }

    public LocalDate getDtFim() {
        return dtFim;
    }

    public void setDtFim(LocalDate dtFim) {
        this.dtFim = dtFim;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
