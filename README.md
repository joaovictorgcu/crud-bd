# Sistema de Gerenciamento de Academia

Aplicação desktop para gerenciamento de uma academia, desenvolvida em Java Swing com banco de dados PostgreSQL. Permite cadastrar, alterar, consultar e remover alunos, instrutores, planos, assinaturas, pagamentos, atividades e equipamentos. Toda a comunicação com o banco é feita via JDBC com SQL puro, sem ORM.

## Tecnologias

- Java 17
- Java Swing (interface gráfica)
- PostgreSQL
- JDBC com PreparedStatement
- Maven

## Banco de dados

O banco segue um modelo conceitual com herança (Pessoa → Aluno/Instrutor), entidade associativa (Assinatura) e especialização (Musculação). São 15 tabelas ao todo:

| Tabela | Descrição |
|--------|-----------|
| **pessoa** | Supertipo com CPF, nome, email e endereço |
| **telefone_pessoa** | Telefones (multivalorado) |
| **aluno** | Herda de Pessoa — matrícula, status, obs. saúde |
| **instrutor** | Herda de Pessoa — CREF, salário, supervisor |
| **plano** | Planos da academia (duração e valor) |
| **assinatura** | Entidade associativa Aluno ↔ Plano (PK composta) |
| **pagamento** | Pagamentos vinculados a assinaturas |
| **atividade** | Atividades oferecidas (Crossfit, Yoga, etc.) |
| **musculacao** | Especialização de Atividade |
| **modalidade** | Níveis (Iniciante, Intermediário, Avançado) |
| **aula** | Aulas com data, modalidade e instrutor responsável |
| **equipamento** | Equipamentos da academia |
| **manutencao** | Manutenções de equipamentos |
| **frequenta** | Registro de frequência dos alunos nas atividades |
| **utiliza** | Equipamentos utilizados por cada atividade |

### Constraints e regras

- **CHECK** para validar valores numéricos (salário > 0, valor > 0, duração > 0) e restringir texto (status só aceita valores definidos, nível da modalidade, etc.)
- **DEFAULT** em datas de cadastro, status de assinatura, nível de modalidade
- **UNIQUE** em CPF, email, nome de plano e modalidade
- **ON UPDATE CASCADE** entre assinatura e plano
- **ON DELETE CASCADE** em telefone_pessoa
- **Chave composta** na assinatura (dt_assinatura, nro_matric, cod_plano)
- **Auto-referência** no instrutor (cref_supervisor → instrutor)

## Interface

A aplicação possui 7 abas:

- **Alunos** — CRUD completo com dados pessoais, endereço e telefone
- **Instrutores** — CRUD com CREF, salário e supervisor
- **Planos** — CRUD com duração e valor mensal
- **Assinaturas** — CRUD com chave composta, combo de aluno e plano
- **Pagamentos** — CRUD vinculado a assinaturas
- **Atividades** — CRUD de atividades oferecidas
- **Equipamentos** — CRUD de equipamentos

## Como rodar

### 1. Criar o banco

```sql
CREATE DATABASE academia_db;
```

### 2. Executar os scripts SQL

Conecte no banco `academia_db` e execute na ordem:

1. `sql/01_criar_tabelas.sql` — cria sequências e tabelas
2. `sql/02_inserir_dados.sql` — popula com dados de exemplo

### 3. Configurar a conexão

Em `src/main/java/com/academia/conexao/ConexaoBD.java`, ajuste usuário e senha:

```java
private static final String URL = "jdbc:postgresql://localhost:5432/academia_db";
private static final String USUARIO = "postgres";
private static final String SENHA = "sua_senha";
```

### 4. Compilar e executar

Com Maven:

```bash
mvn compile exec:java -Dexec.mainClass="com.academia.Main"
```

Ou diretamente com javac/java:

```bash
javac -encoding UTF-8 -cp "postgresql-42.7.3.jar" -d target/classes src/main/java/com/academia/**/*.java src/main/java/com/academia/Main.java
java -cp "target/classes;postgresql-42.7.3.jar" com.academia.Main
```

## Estrutura do projeto

```
ENTREGA_BD_copia/
├── pom.xml
├── bd_schema.sql
├── sql/
│   ├── 01_criar_tabelas.sql
│   └── 02_inserir_dados.sql
└── src/main/java/com/academia/
    ├── Main.java
    ├── conexao/
    │   └── ConexaoBD.java
    ├── modelo/
    │   ├── Aluno.java
    │   ├── Instrutor.java
    │   ├── Plano.java
    │   ├── Assinatura.java
    │   ├── Pagamento.java
    │   ├── Atividade.java
    │   └── Equipamento.java
    ├── dao/
    │   ├── AlunoDAO.java
    │   ├── InstrutorDAO.java
    │   ├── PlanoDAO.java
    │   ├── AssinaturaDAO.java
    │   ├── PagamentoDAO.java
    │   ├── AtividadeDAO.java
    │   └── EquipamentoDAO.java
    └── tela/
        ├── TelaPrincipal.java
        ├── TelaAluno.java
        ├── TelaInstrutor.java
        ├── TelaPlano.java
        ├── TelaAssinatura.java
        ├── TelaPagamento.java
        ├── TelaAtividade.java
        └── TelaEquipamento.java
```
