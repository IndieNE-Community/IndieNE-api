ALTER TABLE jogo ADD COLUMN deletado BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE postagem ADD COLUMN deletado BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_jogo_deletado ON jogo(deletado) WHERE deletado = FALSE;
CREATE INDEX idx_postagem_deletado ON postagem(deletado) WHERE deletado = FALSE;
