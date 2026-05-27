-- =====================================================================
--  Mod 02 - Etapa 05 - Entregável 3: TRIGGERS
--  Sistema de Gerenciamento de Academia (academia_db / PostgreSQL)
-- =====================================================================

-- Tabela auxiliar -- log_alteracao_plano (dependência do Trigger 1)
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
    'Histórico de alterações no valor mensal dos planos. Alimentada pelo trigger tg_log_alteracao_valor_plano.';


-- Trigger 1 -- tg_log_alteracao_valor_plano (atualiza tabela de logs)
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

    INSERT INTO log_alteracao_plano (
        cod_plano, valor_antigo, valor_novo, variacao_pct
    )
    VALUES (
        NEW.cod_plano, OLD.valor_mes, NEW.valor_mes, v_variacao
    );

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS tg_log_alteracao_valor_plano ON plano;
CREATE TRIGGER tg_log_alteracao_valor_plano
AFTER UPDATE OF valor_mes ON plano
FOR EACH ROW
WHEN (OLD.valor_mes IS DISTINCT FROM NEW.valor_mes)
EXECUTE FUNCTION fn_tg_log_alteracao_valor_plano();


-- Trigger 2 -- tg_inativar_aluno_sem_assinatura
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
