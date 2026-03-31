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

-- Dados

-- Pessoas (alunos)
INSERT INTO pessoa (cpf, nome, email, dt_nasc, rua, bairro, cep) VALUES
('111.111.111-11', 'Ana Clara Silva', 'ana.clara@email.com', '1995-03-15', 'Rua das Flores, 100', 'Centro', '50010-000'),
('222.222.222-22', 'Bruno Costa Oliveira', 'bruno.costa@email.com', '1990-07-22', 'Av. Boa Viagem, 200', 'Boa Viagem', '51020-000'),
('333.333.333-33', 'Carla Mendes Souza', 'carla.mendes@email.com', '1988-11-10', 'Rua do Sol, 50', 'Santo Amaro', '50040-000'),
('444.444.444-44', 'Daniel Ferreira Lima', 'daniel.ferreira@email.com', '1992-01-05', 'Rua Aurora, 300', 'Boa Vista', '50060-000'),
('555.555.555-55', 'Elena Rodrigues Santos', 'elena.rodrigues@email.com', '1997-06-18', 'Av. Conde da Boa Vista, 400', 'Boa Vista', '50060-100'),
('666.666.666-66', 'Felipe Almeida Barros', 'felipe.almeida@email.com', '1985-09-30', 'Rua da Concórdia, 75', 'Santo Antônio', '50010-200'),
('777.777.777-77', 'Gabriela Nunes Pereira', 'gabriela.nunes@email.com', '1993-12-25', 'Rua do Hospicio, 150', 'Boa Vista', '50060-200'),
('888.888.888-88', 'Henrique Dias Campos', 'henrique.dias@email.com', '1991-04-08', 'Av. Dantas Barreto, 500', 'São José', '50020-000'),
('999.999.999-99', 'Isabela Martins Rocha', 'isabela.martins@email.com', '1996-08-14', 'Rua da Palma, 80', 'Centro', '50010-300'),
('100.100.100-10', 'João Pedro Araújo', 'joao.pedro@email.com', '1994-02-20', 'Rua do Riachuelo, 60', 'Boa Vista', '50060-300'),
('110.110.110-11', 'Karina Lopes Vieira', 'karina.lopes@email.com', '1989-05-12', 'Av. Norte, 700', 'Casa Forte', '52060-000'),
('120.120.120-12', 'Lucas Teixeira Gomes', 'lucas.teixeira@email.com', '1998-10-03', 'Rua Real da Torre, 250', 'Madalena', '50610-000'),
('130.130.130-13', 'Mariana Ribeiro Castro', 'mariana.ribeiro@email.com', '1987-07-27', 'Rua Benfica, 180', 'Madalena', '50720-000'),
('140.140.140-14', 'Nicolas Cardoso Pinto', 'nicolas.cardoso@email.com', '1999-03-09', 'Av. Caxanga, 900', 'Varzea', '50740-000'),
('150.150.150-15', 'Olivia Barbosa Cunha', 'olivia.barbosa@email.com', '1986-11-16', 'Rua José Bonifácio, 45', 'Torre', '50710-000'),
('160.160.160-16', 'Paulo Henrique Moura', 'paulo.moura@email.com', '1993-09-01', 'Rua Padre Carapuceiro, 300', 'Boa Viagem', '51020-100'),
('170.170.170-17', 'Rafaela Duarte Melo', 'rafaela.duarte@email.com', '1995-04-25', 'Av. Eng. Domingos Ferreira, 600', 'Boa Viagem', '51020-200'),
('180.180.180-18', 'Samuel Correia Braga', 'samuel.correia@email.com', '1990-12-07', 'Rua Antônio Falcão, 100', 'Boa Viagem', '51020-300'),
('190.190.190-19', 'Tatiana Freitas Ramos', 'tatiana.freitas@email.com', '1988-06-19', 'Rua Setúbal, 200', 'Boa Viagem', '51030-000'),
('200.200.200-20', 'Vinicius Monteiro Reis', 'vinicius.monteiro@email.com', '1997-01-30', 'Av. Conselheiro Aguiar, 800', 'Boa Viagem', '51030-100'),
('210.210.210-21', 'Yasmin Azevedo Nogueira', 'yasmin.azevedo@email.com', '1992-08-11', 'Rua do Futuro, 55', 'Graças', '52011-000'),
('220.220.220-22', 'Wagner Pires Moreira', 'wagner.pires@email.com', '1984-03-22', 'Rua da Amizade, 120', 'Graças', '52011-100'),
('230.230.230-23', 'Ximena Tavares Borges', 'ximena.tavares@email.com', '1996-05-14', 'Av. Rosa e Silva, 350', 'Aflitos', '52020-000'),
('240.240.240-24', 'AndréNascimento Farias', 'andre.nascimento@email.com', '1991-09-28', 'Rua Amélia, 90', 'Graças', '52011-200'),
('250.250.250-25', 'Beatriz Campos Guedes', 'beatriz.campos@email.com', '1994-07-06', 'Rua do Espinheiro, 400', 'Espinheiro', '52021-000'),
('260.260.260-26', 'Cesar Roque Amaral', 'cesar.roque@email.com', '1987-12-15', 'Av. Agamenon Magalhães, 1500', 'Espinheiro', '52021-100'),
('270.270.270-27', 'Diana Pimentel Lacerda', 'diana.pimentel@email.com', '1999-02-08', 'Rua das Pernambucanas, 70', 'Graças', '52011-300'),
('280.280.280-28', 'Eduardo Sampaio Torres', 'eduardo.sampaio@email.com', '1986-10-20', 'Rua Dona Magina Pontual, 160', 'Torre', '50710-100'),
('290.290.290-29', 'Fernanda Vasconcelos Cruz', 'fernanda.vasconcelos@email.com', '1993-04-17', 'Rua Padre Roma, 85', 'Santo Amaro', '50040-100'),
('300.300.300-30', 'Gustavo Lins Pacheco', 'gustavo.lins@email.com', '1998-11-02', 'Av. Rui Barbosa, 500', 'Graças', '52011-400');

-- Pessoas (instrutores)
INSERT INTO pessoa (cpf, nome, email, dt_nasc, rua, bairro, cep) VALUES
('311.311.311-31', 'Ricardo Souza Fernandes', 'ricardo.souza@email.com', '1982-05-10', 'Rua Quarenta e Oito, 100', 'Espinheiro', '52021-200'),
('322.322.322-32', 'Patricia Lima Cavalcanti', 'patricia.lima@email.com', '1985-08-23', 'Rua Carneiro Vilela, 200', 'Aflitos', '52020-100'),
('333.433.433-43', 'Marcos Antonio Ribeiro', 'marcos.antonio@email.com', '1980-01-15', 'Av. Dezessete de Agosto, 300', 'Casa Forte', '52060-100'),
('344.344.344-34', 'Juliana Pereira Dantas', 'juliana.pereira@email.com', '1983-11-28', 'Rua do Paissandu, 80', 'Paissandu', '50070-000'),
('355.355.355-35', 'Thiago Monteiro Costa', 'thiago.monteiro@email.com', '1987-04-03', 'Rua José de Alencar, 150', 'Boa Vista', '50060-400'),
('366.366.366-36', 'Amanda Figueiredo Leal', 'amanda.figueiredo@email.com', '1984-09-17', 'Av. Manoel Borba, 400', 'Boa Vista', '50060-500'),
('377.377.377-37', 'Roberto Carlos Mendonça', 'roberto.carlos@email.com', '1979-06-08', 'Rua da Soledade, 60', 'Boa Vista', '50060-600'),
('388.388.388-38', 'Camila Andrade Machado', 'camila.andrade@email.com', '1986-02-14', 'Rua do Príncipe, 95', 'Boa Vista', '50060-700'),
('399.399.399-39', 'Leonardo Dias Oliveira', 'leonardo.dias@email.com', '1981-07-21', 'Av. Cruz Cabuga, 700', 'Santo Amaro', '50040-200'),
('400.400.400-40', 'Vanessa Rocha Alencar', 'vanessa.rocha@email.com', '1988-12-05', 'Rua Imperial, 250', 'São José', '50020-100'),
('411.411.411-41', 'Pedro Henrique Viana', 'pedro.viana@email.com', '1983-03-30', 'Rua da Aurora, 500', 'Boa Vista', '50060-800'),
('422.422.422-42', 'Larissa Brito Novaes', 'larissa.brito@email.com', '1986-10-12', 'Rua Sete de Setembro, 180', 'Santo Antônio', '50010-400'),
('433.433.433-33', 'Diego Fonseca Medeiros', 'diego.fonseca@email.com', '1980-08-25', 'Av. João de Barros, 350', 'Espinheiro', '52021-300'),
('444.544.544-54', 'Renata Silveira Machado', 'renata.silveira@email.com', '1985-05-19', 'Rua do Giriquiti, 40', 'Boa Vista', '50060-900'),
('455.455.455-45', 'Fabio Augusto Santana', 'fabio.augusto@email.com', '1982-09-07', 'Rua da Saudade, 110', 'Boa Vista', '50060-950');

INSERT INTO telefone_pessoa (cpf, telefone) VALUES
('111.111.111-11', '(81) 99111-1111'),
('222.222.222-22', '(81) 99222-2222'),
('333.333.333-33', '(81) 99333-3333'),
('444.444.444-44', '(81) 99444-4444'),
('555.555.555-55', '(81) 99555-5555'),
('666.666.666-66', '(81) 99666-6666'),
('777.777.777-77', '(81) 99777-7777'),
('888.888.888-88', '(81) 99888-8888'),
('999.999.999-99', '(81) 99999-9999'),
('100.100.100-10', '(81) 98100-1010'),
('110.110.110-11', '(81) 98110-1111'),
('120.120.120-12', '(81) 98120-1212'),
('130.130.130-13', '(81) 98130-1313'),
('140.140.140-14', '(81) 98140-1414'),
('150.150.150-15', '(81) 98150-1515'),
('160.160.160-16', '(81) 98160-1616'),
('170.170.170-17', '(81) 98170-1717'),
('180.180.180-18', '(81) 98180-1818'),
('190.190.190-19', '(81) 98190-1919'),
('200.200.200-20', '(81) 98200-2020'),
('311.311.311-31', '(81) 97311-3131'),
('322.322.322-32', '(81) 97322-3232'),
('333.433.433-43', '(81) 97333-4343'),
('344.344.344-34', '(81) 97344-3434'),
('355.355.355-35', '(81) 97355-3535'),
('366.366.366-36', '(81) 97366-3636'),
('377.377.377-37', '(81) 97377-3737'),
('388.388.388-38', '(81) 97388-3838'),
('399.399.399-39', '(81) 97399-3939'),
('400.400.400-40', '(81) 97400-4040'),
('210.210.210-21', '(81) 98210-2121'),
('220.220.220-22', '(81) 98220-2222'),
('230.230.230-23', '(81) 98230-2323'),
('240.240.240-24', '(81) 98240-2424'),
('250.250.250-25', '(81) 98250-2525'),
('260.260.260-26', '(81) 98260-2626'),
('270.270.270-27', '(81) 98270-2727'),
('280.280.280-28', '(81) 98280-2828'),
('290.290.290-29', '(81) 98290-2929'),
('300.300.300-30', '(81) 98300-3030'),
('411.411.411-41', '(81) 97411-4141'),
('422.422.422-42', '(81) 97422-4242'),
('433.433.433-33', '(81) 97433-4333'),
('444.544.544-54', '(81) 97444-5454'),
('455.455.455-45', '(81) 97455-4545');

INSERT INTO aluno (nro_matric, dt_cadastro, status, obs_saude, cpf) VALUES
(nextval('seq_nro_matric'), '2024-01-10 08:30:00', 'ATIVO', NULL, '111.111.111-11'),
(nextval('seq_nro_matric'), '2024-01-15 09:00:00', 'ATIVO', 'Alergia a látex', '222.222.222-22'),
(nextval('seq_nro_matric'), '2024-02-01 10:15:00', 'ATIVO', NULL, '333.333.333-33'),
(nextval('seq_nro_matric'), '2024-02-10 14:00:00', 'INATIVO', 'Problema no joelho esquerdo', '444.444.444-44'),
(nextval('seq_nro_matric'), '2024-02-20 11:30:00', 'ATIVO', NULL, '555.555.555-55'),
(nextval('seq_nro_matric'), '2024-03-01 08:00:00', 'ATIVO', 'Asma leve', '666.666.666-66'),
(nextval('seq_nro_matric'), '2024-03-10 16:45:00', 'SUSPENSO', NULL, '777.777.777-77'),
(nextval('seq_nro_matric'), '2024-03-15 09:30:00', 'ATIVO', NULL, '888.888.888-88'),
(nextval('seq_nro_matric'), '2024-04-01 07:00:00', 'ATIVO', 'Diabetes tipo 2', '999.999.999-99'),
(nextval('seq_nro_matric'), '2024-04-05 13:20:00', 'ATIVO', NULL, '100.100.100-10'),
(nextval('seq_nro_matric'), '2024-04-15 10:00:00', 'INATIVO', NULL, '110.110.110-11'),
(nextval('seq_nro_matric'), '2024-05-01 08:45:00', 'ATIVO', 'Hipertensão controlada', '120.120.120-12'),
(nextval('seq_nro_matric'), '2024-05-10 15:30:00', 'ATIVO', NULL, '130.130.130-13'),
(nextval('seq_nro_matric'), '2024-05-20 11:00:00', 'ATIVO', NULL, '140.140.140-14'),
(nextval('seq_nro_matric'), '2024-06-01 09:15:00', 'SUSPENSO', 'Lesão no ombro direito', '150.150.150-15'),
(nextval('seq_nro_matric'), '2024-06-10 14:30:00', 'ATIVO', NULL, '160.160.160-16'),
(nextval('seq_nro_matric'), '2024-06-15 08:00:00', 'ATIVO', NULL, '170.170.170-17'),
(nextval('seq_nro_matric'), '2024-07-01 10:30:00', 'ATIVO', 'Tendinite no pulso', '180.180.180-18'),
(nextval('seq_nro_matric'), '2024-07-10 16:00:00', 'INATIVO', NULL, '190.190.190-19'),
(nextval('seq_nro_matric'), '2024-07-20 09:45:00', 'ATIVO', NULL, '200.200.200-20'),
(nextval('seq_nro_matric'), '2024-08-01 07:30:00', 'ATIVO', NULL, '210.210.210-21'),
(nextval('seq_nro_matric'), '2024-08-10 13:00:00', 'ATIVO', 'Problema lombar', '220.220.220-22'),
(nextval('seq_nro_matric'), '2024-08-20 11:15:00', 'ATIVO', NULL, '230.230.230-23'),
(nextval('seq_nro_matric'), '2024-09-01 08:30:00', 'SUSPENSO', NULL, '240.240.240-24'),
(nextval('seq_nro_matric'), '2024-09-10 14:45:00', 'ATIVO', NULL, '250.250.250-25'),
(nextval('seq_nro_matric'), '2024-09-20 10:00:00', 'ATIVO', 'Arritmia cardíaca leve', '260.260.260-26'),
(nextval('seq_nro_matric'), '2024-10-01 09:00:00', 'ATIVO', NULL, '270.270.270-27'),
(nextval('seq_nro_matric'), '2024-10-10 15:30:00', 'INATIVO', NULL, '280.280.280-28'),
(nextval('seq_nro_matric'), '2024-10-20 12:00:00', 'ATIVO', NULL, '290.290.290-29'),
(nextval('seq_nro_matric'), '2024-11-01 08:15:00', 'ATIVO', 'Prótese no quadril', '300.300.300-30');

-- Instrutores sem supervisor
INSERT INTO instrutor (cref, salario, dt_admissao, cpf, cref_supervisor) VALUES
('CREF-001', 5500.00, '2020-01-15', '311.311.311-31', NULL),
('CREF-002', 5200.00, '2020-03-01', '322.322.322-32', NULL),
('CREF-003', 6000.00, '2019-06-10', '333.433.433-43', NULL);

-- Instrutores com supervisor
INSERT INTO instrutor (cref, salario, dt_admissao, cpf, cref_supervisor) VALUES
('CREF-004', 4500.00, '2021-02-01', '344.344.344-34', 'CREF-001'),
('CREF-005', 4800.00, '2021-05-15', '355.355.355-35', 'CREF-001'),
('CREF-006', 4300.00, '2021-08-20', '366.366.366-36', 'CREF-002'),
('CREF-007', 4600.00, '2022-01-10', '377.377.377-37', 'CREF-002'),
('CREF-008', 4100.00, '2022-04-01', '388.388.388-38', 'CREF-003'),
('CREF-009', 4400.00, '2022-07-15', '399.399.399-39', 'CREF-003'),
('CREF-010', 3800.00, '2023-01-05', '400.400.400-40', 'CREF-004'),
('CREF-011', 3900.00, '2023-03-20', '411.411.411-41', 'CREF-005'),
('CREF-012', 4000.00, '2023-06-01', '422.422.422-42', 'CREF-006'),
('CREF-013', 3700.00, '2023-09-10', '433.433.433-33', 'CREF-007'),
('CREF-014', 3600.00, '2024-01-15', '444.544.544-54', 'CREF-008'),
('CREF-015', 3500.00, '2024-04-01', '455.455.455-45', 'CREF-009');

INSERT INTO plano (cod_plano, nome, duracao, valor_mes) VALUES
(nextval('seq_plano'), 'Basico Mensal', 1, 79.90),
(nextval('seq_plano'), 'Basico Trimestral', 3, 69.90),
(nextval('seq_plano'), 'Basico Semestral', 6, 59.90),
(nextval('seq_plano'), 'Basico Anual', 12, 49.90),
(nextval('seq_plano'), 'Intermediário Mensal', 1, 119.90),
(nextval('seq_plano'), 'Intermediário Trimestral', 3, 109.90),
(nextval('seq_plano'), 'Intermediário Semestral', 6, 99.90),
(nextval('seq_plano'), 'Intermediário Anual', 12, 89.90),
(nextval('seq_plano'), 'Completo Mensal', 1, 159.90),
(nextval('seq_plano'), 'Completo Trimestral', 3, 149.90),
(nextval('seq_plano'), 'Completo Semestral', 6, 139.90),
(nextval('seq_plano'), 'Completo Anual', 12, 129.90),
(nextval('seq_plano'), 'Premium Mensal', 1, 199.90),
(nextval('seq_plano'), 'Premium Trimestral', 3, 189.90),
(nextval('seq_plano'), 'Premium Semestral', 6, 179.90),
(nextval('seq_plano'), 'Premium Anual', 12, 169.90),
(nextval('seq_plano'), 'Familia Mensal', 1, 249.90),
(nextval('seq_plano'), 'Familia Trimestral', 3, 229.90),
(nextval('seq_plano'), 'Familia Semestral', 6, 209.90),
(nextval('seq_plano'), 'Familia Anual', 12, 189.90),
(nextval('seq_plano'), 'Estudante Mensal', 1, 59.90),
(nextval('seq_plano'), 'Estudante Trimestral', 3, 49.90),
(nextval('seq_plano'), 'Estudante Semestral', 6, 44.90),
(nextval('seq_plano'), 'Estudante Anual', 12, 39.90),
(nextval('seq_plano'), 'Corporativo Mensal', 1, 89.90),
(nextval('seq_plano'), 'Corporativo Trimestral', 3, 79.90),
(nextval('seq_plano'), 'Corporativo Semestral', 6, 69.90),
(nextval('seq_plano'), 'Corporativo Anual', 12, 59.90),
(nextval('seq_plano'), 'VIP Mensal', 1, 299.90),
(nextval('seq_plano'), 'VIP Anual', 12, 249.90);

INSERT INTO atividade (cod_ativ, nome, descricao) VALUES
(nextval('seq_atividade'), 'Musculação Geral', 'Treino de musculação com aparelhos e pesos livres'),
(nextval('seq_atividade'), 'Musculação Funcional', 'Treino funcional com foco em musculação'),
(nextval('seq_atividade'), 'Musculação Terapêutica', 'Musculação adaptada para reabilitação'),
(nextval('seq_atividade'), 'Crossfit', 'Treino funcional de alta intensidade'),
(nextval('seq_atividade'), 'Yoga', 'Prática de posturas, respiração e meditação'),
(nextval('seq_atividade'), 'Pilates', 'Exercícios de fortalecimento e flexibilidade'),
(nextval('seq_atividade'), 'Spinning', 'Aula de ciclismo indoor com música'),
(nextval('seq_atividade'), 'Natação', 'Aulas de natação em piscina semiolímpica'),
(nextval('seq_atividade'), 'Boxe', 'Treino de boxe recreativo e condicionamento'),
(nextval('seq_atividade'), 'Zumba', 'Dança aeróbica com ritmos latinos'),
(nextval('seq_atividade'), 'Jiu-Jitsu', 'Arte marcial brasileira no solo'),
(nextval('seq_atividade'), 'Alongamento', 'Sessão de alongamento e flexibilidade'),
(nextval('seq_atividade'), 'Hidroginástica', 'Ginástica aquática de baixo impacto'),
(nextval('seq_atividade'), 'Corrida Orientada', 'Treino de corrida em esteira com orientação'),
(nextval('seq_atividade'), 'Treinamento HIIT', 'Treino intervalado de alta intensidade');

-- cod_ativ 1, 2 e 3 sao musculacao
INSERT INTO musculacao (cod_ativ) VALUES (1), (2), (3);

INSERT INTO modalidade (cod_modal, nome, nivel) VALUES
(nextval('seq_modalidade'), 'Crossfit Iniciante', 'INICIANTE'),
(nextval('seq_modalidade'), 'Crossfit Intermediário', 'INTERMEDIARIO'),
(nextval('seq_modalidade'), 'Crossfit Avançado', 'AVANCADO'),
(nextval('seq_modalidade'), 'Yoga Iniciante', 'INICIANTE'),
(nextval('seq_modalidade'), 'Yoga Avançado', 'AVANCADO'),
(nextval('seq_modalidade'), 'Pilates Iniciante', 'INICIANTE'),
(nextval('seq_modalidade'), 'Pilates Intermediário', 'INTERMEDIARIO'),
(nextval('seq_modalidade'), 'Spinning Iniciante', 'INICIANTE'),
(nextval('seq_modalidade'), 'Natação Intermediário', 'INTERMEDIARIO'),
(nextval('seq_modalidade'), 'Boxe Avançado', 'AVANCADO');

INSERT INTO equipamento (cod_equip, nome, descricao) VALUES
(nextval('seq_equipamento'), 'Esteira', 'Esteira ergométrica profissional'),
(nextval('seq_equipamento'), 'Bicicleta Ergométrica', 'Bicicleta para exercícios indoor'),
(nextval('seq_equipamento'), 'Haltere 10kg', 'Par de halteres de 10 quilos'),
(nextval('seq_equipamento'), 'Haltere 20kg', 'Par de halteres de 20 quilos'),
(nextval('seq_equipamento'), 'Barra Olímpica', 'Barra de 20kg para levantamento'),
(nextval('seq_equipamento'), 'Supino Reto', 'Banco de supino reto com suporte'),
(nextval('seq_equipamento'), 'Leg Press', 'Aparelho de leg press 45 graus'),
(nextval('seq_equipamento'), 'Puxador Costas', 'Aparelho de puxada para dorsais'),
(nextval('seq_equipamento'), 'Corda Naval', 'Corda de batalha para treino funcional'),
(nextval('seq_equipamento'), 'Kettlebell 16kg', 'Kettlebell de ferro fundido 16kg'),
(nextval('seq_equipamento'), 'Bola Suica', 'Bola de pilates 65cm'),
(nextval('seq_equipamento'), 'Saco de Pancada', 'Saco de boxe profissional'),
(nextval('seq_equipamento'), 'Colchonete', 'Colchonete para exercicios no solo'),
(nextval('seq_equipamento'), 'Elíptico', 'Aparelho elíptico para cardio'),
(nextval('seq_equipamento'), 'Smith Machine', 'Maquina Smith para musculação guiada');

INSERT INTO assinatura (dt_assinatura, nro_matric, cod_plano, dt_inicio, dt_fim, status) VALUES
('2024-01-10', 1, 1, '2024-01-10', '2024-02-10', 'VENCIDA'),
('2024-01-15', 2, 5, '2024-01-15', '2024-02-15', 'VENCIDA'),
('2024-02-01', 3, 9, '2024-02-01', '2024-03-01', 'VENCIDA'),
('2024-02-10', 4, 2, '2024-02-10', '2024-05-10', 'CANCELADA'),
('2024-02-20', 5, 12, '2024-02-20', '2025-02-20', 'ATIVA'),
('2024-03-01', 6, 6, '2024-03-01', '2024-06-01', 'VENCIDA'),
('2024-03-10', 7, 13, '2024-03-10', '2024-04-10', 'CANCELADA'),
('2024-03-15', 8, 4, '2024-03-15', '2025-03-15', 'ATIVA'),
('2024-04-01', 9, 10, '2024-04-01', '2024-07-01', 'VENCIDA'),
('2024-04-05', 10, 21, '2024-04-05', '2024-05-05', 'VENCIDA'),
('2024-04-15', 11, 7, '2024-04-15', '2024-10-15', 'CANCELADA'),
('2024-05-01', 12, 16, '2024-05-01', '2025-05-01', 'ATIVA'),
('2024-05-10', 13, 3, '2024-05-10', '2024-11-10', 'VENCIDA'),
('2024-05-20', 14, 25, '2024-05-20', '2024-06-20', 'VENCIDA'),
('2024-06-01', 15, 8, '2024-06-01', '2025-06-01', 'CANCELADA'),
('2024-06-10', 16, 14, '2024-06-10', '2024-09-10', 'VENCIDA'),
('2024-06-15', 17, 29, '2024-06-15', '2024-07-15', 'VENCIDA'),
('2024-07-01', 18, 11, '2024-07-01', '2025-01-01', 'ATIVA'),
('2024-07-10', 19, 22, '2024-07-10', '2024-10-10', 'CANCELADA'),
('2024-07-20', 20, 17, '2024-07-20', '2024-08-20', 'VENCIDA'),
('2024-08-01', 21, 15, '2024-08-01', '2025-02-01', 'ATIVA'),
('2024-08-10', 22, 26, '2024-08-10', '2024-11-10', 'VENCIDA'),
('2024-08-20', 23, 30, '2024-08-20', '2025-08-20', 'ATIVA'),
('2024-09-01', 24, 18, '2024-09-01', '2024-12-01', 'CANCELADA'),
('2024-09-10', 25, 23, '2024-09-10', '2025-03-10', 'ATIVA'),
('2024-09-20', 26, 20, '2024-09-20', '2025-09-20', 'ATIVA'),
('2024-10-01', 27, 24, '2024-10-01', '2025-10-01', 'ATIVA'),
('2024-10-10', 28, 19, '2024-10-10', '2025-04-10', 'CANCELADA'),
('2024-10-20', 29, 27, '2024-10-20', '2025-04-20', 'ATIVA'),
('2024-11-01', 30, 28, '2024-11-01', '2025-11-01', 'ATIVA');

INSERT INTO pagamento (cod_pgto, dt_venc, status, valor, dt_assinatura, nro_matric, cod_plano) VALUES
(nextval('seq_pagamento'), '2024-01-10', 'PAGO', 79.90, '2024-01-10', 1, 1),
(nextval('seq_pagamento'), '2024-02-10', 'PAGO', 79.90, '2024-01-10', 1, 1),
(nextval('seq_pagamento'), '2024-01-15', 'PAGO', 119.90, '2024-01-15', 2, 5),
(nextval('seq_pagamento'), '2024-02-15', 'ATRASADO', 119.90, '2024-01-15', 2, 5),
(nextval('seq_pagamento'), '2024-02-01', 'PAGO', 159.90, '2024-02-01', 3, 9),
(nextval('seq_pagamento'), '2024-03-01', 'PAGO', 159.90, '2024-02-01', 3, 9),
(nextval('seq_pagamento'), '2024-02-10', 'PAGO', 69.90, '2024-02-10', 4, 2),
(nextval('seq_pagamento'), '2024-03-10', 'CANCELADO', 69.90, '2024-02-10', 4, 2),
(nextval('seq_pagamento'), '2024-02-20', 'PAGO', 129.90, '2024-02-20', 5, 12),
(nextval('seq_pagamento'), '2024-03-20', 'PAGO', 129.90, '2024-02-20', 5, 12),
(nextval('seq_pagamento'), '2024-03-01', 'PAGO', 109.90, '2024-03-01', 6, 6),
(nextval('seq_pagamento'), '2024-04-01', 'PAGO', 109.90, '2024-03-01', 6, 6),
(nextval('seq_pagamento'), '2024-03-10', 'PAGO', 199.90, '2024-03-10', 7, 13),
(nextval('seq_pagamento'), '2024-04-10', 'CANCELADO', 199.90, '2024-03-10', 7, 13),
(nextval('seq_pagamento'), '2024-03-15', 'PAGO', 49.90, '2024-03-15', 8, 4),
(nextval('seq_pagamento'), '2024-04-15', 'PAGO', 49.90, '2024-03-15', 8, 4),
(nextval('seq_pagamento'), '2024-04-01', 'PAGO', 149.90, '2024-04-01', 9, 10),
(nextval('seq_pagamento'), '2024-05-01', 'ATRASADO', 149.90, '2024-04-01', 9, 10),
(nextval('seq_pagamento'), '2024-04-05', 'PAGO', 59.90, '2024-04-05', 10, 21),
(nextval('seq_pagamento'), '2024-05-05', 'PAGO', 59.90, '2024-04-05', 10, 21),
(nextval('seq_pagamento'), '2024-04-15', 'PAGO', 99.90, '2024-04-15', 11, 7),
(nextval('seq_pagamento'), '2024-05-15', 'CANCELADO', 99.90, '2024-04-15', 11, 7),
(nextval('seq_pagamento'), '2024-05-01', 'PAGO', 169.90, '2024-05-01', 12, 16),
(nextval('seq_pagamento'), '2024-06-01', 'PAGO', 169.90, '2024-05-01', 12, 16),
(nextval('seq_pagamento'), '2024-05-10', 'PAGO', 59.90, '2024-05-10', 13, 3),
(nextval('seq_pagamento'), '2024-06-10', 'PAGO', 59.90, '2024-05-10', 13, 3),
(nextval('seq_pagamento'), '2024-05-20', 'PAGO', 89.90, '2024-05-20', 14, 25),
(nextval('seq_pagamento'), '2024-06-20', 'ATRASADO', 89.90, '2024-05-20', 14, 25),
(nextval('seq_pagamento'), '2024-06-01', 'PAGO', 89.90, '2024-06-01', 15, 8),
(nextval('seq_pagamento'), '2024-07-01', 'CANCELADO', 89.90, '2024-06-01', 15, 8),
(nextval('seq_pagamento'), '2024-06-10', 'PAGO', 189.90, '2024-06-10', 16, 14),
(nextval('seq_pagamento'), '2024-07-10', 'PAGO', 189.90, '2024-06-10', 16, 14),
(nextval('seq_pagamento'), '2024-06-15', 'PAGO', 299.90, '2024-06-15', 17, 29),
(nextval('seq_pagamento'), '2024-07-15', 'PAGO', 299.90, '2024-06-15', 17, 29),
(nextval('seq_pagamento'), '2024-07-01', 'PAGO', 139.90, '2024-07-01', 18, 11),
(nextval('seq_pagamento'), '2024-08-01', 'PAGO', 139.90, '2024-07-01', 18, 11),
(nextval('seq_pagamento'), '2024-07-10', 'PAGO', 49.90, '2024-07-10', 19, 22),
(nextval('seq_pagamento'), '2024-08-10', 'CANCELADO', 49.90, '2024-07-10', 19, 22),
(nextval('seq_pagamento'), '2024-07-20', 'PAGO', 249.90, '2024-07-20', 20, 17),
(nextval('seq_pagamento'), '2024-08-20', 'ATRASADO', 249.90, '2024-07-20', 20, 17),
(nextval('seq_pagamento'), '2024-08-01', 'PAGO', 179.90, '2024-08-01', 21, 15),
(nextval('seq_pagamento'), '2024-09-01', 'PAGO', 179.90, '2024-08-01', 21, 15),
(nextval('seq_pagamento'), '2024-08-10', 'PAGO', 79.90, '2024-08-10', 22, 26),
(nextval('seq_pagamento'), '2024-09-10', 'PAGO', 79.90, '2024-08-10', 22, 26),
(nextval('seq_pagamento'), '2024-08-20', 'PAGO', 249.90, '2024-08-20', 23, 30),
(nextval('seq_pagamento'), '2024-09-20', 'PAGO', 249.90, '2024-08-20', 23, 30),
(nextval('seq_pagamento'), '2024-09-01', 'PAGO', 229.90, '2024-09-01', 24, 18),
(nextval('seq_pagamento'), '2024-10-01', 'CANCELADO', 229.90, '2024-09-01', 24, 18),
(nextval('seq_pagamento'), '2024-09-10', 'PAGO', 44.90, '2024-09-10', 25, 23),
(nextval('seq_pagamento'), '2024-10-10', 'PAGO', 44.90, '2024-09-10', 25, 23),
(nextval('seq_pagamento'), '2024-09-20', 'PAGO', 189.90, '2024-09-20', 26, 20),
(nextval('seq_pagamento'), '2024-10-20', 'PENDENTE', 189.90, '2024-09-20', 26, 20),
(nextval('seq_pagamento'), '2024-10-01', 'PAGO', 39.90, '2024-10-01', 27, 24),
(nextval('seq_pagamento'), '2024-11-01', 'PENDENTE', 39.90, '2024-10-01', 27, 24),
(nextval('seq_pagamento'), '2024-10-10', 'PAGO', 209.90, '2024-10-10', 28, 19),
(nextval('seq_pagamento'), '2024-11-10', 'CANCELADO', 209.90, '2024-10-10', 28, 19),
(nextval('seq_pagamento'), '2024-10-20', 'PAGO', 69.90, '2024-10-20', 29, 27),
(nextval('seq_pagamento'), '2024-11-20', 'PENDENTE', 69.90, '2024-10-20', 29, 27),
(nextval('seq_pagamento'), '2024-11-01', 'PAGO', 59.90, '2024-11-01', 30, 28),
(nextval('seq_pagamento'), '2024-12-01', 'PENDENTE', 59.90, '2024-11-01', 30, 28);

INSERT INTO frequenta (nro_matric, cod_ativ, hr_entrada, hr_saida) VALUES
(1, 1, '2024-06-01 07:00:00', '2024-06-01 08:30:00'),
(2, 1, '2024-06-01 09:00:00', '2024-06-01 10:00:00'),
(3, 4, '2024-06-02 06:30:00', '2024-06-02 07:30:00'),
(5, 5, '2024-06-02 08:00:00', '2024-06-02 09:15:00'),
(6, 6, '2024-06-03 10:00:00', '2024-06-03 11:00:00'),
(8, 7, '2024-06-03 18:00:00', '2024-06-03 19:00:00'),
(9, 8, '2024-06-04 07:00:00', '2024-06-04 08:00:00'),
(10, 9, '2024-06-04 17:00:00', '2024-06-04 18:00:00'),
(12, 10, '2024-06-05 08:30:00', '2024-06-05 09:30:00'),
(13, 11, '2024-06-05 19:00:00', '2024-06-05 20:30:00'),
(14, 1, '2024-06-06 06:00:00', '2024-06-06 07:30:00'),
(16, 2, '2024-06-06 10:00:00', '2024-06-06 11:00:00'),
(17, 12, '2024-06-07 07:00:00', '2024-06-07 07:45:00'),
(18, 13, '2024-06-07 09:00:00', '2024-06-07 10:00:00'),
(20, 14, '2024-06-08 06:30:00', '2024-06-08 07:30:00'),
(21, 15, '2024-06-08 17:00:00', '2024-06-08 17:45:00'),
(22, 4, '2024-06-09 08:00:00', '2024-06-09 09:00:00'),
(25, 5, '2024-06-09 10:00:00', '2024-06-09 11:15:00'),
(26, 7, '2024-06-10 18:30:00', '2024-06-10 19:30:00'),
(29, 1, '2024-06-10 07:00:00', NULL);

INSERT INTO utiliza (cod_ativ, cod_equip, qtd_utilizada) VALUES
(1, 3, 4),
(1, 4, 2),
(1, 5, 2),
(1, 6, 1),
(2, 9, 3),
(2, 10, 4),
(4, 5, 2),
(6, 11, 8),
(7, 2, 15),
(14, 1, 10);

INSERT INTO aula (cod_ativ, data, status, cod_modal, cref) VALUES
(4, '2024-06-01', 'REALIZADA', 1, 'CREF-003'),
(4, '2024-06-03', 'REALIZADA', 2, 'CREF-003'),
(4, '2024-06-05', 'REALIZADA', 3, 'CREF-003'),
(5, '2024-06-02', 'REALIZADA', 4, 'CREF-004'),
(5, '2024-06-04', 'REALIZADA', 5, 'CREF-004'),
(6, '2024-06-03', 'REALIZADA', 6, 'CREF-005'),
(6, '2024-06-05', 'CANCELADA', 7, 'CREF-005'),
(7, '2024-06-04', 'REALIZADA', 8, 'CREF-006'),
(8, '2024-06-05', 'REALIZADA', 9, 'CREF-007'),
(9, '2024-06-06', 'AGENDADA', 10, 'CREF-008'),
(4, '2024-06-08', 'AGENDADA', 1, 'CREF-003'),
(5, '2024-06-09', 'AGENDADA', 4, 'CREF-004'),
(7, '2024-06-10', 'AGENDADA', 8, 'CREF-006'),
(6, '2024-06-10', 'AGENDADA', 6, 'CREF-005'),
(8, '2024-06-12', 'AGENDADA', 9, 'CREF-007');

INSERT INTO manutencao (cod_manut, data, tipo, custo, cod_equip) VALUES
(nextval('seq_manutencao'), '2024-03-15', 'Preventiva', 150.00, 1),
(nextval('seq_manutencao'), '2024-04-10', 'Corretiva', 350.00, 2),
(nextval('seq_manutencao'), '2024-05-01', 'Preventiva', 80.00, 5),
(nextval('seq_manutencao'), '2024-06-20', 'Corretiva', 500.00, 7),
(nextval('seq_manutencao'), '2024-07-05', 'Preventiva', 120.00, 1),
(nextval('seq_manutencao'), '2024-08-12', 'Troca de peça', 220.00, 8),
(nextval('seq_manutencao'), '2024-09-01', 'Preventiva', 0.00, 13),
(nextval('seq_manutencao'), '2024-10-18', 'Corretiva', 450.00, 14),
(nextval('seq_manutencao'), '2024-11-05', 'Preventiva', 200.00, 15),
(nextval('seq_manutencao'), '2024-12-01', 'Lubrificação', 75.00, 6);
