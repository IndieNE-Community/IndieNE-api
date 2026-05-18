CREATE TABLE usuarios (
    id SERIAL PRIMARY KEY,
    nome TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    senha TEXT NOT NULL,
    tipo VARCHAR(50)
);

CREATE TABLE jogo (
    id SERIAL PRIMARY KEY,
    titulo TEXT NOT NULL,
    descricao TEXT,
    meta_financeira DOUBLE PRECISION,
    campanha INTEGER,
    data_inicio DATE,
    data_conclusao DATE,
    num_jogadores INTEGER,
    genero TEXT,
    controle BOOLEAN,
    img_thumb TEXT,

    usuario_id INTEGER NOT NULL,

    CONSTRAINT fk_jogo_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE
);

CREATE TABLE plataformas (
    id SERIAL PRIMARY KEY,
    plataforma TEXT NOT NULL,

    jogo_id INTEGER NOT NULL,

    CONSTRAINT fk_plataforma_jogo
        FOREIGN KEY (jogo_id)
        REFERENCES jogo(id)
        ON DELETE CASCADE
);

CREATE TABLE postagem (
    id SERIAL PRIMARY KEY,
    titulo TEXT NOT NULL,
    descricao TEXT,
    data TIMESTAMP,

    jogo_id INTEGER NOT NULL,

    CONSTRAINT fk_postagem_jogo
        FOREIGN KEY (jogo_id)
        REFERENCES jogo(id)
        ON DELETE CASCADE
);

CREATE TABLE comentario (
    id SERIAL PRIMARY KEY,
    texto TEXT NOT NULL,
    data TIMESTAMP,

    user_id INTEGER NOT NULL,
    postagem_id INTEGER NOT NULL,

    CONSTRAINT fk_comentario_usuario
        FOREIGN KEY (user_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_comentario_postagem
        FOREIGN KEY (postagem_id)
        REFERENCES postagem(id)
        ON DELETE CASCADE
);

CREATE TABLE curtida (
    id SERIAL PRIMARY KEY,
    tipo VARCHAR(50),

    comentario_id INTEGER,
    postagem_id INTEGER,

    CONSTRAINT fk_curtida_comentario
        FOREIGN KEY (comentario_id)
        REFERENCES comentario(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_curtida_postagem
        FOREIGN KEY (postagem_id)
        REFERENCES postagem(id)
        ON DELETE CASCADE
);

CREATE TABLE doacao (
    id SERIAL PRIMARY KEY,
    valor DOUBLE PRECISION NOT NULL,
    data TIMESTAMP,

    user_id INTEGER NOT NULL,
    jogo_id INTEGER NOT NULL,

    CONSTRAINT fk_doacao_usuario
        FOREIGN KEY (user_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_doacao_jogo
        FOREIGN KEY (jogo_id)
        REFERENCES jogo(id)
        ON DELETE CASCADE
);

CREATE TABLE imagem (
    id SERIAL PRIMARY KEY,
    imagem TEXT NOT NULL,

    jogo_id INTEGER,
    postagem_id INTEGER,

    CONSTRAINT fk_imagem_jogo
        FOREIGN KEY (jogo_id)
        REFERENCES jogo(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_imagem_postagem
        FOREIGN KEY (postagem_id)
        REFERENCES postagem(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_jogo_usuario ON jogo(usuario_id);
CREATE INDEX idx_postagem_jogo ON postagem(jogo_id);
CREATE INDEX idx_comentario_usuario ON comentario(user_id);
CREATE INDEX idx_comentario_postagem ON comentario(postagem_id);
CREATE INDEX idx_doacao_usuario ON doacao(user_id);
CREATE INDEX idx_doacao_jogo ON doacao(jogo_id);
