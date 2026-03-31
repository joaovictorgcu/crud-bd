package com.academia.modelo;

import java.math.BigDecimal;

public class Plano {

    private int codPlano;
    private String nome;
    private int duracao;
    private BigDecimal valorMes;

    public Plano() {
    }

    public int getCodPlano() {
        return codPlano;
    }

    public void setCodPlano(int codPlano) {
        this.codPlano = codPlano;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getDuracao() {
        return duracao;
    }

    public void setDuracao(int duracao) {
        this.duracao = duracao;
    }

    public BigDecimal getValorMes() {
        return valorMes;
    }

    public void setValorMes(BigDecimal valorMes) {
        this.valorMes = valorMes;
    }

    @Override
    public String toString() {
        return nome;
    }
}
