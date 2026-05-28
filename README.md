# Sistema de Gerenciamento de Academia

Aplicação desktop para gerenciamento de uma academia, desenvolvida em **Java Swing** com banco de dados **PostgreSQL**. Permite cadastrar, alterar, consultar e remover alunos, instrutores, planos, assinaturas, pagamentos, atividades e equipamentos. Toda a comunicação com o banco é feita via **JDBC com SQL puro**, sem ORM.

O projeto reúne os entregáveis das etapas da disciplina de Banco de Dados (Módulo 02):

- **Etapa inicial** — modelagem e carga
- **Etapa 04** — consultas, visões e índices
- **Etapa 05** — funções, procedimentos e triggers
- **Etapa 06** — interface final com **Dashboard Estatístico Integrado** (indicadores + 6 gráficos dinâmicos com filtro de período)

## Tecnologias

- Java 17
- Java Swing com Nimbus Look and Feel + componentes próprios (cards arredondados com sombra, cabeçalho em gradiente, gráficos em Java2D)
- PostgreSQL 12+
- JDBC com `PreparedStatement` e `CallableStatement` (sem ORM)
- Maven (build e empacotamento com `maven-assembly-plugin`)

> **Restrição da Etapa 06:** todo SQL é explícito no backend; nenhuma biblioteca de mapeamento objeto-relacional ou camada de abstração de banco é utilizada. Os gráficos do dashboard são desenhados em Java2D puro, sem dependência externa — a única dependência continua sendo o driver `org.postgresql`.

## Banco de dados

O banco segue um modelo conceitual com **herança** (Pessoa → Aluno/Instrutor), **entidade associativa** (Assinatura), **especialização** (Musculação) e **auto-relacionamento** (Instrutor supervisiona Instrutor). São **13 tabelas**:

| Tabela | Descrição |
|--------|-----------|
| **pessoa** | Supertipo com CPF (PK), nome, email, data de nascimento e endereço (atributo composto: rua, bairro, cep) |
| **telefone_pessoa** | Telefones do contato (atributo multivalorado; PK composta cpf + telefone) |
| **aluno** | Herda de Pessoa — matrícula, status (`ATIVO`/`INATIVO`/`SUSPENSO`), obs. de saúde |
| **instrutor** | Herda de Pessoa — CREF, salário, data de admissão, auto-relacionamento (`cref_supervisor`) |
| **plano** | Planos da academia (duração em meses e valor mensal) |
| **assinatura** | Entidade associativa Aluno ↔ Plano (PK composta: dt_assinatura, nro_matric, cod_plano) |
| **pagamento** | Entidade fraca vinculada à assinatura (status `PENDENTE`/`PAGO`/`ATRASADO`/`CANCELADO`) |
| **atividade** | Atividades oferecidas (Crossfit, Yoga, etc.) |
| **musculacao** | Especialização total/disjunta de Atividade |
| **modalidade** | Níveis das atividades (`INICIANTE`/`INTERMEDIARIO`/`AVANCADO`) |
| **aula** | Entidade fraca — aulas com data, modalidade e instrutor |
| **equipamento** | Equipamentos da academia vinculados a atividades |
| **manutencao** | Entidade fraca — manutenções de equipamentos |

### Constraints e regras

- **CHECK** para validar valores numéricos (`salario > 0`, `valor > 0`, `duracao > 0`) e restringir texto (valores de `status`, `nivel` da modalidade, etc.)
- **DEFAULT** em datas de cadastro, status de assinatura, nível de modalidade, descrição de atividade/equipamento
- **UNIQUE** em CPF, email, nome de plano e nome de modalidade
- **ON UPDATE CASCADE** entre assinatura e plano
- **ON DELETE SET NULL** entre aula e instrutor
- **Chave composta** na assinatura (`dt_assinatura`, `nro_matric`, `cod_plano`)
- **Auto-referência** no instrutor (`cref_supervisor` → `instrutor.cref`)
- **Sequências** para geração automática de IDs (`seq_nro_matric`, `seq_plano`, `seq_pagamento`, etc.)

### Dados de exemplo

Cada tabela possui ao menos 30 tuplas inseridas, totalizando mais de 400 registros. O script opcional `sql/etapa06/00_mock_dados_anuais.sql` redistribui esses dados pelos anos **2024, 2025 e 2026** (de forma determinística e reaplicável), para que o filtro de período do dashboard tenha informação em todos os anos.

## Objetos de banco (Etapas 04 e 05)

### Script consolidado (recomendado)

Todos os objetos das Etapas 04 e 05 ficam reunidos em **[sql/script_completo_etapas_04_05.sql](sql/script_completo_etapas_04_05.sql)** — basta rodá-lo após a carga inicial:

```bash
psql -U postgres -h localhost -d academia_db -f sql/script_completo_etapas_04_05.sql
```

É **re-executável** (usa `CREATE OR REPLACE`, `IF NOT EXISTS`, `DROP TRIGGER IF EXISTS`) e está dividido em 7 partes: índices, views, funções, procedimentos, triggers, consultas de demonstração e exemplos de uso.

### Arquivos por entregável (separados)

Para quem prefere ver cada entregável isolado, os scripts originais estão em `sql/etapa04/` e `sql/etapa05/`:

| Arquivo | Conteúdo |
|---------|----------|
| `sql/etapa04/01_consultas.sql` | **4 consultas**: (1) JOIN + GROUP BY + HAVING — planos mais rentáveis; (2) 2 JOINs + WHERE — cobrança; (3) Anti-join — alunos sem assinatura; (4) Subconsultas — instrutores sêniores |
| `sql/etapa04/02_visoes.sql` | **2 views**: `vw_assinaturas_ativas` e `vw_equipamentos_custosos` |
| `sql/etapa04/03_indices.sql` | **2 índices**: `idx_pagamento_status_plano` e `idx_assinatura_nro_matric` |
| `sql/etapa05/01_funcoes.sql` | **2 funções**: `fn_receita_mes_plano(plano, ano, mes)` e `fn_classificar_inadimplencia(matric)` (usa IF/ELSIF) |
| `sql/etapa05/02_procedimentos.sql` | **2 procedimentos**: `sp_reajustar_valor_plano(plano, %)` e `sp_renovar_assinaturas_vencidas(janela)` (usa **CURSOR**) |
| `sql/etapa05/03_triggers.sql` | Tabela de log `log_alteracao_plano` + **2 triggers** |

> O procedimento `sp_reajustar_valor_plano` dispara automaticamente o trigger `tg_log_alteracao_valor_plano`, que grava o histórico de reajustes na tabela `log_alteracao_plano` — efeito do trigger visível na hora.

## Etapa 06 — Dashboard Estatístico Integrado

A aba **Dashboard** é a entrega da Etapa 06 (opcional +0,5). Todos os números vêm do banco em tempo real via `RelatorioDAO` (SQL cru, sem ORM).

- **8 cards de KPI year-aware** — Alunos cadastrados, Instrutores admitidos, Assinaturas iniciadas, Receita PAGO, Pagamentos pendentes, Total de pagamentos, Ticket médio, % Pagos. Cada card usa a coluna de data adequada (`dt_cadastro`/`dt_admissao`/`dt_inicio`/`dt_venc`) e atualiza junto com o filtro de período.
- **Estatísticas do salário dos instrutores** acumuladas até o ano selecionado — **média**, **mediana** (`percentile_cont`), **moda** (`mode() WITHIN GROUP`), **variância** e **desvio padrão** direto no banco.
- **6 gráficos dinâmicos** em Java2D puro:
  1. Pizza — pagamentos por status
  2. Barras horizontais — Top 10 planos por receita
  3. Linha — receita por mês (tendência temporal)
  4. Barras horizontais — alunos por faixa de inadimplência (usa a função da Etapa 05)
  5. Barras — assinaturas por status
  6. Pizza — alunos por status
- **Filtro interativo de período (ano)** que re-consulta cards, estatísticas e gráficos.

> **Observação sobre a Etapa 06:** os requisitos de "interface para executar/visualizar função+procedimento+trigger" e "consultas/views acessíveis na interface" foram, a pedido, movidos para o script consolidado `sql/script_completo_etapas_04_05.sql`, executável diretamente no PostgreSQL via `psql` ou qualquer cliente SQL.

## Interface

A aplicação possui 8 abas:

- **Dashboard** — Etapa 06 (indicadores year-aware, estatísticas e 6 gráficos)
- **Alunos** — CRUD completo com dados pessoais, endereço e telefone
- **Instrutores** — CRUD com CREF, salário e supervisor
- **Planos** — CRUD com duração e valor mensal
- **Assinaturas** — CRUD com chave composta, combo de aluno e plano
- **Pagamentos** — CRUD vinculado a assinaturas
- **Atividades** — CRUD de atividades oferecidas
- **Equipamentos** — CRUD de equipamentos

### Funcionalidades extras

- Busca em tempo real em todas as tabelas
- Exportação para CSV (compatível com Excel)
- Validação de campos obrigatórios
- Máscaras automáticas de CPF, telefone, CEP e datas
- **Design system** próprio (`Tema.java`) — paleta refinada, fontes Segoe UI, botões com hover
- **Cards arredondados com sombra suave** (`Cartao.java`) e **cabeçalho com gradiente** (`Tema.cabecalho(...)`)
- Gráficos com paleta consistente e antialiasing

## Como rodar

### 1. Pré-requisitos

- JDK 17
- Maven 3.6+
- PostgreSQL 12+ em execução

### 2. Criar e popular o banco

```bash
psql -U postgres -h localhost -c "CREATE DATABASE academia_db"
psql -U postgres -h localhost -d academia_db -f sql/01_criar_tabelas.sql
psql -U postgres -h localhost -d academia_db -f sql/02_inserir_dados.sql
psql -U postgres -h localhost -d academia_db -f sql/script_completo_etapas_04_05.sql
```

Opcional — para distribuir os dados de demonstração pelos anos 2024-2026 (deixa o filtro de período do dashboard com dados em todos os anos):

```bash
psql -U postgres -h localhost -d academia_db -f sql/etapa06/00_mock_dados_anuais.sql
```

### 3. Configurar a conexão

Em [`src/main/java/com/academia/conexao/ConexaoBD.java`](src/main/java/com/academia/conexao/ConexaoBD.java), ajuste usuário e senha:

```java
private static final String URL = "jdbc:postgresql://localhost:5432/academia_db";
private static final String USUARIO = "postgres";
private static final String SENHA = "sua_senha";
```

### 4. Compilar e executar

```bash
mvn clean package
java -jar target/sistema-academia-1.0-jar-with-dependencies.jar
```

## Exemplos de uso dos objetos de banco

```sql
-- Receita confirmada de um plano em um mês
SELECT fn_receita_mes_plano(1, 2024, 1);

-- Classificação de inadimplência por aluno
SELECT a.nro_matric, fn_classificar_inadimplencia(a.nro_matric) AS situacao
  FROM aluno a LIMIT 20;

-- Reajuste de plano (dispara o trigger de log automaticamente)
CALL sp_reajustar_valor_plano(1, 8.0);
SELECT * FROM log_alteracao_plano ORDER BY dt_alteracao DESC LIMIT 5;

-- Renovar assinaturas vencidas (procedure com cursor)
CALL sp_renovar_assinaturas_vencidas(0);

-- Views
SELECT * FROM vw_assinaturas_ativas    LIMIT 10;
SELECT * FROM vw_equipamentos_custosos ORDER BY custo_total DESC;
```

## Estrutura do projeto

```
crud-bd/
├── pom.xml                                       -- Configuração Maven
├── bd_schema.sql                                 -- Schema de referência
├── sql/
│   ├── 01_criar_tabelas.sql                      -- Sequências e 13 tabelas
│   ├── 02_inserir_dados.sql                      -- Carga de dados de exemplo
│   ├── script_completo_etapas_04_05.sql          -- Script consolidado (Etapa 04 + 05)
│   ├── etapa04/                                  -- Consultas, visões e índices (separados)
│   │   ├── 01_consultas.sql
│   │   ├── 02_visoes.sql
│   │   └── 03_indices.sql
│   ├── etapa05/                                  -- Funções, procedimentos e triggers (separados)
│   │   ├── 01_funcoes.sql
│   │   ├── 02_procedimentos.sql
│   │   └── 03_triggers.sql
│   └── etapa06/
│       └── 00_mock_dados_anuais.sql              -- Redistribui dados por ano (opcional)
└── src/main/java/com/academia/
    ├── Main.java                                 -- Entry point (splash + boot)
    ├── conexao/
    │   └── ConexaoBD.java                        -- Conexão JDBC centralizada
    ├── modelo/                                   -- POJOs de domínio
    │   ├── Aluno.java / Instrutor.java / Plano.java
    │   ├── Assinatura.java / Pagamento.java
    │   ├── Atividade.java / Equipamento.java
    ├── dao/                                      -- Data Access Objects (SQL explícito)
    │   ├── AlunoDAO.java / InstrutorDAO.java / PlanoDAO.java
    │   ├── AssinaturaDAO.java / PagamentoDAO.java
    │   ├── AtividadeDAO.java / EquipamentoDAO.java
    │   ├── RelatorioDAO.java                     -- Estatísticas e dados dos gráficos do Dashboard
    │   └── ResultadoTabela.java                  -- DTO genérico (colunas + linhas)
    └── tela/                                     -- Telas Swing
        ├── Tema.java                             -- Design system (paleta, fontes, botões, cabeçalho)
        ├── Cartao.java                           -- Painel arredondado com sombra (Java2D)
        ├── Mascara.java                          -- Máscaras de CPF, telefone, CEP, data
        ├── TelaPrincipal.java
        ├── TelaDashboard.java                    -- Etapa 06: indicadores + 6 gráficos
        ├── TelaAluno.java / TelaInstrutor.java / TelaPlano.java
        ├── TelaAssinatura.java / TelaPagamento.java
        ├── TelaAtividade.java / TelaEquipamento.java
        └── grafico/                              -- Gráficos em Java2D puro
            ├── PaletaGrafico.java
            ├── GraficoBarras.java
            ├── GraficoBarrasHorizontal.java
            ├── GraficoLinha.java
            └── GraficoPizza.java
```

### Arquitetura

A aplicação segue o padrão **DAO** com separação em camadas:

- **modelo/** — Classes de domínio com encapsulamento (atributos privados, construtores, getters/setters)
- **dao/** — Data Access Objects que concentram todo o SQL de cada entidade, retornando objetos e listas tipadas. `RelatorioDAO` centraliza as consultas analíticas do dashboard (`PreparedStatement` para SELECTs, `CallableStatement` para procedimentos)
- **conexao/** — Classe utilitária que centraliza os parâmetros de conexão JDBC
- **tela/** — Telas Swing (uma por entidade) + Dashboard + design system + gráficos Java2D
