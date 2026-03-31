package com.academia.dao;

import com.academia.conexao.ConexaoBD;
import com.academia.modelo.Aluno;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlunoDAO {

    public void inserir(Aluno aluno) {
        Connection conn = null;
        PreparedStatement stmtPessoa = null;
        PreparedStatement stmtAluno = null;
        PreparedStatement stmtTel = null;
        try {
            conn = ConexaoBD.getConexao();
            conn.setAutoCommit(false);

            String sqlPessoa = "INSERT INTO pessoa (cpf, nome, email, dt_nasc, rua, bairro, cep) VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmtPessoa = conn.prepareStatement(sqlPessoa);
            stmtPessoa.setString(1, aluno.getCpf());
            stmtPessoa.setString(2, aluno.getNome());
            stmtPessoa.setString(3, aluno.getEmail());
            stmtPessoa.setDate(4, aluno.getDtNasc() != null ? Date.valueOf(aluno.getDtNasc()) : null);
            stmtPessoa.setString(5, aluno.getRua());
            stmtPessoa.setString(6, aluno.getBairro());
            stmtPessoa.setString(7, aluno.getCep());
            stmtPessoa.executeUpdate();

            String sqlAluno = "INSERT INTO aluno (nro_matric, dt_cadastro, status, obs_saude, cpf) VALUES (nextval('seq_nro_matric'), DEFAULT, ?, ?, ?)";
            stmtAluno = conn.prepareStatement(sqlAluno);
            stmtAluno.setString(1, aluno.getStatus());
            stmtAluno.setString(2, aluno.getObsSaude());
            stmtAluno.setString(3, aluno.getCpf());
            stmtAluno.executeUpdate();

            if (aluno.getTelefone() != null && !aluno.getTelefone().trim().isEmpty()) {
                String sqlTel = "INSERT INTO telefone_pessoa (cpf, telefone) VALUES (?, ?)";
                stmtTel = conn.prepareStatement(sqlTel);
                stmtTel.setString(1, aluno.getCpf());
                stmtTel.setString(2, aluno.getTelefone());
                stmtTel.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(stmtTel, stmtAluno, stmtPessoa, conn);
        }
    }

    public List<Aluno> listarTodos() {
        String sql = "SELECT a.nro_matric, a.dt_cadastro, a.status, a.obs_saude, a.cpf, "
                + "p.nome, p.email, p.dt_nasc, p.rua, p.bairro, p.cep, "
                + "tp.telefone "
                + "FROM aluno a "
                + "JOIN pessoa p ON a.cpf = p.cpf "
                + "LEFT JOIN telefone_pessoa tp ON p.cpf = tp.cpf "
                + "ORDER BY a.nro_matric";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Aluno> lista = new ArrayList<>();

        try {
            conn = ConexaoBD.getConexao();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            int ultimoMatric = -1;

            while (rs.next()) {
                int nroMatric = rs.getInt("nro_matric");
                if (nroMatric == ultimoMatric) continue;
                ultimoMatric = nroMatric;

                Aluno a = new Aluno();
                a.setNroMatric(nroMatric);
                Timestamp ts = rs.getTimestamp("dt_cadastro");
                if (ts != null) a.setDtCadastro(ts.toLocalDateTime());
                a.setStatus(rs.getString("status"));
                a.setObsSaude(rs.getString("obs_saude"));
                a.setCpf(rs.getString("cpf"));
                a.setNome(rs.getString("nome"));
                a.setEmail(rs.getString("email"));
                Date dt = rs.getDate("dt_nasc");
                if (dt != null) a.setDtNasc(dt.toLocalDate());
                a.setRua(rs.getString("rua"));
                a.setBairro(rs.getString("bairro"));
                a.setCep(rs.getString("cep"));
                a.setTelefone(rs.getString("telefone"));
                lista.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(rs, stmt, conn);
        }
        return lista;
    }

    public void atualizar(Aluno aluno) {
        Connection conn = null;
        PreparedStatement stmtPessoa = null;
        PreparedStatement stmtAluno = null;
        try {
            conn = ConexaoBD.getConexao();
            conn.setAutoCommit(false);

            String sqlPessoa = "UPDATE pessoa SET nome = ?, email = ?, dt_nasc = ?, rua = ?, bairro = ?, cep = ? WHERE cpf = ?";
            stmtPessoa = conn.prepareStatement(sqlPessoa);
            stmtPessoa.setString(1, aluno.getNome());
            stmtPessoa.setString(2, aluno.getEmail());
            stmtPessoa.setDate(3, aluno.getDtNasc() != null ? Date.valueOf(aluno.getDtNasc()) : null);
            stmtPessoa.setString(4, aluno.getRua());
            stmtPessoa.setString(5, aluno.getBairro());
            stmtPessoa.setString(6, aluno.getCep());
            stmtPessoa.setString(7, aluno.getCpf());
            stmtPessoa.executeUpdate();

            String sqlAluno = "UPDATE aluno SET status = ?, obs_saude = ? WHERE nro_matric = ?";
            stmtAluno = conn.prepareStatement(sqlAluno);
            stmtAluno.setString(1, aluno.getStatus());
            stmtAluno.setString(2, aluno.getObsSaude());
            stmtAluno.setInt(3, aluno.getNroMatric());
            stmtAluno.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            ConexaoBD.fechar(stmtAluno, stmtPessoa, conn);
        }
    }

    public void deletar(int nroMatric) throws SQLException {
        Connection conn = null;
        try {
            conn = ConexaoBD.getConexao();
            conn.setAutoCommit(false);

            String cpf = null;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT cpf FROM aluno WHERE nro_matric = ?")) {
                stmt.setInt(1, nroMatric);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) cpf = rs.getString("cpf");
            }

            // deletar dependencias do aluno
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM pagamento WHERE nro_matric = ?")) {
                stmt.setInt(1, nroMatric);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM assinatura WHERE nro_matric = ?")) {
                stmt.setInt(1, nroMatric);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM aluno WHERE nro_matric = ?")) {
                stmt.setInt(1, nroMatric);
                stmt.executeUpdate();
            }

            if (cpf != null) {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM telefone_pessoa WHERE cpf = ?")) {
                    stmt.setString(1, cpf);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM pessoa WHERE cpf = ?")) {
                    stmt.setString(1, cpf);
                    stmt.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw e;
        } finally {
            ConexaoBD.fechar(conn);
        }
    }
}
