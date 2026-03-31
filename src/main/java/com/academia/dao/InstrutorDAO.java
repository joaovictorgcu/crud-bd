package com.academia.dao;

import com.academia.conexao.ConexaoBD;
import com.academia.modelo.Instrutor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstrutorDAO {

    public void inserir(Instrutor instrutor) {
        Connection conn = null;
        PreparedStatement stmtPessoa = null;
        PreparedStatement stmtInstrutor = null;
        try {
            conn = ConexaoBD.getConexao();
            conn.setAutoCommit(false);

            String sqlPessoa = "INSERT INTO pessoa (cpf, nome, email, dt_nasc, rua, bairro, cep) VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmtPessoa = conn.prepareStatement(sqlPessoa);
            stmtPessoa.setString(1, instrutor.getCpf());
            stmtPessoa.setString(2, instrutor.getNome());
            stmtPessoa.setString(3, instrutor.getEmail());
            stmtPessoa.setDate(4, instrutor.getDtNasc() != null ? Date.valueOf(instrutor.getDtNasc()) : null);
            stmtPessoa.setString(5, instrutor.getRua());
            stmtPessoa.setString(6, instrutor.getBairro());
            stmtPessoa.setString(7, instrutor.getCep());
            stmtPessoa.executeUpdate();

            String sqlInst = "INSERT INTO instrutor (cref, salario, dt_admissao, cpf, cref_supervisor) VALUES (?, ?, ?, ?, ?)";
            stmtInstrutor = conn.prepareStatement(sqlInst);
            stmtInstrutor.setString(1, instrutor.getCref());
            stmtInstrutor.setBigDecimal(2, instrutor.getSalario());
            stmtInstrutor.setDate(3, instrutor.getDtAdmissao() != null ? Date.valueOf(instrutor.getDtAdmissao()) : null);
            stmtInstrutor.setString(4, instrutor.getCpf());
            stmtInstrutor.setString(5, instrutor.getCrefSupervisor());
            stmtInstrutor.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(stmtInstrutor, stmtPessoa, conn);
        }
    }

    public List<Instrutor> listarTodos() {
        String sql = "SELECT i.cref, i.salario, i.dt_admissao, i.cpf, i.cref_supervisor, "
                + "p.nome, p.email, p.dt_nasc, p.rua, p.bairro, p.cep "
                + "FROM instrutor i "
                + "JOIN pessoa p ON i.cpf = p.cpf "
                + "ORDER BY p.nome";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Instrutor> lista = new ArrayList<>();

        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Instrutor i = new Instrutor();
                i.setCref(rs.getString("cref"));
                i.setSalario(rs.getBigDecimal("salario"));
                Date dtAdm = rs.getDate("dt_admissao");
                if (dtAdm != null) i.setDtAdmissao(dtAdm.toLocalDate());
                i.setCpf(rs.getString("cpf"));
                i.setCrefSupervisor(rs.getString("cref_supervisor"));
                i.setNome(rs.getString("nome"));
                i.setEmail(rs.getString("email"));
                Date dtNasc = rs.getDate("dt_nasc");
                if (dtNasc != null) i.setDtNasc(dtNasc.toLocalDate());
                i.setRua(rs.getString("rua"));
                i.setBairro(rs.getString("bairro"));
                i.setCep(rs.getString("cep"));
                lista.add(i);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(rs, stmt, conn);
        }
        return lista;
    }

    public void atualizar(Instrutor instrutor) {
        Connection conn = null;
        PreparedStatement stmtPessoa = null;
        PreparedStatement stmtInstrutor = null;
        try {
            conn = ConexaoBD.getConexao();
            conn.setAutoCommit(false);

            String sqlPessoa = "UPDATE pessoa SET nome = ?, email = ?, dt_nasc = ?, rua = ?, bairro = ?, cep = ? WHERE cpf = ?";
            stmtPessoa = conn.prepareStatement(sqlPessoa);
            stmtPessoa.setString(1, instrutor.getNome());
            stmtPessoa.setString(2, instrutor.getEmail());
            stmtPessoa.setDate(3, instrutor.getDtNasc() != null ? Date.valueOf(instrutor.getDtNasc()) : null);
            stmtPessoa.setString(4, instrutor.getRua());
            stmtPessoa.setString(5, instrutor.getBairro());
            stmtPessoa.setString(6, instrutor.getCep());
            stmtPessoa.setString(7, instrutor.getCpf());
            stmtPessoa.executeUpdate();

            String sqlInst = "UPDATE instrutor SET salario = ?, dt_admissao = ?, cref_supervisor = ? WHERE cref = ?";
            stmtInstrutor = conn.prepareStatement(sqlInst);
            stmtInstrutor.setBigDecimal(1, instrutor.getSalario());
            stmtInstrutor.setDate(2, instrutor.getDtAdmissao() != null ? Date.valueOf(instrutor.getDtAdmissao()) : null);
            stmtInstrutor.setString(3, instrutor.getCrefSupervisor());
            stmtInstrutor.setString(4, instrutor.getCref());
            stmtInstrutor.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(stmtInstrutor, stmtPessoa, conn);
        }
    }

    public void deletar(String cref) {
        Connection conn = null;
        PreparedStatement stmtGetCpf = null;
        PreparedStatement stmtDelInstrutor = null;
        PreparedStatement stmtDelPessoa = null;
        ResultSet rs = null;
        try {
            conn = ConexaoBD.getConexao();
            conn.setAutoCommit(false);

            stmtGetCpf = conn.prepareStatement("SELECT cpf FROM instrutor WHERE cref = ?");
            stmtGetCpf.setString(1, cref);
            rs = stmtGetCpf.executeQuery();
            String cpf = null;
            if (rs.next()) {
                cpf = rs.getString("cpf");
            }

            stmtDelInstrutor = conn.prepareStatement("DELETE FROM instrutor WHERE cref = ?");
            stmtDelInstrutor.setString(1, cref);
            stmtDelInstrutor.executeUpdate();

            if (cpf != null) {
                stmtDelPessoa = conn.prepareStatement("DELETE FROM pessoa WHERE cpf = ?");
                stmtDelPessoa.setString(1, cpf);
                stmtDelPessoa.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(rs, stmtDelPessoa, stmtDelInstrutor, stmtGetCpf, conn);
        }
    }
}
