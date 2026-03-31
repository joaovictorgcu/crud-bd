package com.academia.dao;

import com.academia.conexao.ConexaoBD;
import com.academia.modelo.Pagamento;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagamentoDAO {

    public void inserir(Pagamento pagamento) {
        String sql = "INSERT INTO pagamento (cod_pgto, dt_venc, status, valor, dt_assinatura, nro_matric, cod_plano) "
                + "VALUES (nextval('seq_pagamento'), ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, pagamento.getDtVenc() != null ? Date.valueOf(pagamento.getDtVenc()) : null);
            stmt.setString(2, pagamento.getStatus());
            stmt.setBigDecimal(3, pagamento.getValor());
            stmt.setDate(4, pagamento.getDtAssinatura() != null ? Date.valueOf(pagamento.getDtAssinatura()) : null);
            stmt.setInt(5, pagamento.getNroMatric());
            stmt.setInt(6, pagamento.getCodPlano());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(stmt, conn);
        }
    }

    public List<Pagamento> listarTodos() {
        String sql = "SELECT pg.cod_pgto, pg.dt_venc, pg.status, pg.valor, "
                + "pg.dt_assinatura, pg.nro_matric, pg.cod_plano, "
                + "p.nome AS aluno_nome "
                + "FROM pagamento pg "
                + "LEFT JOIN assinatura s ON pg.dt_assinatura = s.dt_assinatura AND pg.nro_matric = s.nro_matric AND pg.cod_plano = s.cod_plano "
                + "LEFT JOIN aluno a ON s.nro_matric = a.nro_matric "
                + "LEFT JOIN pessoa p ON a.cpf = p.cpf "
                + "ORDER BY pg.cod_pgto";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Pagamento> lista = new ArrayList<>();
        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Pagamento pg = new Pagamento();
                pg.setCodPgto(rs.getInt("cod_pgto"));
                Date dtVenc = rs.getDate("dt_venc");
                if (dtVenc != null) pg.setDtVenc(dtVenc.toLocalDate());
                pg.setStatus(rs.getString("status"));
                pg.setValor(rs.getBigDecimal("valor"));
                Date dtAss = rs.getDate("dt_assinatura");
                if (dtAss != null) pg.setDtAssinatura(dtAss.toLocalDate());
                pg.setNroMatric(rs.getInt("nro_matric"));
                pg.setCodPlano(rs.getInt("cod_plano"));
                pg.setAlunoNome(rs.getString("aluno_nome"));
                lista.add(pg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(rs, stmt, conn);
        }
        return lista;
    }

    public void atualizar(Pagamento pagamento) {
        String sql = "UPDATE pagamento SET dt_venc = ?, status = ?, valor = ? WHERE cod_pgto = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, pagamento.getDtVenc() != null ? Date.valueOf(pagamento.getDtVenc()) : null);
            stmt.setString(2, pagamento.getStatus());
            stmt.setBigDecimal(3, pagamento.getValor());
            stmt.setInt(4, pagamento.getCodPgto());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(stmt, conn);
        }
    }

    public void deletar(int codPgto) {
        String sql = "DELETE FROM pagamento WHERE cod_pgto = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, codPgto);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(stmt, conn);
        }
    }
}
