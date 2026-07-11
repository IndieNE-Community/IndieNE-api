-- Imagem mira exatamente um alvo: um jogo OU uma publicação (nunca ambos, nunca nenhum).
ALTER TABLE imagem ADD CONSTRAINT chk_imagem_alvo_unico
    CHECK ((jogo_id IS NOT NULL)::int + (postagem_id IS NOT NULL)::int = 1);
