package modelo;

import java.sql.Date;

public class Departamento {
    private int idDepartamento;
    private String nome;
    private String sigla;
    private Date dataCriacao;

    public Departamento() {}

    public Departamento(String nome, String sigla) {
        this.nome = nome;
        this.sigla = sigla;
    }

    public int getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(int idDepartamento) { this.idDepartamento = idDepartamento; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getSigla() { return sigla; }
    public void setSigla(String sigla) { this.sigla = sigla; }

    public Date getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(Date dataCriacao) { this.dataCriacao = dataCriacao; }

    @Override
    public String toString() {
        return String.format("%d | %s | %s | %s", idDepartamento, nome, sigla, dataCriacao);
    }
}
