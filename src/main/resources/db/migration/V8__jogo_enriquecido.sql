-- Fase 2: enriquece o jogo para o catálogo público.

-- Avaliação (rating 0-100).
ALTER TABLE jogo ADD COLUMN avaliacao INTEGER;

-- Faixa de jogadores: substitui o num_jogadores único por min/max.
ALTER TABLE jogo ADD COLUMN num_jogadores_min INTEGER;
ALTER TABLE jogo ADD COLUMN num_jogadores_max INTEGER;
UPDATE jogo SET num_jogadores_min = num_jogadores, num_jogadores_max = num_jogadores;
ALTER TABLE jogo DROP COLUMN num_jogadores;

-- O campo 'campanha' (INTEGER, já existente) passa a significar a DURAÇÃO da
-- campanha em dias; os dias restantes são derivados de data_inicio + campanha.

-- Gêneros (múltiplos) — substitui a coluna 'genero' única.
CREATE TABLE jogo_generos (
    jogo_id INTEGER NOT NULL,
    genero TEXT NOT NULL,
    CONSTRAINT fk_jogo_generos_jogo
        FOREIGN KEY (jogo_id) REFERENCES jogo(id) ON DELETE CASCADE
);
INSERT INTO jogo_generos (jogo_id, genero)
    SELECT id, genero FROM jogo WHERE genero IS NOT NULL AND genero <> '';
ALTER TABLE jogo DROP COLUMN genero;
CREATE INDEX idx_jogo_generos_jogo ON jogo_generos(jogo_id);

-- Categorias (múltiplas) — destaque, destaque-hero, rpg, sobrevivencia, etc.
CREATE TABLE jogo_categorias (
    jogo_id INTEGER NOT NULL,
    categoria TEXT NOT NULL,
    CONSTRAINT fk_jogo_categorias_jogo
        FOREIGN KEY (jogo_id) REFERENCES jogo(id) ON DELETE CASCADE
);
CREATE INDEX idx_jogo_categorias_jogo ON jogo_categorias(jogo_id);
