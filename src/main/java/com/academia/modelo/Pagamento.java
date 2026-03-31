package com.academia.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Pagamento {

    private int codPgto;
    private LocalDate dtVenc;
    private String status;
    private BigDecimal valor;
    private LocalDate dtAssinatura;
    private int nroMatric;
    private int codPlano;
    private String alunoNome;

    public Pagamento() {
    }

    public int getCodPgto() {
        return codPgto;
    }

    public void setCodPgto(int codPgto) {
        this.codPgto = codPgto;
    }

    public LocalDate getDtVenc() {
        return dtVenc;
    }

    public void setDtVenc(LocalDate dtVenc) {
        this.dtVenc = dtVenc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
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

    public int getCodPlano() {
        return codPlano;
    }

    public void setCodPlano(int codPlano) {
        this.codPlano = codPlano;
    }

    public String getAlunoNome() {
        return alunoNome;
    }

    public void setAlunoNome(String alunoNome) {
        this.alunoNome = alunoNome;
    }
}
