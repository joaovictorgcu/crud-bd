package com.academia.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Aluno {

    private int nroMatric;
    private String nome;
    private String cpf;
    private String email;
    private LocalDate dtNasc;
    private String rua;
    private String bairro;
    private String cep;
    private String telefone;
    private LocalDateTime dtCadastro;
    private String status;
    private String obsSaude;

    public Aluno() {
    }

    public int getNroMatric() {
        return nroMatric;
    }

    public void setNroMatric(int nroMatric) {
        this.nroMatric = nroMatric;
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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public LocalDateTime getDtCadastro() {
        return dtCadastro;
    }

    public void setDtCadastro(LocalDateTime dtCadastro) {
        this.dtCadastro = dtCadastro;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObsSaude() {
        return obsSaude;
    }

    public void setObsSaude(String obsSaude) {
        this.obsSaude = obsSaude;
    }

    @Override
    public String toString() {
        return nome;
    }
}
