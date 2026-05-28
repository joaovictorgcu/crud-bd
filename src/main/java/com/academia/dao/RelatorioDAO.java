package com.academia.dao;

import com.academia.conexao.ConexaoBD;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;

/**
 * DAO de relatorios (Etapas 04, 05 e Dashboard).
 *
 * Todo o SQL fica explicito aqui (backend), via JDBC + PreparedStatement /
 * CallableStatement. Nenhum ORM ou camada de abstracao de banco e utilizado:
 * os comandos SELECT, CALL e funcoes sao enviados como texto SQL ao PostgreSQL.
 */
public class RelatorioDAO {

    // =====================================================================
    //  Motor generico
    // =====================================================================

    /** Executa um SELECT e devolve colunas + linhas (usa ResultSetMetaData). */
    public ResultadoTabela executar(String sql, Object... params) throws SQLException {
        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int n = md.getColumnCount();

                String[] colunas = new String[n];
                for (int i = 0; i < n; i++) {
                    colunas[i] = md.getColumnLabel(i + 1);
                }

                ResultadoTabela rt = new ResultadoTabela(colunas);
                while (rs.next()) {
                    Object[] linha = new Object[n];
                    for (int i = 0; i < n; i++) {
                        linha[i] = rs.getObject(i + 1);
                    }
                    rt.addLinha(linha);
                }
                return rt;
            }
        }
    }

    /** Retorna o primeiro valor da primeira linha (indicadores). */
    public Object valorUnico(String sql, Object... params) throws SQLException {
        try (Connection conn = ConexaoBD.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getObject(1);
            }
        }
        return null;
    }

    public double valorDouble(String sql, Object... params) throws SQLException {
        Object v = valorUnico(sql, params);
        return (v instanceof Number) ? ((Number) v).doubleValue() : 0d;
    }

    // =====================================================================
    //  Etapa 04 - Consultas (com filtros parametrizados onde faz sentido)
    // =====================================================================

    /** Consulta 1 - JOIN + GROUP BY + HAVING (planos mais rentaveis). */
    public ResultadoTabela consultaPlanosRentaveis(double minReceita, int minPagamentos) throws SQLException {
        String sql =
            "SELECT  p.cod_plano, p.nome AS plano, p.duracao AS duracao_meses, p.valor_mes, " +
            "        COUNT(pg.cod_pgto) AS pagamentos_confirmados, " +
            "        SUM(pg.valor) AS receita_confirmada, " +
            "        ROUND(AVG(pg.valor), 2) AS ticket_medio " +
            "FROM    plano p " +
            "JOIN    pagamento pg ON pg.cod_plano = p.cod_plano " +
            "WHERE   pg.status = 'PAGO' " +
            "GROUP BY p.cod_plano, p.nome, p.duracao, p.valor_mes " +
            "HAVING   SUM(pg.valor) > ? AND COUNT(pg.cod_pgto) >= ? " +
            "ORDER BY receita_confirmada DESC, plano ASC";
        return executar(sql, minReceita, minPagamentos);
    }

    /** Consulta 2 - 2 JOINs + WHERE (cobranca: pagamentos a vencer/atrasados). */
    public ResultadoTabela consultaCobranca(int diasJanela) throws SQLException {
        String sql =
            "SELECT  pe.nome AS aluno, pe.email, a.nro_matric, pg.cod_pgto, pg.valor, " +
            "        pg.dt_venc, pg.status, (CURRENT_DATE - pg.dt_venc) AS dias_atraso " +
            "FROM    pagamento pg " +
            "JOIN    aluno  a  ON a.nro_matric = pg.nro_matric " +
            "JOIN    pessoa pe ON pe.cpf = a.cpf " +
            "WHERE   pg.status IN ('PENDENTE', 'ATRASADO') " +
            "  AND   pg.dt_venc < CURRENT_DATE + (? || ' days')::INTERVAL " +
            "ORDER BY pg.dt_venc ASC, pe.nome ASC";
        return executar(sql, String.valueOf(diasJanela));
    }

    /** Consulta 3 - Anti Join (LEFT JOIN + IS NULL): alunos sem assinatura. */
    public ResultadoTabela consultaAlunosSemAssinatura() throws SQLException {
        String sql =
            "SELECT  a.nro_matric, pe.nome, pe.email, a.dt_cadastro, a.status AS status_aluno " +
            "FROM      aluno a " +
            "JOIN      pessoa pe ON pe.cpf = a.cpf " +
            "LEFT JOIN assinatura asg ON asg.nro_matric = a.nro_matric " +
            "WHERE     asg.nro_matric IS NULL " +
            "ORDER BY  a.dt_cadastro DESC, pe.nome ASC";
        return executar(sql);
    }

    /** Consulta 4 - Subconsulta (escalar + IN): instrutores senioures. */
    public ResultadoTabela consultaInstrutoresSeniores() throws SQLException {
        String sql =
            "SELECT  i.cref, pe.nome, i.salario, i.dt_admissao, " +
            "        (SELECT ROUND(AVG(salario), 2) FROM instrutor) AS media_geral " +
            "FROM    instrutor i " +
            "JOIN    pessoa pe ON pe.cpf = i.cpf " +
            "WHERE   i.salario > (SELECT AVG(salario) FROM instrutor) " +
            "  AND   i.cref IN (SELECT DISTINCT cref_supervisor FROM instrutor WHERE cref_supervisor IS NOT NULL) " +
            "ORDER BY i.salario DESC, pe.nome ASC";
        return executar(sql);
    }

    // =====================================================================
    //  Etapa 04 - Views
    // =====================================================================

    public ResultadoTabela viewAssinaturasAtivas() throws SQLException {
        return executar("SELECT * FROM vw_assinaturas_ativas ORDER BY aluno");
    }

    public ResultadoTabela viewEquipamentosCustosos() throws SQLException {
        return executar("SELECT * FROM vw_equipamentos_custosos ORDER BY custo_total DESC");
    }

    // =====================================================================
    //  Etapa 05 - Funcoes
    // =====================================================================

    /** fn_receita_mes_plano(cod_plano, ano, mes). */
    public double receitaMesPlano(int codPlano, int ano, int mes) throws SQLException {
        return valorDouble("SELECT fn_receita_mes_plano(?, ?, ?)", codPlano, ano, mes);
    }

    /** fn_classificar_inadimplencia(nro_matric). */
    public String classificarInadimplencia(int nroMatric) throws SQLException {
        Object v = valorUnico("SELECT fn_classificar_inadimplencia(?)", nroMatric);
        return v != null ? v.toString() : "-";
    }

    /** Lista todos os alunos com a classificacao da funcao (para tabela). */
    public ResultadoTabela classificacaoTodosAlunos() throws SQLException {
        String sql =
            "SELECT a.nro_matric, pe.nome, a.status AS status_aluno, " +
            "       fn_classificar_inadimplencia(a.nro_matric) AS situacao " +
            "FROM   aluno a JOIN pessoa pe ON pe.cpf = a.cpf " +
            "ORDER BY a.nro_matric";
        return executar(sql);
    }

    // =====================================================================
    //  Etapa 05 - Procedimentos (captura mensagens RAISE NOTICE/WARNING)
    // =====================================================================

    /** sp_reajustar_valor_plano(cod_plano, percentual). Dispara o Trigger 1. */
    public String reajustarPlano(int codPlano, double percentual) throws SQLException {
        return executarProcedure("CALL sp_reajustar_valor_plano(?, ?)", codPlano, percentual);
    }

    /** sp_renovar_assinaturas_vencidas(dias_janela). Usa cursor. */
    public String renovarAssinaturas(int diasJanela) throws SQLException {
        return executarProcedure("CALL sp_renovar_assinaturas_vencidas(?)", diasJanela);
    }

    /**
     * Executa um CALL e coleta as mensagens emitidas por RAISE NOTICE/WARNING
     * (o driver PostgreSQL as entrega como SQLWarning no statement).
     */
    private String executarProcedure(String callSql, Object... params) throws SQLException {
        try (Connection conn = ConexaoBD.getConexao();
             CallableStatement cs = conn.prepareCall(callSql)) {

            for (int i = 0; i < params.length; i++) {
                cs.setObject(i + 1, params[i]);
            }
            cs.execute();

            StringBuilder sb = new StringBuilder();
            SQLWarning w = cs.getWarnings();
            while (w != null) {
                sb.append("- ").append(w.getMessage()).append('\n');
                w = w.getNextWarning();
            }
            return sb.length() == 0 ? "Procedimento executado (sem mensagens)." : sb.toString();
        }
    }

    /** Trigger 1 - conteudo da tabela de log alimentada por tg_log_alteracao_valor_plano. */
    public ResultadoTabela logAlteracaoPlano() throws SQLException {
        String sql =
            "SELECT cod_log, cod_plano, valor_antigo, valor_novo, variacao_pct, usuario_bd, " +
            "       to_char(dt_alteracao, 'DD/MM/YYYY HH24:MI:SS') AS dt_alteracao " +
            "FROM   log_alteracao_plano ORDER BY dt_alteracao DESC, cod_log DESC";
        return executar(sql);
    }

    // =====================================================================
    //  Apoio (combos)
    // =====================================================================

    public ResultadoTabela listarPlanos() throws SQLException {
        return executar("SELECT cod_plano, nome, valor_mes FROM plano ORDER BY cod_plano");
    }

    public ResultadoTabela listarAnosPagamento() throws SQLException {
        return executar("SELECT DISTINCT EXTRACT(YEAR FROM dt_venc)::INTEGER AS ano " +
                        "FROM pagamento WHERE dt_venc IS NOT NULL ORDER BY ano DESC");
    }

    // =====================================================================
    //  Dashboard - Estatisticas (AVG / mediana / moda / variancia / desvio)
    // =====================================================================

    /**
     * Estatisticas do salario dos instrutores em uma unica linha.
     * Filtro opcional por ano (0 = todos): considera instrutores admitidos
     * ate o fim do ano selecionado (visao acumulada do quadro naquele periodo).
     */
    public ResultadoTabela estatisticasSalario(int ano) throws SQLException {
        String filtro = (ano > 0) ? " WHERE EXTRACT(YEAR FROM dt_admissao) <= ?" : "";
        String sql =
            "SELECT ROUND(AVG(salario), 2) AS media, " +
            "       ROUND(percentile_cont(0.5) WITHIN GROUP (ORDER BY salario)::numeric, 2) AS mediana, " +
            "       mode() WITHIN GROUP (ORDER BY salario) AS moda, " +
            "       ROUND(VARIANCE(salario), 2) AS variancia, " +
            "       ROUND(STDDEV(salario), 2) AS desvio_padrao, " +
            "       MIN(salario) AS minimo, MAX(salario) AS maximo " +
            "FROM instrutor" + filtro;
        return (ano > 0) ? executar(sql, ano) : executar(sql);
    }

    // =====================================================================
    //  Dashboard - Dados para graficos
    // =====================================================================

    /** Pizza: quantidade de pagamentos por status (filtro opcional por ano; 0 = todos). */
    public ResultadoTabela pagamentosPorStatus(int ano) throws SQLException {
        if (ano > 0) {
            return executar("SELECT status, COUNT(*) AS qtd FROM pagamento " +
                            "WHERE EXTRACT(YEAR FROM dt_venc) = ? GROUP BY status ORDER BY status", ano);
        }
        return executar("SELECT status, COUNT(*) AS qtd FROM pagamento GROUP BY status ORDER BY status");
    }

    /** Barras: Top N planos por receita confirmada (PAGO); filtro opcional por ano. */
    public ResultadoTabela receitaPorPlano(int ano, int limite) throws SQLException {
        String filtroAno = (ano > 0) ? "AND EXTRACT(YEAR FROM pg.dt_venc) = ? " : "";
        String sql =
            "SELECT p.nome, COALESCE(SUM(pg.valor), 0) AS receita " +
            "FROM   plano p " +
            "LEFT JOIN pagamento pg ON pg.cod_plano = p.cod_plano AND pg.status = 'PAGO' " + filtroAno +
            "GROUP BY p.cod_plano, p.nome ORDER BY receita DESC LIMIT ?";
        return (ano > 0) ? executar(sql, ano, limite) : executar(sql, limite);
    }

    /** Linha: receita confirmada por mes (filtro opcional por ano; 0 = todos). */
    public ResultadoTabela receitaPorMes(int ano) throws SQLException {
        if (ano > 0) {
            return executar(
                "SELECT to_char(dt_venc, 'YYYY-MM') AS mes, SUM(valor) AS receita " +
                "FROM pagamento WHERE status = 'PAGO' AND EXTRACT(YEAR FROM dt_venc) = ? " +
                "GROUP BY mes ORDER BY mes", ano);
        }
        return executar(
            "SELECT to_char(dt_venc, 'YYYY-MM') AS mes, SUM(valor) AS receita " +
            "FROM pagamento WHERE status = 'PAGO' " +
            "GROUP BY mes ORDER BY mes");
    }

    /** Barras horizontais: alunos por faixa de inadimplencia (usa a funcao da Etapa 05). */
    public ResultadoTabela alunosPorInadimplencia() throws SQLException {
        String sql =
            "SELECT fn_classificar_inadimplencia(a.nro_matric) AS faixa, COUNT(*) AS qtd " +
            "FROM aluno a GROUP BY faixa ORDER BY qtd DESC";
        return executar(sql);
    }

    /** Barras: assinaturas por status. */
    public ResultadoTabela assinaturasPorStatus() throws SQLException {
        return executar("SELECT status, COUNT(*) AS qtd FROM assinatura GROUP BY status ORDER BY status");
    }

    /** Pizza: alunos por status (ATIVO / INATIVO / SUSPENSO). */
    public ResultadoTabela alunosPorStatus() throws SQLException {
        return executar("SELECT status, COUNT(*) AS qtd FROM aluno GROUP BY status ORDER BY status");
    }
}
