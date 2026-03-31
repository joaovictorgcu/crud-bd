package com.academia.dao;

import com.academia.conexao.ConexaoBD;
import com.academia.modelo.Plano;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlanoDAO {

    public void inserir(Plano plano) {
        String sql = "INSERT INTO plano (cod_plano, nome, duracao, valor_mes) VALUES (nextval('seq_plano'), ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, plano.getNome());
            stmt.setInt(2, plano.getDuracao());
            stmt.setBigDecimal(3, plano.getValorMes());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(stmt, conn);
        }
    }

    public List<Plano> listarTodos() {
        String sql = "SELECT cod_plano, nome, duracao, valor_mes FROM plano ORDER BY cod_plano";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Plano> lista = new ArrayList<>();
        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Plano p = new Plano();
                p.setCodPlano(rs.getInt("cod_plano"));
                p.setNome(rs.getString("nome"));
                p.setDuracao(rs.getInt("duracao"));
                p.setValorMes(rs.getBigDecimal("valor_mes"));
                lista.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(rs, stmt, conn);
        }
        return lista;
    }

    public void atualizar(Plano plano) {
        String sql = "UPDATE plano SET nome = ?, duracao = ?, valor_mes = ? WHERE cod_plano = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, plano.getNome());
            stmt.setInt(2, plano.getDuracao());
            stmt.setBigDecimal(3, plano.getValorMes());
            stmt.setInt(4, plano.getCodPlano());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(stmt, conn);
        }
    }

    public void deletar(int codPlano) {
        String sql = "DELETE FROM plano WHERE cod_plano = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, codPlano);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(stmt, conn);
        }
    }
}
