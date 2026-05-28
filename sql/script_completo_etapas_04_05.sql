-- =====================================================================
--  SCRIPT COMPLETO - Etapas 04 e 05
--  Sistema de Gerenciamento de Academia  (academia_db / PostgreSQL 12)
-- =====================================================================
--
--  Este arquivo reune, em um unico lugar, TODOS os objetos das etapas:
--      Etapa 04 -> Indices, Views e Consultas
--      Etapa 05 -> Funcoes, Procedimentos e Triggers
--
--  Pre-requisitos (rodar antes, uma vez):
--      1) sql/01_criar_tabelas.sql   (sequencias + 13 tabelas)
--      2) sql/02_inserir_dados.sql   (dados de exemplo)
--
--  Como rodar (PostgreSQL 12):
--      psql -U postgres -h localhost -d academia_db -f script_completo_etapas_04_05.sql
--
--  Observacoes:
--      - Tudo aqui e SQL puro enviado ao banco (sem ORM).
--      - O script e RE-EXECUTAVEL: usa CREATE OR REPLACE, CREATE INDEX
--        IF NOT EXISTS, CREATE TABLE IF NOT EXISTS e DROP TRIGGER IF EXISTS,
--        entao pode rodar quantas vezes quiser sem dar erro.
--      - As PARTES 1 a 5 CRIAM os objetos. As PARTES 6 e 7 apenas
--        DEMONSTRAM (SELECT/CALL) e podem ser removidas se quiser so a
--        criacao dos objetos.
-- =====================================================================


-- #####################################################################
-- ##  ETAPA 04
-- #####################################################################

-- =====================================================================
--  PARTE 1 - INDICES
-- =====================================================================

-- Indice 1: acelera as consultas de pagamento por status + plano
CREATE INDEX IF NOT EXISTS idx_pagamento_status_plano
    ON pagamento (status, cod_plano);

-- Indice 2: acelera o join por matricula (anti-join e view de assinaturas)
CREATE INDEX IF NOT EXISTS idx_assinatura_nro_matric
    ON assinatura (nro_matric);

ANALYZE;


-- =====================================================================
--  PARTE 2 - VIEWS
-- =====================================================================

-- View 1: assinaturas ativas (3 JOINs + WHERE), resolve a heranca aluno->pessoa
CREATE OR REPLACE VIEW vw_assinaturas_ativas AS
SELECT  asg.dt_assinatura,
        asg.dt_inicio,
        asg.dt_fim,
        asg.status                  AS status_assinatura,
        a.nro_matric,
        pe.cpf,
        pe.nome                     AS aluno,
        pe.email,
        pl.cod_plano,
        pl.nome                     AS plano,
        pl.duracao                  AS duracao_meses,
        pl.valor_mes
FROM    assinatura asg
JOIN    aluno      a  ON a.nro_matric = asg.nro_matric
JOIN    pessoa     pe ON pe.cpf       = a.cpf
JOIN    plano      pl ON pl.cod_plano = asg.cod_plano
WHERE   asg.status = 'ATIVA';

-- View 2: equipamentos com custo de manutencao acima da media do parque
CREATE OR REPLACE VIEW vw_equipamentos_custosos AS
SELECT  e.cod_equip,
        e.nome                                                                          AS equipamento,
        at.nome                                                                         AS atividade,
        e.qtd_utilizada,
        (SELECT COUNT(*)               FROM manutencao m WHERE m.cod_equip = e.cod_equip) AS total_manutencoes,
        (SELECT COALESCE(SUM(custo),0) FROM manutencao m WHERE m.cod_equip = e.cod_equip) AS custo_total,
        (SELECT MAX(data)              FROM manutencao m WHERE m.cod_equip = e.cod_equip) AS ultima_manutencao
FROM    equipamento e
JOIN    atividade   at ON at.cod_ativ = e.cod_ativ
WHERE   (SELECT COALESCE(SUM(custo),0) FROM manutencao m WHERE m.cod_equip = e.cod_equip)
        >
        (SELECT AVG(soma_por_eq)
           FROM (SELECT SUM(custo) AS soma_por_eq
                   FROM manutencao
                  GROUP BY cod_equip) sub);


-- #####################################################################
-- ##  ETAPA 05
-- #####################################################################

-- =====================================================================
--  PARTE 3 - FUNCOES
-- =====================================================================

-- Funcao 1: receita confirmada (PAGO) de um plano num mes especifico
CREATE OR REPLACE FUNCTION fn_receita_mes_plano(
    p_cod_plano   INTEGER,
    p_ano         INTEGER,
    p_mes         INTEGER
)
RETURNS NUMERIC
LANGUAGE plpgsql
STABLE
AS $$
DECLARE
    v_total NUMERIC;
BEGIN
    SELECT COALESCE(SUM(valor), 0)
      INTO v_total
      FROM pagamento
     WHERE cod_plano                 = p_cod_plano
       AND status                    = 'PAGO'
       AND EXTRACT(YEAR  FROM dt_venc) = p_ano
       AND EXTRACT(MONTH FROM dt_venc) = p_mes;

    RETURN v_total;
END;
$$;

COMMENT ON FUNCTION fn_receita_mes_plano(INTEGER, INTEGER, INTEGER) IS
    'Soma da receita confirmada (PAGO) de um plano em um mes especifico. Retorna 0 quando nao ha pagamentos.';


-- Funcao 2: classifica a situacao de inadimplencia de um aluno (usa IF/ELSIF)
CREATE OR REPLACE FUNCTION fn_classificar_inadimplencia(
    p_nro_matric INTEGER
)
RETURNS TEXT
LANGUAGE plpgsql
STABLE
AS $$
DECLARE
    v_max_dias_atraso INTEGER;
    v_qtd_pendentes   INTEGER;
BEGIN
    SELECT  MAX(CURRENT_DATE - dt_venc),
            COUNT(*)
      INTO  v_max_dias_atraso,
            v_qtd_pendentes
      FROM  pagamento
     WHERE  nro_matric = p_nro_matric
       AND  status IN ('PENDENTE', 'ATRASADO');

    IF v_qtd_pendentes = 0 OR v_qtd_pendentes IS NULL THEN
        RETURN 'EM DIA';
    ELSIF v_max_dias_atraso > 30 THEN
        RETURN 'CRITICO';
    ELSIF v_max_dias_atraso > 7 THEN
        RETURN 'INADIMPLENTE';
    ELSIF v_max_dias_atraso > 0 THEN
        RETURN 'INADIMPLENTE';
    ELSE
        RETURN 'ATENCAO';
    END IF;
END;
$$;

COMMENT ON FUNCTION fn_classificar_inadimplencia(INTEGER) IS
    'Classifica situacao financeira do aluno em faixas (EM DIA / ATENCAO / INADIMPLENTE / CRITICO).';


-- =====================================================================
--  PARTE 4 - PROCEDIMENTOS
-- =====================================================================

-- Procedimento 1: reajusta o valor de um plano (ATUALIZACAO). Dispara o Trigger 1.
CREATE OR REPLACE PROCEDURE sp_reajustar_valor_plano(
    p_cod_plano    INTEGER,
    p_percentual   NUMERIC
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_valor_antigo  NUMERIC;
    v_valor_novo    NUMERIC;
BEGIN
    IF p_percentual IS NULL OR p_percentual < 0 THEN
        RAISE EXCEPTION 'Percentual de reajuste invalido: %', p_percentual;
    END IF;

    IF p_percentual > 100 THEN
        RAISE EXCEPTION 'Percentual % parece alto demais. Confirme o valor.', p_percentual;
    END IF;

    SELECT valor_mes
      INTO v_valor_antigo
      FROM plano
     WHERE cod_plano = p_cod_plano;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Plano com codigo % nao encontrado.', p_cod_plano;
    END IF;

    v_valor_novo := ROUND(v_valor_antigo * (1 + p_percentual / 100.0), 2);

    UPDATE plano
       SET valor_mes = v_valor_novo
     WHERE cod_plano = p_cod_plano;

    RAISE NOTICE 'Plano % reajustado de R$ % para R$ % (%%%).',
                 p_cod_plano, v_valor_antigo, v_valor_novo, p_percentual;
END;
$$;

COMMENT ON PROCEDURE sp_reajustar_valor_plano(INTEGER, NUMERIC) IS
    'Aplica reajuste percentual no valor mensal de um plano. Dispara o trigger de log de alteracao.';


-- Procedimento 2: renova assinaturas vencidas usando CURSOR (tolera falha por linha)
CREATE OR REPLACE PROCEDURE sp_renovar_assinaturas_vencidas(
    p_dias_janela  INTEGER DEFAULT 0
)
LANGUAGE plpgsql
AS $$
DECLARE
    cur_assin CURSOR FOR
        SELECT  asg.dt_assinatura,
                asg.nro_matric,
                asg.cod_plano,
                asg.dt_fim,
                pl.duracao,
                pl.valor_mes
          FROM  assinatura asg
          JOIN  plano       pl ON pl.cod_plano = asg.cod_plano
         WHERE  asg.status = 'ATIVA'
           AND  asg.dt_fim <= CURRENT_DATE + (p_dias_janela || ' days')::INTERVAL
         ORDER BY asg.dt_fim ASC;

    rec               RECORD;
    v_nova_dt_inicio  DATE;
    v_nova_dt_fim     DATE;
    v_parcela         INTEGER;
    v_total_ok        INTEGER := 0;
    v_total_erro      INTEGER := 0;
BEGIN
    OPEN cur_assin;

    LOOP
        FETCH cur_assin INTO rec;
        EXIT WHEN NOT FOUND;

        BEGIN
            -- encerra a assinatura antiga (status valido no CHECK: VENCIDA)
            UPDATE  assinatura
               SET  status = 'VENCIDA'
             WHERE  dt_assinatura = rec.dt_assinatura
               AND  nro_matric    = rec.nro_matric
               AND  cod_plano     = rec.cod_plano;

            v_nova_dt_inicio := rec.dt_fim + INTERVAL '1 day';
            v_nova_dt_fim    := v_nova_dt_inicio + (rec.duracao || ' months')::INTERVAL - INTERVAL '1 day';

            INSERT INTO assinatura (dt_assinatura, nro_matric, cod_plano, dt_inicio, dt_fim, status)
            VALUES (CURRENT_DATE, rec.nro_matric, rec.cod_plano,
                    v_nova_dt_inicio, v_nova_dt_fim, 'ATIVA');

            FOR v_parcela IN 1..rec.duracao LOOP
                INSERT INTO pagamento (nro_matric, cod_plano, valor, dt_venc, status)
                VALUES (rec.nro_matric,
                        rec.cod_plano,
                        rec.valor_mes,
                        v_nova_dt_inicio + ((v_parcela - 1) || ' months')::INTERVAL,
                        'PENDENTE');
            END LOOP;

            v_total_ok := v_total_ok + 1;
            RAISE NOTICE 'OK -> matricula % renovada (plano %, % parcelas).',
                         rec.nro_matric, rec.cod_plano, rec.duracao;

        EXCEPTION WHEN OTHERS THEN
            v_total_erro := v_total_erro + 1;
            RAISE WARNING 'Falha ao renovar matricula % no plano %: %',
                          rec.nro_matric, rec.cod_plano, SQLERRM;
        END;
    END LOOP;

    CLOSE cur_assin;

    RAISE NOTICE 'Renovacao concluida. Sucesso: %, Falhas: %.',
                 v_total_ok, v_total_erro;
END;
$$;

COMMENT ON PROCEDURE sp_renovar_assinaturas_vencidas(INTEGER) IS
    'Renova assinaturas vencidas: encerra a antiga, cria a nova e gera as parcelas (PENDENTE). Usa cursor para tolerar falhas linha a linha.';


-- =====================================================================
--  PARTE 5 - TRIGGERS (+ tabela de log)
-- =====================================================================

-- Tabela de log (dependencia do Trigger 1)
CREATE TABLE IF NOT EXISTS log_alteracao_plano (
    cod_log          SERIAL          PRIMARY KEY,
    cod_plano        INTEGER         NOT NULL,
    valor_antigo     DECIMAL(10,2)   NOT NULL,
    valor_novo       DECIMAL(10,2)   NOT NULL,
    variacao_pct     DECIMAL(6,2),
    usuario_bd       TEXT            NOT NULL DEFAULT CURRENT_USER,
    dt_alteracao     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE log_alteracao_plano IS
    'Historico de alteracoes no valor mensal dos planos. Alimentada pelo trigger tg_log_alteracao_valor_plano.';

-- Trigger 1: registra no log toda alteracao de valor de plano
CREATE OR REPLACE FUNCTION fn_tg_log_alteracao_valor_plano()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_variacao NUMERIC;
BEGIN
    IF OLD.valor_mes = 0 THEN
        v_variacao := NULL;
    ELSE
        v_variacao := ROUND( ((NEW.valor_mes - OLD.valor_mes) / OLD.valor_mes) * 100, 2 );
    END IF;

    INSERT INTO log_alteracao_plano (cod_plano, valor_antigo, valor_novo, variacao_pct)
    VALUES (NEW.cod_plano, OLD.valor_mes, NEW.valor_mes, v_variacao);

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS tg_log_alteracao_valor_plano ON plano;
CREATE TRIGGER tg_log_alteracao_valor_plano
AFTER UPDATE OF valor_mes ON plano
FOR EACH ROW
WHEN (OLD.valor_mes IS DISTINCT FROM NEW.valor_mes)
EXECUTE FUNCTION fn_tg_log_alteracao_valor_plano();


-- Trigger 2: inativa o aluno quando ele fica sem nenhuma assinatura ATIVA
CREATE OR REPLACE FUNCTION fn_tg_inativar_aluno_sem_assinatura()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_qtd_ativas INTEGER;
BEGIN
    IF OLD.status = 'ATIVA' AND NEW.status <> 'ATIVA' THEN

        SELECT COUNT(*)
          INTO v_qtd_ativas
          FROM assinatura
         WHERE nro_matric = NEW.nro_matric
           AND status     = 'ATIVA';

        IF v_qtd_ativas = 0 THEN
            UPDATE aluno
               SET status = 'INATIVO'
             WHERE nro_matric = NEW.nro_matric
               AND status     = 'ATIVO';

            RAISE NOTICE 'Aluno % marcado como INATIVO (sem assinaturas ativas).',
                         NEW.nro_matric;
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS tg_inativar_aluno_sem_assinatura ON assinatura;
CREATE TRIGGER tg_inativar_aluno_sem_assinatura
AFTER UPDATE OF status ON assinatura
FOR EACH ROW
EXECUTE FUNCTION fn_tg_inativar_aluno_sem_assinatura();


-- #####################################################################
-- ##  DEMONSTRACAO (opcional) - pode apagar daqui pra baixo
-- #####################################################################

-- =====================================================================
--  PARTE 6 - CONSULTAS (Etapa 04)
-- =====================================================================

\echo '--- Consulta 1: planos mais rentaveis (JOIN + GROUP BY + HAVING) ---'
SELECT  p.cod_plano,
        p.nome                                   AS plano,
        p.duracao                                AS duracao_meses,
        p.valor_mes,
        COUNT(pg.cod_pgto)                       AS pagamentos_confirmados,
        SUM(pg.valor)                            AS receita_confirmada,
        ROUND(AVG(pg.valor), 2)                  AS ticket_medio
FROM    plano     p
JOIN    pagamento pg ON pg.cod_plano = p.cod_plano
WHERE   pg.status = 'PAGO'
GROUP BY p.cod_plano, p.nome, p.duracao, p.valor_mes
HAVING   SUM(pg.valor) > 100 AND COUNT(pg.cod_pgto) >= 1
ORDER BY receita_confirmada DESC, plano ASC;

\echo '--- Consulta 2: cobranca - pagamentos a vencer/atrasados (2 JOINs + WHERE) ---'
SELECT  pe.nome                                AS aluno,
        pe.email,
        a.nro_matric,
        pg.cod_pgto,
        pg.valor,
        pg.dt_venc,
        pg.status,
        (CURRENT_DATE - pg.dt_venc)            AS dias_atraso
FROM    pagamento pg
JOIN    aluno     a  ON a.nro_matric = pg.nro_matric
JOIN    pessoa    pe ON pe.cpf       = a.cpf
WHERE   pg.status IN ('PENDENTE', 'ATRASADO')
  AND   pg.dt_venc < CURRENT_DATE + INTERVAL '30 days'
ORDER BY pg.dt_venc ASC, pe.nome ASC;

\echo '--- Consulta 3: alunos sem assinatura (anti-join LEFT JOIN + IS NULL) ---'
SELECT  a.nro_matric, pe.nome, pe.email, a.dt_cadastro, a.status AS status_aluno
FROM      aluno      a
JOIN      pessoa     pe  ON pe.cpf        = a.cpf
LEFT JOIN assinatura asg ON asg.nro_matric = a.nro_matric
WHERE     asg.nro_matric IS NULL
ORDER BY  a.dt_cadastro DESC, pe.nome ASC;

\echo '--- Consulta 4: instrutores seniores (subconsultas) ---'
SELECT  i.cref,
        pe.nome,
        i.salario,
        i.dt_admissao,
        (SELECT ROUND(AVG(salario), 2) FROM instrutor) AS media_geral
FROM    instrutor i
JOIN    pessoa    pe ON pe.cpf = i.cpf
WHERE   i.salario > (SELECT AVG(salario) FROM instrutor)
  AND   i.cref   IN (SELECT DISTINCT cref_supervisor FROM instrutor WHERE cref_supervisor IS NOT NULL)
ORDER BY i.salario DESC, pe.nome ASC;


-- =====================================================================
--  PARTE 7 - EXEMPLOS DE USO (funcoes / views / procedimentos / triggers)
-- =====================================================================

\echo '--- View 1: assinaturas ativas (10 primeiras) ---'
SELECT * FROM vw_assinaturas_ativas ORDER BY aluno LIMIT 10;

\echo '--- View 2: equipamentos custosos ---'
SELECT * FROM vw_equipamentos_custosos ORDER BY custo_total DESC;

\echo '--- Funcao 1: receita do plano 1 em 01/2024 ---'
SELECT fn_receita_mes_plano(1, 2024, 1) AS receita_jan_2024;

\echo '--- Funcao 2: classificacao de inadimplencia (10 alunos) ---'
SELECT a.nro_matric, fn_classificar_inadimplencia(a.nro_matric) AS situacao
  FROM aluno a ORDER BY a.nro_matric LIMIT 10;

\echo '--- Procedimento 1 + Trigger 1: reajuste do plano 1 e log gerado ---'
CALL sp_reajustar_valor_plano(1, 5.0);
SELECT cod_plano, valor_antigo, valor_novo, variacao_pct, dt_alteracao
  FROM log_alteracao_plano ORDER BY dt_alteracao DESC, cod_log DESC LIMIT 5;

\echo '--- Procedimento 2: renovar assinaturas vencidas (janela 0 dias) ---'
-- Obs.: se voce rodar este CALL mais de uma vez no MESMO dia, as novas
-- assinaturas tentam usar dt_assinatura = CURRENT_DATE e colidem com a PK
-- composta. Isso aparece como WARNING e o cursor pula a linha (tolerancia a
-- falha por linha) -- comportamento esperado, nao e erro do script.
CALL sp_renovar_assinaturas_vencidas(0);

-- =====================================================================
-- Fim do script completo - Etapas 04 e 05.
-- =====================================================================
