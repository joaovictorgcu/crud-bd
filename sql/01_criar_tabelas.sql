-- Drop das tabelas (ordem reversa por causa das FKs)
DROP TABLE IF EXISTS utiliza CASCADE;
DROP TABLE IF EXISTS frequenta CASCADE;
DROP TABLE IF EXISTS manutencao CASCADE;
DROP TABLE IF EXISTS aula CASCADE;
DROP TABLE IF EXISTS musculacao CASCADE;
DROP TABLE IF EXISTS pagamento CASCADE;
DROP TABLE IF EXISTS assinatura CASCADE;
DROP TABLE IF EXISTS equipamento CASCADE;
DROP TABLE IF EXISTS modalidade CASCADE;
DROP TABLE IF EXISTS atividade CASCADE;
DROP TABLE IF EXISTS plano CASCADE;
DROP TABLE IF EXISTS instrutor CASCADE;
DROP TABLE IF EXISTS aluno CASCADE;
DROP TABLE IF EXISTS telefone_pessoa CASCADE;
DROP TABLE IF EXISTS pessoa CASCADE;

DROP SEQUENCE IF EXISTS seq_nro_matric;
DROP SEQUENCE IF EXISTS seq_plano;
DROP SEQUENCE IF EXISTS seq_pagamento;
DROP SEQUENCE IF EXISTS seq_atividade;
DROP SEQUENCE IF EXISTS seq_modalidade;
DROP SEQUENCE IF EXISTS seq_equipamento;
DROP SEQUENCE IF EXISTS seq_manutencao;

-- Sequences
CREATE SEQUENCE seq_nro_matric START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_plano START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_pagamento START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_atividade START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_modalidade START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_equipamento START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_manutencao START WITH 1 INCREMENT BY 1;

-- Tabelas

CREATE TABLE pessoa (
    cpf         VARCHAR(14)     PRIMARY KEY,
    nome        VARCHAR(200)    NOT NULL,
    email       VARCHAR(150)    UNIQUE,
    dt_nasc     DATE,
    rua         VARCHAR(200),
    bairro      VARCHAR(100),
    cep         VARCHAR(10)
);

CREATE TABLE telefone_pessoa (
    cpf         VARCHAR(14)     NOT NULL,
    telefone    VARCHAR(15)     NOT NULL,
    PRIMARY KEY (cpf, telefone),
    FOREIGN KEY (cpf) REFERENCES pessoa(cpf)
);

CREATE TABLE aluno (
    nro_matric  INTEGER         PRIMARY KEY DEFAULT nextval('seq_nro_matric'),
    dt_cadastro TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    status      VARCHAR(20)     DEFAULT 'ATIVO'
                                CHECK (status IN ('ATIVO', 'INATIVO', 'SUSPENSO')),
    obs_saude   TEXT,
    cpf         VARCHAR(14)     NOT NULL UNIQUE,
    FOREIGN KEY (cpf) REFERENCES pessoa(cpf)
);

CREATE TABLE instrutor (
    cref            VARCHAR(20)     PRIMARY KEY,
    salario         DECIMAL(10,2)   CHECK (salario > 0),
    dt_admissao     DATE            DEFAULT CURRENT_DATE,
    cpf             VARCHAR(14)     NOT NULL UNIQUE,
    cref_supervisor VARCHAR(20),
    FOREIGN KEY (cpf) REFERENCES pessoa(cpf),
    FOREIGN KEY (cref_supervisor) REFERENCES instrutor(cref)  -- auto-referencia
);

CREATE TABLE plano (
    cod_plano   INTEGER         PRIMARY KEY DEFAULT nextval('seq_plano'),
    nome        VARCHAR(100)    NOT NULL UNIQUE,
    duracao     INTEGER         NOT NULL CHECK (duracao > 0),
    valor_mes   DECIMAL(10,2)   NOT NULL CHECK (valor_mes > 0)
);

CREATE TABLE assinatura (
    dt_assinatura   DATE        NOT NULL,
    nro_matric      INTEGER     NOT NULL,
    cod_plano       INTEGER     NOT NULL,
    dt_inicio       DATE        DEFAULT CURRENT_DATE,
    dt_fim          DATE,
    status          VARCHAR(20) DEFAULT 'ATIVA'
                                CHECK (status IN ('ATIVA', 'CANCELADA', 'VENCIDA')),
    PRIMARY KEY (dt_assinatura, nro_matric, cod_plano),
    FOREIGN KEY (nro_matric) REFERENCES aluno(nro_matric),
    FOREIGN KEY (cod_plano) REFERENCES plano(cod_plano) ON UPDATE CASCADE
);

CREATE TABLE pagamento (
    cod_pgto        INTEGER         PRIMARY KEY DEFAULT nextval('seq_pagamento'),
    dt_venc         DATE            NOT NULL,
    status          VARCHAR(20)     DEFAULT 'PENDENTE'
                                    CHECK (status IN ('PENDENTE', 'PAGO', 'ATRASADO', 'CANCELADO')),
    valor           DECIMAL(10,2)   NOT NULL CHECK (valor > 0),
    dt_assinatura   DATE,
    nro_matric      INTEGER,
    cod_plano       INTEGER,
    FOREIGN KEY (dt_assinatura, nro_matric, cod_plano)
        REFERENCES assinatura(dt_assinatura, nro_matric, cod_plano)
);

CREATE TABLE atividade (
    cod_ativ    INTEGER         PRIMARY KEY DEFAULT nextval('seq_atividade'),
    nome        VARCHAR(100)    NOT NULL,
    descricao   TEXT            DEFAULT 'Sem descrição'
);

-- especializacao de atividade
CREATE TABLE musculacao (
    cod_ativ    INTEGER     PRIMARY KEY,
    FOREIGN KEY (cod_ativ) REFERENCES atividade(cod_ativ)
);

CREATE TABLE modalidade (
    cod_modal   INTEGER         PRIMARY KEY DEFAULT nextval('seq_modalidade'),
    nome        VARCHAR(100)    NOT NULL UNIQUE,
    nivel       VARCHAR(20)     DEFAULT 'INICIANTE'
                                CHECK (nivel IN ('INICIANTE', 'INTERMEDIARIO', 'AVANCADO'))
);

CREATE TABLE aula (
    cod_ativ    INTEGER     NOT NULL,
    data        DATE        NOT NULL,
    status      VARCHAR(20) DEFAULT 'AGENDADA'
                            CHECK (status IN ('AGENDADA', 'REALIZADA', 'CANCELADA')),
    cod_modal   INTEGER     NOT NULL,
    cref        VARCHAR(20),
    PRIMARY KEY (cod_ativ, data),
    FOREIGN KEY (cod_ativ) REFERENCES atividade(cod_ativ),
    FOREIGN KEY (cod_modal) REFERENCES modalidade(cod_modal),
    FOREIGN KEY (cref) REFERENCES instrutor(cref)
);

CREATE TABLE equipamento (
    cod_equip   INTEGER         PRIMARY KEY DEFAULT nextval('seq_equipamento'),
    nome        VARCHAR(100)    NOT NULL,
    descricao   TEXT            DEFAULT 'Sem descrição'
);

CREATE TABLE manutencao (
    cod_manut   INTEGER         PRIMARY KEY DEFAULT nextval('seq_manutencao'),
    data        DATE            DEFAULT CURRENT_DATE,
    tipo        VARCHAR(50)     NOT NULL,
    custo       DECIMAL(10,2)   CHECK (custo >= 0),
    cod_equip   INTEGER         NOT NULL,
    FOREIGN KEY (cod_equip) REFERENCES equipamento(cod_equip)
);

CREATE TABLE frequenta (
    nro_matric  INTEGER     NOT NULL,
    cod_ativ    INTEGER     NOT NULL,
    hr_entrada  TIMESTAMP   NOT NULL,
    hr_saida    TIMESTAMP,
    PRIMARY KEY (nro_matric, cod_ativ, hr_entrada),
    FOREIGN KEY (nro_matric) REFERENCES aluno(nro_matric),
    FOREIGN KEY (cod_ativ) REFERENCES atividade(cod_ativ)
);

CREATE TABLE utiliza (
    cod_ativ        INTEGER     NOT NULL,
    cod_equip       INTEGER     NOT NULL,
    qtd_utilizada   INTEGER     DEFAULT 1 CHECK (qtd_utilizada > 0),
    PRIMARY KEY (cod_ativ, cod_equip),
    FOREIGN KEY (cod_ativ) REFERENCES atividade(cod_ativ),
    FOREIGN KEY (cod_equip) REFERENCES equipamento(cod_equip)
);
