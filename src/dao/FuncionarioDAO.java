package dao;

import conexao.ConexaoBD;
import modelo.Funcionario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FuncionarioDAO {

    public int inserir(Funcionario func) throws SQLException {
        String sql = "INSERT INTO funcionario (nome, email, salario, id_departamento) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, func.getNome());
            stmt.setString(2, func.getEmail());
            stmt.setDouble(3, func.getSalario());
            if (func.getIdDepartamento() == null) {
                stmt.setNull(4, Types.INTEGER);
            } else {
                stmt.setInt(4, func.getIdDepartamento());
            }
            return stmt.executeUpdate();
        }
    }

    public int atualizarSalario(int id, double novoSalario) throws SQLException {
        String sql = "UPDATE funcionario SET salario = ? WHERE id_funcionario = ?";
        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, novoSalario);
            stmt.setInt(2, id);
            return stmt.executeUpdate();
        }
    }

    public int deletar(int id) throws SQLException {
        String sql = "DELETE FROM funcionario WHERE id_funcionario = ?";
        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        }
    }

    public List<Funcionario> listar() throws SQLException {
        String sql = "SELECT id_funcionario, nome, email, salario, id_departamento FROM funcionario ORDER BY id_funcionario LIMIT 100";
        List<Funcionario> lista = new ArrayList<>();
        try (Connection conn = ConexaoBD.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Funcionario f = new Funcionario();
                f.setIdFuncionario(rs.getInt("id_funcionario"));
                f.setNome(rs.getString("nome"));
                f.setEmail(rs.getString("email"));
                f.setSalario(rs.getDouble("salario"));
                f.setIdDepartamento(rs.getObject("id_departamento", Integer.class));
                lista.add(f);
            }
        }
        return lista;
    }
}
