-- =====================================================================
--  Mock - distribuir dados por ano (2024, 2025, 2026)
--  Sistema de Gerenciamento de Academia (academia_db / PostgreSQL)
-- =====================================================================
--
--  Objetivo: fazer com que o filtro de periodo (ano) do Dashboard tenha
--  informacao real em TODOS os anos. Distribui as datas das tabelas
--  principais e variando o status dos pagamentos (garantindo PAGO em
--  cada ano, para haver receita em qualquer filtro).
--
--  E um script determinístico (usa o id de cada linha), entao pode ser
--  reaplicado sem duplicar dados e sem quebrar chaves estrangeiras
--  (nao altera PKs nem o status de assinatura).
-- =====================================================================
BEGIN;

-- Alunos: dt_cadastro distribuida entre 2024-2026
UPDATE aluno
   SET dt_cadastro = make_timestamp(2024 + (nro_matric % 3),
                                    1 + (nro_matric % 12),
                                    1 + (nro_matric % 28), 10, 0, 0);

-- Instrutores recentes (>= 2023): distribui entre 2024-2026.
-- Preserva o historico antigo (2019-2022) para a estatistica acumulada.
UPDATE instrutor
   SET dt_admissao = make_date(2024 + (split_part(cref, '-', 2)::int % 3),
                               1 + (split_part(cref, '-', 2)::int % 12),
                               1 + (split_part(cref, '-', 2)::int % 28))
 WHERE EXTRACT(YEAR FROM dt_admissao) >= 2023;

-- Assinaturas: dt_inicio distribuida entre 2024-2026
UPDATE assinatura
   SET dt_inicio = make_date(2024 + (nro_matric % 3),
                             1 + (cod_plano % 12),
                             1 + (nro_matric % 28));

-- Pagamentos: dt_venc distribuida entre 2024-2026 e mix de status.
-- (% 5 no status x % 3 no ano => todos os status aparecem em todos os anos,
--  com ~40% PAGO para gerar receita em cada periodo.)
UPDATE pagamento
   SET dt_venc = make_date(2024 + (cod_pgto % 3),
                           1 + (cod_pgto % 12),
                           1 + (cod_pgto % 28)),
       status  = (ARRAY['PAGO', 'PAGO', 'PENDENTE', 'ATRASADO', 'CANCELADO'])
                 [1 + (cod_pgto % 5)];

COMMIT;

-- ------------------------------------------------------------ conferencia
SELECT 'alunos cadastrados' AS metrica, EXTRACT(YEAR FROM dt_cadastro)::int AS ano, COUNT(*) AS qtd
  FROM aluno GROUP BY 2 ORDER BY 2;

SELECT 'instrutores admitidos' AS metrica, EXTRACT(YEAR FROM dt_admissao)::int AS ano, COUNT(*) AS qtd
  FROM instrutor GROUP BY 2 ORDER BY 2;

SELECT 'assinaturas iniciadas' AS metrica, EXTRACT(YEAR FROM dt_inicio)::int AS ano, COUNT(*) AS qtd
  FROM assinatura GROUP BY 2 ORDER BY 2;

SELECT EXTRACT(YEAR FROM dt_venc)::int AS ano, status, COUNT(*) AS qtd, SUM(valor) AS valor
  FROM pagamento GROUP BY 1, 2 ORDER BY 1, 2;
