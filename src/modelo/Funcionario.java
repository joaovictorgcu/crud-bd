package modelo;

public class Funcionario {
    private int idFuncionario;
    private String nome;
    private String email;
    private double salario;
    private Integer idDepartamento;

    public Funcionario() {}

    public Funcionario(String nome, String email, double salario, Integer idDepartamento) {
        this.nome = nome;
        this.email = email;
        this.salario = salario;
        this.idDepartamento = idDepartamento;
    }

    public int getIdFuncionario() { return idFuncionario; }
    public void setIdFuncionario(int idFuncionario) { this.idFuncionario = idFuncionario; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getSalario() { return salario; }
    public void setSalario(double salario) { this.salario = salario; }

    public Integer getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(Integer idDepartamento) { this.idDepartamento = idDepartamento; }

    @Override
    public String toString() {
        return String.format("%d | %s | %s | %.2f | %s", idFuncionario, nome, email, salario, idDepartamento);
    }
}
