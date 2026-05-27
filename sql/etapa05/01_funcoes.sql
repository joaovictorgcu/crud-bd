-- =====================================================================
--  Mod 02 - Etapa 05 - Entregável 1: FUNÇÕES
--  Sistema de Gerenciamento de Academia (academia_db / PostgreSQL)
-- =====================================================================

-- Função 1 -- fn_receita_mes_plano
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
    'Soma da receita confirmada (PAGO) de um plano em um mês específico. Retorna 0 quando não há pagamentos.';


-- Função 2 -- fn_classificar_inadimplencia (usa IF/condicional)
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
    'Classifica situação financeira do aluno em faixas (EM DIA / ATENCAO / INADIMPLENTE / CRITICO).';
