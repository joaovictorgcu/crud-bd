package com.academia.modelo;

public class Atividade {

    private int codAtiv;
    private String nome;
    private String descricao;

    public Atividade() {
    }

    public int getCodAtiv() {
        return codAtiv;
    }

    public void setCodAtiv(int codAtiv) {
        this.codAtiv = codAtiv;
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
