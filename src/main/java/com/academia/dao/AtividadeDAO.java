package com.academia.dao;

import com.academia.conexao.ConexaoBD;
import com.academia.modelo.Atividade;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AtividadeDAO {

    public void inserir(Atividade atividade) {
        String sql = "INSERT INTO atividade (cod_ativ, nome, descricao) VALUES (nextval('seq_atividade'), ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, atividade.getNome());
            stmt.setString(2, atividade.getDescricao());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(stmt, conn);
        }
    }

    public List<Atividade> listarTodos() {
        String sql = "SELECT cod_ativ, nome, descricao FROM atividade ORDER BY cod_ativ";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Atividade> lista = new ArrayList<>();
        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Atividade a = new Atividade();
                a.setCodAtiv(rs.getInt("cod_ativ"));
                a.setNome(rs.getString("nome"));
                a.setDescricao(rs.getString("descricao"));
                lista.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(rs, stmt, conn);
        }
        return lista;
    }

    public void atualizar(Atividade atividade) {
        String sql = "UPDATE atividade SET nome = ?, descricao = ? WHERE cod_ativ = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, atividade.getNome());
            stmt.setString(2, atividade.getDescricao());
            stmt.setInt(3, atividade.getCodAtiv());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(stmt, conn);
        }
    }

    public void deletar(int codAtiv) {
        String sql = "DELETE FROM atividade WHERE cod_ativ = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, codAtiv);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(stmt, conn);
        }
    }
}
