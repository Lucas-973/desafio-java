CREATE TABLE IF NOT EXISTS projeto
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome            VARCHAR(150)   NOT NULL,
    datainicio      DATE           NOT NULL,
    datafimprevisao DATE           NOT NULL,
    datafimfinal    DATE,
    orcamento       NUMERIC(19, 2) NOT NULL,
    descricao       VARCHAR(2000)  NOT NULL,
    idgerente       BIGINT         NOT NULL,
    situacao        VARCHAR(30)    NOT NULL
);

CREATE INDEX idx_projeto_idgerente ON projeto (idgerente);
