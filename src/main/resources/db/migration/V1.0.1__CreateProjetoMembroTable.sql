CREATE TABLE IF NOT EXISTS projetomembro
(
    idprojeto BIGINT NOT NULL REFERENCES projeto (id) ON DELETE CASCADE,
    idmembro  BIGINT NOT NULL,
    PRIMARY KEY (idprojeto, idmembro)
);

CREATE INDEX idx_projetomembro_idmembro ON projetomembro (idmembro);
