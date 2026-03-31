package com.academia.dao;

import com.academia.conexao.ConexaoBD;
import com.academia.modelo.Equipamento;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipamentoDAO {

    public void inserir(Equipamento equipamento) {
        String sql = "INSERT INTO equipamento (cod_equip, nome, descricao) VALUES (nextval('seq_equipamento'), ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, equipamento.getNome());
            stmt.setString(2, equipamento.getDescricao());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(stmt, conn);
        }
    }

    public List<Equipamento> listarTodos() {
        String sql = "SELECT cod_equip, nome, descricao FROM equipamento ORDER BY cod_equip";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Equipamento> lista = new ArrayList<>();
        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Equipamento eq = new Equipamento();
                eq.setCodEquip(rs.getInt("cod_equip"));
                eq.setNome(rs.getString("nome"));
                eq.setDescricao(rs.getString("descricao"));
                lista.add(eq);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(rs, stmt, conn);
        }
        return lista;
    }

    public void atualizar(Equipamento equipamento) {
        String sql = "UPDATE equipamento SET nome = ?, descricao = ? WHERE cod_equip = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, equipamento.getNome());
            stmt.setString(2, equipamento.getDescricao());
            stmt.setInt(3, equipamento.getCodEquip());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(stmt, conn);
        }
    }

    public void deletar(int codEquip) {
        String sql = "DELETE FROM equipamento WHERE cod_equip = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, codEquip);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(stmt, conn);
        }
    }
}
