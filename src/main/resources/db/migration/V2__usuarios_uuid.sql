ALTER TABLE jogo DROP CONSTRAINT fk_jogo_usuario;
ALTER TABLE comentario DROP CONSTRAINT fk_comentario_usuario;
ALTER TABLE doacao DROP CONSTRAINT fk_doacao_usuario;

DROP TABLE usuarios;

CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    senha TEXT NOT NULL,
    tipo VARCHAR(50) NOT NULL
);

ALTER TABLE jogo DROP COLUMN usuario_id;
ALTER TABLE jogo ADD COLUMN usuario_id UUID NOT NULL;
ALTER TABLE jogo ADD CONSTRAINT fk_jogo_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE;

ALTER TABLE comentario DROP COLUMN user_id;
ALTER TABLE comentario ADD COLUMN user_id UUID NOT NULL;
ALTER TABLE comentario ADD CONSTRAINT fk_comentario_usuario
    FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE CASCADE;

ALTER TABLE doacao DROP COLUMN user_id;
ALTER TABLE doacao ADD COLUMN user_id UUID NOT NULL;
ALTER TABLE doacao ADD CONSTRAINT fk_doacao_usuario
    FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE CASCADE;

CREATE INDEX idx_jogo_usuario ON jogo(usuario_id);
CREATE INDEX idx_comentario_usuario ON comentario(user_id);
CREATE INDEX idx_doacao_usuario ON doacao(user_id);
