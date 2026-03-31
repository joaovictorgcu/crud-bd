package com.academia.dao;

import com.academia.conexao.ConexaoBD;
import com.academia.modelo.Assinatura;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AssinaturaDAO {

    public void inserir(Assinatura assinatura) {
        String sql = "INSERT INTO assinatura (dt_assinatura, nro_matric, cod_plano, dt_inicio, dt_fim, status) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, Date.valueOf(assinatura.getDtAssinatura()));
            stmt.setInt(2, assinatura.getNroMatric());
            stmt.setInt(3, assinatura.getCodPlano());
            stmt.setDate(4, assinatura.getDtInicio() != null ? Date.valueOf(assinatura.getDtInicio()) : null);
            stmt.setDate(5, assinatura.getDtFim() != null ? Date.valueOf(assinatura.getDtFim()) : null);
            stmt.setString(6, assinatura.getStatus());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(stmt, conn);
        }
    }

    public List<Assinatura> listarTodas() {
        String sql = "SELECT s.dt_assinatura, s.nro_matric, s.cod_plano, s.dt_inicio, s.dt_fim, s.status, "
                + "p.nome AS aluno_nome, pl.nome AS plano_nome "
                + "FROM assinatura s "
                + "LEFT JOIN aluno a ON s.nro_matric = a.nro_matric "
                + "JOIN pessoa p ON a.cpf = p.cpf "
                + "LEFT JOIN plano pl ON s.cod_plano = pl.cod_plano "
                + "ORDER BY s.dt_assinatura DESC";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Assinatura> lista = new ArrayList<>();
        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Assinatura a = new Assinatura();
                Date dtAss = rs.getDate("dt_assinatura");
                if (dtAss != null) a.setDtAssinatura(dtAss.toLocalDate());
                a.setNroMatric(rs.getInt("nro_matric"));
                a.setCodPlano(rs.getInt("cod_plano"));
                Date dtIni = rs.getDate("dt_inicio");
                if (dtIni != null) a.setDtInicio(dtIni.toLocalDate());
                Date dtFim = rs.getDate("dt_fim");
                if (dtFim != null) a.setDtFim(dtFim.toLocalDate());
                a.setStatus(rs.getString("status"));
                a.setAlunoNome(rs.getString("aluno_nome"));
                a.setPlanoNome(rs.getString("plano_nome"));
                lista.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(rs, stmt, conn);
        }
        return lista;
    }

    public void atualizar(Assinatura assinatura) {
        String sql = "UPDATE assinatura SET dt_inicio = ?, dt_fim = ?, status = ? WHERE dt_assinatura = ? AND nro_matric = ? AND cod_plano = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, assinatura.getDtInicio() != null ? Date.valueOf(assinatura.getDtInicio()) : null);
            stmt.setDate(2, assinatura.getDtFim() != null ? Date.valueOf(assinatura.getDtFim()) : null);
            stmt.setString(3, assinatura.getStatus());
            stmt.setDate(4, Date.valueOf(assinatura.getDtAssinatura()));
            stmt.setInt(5, assinatura.getNroMatric());
            stmt.setInt(6, assinatura.getCodPlano());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(stmt, conn);
        }
    }

    public void deletar(LocalDate dtAssinatura, int nroMatric, int codPlano) {
        String sql = "DELETE FROM assinatura WHERE dt_assinatura = ? AND nro_matric = ? AND cod_plano = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, Date.valueOf(dtAssinatura));
            stmt.setInt(2, nroMatric);
            stmt.setInt(3, codPlano);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(stmt, conn);
        }
    }
}
