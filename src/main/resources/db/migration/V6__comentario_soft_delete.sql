ALTER TABLE comentario ADD COLUMN deletado BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_comentario_deletado ON comentario(deletado) WHERE deletado = FALSE;
