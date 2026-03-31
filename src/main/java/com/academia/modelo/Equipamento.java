package com.academia.modelo;

public class Equipamento {

    private int codEquip;
    private String nome;
    private String descricao;

    public Equipamento() {
    }

    public int getCodEquip() {
        return codEquip;
    }

    public void setCodEquip(int codEquip) {
        this.codEquip = codEquip;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return nome;
    }
}
