package com.academia.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Instrutor {

    private String cref;
    private String nome;
    private String cpf;
    private String email;
    private LocalDate dtNasc;
    private String rua;
    private String bairro;
    private String cep;
    private BigDecimal salario;
    private LocalDate dtAdmissao;
    private String crefSupervisor;

    public Instrutor() {
    }

    public String getCref() {
        return cref;
    }

    public void setCref(String cref) {
        this.cref = cref;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDtNasc() {
        return dtNasc;
    }

    public void setDtNasc(LocalDate dtNasc) {
        this.dtNasc = dtNasc;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public BigDecimal getSalario() {
        return salario;
    }

    public void setSalario(BigDecimal salario) {
        this.salario = salario;
    }

    public LocalDate getDtAdmissao() {
        return dtAdmissao;
    }

    public void setDtAdmissao(LocalDate dtAdmissao) {
        this.dtAdmissao = dtAdmissao;
    }

    public String getCrefSupervisor() {
        return crefSupervisor;
    }

    public void setCrefSupervisor(String crefSupervisor) {
        this.crefSupervisor = crefSupervisor;
    }

    @Override
    public String toString() {
        return nome;
    }
}
