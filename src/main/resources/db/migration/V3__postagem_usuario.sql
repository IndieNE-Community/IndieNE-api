ALTER TABLE postagem ADD COLUMN usuario_id UUID NOT NULL;

ALTER TABLE postagem ADD CONSTRAINT fk_postagem_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE;

CREATE INDEX idx_postagem_usuario ON postagem(usuario_id);
