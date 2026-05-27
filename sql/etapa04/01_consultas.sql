-- =====================================================================
--  Mod 02 - Etapa 04 - Entregável 1: CONSULTAS
--  Sistema de Gerenciamento de Academia (academia_db / PostgreSQL)
-- =====================================================================

-- Consulta 1 -- JOIN + GROUP BY + HAVING
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
HAVING   SUM(pg.valor)      >  500
   AND   COUNT(pg.cod_pgto) >= 3
ORDER BY receita_confirmada DESC, plano ASC;


-- Consulta 2 -- 2 JOINs + WHERE
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


-- Consulta 3 -- Anti Join (LEFT JOIN + IS NULL)
SELECT  a.nro_matric,
        pe.nome,
        pe.email,
        a.dt_cadastro,
        a.status                              AS status_aluno
FROM      aluno      a
JOIN      pessoa     pe  ON pe.cpf       = a.cpf
LEFT JOIN assinatura asg ON asg.nro_matric = a.nro_matric
WHERE     asg.nro_matric IS NULL
ORDER BY  a.dt_cadastro DESC, pe.nome ASC;


-- Consulta 4 -- Subconsulta (escalar no WHERE + IN com subselect)
SELECT  i.cref,
        pe.nome,
        i.salario,
        i.dt_admissao,
        (SELECT ROUND(AVG(salario), 2) FROM instrutor) AS media_geral
FROM    instrutor i
JOIN    pessoa    pe ON pe.cpf = i.cpf
WHERE   i.salario > (SELECT AVG(salario) FROM instrutor)
  AND   i.cref   IN (SELECT DISTINCT cref_supervisor
                       FROM instrutor
                      WHERE cref_supervisor IS NOT NULL)
ORDER BY i.salario DESC, pe.nome ASC;
