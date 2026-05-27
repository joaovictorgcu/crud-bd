# Sistema de Gerenciamento de Academia

Aplicação desktop para gerenciamento de uma academia, desenvolvida em **Java Swing** com banco de dados **PostgreSQL**. Permite cadastrar, alterar, consultar e remover alunos, instrutores, planos, assinaturas, pagamentos, atividades e equipamentos. Toda a comunicação com o banco é feita via **JDBC com SQL puro**, sem ORM.

O projeto reúne os entregáveis das etapas da disciplina de Banco de Dados (Módulo 02): modelagem e carga (Etapa inicial), **consultas, visões e índices** (Etapa 04) e **funções, procedimentos e triggers** (Etapa 05).

## Tecnologias

- Java 17
- Java Swing com Nimbus Look and Feel
- PostgreSQL 12+
- JDBC com PreparedStatement
- Maven (build e empacotamento com `maven-assembly-plugin`)

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
- **ON DELETE SET NULL** entre aula e instrutor (se deletar o instrutor, a aula fica sem responsável)
- **Chave composta** na assinatura (`dt_assinatura`, `nro_matric`, `cod_plano`)
- **Auto-referência** no instrutor (`cref_supervisor` → `instrutor.cref`)
- **Sequências** para geração automática de IDs (`seq_nro_matric`, `seq_plano`, `seq_pagamento`, etc.)

### Dados de exemplo

Cada tabela possui ao menos 30 tuplas inseridas, totalizando mais de 400 registros.

## Objetos de banco (Etapas 04 e 05)

Além do schema base, o projeto inclui objetos avançados de banco, organizados em `sql/etapa04/` e `sql/etapa05/`.

### Etapa 04 — Consultas, Visões e Índices

| Arquivo | Conteúdo |
|---------|----------|
| `sql/etapa04/01_consultas.sql` | **4 consultas**: (1) JOIN + GROUP BY + HAVING — planos mais rentáveis; (2) 2 JOINs + WHERE — cobrança de pagamentos a vencer; (3) Anti-join (LEFT JOIN + IS NULL) — alunos sem assinatura; (4) Subconsulta escalar + IN — instrutores sêniores |
| `sql/etapa04/02_visoes.sql` | **2 views**: `vw_assinaturas_ativas` (3 JOINs + WHERE) e `vw_equipamentos_custosos` (1 JOIN + subconsultas) |
| `sql/etapa04/03_indices.sql` | **2 índices**: `idx_pagamento_status_plano` (pagamento) e `idx_assinatura_nro_matric` (assinatura) + `ANALYZE` |

### Etapa 05 — Funções, Procedimentos e Triggers

| Arquivo | Conteúdo |
|---------|----------|
| `sql/etapa05/01_funcoes.sql` | **2 funções**: `fn_receita_mes_plano(cod_plano, ano, mes)` → receita confirmada; `fn_classificar_inadimplencia(nro_matric)` → faixa de inadimplência (usa IF/ELSIF) |
| `sql/etapa05/02_procedimentos.sql` | **2 procedimentos**: `sp_reajustar_valor_plano(cod_plano, percentual)` (UPDATE validado) e `sp_renovar_assinaturas_vencidas(dias_janela)` (usa **CURSOR** com tratamento de erro linha a linha) |
| `sql/etapa05/03_triggers.sql` | Tabela de log `log_alteracao_plano` + **2 triggers**: `tg_log_alteracao_valor_plano` (audita reajustes de valor) e `tg_inativar_aluno_sem_assinatura` (inativa aluno sem assinatura ativa) |

> O procedimento `sp_reajustar_valor_plano` dispara automaticamente o trigger `tg_log_alteracao_valor_plano`, que grava o histórico de reajustes na tabela `log_alteracao_plano`.

## Interface

A aplicação possui 8 abas:

- **Dashboard** — Estatísticas da academia (total de alunos, receita, pagamentos pendentes, etc.)
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
- Sistema de design com cores semânticas (`Tema.java`)
- Botões com hierarquia visual (primário, secundário, perigo)

## Como rodar

### 1. Pré-requisitos

- JDK 17
- Maven 3.6+
- PostgreSQL 12+ em execução

### 2. Criar e popular o banco

Crie o banco e execute os scripts **na ordem**:

```sql
CREATE DATABASE academia_db;
```

Conectado em `academia_db`:

1. `sql/01_criar_tabelas.sql` — cria sequências e tabelas
2. `sql/02_inserir_dados.sql` — popula com dados de exemplo
3. `sql/etapa04/03_indices.sql` — cria índices (recomendado antes das consultas/views)
4. `sql/etapa04/02_visoes.sql` — cria as views
5. `sql/etapa05/01_funcoes.sql` — cria as funções
6. `sql/etapa05/02_procedimentos.sql` — cria os procedimentos
7. `sql/etapa05/03_triggers.sql` — cria a tabela de log e os triggers

As consultas (`sql/etapa04/01_consultas.sql`) podem ser executadas a qualquer momento para validação.

Exemplo via `psql`:

```bash
psql -U postgres -h localhost -c "CREATE DATABASE academia_db"
psql -U postgres -h localhost -d academia_db -f sql/01_criar_tabelas.sql
psql -U postgres -h localhost -d academia_db -f sql/02_inserir_dados.sql
psql -U postgres -h localhost -d academia_db -f sql/etapa04/03_indices.sql
psql -U postgres -h localhost -d academia_db -f sql/etapa04/02_visoes.sql
psql -U postgres -h localhost -d academia_db -f sql/etapa05/01_funcoes.sql
psql -U postgres -h localhost -d academia_db -f sql/etapa05/02_procedimentos.sql
psql -U postgres -h localhost -d academia_db -f sql/etapa05/03_triggers.sql
```

### 3. Configurar a conexão

Em `src/main/java/com/academia/conexao/ConexaoBD.java`, ajuste usuário e senha:

```java
private static final String URL = "jdbc:postgresql://localhost:5432/academia_db";
private static final String USUARIO = "postgres";
private static final String SENHA = "sua_senha";
```

### 4. Compilar e executar

Com Maven (gera um JAR único com o driver embutido via `maven-assembly-plugin`):

```bash
mvn clean package
java -jar target/sistema-academia-1.0-jar-with-dependencies.jar
```

## Exemplos de uso dos objetos de banco

```sql
-- Receita confirmada de um plano em um mês
SELECT fn_receita_mes_plano(1, 2025, 10);

-- Classificação de inadimplência por aluno
SELECT a.nro_matric, fn_classificar_inadimplencia(a.nro_matric) AS situacao
  FROM aluno a LIMIT 20;

-- Reajuste de plano (dispara o trigger de log)
CALL sp_reajustar_valor_plano(1, 8.0);
SELECT * FROM log_alteracao_plano ORDER BY dt_alteracao DESC LIMIT 5;

-- Views
SELECT * FROM vw_assinaturas_ativas    LIMIT 10;
SELECT * FROM vw_equipamentos_custosos ORDER BY custo_total DESC;
```

## Estrutura do projeto

```
crud-bd/
├── pom.xml                          -- Configuração Maven
├── bd_schema.sql                    -- Schema completo de referência
├── sql/
│   ├── 01_criar_tabelas.sql         -- Sequências e 13 tabelas
│   ├── 02_inserir_dados.sql         -- Carga de dados de exemplo
│   ├── etapa04/                     -- Consultas, visões e índices
│   │   ├── 01_consultas.sql
│   │   ├── 02_visoes.sql
│   │   └── 03_indices.sql
│   └── etapa05/                     -- Funções, procedimentos e triggers
│       ├── 01_funcoes.sql
│       ├── 02_procedimentos.sql
│       └── 03_triggers.sql
└── src/
    ├── main/java/com/academia/      -- Aplicação Swing (entry point: Main)
    │   ├── Main.java
    │   ├── conexao/
    │   │   └── ConexaoBD.java        -- Conexão JDBC centralizada
    │   ├── modelo/                   -- Classes de domínio (POJOs)
    │   │   ├── Aluno.java
    │   │   ├── Instrutor.java
    │   │   ├── Plano.java
    │   │   ├── Assinatura.java
    │   │   ├── Pagamento.java
    │   │   ├── Atividade.java
    │   │   └── Equipamento.java
    │   ├── dao/                      -- Data Access Objects (SQL por entidade)
    │   │   ├── AlunoDAO.java
    │   │   ├── InstrutorDAO.java
    │   │   ├── PlanoDAO.java
    │   │   ├── AssinaturaDAO.java
    │   │   ├── PagamentoDAO.java
    │   │   ├── AtividadeDAO.java
    │   │   └── EquipamentoDAO.java
    │   └── tela/                     -- Telas Swing
    │       ├── Tema.java             -- Sistema de design (cores semânticas)
    │       ├── Mascara.java          -- Máscaras de CPF, telefone, CEP, data
    │       ├── TelaPrincipal.java
    │       ├── TelaDashboard.java
    │       ├── TelaAluno.java
    │       ├── TelaInstrutor.java
    │       ├── TelaPlano.java
    │       ├── TelaAssinatura.java
    │       ├── TelaPagamento.java
    │       ├── TelaAtividade.java
    │       └── TelaEquipamento.java
    ├── main/resources/
    │   └── muscle.png                -- Ícone da aplicação
    ├── conexao/ConexaoBD.java        -- Exercício POO (console) — conexão
    ├── modelo/                       -- Exercício POO: Departamento, Funcionario
    └── Main.java                     -- Exercício POO: menu interativo (console)
```

### Arquitetura

A aplicação principal (`src/main/java/com/academia/`) segue o padrão **DAO** com separação em camadas:

- **modelo/** — Classes de domínio com atributos privados, construtores, getters e setters (encapsulamento)
- **dao/** — Data Access Objects que concentram todo o SQL de cada entidade, retornando objetos e listas tipadas
- **conexao/** — Classe utilitária que centraliza os parâmetros de conexão JDBC
- **tela/** — Telas Swing (uma por entidade) + Dashboard, tema e máscaras

> O diretório `src/` (raiz) contém um exercício de POO separado (CRUD de Departamento/Funcionário em modo console), independente da aplicação da academia.
