-- Curtida pertence a um usuário e mira exatamente um alvo (postagem OU comentário).

ALTER TABLE curtida ADD COLUMN user_id UUID NOT NULL;

ALTER TABLE curtida ADD CONSTRAINT fk_curtida_usuario
    FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE CASCADE;

-- Exatamente um alvo deve estar preenchido.
ALTER TABLE curtida ADD CONSTRAINT chk_curtida_alvo_unico
    CHECK ((postagem_id IS NOT NULL)::int + (comentario_id IS NOT NULL)::int = 1);

-- Impede curtida duplicada do mesmo usuário no mesmo alvo.
CREATE UNIQUE INDEX uq_curtida_usuario_postagem
    ON curtida(user_id, postagem_id) WHERE postagem_id IS NOT NULL;
CREATE UNIQUE INDEX uq_curtida_usuario_comentario
    ON curtida(user_id, comentario_id) WHERE comentario_id IS NOT NULL;

CREATE INDEX idx_curtida_usuario ON curtida(user_id);
CREATE INDEX idx_curtida_postagem ON curtida(postagem_id);
CREATE INDEX idx_curtida_comentario ON curtida(comentario_id);
