-- =====================================================================
--  Mod 02 - Etapa 04 - Entregável 2: VISÕES
--  Sistema de Gerenciamento de Academia (academia_db / PostgreSQL)
-- =====================================================================

-- View 1 -- vw_assinaturas_ativas (3 JOINs + WHERE)
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


-- View 2 -- vw_equipamentos_custosos (1 JOIN + subconsultas)
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
