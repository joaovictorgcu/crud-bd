package dao;

import conexao.ConexaoBD;
import modelo.Departamento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartamentoDAO {

    public int inserir(Departamento dept) throws SQLException {
        String sql = "INSERT INTO departamento (nome, sigla) VALUES (?, ?)";
        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dept.getNome());
            stmt.setString(2, dept.getSigla());
            return stmt.executeUpdate();
        }
    }

    public int atualizarNome(int id, String novoNome) throws SQLException {
        String sql = "UPDATE departamento SET nome = ? WHERE id_departamento = ?";
        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novoNome);
            stmt.setInt(2, id);
            return stmt.executeUpdate();
        }
    }

    public int deletar(int id) throws SQLException {
        String sql = "DELETE FROM departamento WHERE id_departamento = ?";
        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        }
    }

    public List<Departamento> listar() throws SQLException {
        String sql = "SELECT id_departamento, nome, sigla, data_criacao FROM departamento ORDER BY id_departamento";
        List<Departamento> lista = new ArrayList<>();
        try (Connection conn = ConexaoBD.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Departamento d = new Departamento();
                d.setIdDepartamento(rs.getInt("id_departamento"));
                d.setNome(rs.getString("nome"));
                d.setSigla(rs.getString("sigla"));
                d.setDataCriacao(rs.getDate("data_criacao"));
                lista.add(d);
            }
        }
        return lista;
    }
}
