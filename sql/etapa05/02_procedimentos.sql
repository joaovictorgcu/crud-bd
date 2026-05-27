-- =====================================================================
--  Mod 02 - Etapa 05 - Entregável 2: PROCEDIMENTOS
--  Sistema de Gerenciamento de Academia (academia_db / PostgreSQL)
-- =====================================================================

-- Procedimento 1 -- sp_reajustar_valor_plano (ATUALIZAÇÃO de dados)
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
        RAISE EXCEPTION 'Percentual de reajuste inválido: %', p_percentual;
    END IF;

    IF p_percentual > 100 THEN
        RAISE EXCEPTION 'Percentual % parece alto demais. Confirme o valor.', p_percentual;
    END IF;

    SELECT valor_mes
      INTO v_valor_antigo
      FROM plano
     WHERE cod_plano = p_cod_plano;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Plano com código % não encontrado.', p_cod_plano;
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
    'Aplica reajuste percentual no valor mensal de um plano. Dispara o trigger de log de alteração.';


-- Procedimento 2 -- sp_renovar_assinaturas_vencidas (com CURSOR)
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
            UPDATE  assinatura
               SET  status = 'ENCERRADA'
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
            RAISE NOTICE 'OK -> matrícula % renovada (plano %, % parcelas).',
                         rec.nro_matric, rec.cod_plano, rec.duracao;

        EXCEPTION WHEN OTHERS THEN
            v_total_erro := v_total_erro + 1;
            RAISE WARNING 'Falha ao renovar matrícula % no plano %: %',
                          rec.nro_matric, rec.cod_plano, SQLERRM;
        END;
    END LOOP;

    CLOSE cur_assin;

    RAISE NOTICE 'Renovação concluída. Sucesso: %, Falhas: %.',
                 v_total_ok, v_total_erro;
END;
$$;

COMMENT ON PROCEDURE sp_renovar_assinaturas_vencidas(INTEGER) IS
    'Renova assinaturas vencidas: encerra a antiga, cria a nova e gera as parcelas (PENDENTE). Usa cursor para tolerar falhas linha a linha.';
