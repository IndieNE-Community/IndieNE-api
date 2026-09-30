-- ============================================================================
--  IndieNE — V9: Seed de dados para APRESENTAÇÃO (Flyway migration)
--  (Flyway roda esta migration dentro da propria transacao; NAO usar BEGIN/COMMIT aqui.)
-- ----------------------------------------------------------------------------
--  Usa JOGOS INDIES REAIS com as IMAGENS OFICIAIS da CDN da Steam (bonitas e
--  variadas de verdade). Cobre todas as variações de cada tabela.
--
--  AUTOSSUFICIENTE: cria TODOS os usuários (inclusive 'Jangada Games'/fulano@x.com,
--  que no banco original já existia). Roda em banco novo/vazio sem depender de nada.
--  Devs: Jangada Games, Estúdio Lua Nova, Pixel Foundry. Comuns: Ana, Bruno, Carla.
--
--  Imagens (todas testadas 200):
--    header.jpg          -> capa (thumb)
--    capsule_616x353.jpg -> arte de galeria
--    library_hero.jpg    -> banner largo
--    page_bg_*.jpg       -> fundo/arte extra
--
--  Senha de TODOS os usuários: senha123
--    fulano@x.com (Jangada Games) / studio@indiene.com.br / retrilha@indiene.com.br (devs)
--    ana@indiene.com.br / bruno@indiene.com.br / carla@indiene.com.br (comuns)
--
--  Variações cobertas:
--    * usuario: DESENVOLVEDOR e USUARIO_COMUM
--    * jogo: em andamento/encerrado; controle sim/não; single e multiplayer;
--            avaliacao alta/NULL; descricao NULL; img_thumb NULL;
--            financiamento 0% / parcial / ~100% / acima de 100%
--    * generos/categorias/plataformas: de 0 a vários por jogo
--    * imagem: de jogo E de postagem; galeria de 1 a 3; artes diferentes
--    * postagem: jogo com 0, 1 e 2 postagens (toda postagem tem imagem)
--    * comentario: 3 autores; postagem com 0, 1, 2 e 3 comentários
--    * doacao: jogo sem doação; parcial; acima da meta; mesmo doador em vários
--    * curtida: LIKE e DISLIKE; em postagem e em comentário; comentário sem reação
--
--  Katana ZERO = caso "mínimo": sem descrição e sem postagem (mas com capa e galeria).
--  Cuphead     = caso "vazio":   sem avaliação, sem categoria, sem doação, postagem sem comentário.
--
--  REEXECUTÁVEL: apaga (por título/e-mail) os jogos e os 6 usuários do seed antes
--  de recriar tudo. Também limpa títulos de um seed anterior.
-- ============================================================================
-- ---------------------------------------------------------------------------
-- 0) Limpa execução anterior (cascata via FK ON DELETE CASCADE).
--    Inclui os títulos deste seed E os do seed anterior (temático BR).
-- ---------------------------------------------------------------------------
DELETE FROM jogo WHERE titulo IN (
  'Celeste', 'Hollow Knight', 'Stardew Valley', 'Hades',
  'Undertale', 'Cuphead', 'Dead Cells', 'Katana ZERO',
  -- títulos do seed anterior:
  'Raízes do Sertão', 'Pixel Capoeira', 'Farol', 'Cangaço Tactics',
  'Frevo Runner', 'Baião de Bytes', 'Maracatu Mechs', 'Sereia do Recife'
);
DELETE FROM usuarios WHERE email IN (
  'fulano@x.com',
  'studio@indiene.com.br', 'retrilha@indiene.com.br',
  'ana@indiene.com.br', 'bruno@indiene.com.br', 'carla@indiene.com.br'
);

-- ---------------------------------------------------------------------------
-- 1) USUÁRIOS  (hash BCrypt = senha "senha123" para TODOS)
--    'Jangada Games' (fulano@x.com) é o dono de Celeste, Stardew e Katana ZERO.
--    No banco original ele já existia; aqui o seed o cria com o MESMO UUID,
--    pra ficar autossuficiente em um banco novo/vazio.
-- ---------------------------------------------------------------------------
INSERT INTO usuarios (id, nome, email, senha, tipo) VALUES
  ('043ce6d7-a53d-463c-91e3-71eb68963624', 'Jangada Games',    'fulano@x.com',
   '$2a$10$yseUJBdcAwJf.etG3mVaDuO9IMVjXE.zmFOyretiVzYYMJs30GtD6', 'DESENVOLVEDOR'),
  ('11111111-1111-1111-1111-111111111111', 'Estúdio Lua Nova', 'studio@indiene.com.br',
   '$2a$10$yseUJBdcAwJf.etG3mVaDuO9IMVjXE.zmFOyretiVzYYMJs30GtD6', 'DESENVOLVEDOR'),
  ('44444444-4444-4444-4444-444444444444', 'Pixel Foundry',     'retrilha@indiene.com.br',
   '$2a$10$yseUJBdcAwJf.etG3mVaDuO9IMVjXE.zmFOyretiVzYYMJs30GtD6', 'DESENVOLVEDOR'),
  ('22222222-2222-2222-2222-222222222222', 'Ana Beatriz',       'ana@indiene.com.br',
   '$2a$10$yseUJBdcAwJf.etG3mVaDuO9IMVjXE.zmFOyretiVzYYMJs30GtD6', 'USUARIO_COMUM'),
  ('33333333-3333-3333-3333-333333333333', 'Bruno Lima',        'bruno@indiene.com.br',
   '$2a$10$yseUJBdcAwJf.etG3mVaDuO9IMVjXE.zmFOyretiVzYYMJs30GtD6', 'USUARIO_COMUM'),
  ('55555555-5555-5555-5555-555555555555', 'Carla Mendes',      'carla@indiene.com.br',
   '$2a$10$yseUJBdcAwJf.etG3mVaDuO9IMVjXE.zmFOyretiVzYYMJs30GtD6', 'USUARIO_COMUM');

-- ---------------------------------------------------------------------------
-- 2) JOGOS  (owners: 043ce6d7=Fulano, 1111=Lua Nova, 4444=Pixel Foundry)
--    Katana ZERO: descricao NULL + avaliacao NULL (com capa da Steam).
--    Cuphead:     avaliacao NULL.
-- ---------------------------------------------------------------------------
INSERT INTO jogo
  (titulo, descricao, meta_financeira, campanha, data_inicio, data_conclusao,
   avaliacao, num_jogadores_min, num_jogadores_max, controle, img_thumb, usuario_id)
VALUES
  ('Celeste',
   'Plataforma de precisão sobre superar a montanha Celeste — e a si mesma. Controles afiados e uma história tocante sobre ansiedade.',
   150000, 60, DATE '2026-06-20', NULL, 92, 1, 1, TRUE,
   'https://cdn.cloudflare.steamstatic.com/steam/apps/504230/header.jpg',
   '043ce6d7-a53d-463c-91e3-71eb68963624'),

  ('Hollow Knight',
   'Metroidvania de ação em um vasto reino subterrâneo de insetos. Explore, lute e desvende os segredos de Hallownest.',
   80000, 45, DATE '2026-07-01', NULL, 95, 1, 1, TRUE,
   'https://cdn.cloudflare.steamstatic.com/steam/apps/367520/header.jpg',
   '11111111-1111-1111-1111-111111111111'),

  ('Stardew Valley',
   'Herde a antiga fazenda do seu avô e construa a vida no campo dos seus sonhos. Plante, crie, pesque e faça amizades.',
   50000, 40, DATE '2026-05-10', DATE '2026-06-19', 98, 1, 4, TRUE,
   'https://cdn.cloudflare.steamstatic.com/steam/apps/413150/header.jpg',
   '043ce6d7-a53d-463c-91e3-71eb68963624'),

  ('Hades',
   'Roguelike de ação em que você foge do Submundo como o príncipe Zagreus, encarando os deuses do Olimpo a cada tentativa.',
   120000, 70, DATE '2026-07-10', NULL, 93, 1, 1, TRUE,
   'https://cdn.cloudflare.steamstatic.com/steam/apps/1145360/header.jpg',
   '11111111-1111-1111-1111-111111111111'),

  ('Undertale',
   'RPG onde ninguém precisa morrer. Faça amizade ou enfrente monstros em uma jornada cheia de humor e coração.',
   30000, 30, DATE '2026-07-15', NULL, 96, 1, 1, FALSE,
   'https://cdn.cloudflare.steamstatic.com/steam/apps/391540/header.jpg',
   '44444444-4444-4444-4444-444444444444'),

  ('Cuphead',
   'Run and gun clássico com animação desenhada à mão dos anos 1930. Batalhas de chefe intensas, sozinho ou em co-op.',
   45000, 50, DATE '2026-07-20', NULL, NULL, 1, 2, TRUE,
   'https://cdn.cloudflare.steamstatic.com/steam/apps/268910/header.jpg',
   '44444444-4444-4444-4444-444444444444'),

  ('Dead Cells',
   'Roguevania de ação frenética. Explore um castelo em constante mutação, morra, aprenda e recomece mais forte.',
   200000, 90, DATE '2026-06-01', NULL, 90, 1, 1, TRUE,
   'https://cdn.cloudflare.steamstatic.com/steam/apps/588650/header.jpg',
   '11111111-1111-1111-1111-111111111111'),

  ('Katana ZERO',
   NULL,
   40000, 35, DATE '2026-07-22', NULL, NULL, 1, 1, FALSE,
   'https://cdn.cloudflare.steamstatic.com/steam/apps/460950/header.jpg',
   '043ce6d7-a53d-463c-91e3-71eb68963624');

-- ---------------------------------------------------------------------------
-- 3) GÊNEROS  (de 1 a 3 por jogo)
-- ---------------------------------------------------------------------------
INSERT INTO jogo_generos (jogo_id, genero)
SELECT j.id, g.genero
FROM jogo j
JOIN (VALUES
  ('Celeste',        'Plataforma'),
  ('Celeste',        'Aventura'),
  ('Celeste',        'Precisão'),
  ('Hollow Knight',  'Metroidvania'),
  ('Hollow Knight',  'Ação'),
  ('Hollow Knight',  'Aventura'),
  ('Stardew Valley', 'Simulação'),
  ('Stardew Valley', 'RPG'),
  ('Stardew Valley', 'Indie'),
  ('Hades',          'Roguelike'),
  ('Hades',          'Ação'),
  ('Hades',          'RPG'),
  ('Undertale',      'RPG'),
  ('Undertale',      'Narrativo'),
  ('Cuphead',        'Ação'),
  ('Cuphead',        'Plataforma'),
  ('Dead Cells',     'Roguelike'),
  ('Dead Cells',     'Metroidvania'),
  ('Dead Cells',     'Ação'),
  ('Katana ZERO',    'Ação')
) AS g(titulo, genero) ON g.titulo = j.titulo;

-- ---------------------------------------------------------------------------
-- 4) CATEGORIAS  (Cuphead fica SEM categoria de propósito)
-- ---------------------------------------------------------------------------
INSERT INTO jogo_categorias (jogo_id, categoria)
SELECT j.id, c.categoria
FROM jogo j
JOIN (VALUES
  ('Celeste',        'destaque'),
  ('Celeste',        'destaque-hero'),
  ('Celeste',        'plataforma'),
  ('Hollow Knight',  'destaque'),
  ('Hollow Knight',  'acao'),
  ('Stardew Valley', 'destaque'),
  ('Stardew Valley', 'indie'),
  ('Hades',          'destaque'),
  ('Hades',          'rpg'),
  ('Undertale',      'lancamento'),
  ('Dead Cells',     'destaque'),
  ('Katana ZERO',    'indie')
) AS c(titulo, categoria) ON c.titulo = j.titulo;

-- ---------------------------------------------------------------------------
-- 5) PLATAFORMAS  (Cuphead/Katana com 1; Stardew/Dead Cells com 5)
-- ---------------------------------------------------------------------------
INSERT INTO plataformas (plataforma, jogo_id)
SELECT p.plataforma, j.id
FROM jogo j
JOIN (VALUES
  ('Celeste',        'Windows'),
  ('Celeste',        'macOS'),
  ('Celeste',        'Linux'),
  ('Celeste',        'Nintendo Switch'),
  ('Hollow Knight',  'Windows'),
  ('Hollow Knight',  'macOS'),
  ('Hollow Knight',  'Linux'),
  ('Stardew Valley', 'Windows'),
  ('Stardew Valley', 'macOS'),
  ('Stardew Valley', 'Linux'),
  ('Stardew Valley', 'Nintendo Switch'),
  ('Stardew Valley', 'Android'),
  ('Hades',          'Windows'),
  ('Hades',          'macOS'),
  ('Hades',          'Nintendo Switch'),
  ('Undertale',      'Windows'),
  ('Undertale',      'macOS'),
  ('Undertale',      'Linux'),
  ('Cuphead',        'Windows'),
  ('Dead Cells',     'Windows'),
  ('Dead Cells',     'macOS'),
  ('Dead Cells',     'Linux'),
  ('Dead Cells',     'PlayStation 5'),
  ('Dead Cells',     'Xbox Series'),
  ('Katana ZERO',    'Windows')
) AS p(titulo, plataforma) ON p.titulo = j.titulo;

-- ---------------------------------------------------------------------------
-- 6) IMAGENS DA GALERIA DO JOGO  (1 a 3 por jogo; artes reais da Steam)
-- ---------------------------------------------------------------------------
INSERT INTO imagem (imagem, jogo_id)
SELECT i.url, j.id
FROM jogo j
JOIN (VALUES
  ('Celeste',        'https://cdn.cloudflare.steamstatic.com/steam/apps/504230/capsule_616x353.jpg'),
  ('Celeste',        'https://cdn.cloudflare.steamstatic.com/steam/apps/504230/library_hero.jpg'),
  ('Celeste',        'https://cdn.cloudflare.steamstatic.com/steam/apps/504230/page_bg_generated_v6b.jpg'),
  ('Hollow Knight',  'https://cdn.cloudflare.steamstatic.com/steam/apps/367520/capsule_616x353.jpg'),
  ('Hollow Knight',  'https://cdn.cloudflare.steamstatic.com/steam/apps/367520/library_hero.jpg'),
  ('Stardew Valley', 'https://cdn.cloudflare.steamstatic.com/steam/apps/413150/capsule_616x353.jpg'),
  ('Stardew Valley', 'https://cdn.cloudflare.steamstatic.com/steam/apps/413150/library_hero.jpg'),
  ('Hades',          'https://cdn.cloudflare.steamstatic.com/steam/apps/1145360/capsule_616x353.jpg'),
  ('Hades',          'https://cdn.cloudflare.steamstatic.com/steam/apps/1145360/library_hero.jpg'),
  ('Hades',          'https://cdn.cloudflare.steamstatic.com/steam/apps/1145360/page_bg_generated_v6b.jpg'),
  ('Undertale',      'https://cdn.cloudflare.steamstatic.com/steam/apps/391540/library_hero.jpg'),
  ('Cuphead',        'https://cdn.cloudflare.steamstatic.com/steam/apps/268910/capsule_616x353.jpg'),
  ('Dead Cells',     'https://cdn.cloudflare.steamstatic.com/steam/apps/588650/capsule_616x353.jpg'),
  ('Dead Cells',     'https://cdn.cloudflare.steamstatic.com/steam/apps/588650/library_hero.jpg'),
  ('Dead Cells',     'https://cdn.cloudflare.steamstatic.com/steam/apps/588650/page_bg_generated_v6b.jpg'),
  ('Katana ZERO',    'https://cdn.cloudflare.steamstatic.com/steam/apps/460950/capsule_616x353.jpg'),
  ('Katana ZERO',    'https://cdn.cloudflare.steamstatic.com/steam/apps/460950/library_hero.jpg'),
  ('Katana ZERO',    'https://cdn.cloudflare.steamstatic.com/steam/apps/460950/page_bg_generated_v6b.jpg')
) AS i(titulo, url) ON i.titulo = j.titulo;

-- ---------------------------------------------------------------------------
-- 7) PUBLICAÇÕES / DEVLOGS  (autor = dono do jogo)
--    Celeste: 2 postagens.  Katana ZERO: 0 postagens (não aparece aqui).
-- ---------------------------------------------------------------------------
INSERT INTO postagem (titulo, descricao, data, jogo_id, usuario_id)
SELECT p.titulo, p.descricao, p.data, j.id, j.usuario_id
FROM jogo j
JOIN (VALUES
  ('Celeste', 'Devlog #1 — Celeste',
   'Primeiro devlog! Mostramos o dash, a montanha e o capítulo 1.',
   TIMESTAMP '2026-07-05 10:00:00'),
  ('Celeste', 'Devlog #2 — Celeste',
   'Atualização: novo capítulo, assist mode e balanceamento dos controles.',
   TIMESTAMP '2026-07-12 10:00:00'),
  ('Hollow Knight', 'Devlog #1 — Hollow Knight',
   'Apresentando Hallownest: mapa interconectado, chefes e o sistema de charms.',
   TIMESTAMP '2026-07-08 14:30:00'),
  ('Stardew Valley', 'Lançamento — Stardew Valley 1.0',
   'Campanha encerrada e jogo lançado! Multiplayer co-op e novas fazendas. Obrigado a todos.',
   TIMESTAMP '2026-06-20 09:00:00'),
  ('Hades', 'Devlog #1 — Hades',
   'Sistema de fuga do Submundo, os deuses do Olimpo e as bênçãos. Cada run é única!',
   TIMESTAMP '2026-07-14 16:00:00'),
  ('Undertale', 'Devlog #1 — Undertale',
   'Combate por turnos com bullet hell e as rotas pacifista/genocida. Suas escolhas importam.',
   TIMESTAMP '2026-07-18 11:15:00'),
  ('Cuphead', 'Devlog #1 — Cuphead',
   'Abrimos a campanha! Animação anos 30 quadro a quadro. Ainda sem apoiadores — seja o primeiro.',
   TIMESTAMP '2026-07-21 09:00:00'),
  ('Dead Cells', 'Devlog #1 — Dead Cells',
   'Castelo procedural, combate rápido e progressão permanente. Morra, aprenda, evolua.',
   TIMESTAMP '2026-06-10 13:00:00')
) AS p(jtitulo, titulo, descricao, data) ON p.jtitulo = j.titulo;

-- ---------------------------------------------------------------------------
-- 8) IMAGENS DE POSTAGEM  (1 ou 2 por postagem; toda postagem tem imagem)
-- ---------------------------------------------------------------------------
INSERT INTO imagem (imagem, postagem_id)
SELECT v.url, po.id
FROM postagem po
JOIN (VALUES
  ('Devlog #1 — Celeste',            'https://cdn.cloudflare.steamstatic.com/steam/apps/504230/library_hero.jpg'),
  ('Devlog #1 — Celeste',            'https://cdn.cloudflare.steamstatic.com/steam/apps/504230/page_bg_generated_v6b.jpg'),
  ('Devlog #2 — Celeste',            'https://cdn.cloudflare.steamstatic.com/steam/apps/504230/capsule_616x353.jpg'),
  ('Devlog #1 — Hollow Knight',      'https://cdn.cloudflare.steamstatic.com/steam/apps/367520/page_bg_generated_v6b.jpg'),
  ('Lançamento — Stardew Valley 1.0','https://cdn.cloudflare.steamstatic.com/steam/apps/413150/page_bg_generated_v6b.jpg'),
  ('Devlog #1 — Hades',              'https://cdn.cloudflare.steamstatic.com/steam/apps/1145360/library_hero.jpg'),
  ('Devlog #1 — Undertale',          'https://cdn.cloudflare.steamstatic.com/steam/apps/391540/capsule_616x353.jpg'),
  ('Devlog #1 — Cuphead',            'https://cdn.cloudflare.steamstatic.com/steam/apps/268910/library_hero.jpg'),
  ('Devlog #1 — Dead Cells',         'https://cdn.cloudflare.steamstatic.com/steam/apps/588650/capsule_616x353.jpg'),
  ('Devlog #1 — Dead Cells',         'https://cdn.cloudflare.steamstatic.com/steam/apps/588650/library_hero.jpg')
) AS v(post_titulo, url) ON v.post_titulo = po.titulo;

-- ---------------------------------------------------------------------------
-- 9) COMENTÁRIOS  (3 autores: Ana/Bruno/Carla)
--    Postagem com 3 / 2 / 1 / 0 comentários (Cuphead = 0).
-- ---------------------------------------------------------------------------
INSERT INTO comentario (texto, data, user_id, postagem_id)
SELECT c.texto, c.data, c.user_id, po.id
FROM postagem po
JOIN (VALUES
  ('Devlog #1 — Celeste', '22222222-2222-2222-2222-222222222222'::uuid,
   'Os controles são perfeitos, cada morte é culpa minha mesmo haha.', TIMESTAMP '2026-07-05 12:20:00'),
  ('Devlog #1 — Celeste', '33333333-3333-3333-3333-333333333333'::uuid,
   'A história sobre ansiedade me pegou de jeito. Lindo demais.', TIMESTAMP '2026-07-06 08:45:00'),
  ('Devlog #1 — Celeste', '55555555-5555-5555-5555-555555555555'::uuid,
   'Trilha sonora do Lena Raine é de outro planeta!', TIMESTAMP '2026-07-06 19:10:00'),

  ('Devlog #2 — Celeste', '33333333-3333-3333-3333-333333333333'::uuid,
   'O assist mode foi uma decisão de acessibilidade incrível. Parabéns!', TIMESTAMP '2026-07-12 15:00:00'),

  ('Devlog #1 — Hollow Knight', '22222222-2222-2222-2222-222222222222'::uuid,
   'Hallownest é gigante e lindo. Me perdi por horas explorando.', TIMESTAMP '2026-07-08 18:10:00'),
  ('Devlog #1 — Hollow Knight', '55555555-5555-5555-5555-555555555555'::uuid,
   'Os chefes são desafiadores na medida certa. Viciante!', TIMESTAMP '2026-07-09 09:30:00'),

  ('Lançamento — Stardew Valley 1.0', '22222222-2222-2222-2222-222222222222'::uuid,
   'Joguei 200 horas e não enjoo. O co-op com amigos é o melhor.', TIMESTAMP '2026-06-20 21:30:00'),
  ('Lançamento — Stardew Valley 1.0', '33333333-3333-3333-3333-333333333333'::uuid,
   'Melhor jogo de fazenda que existe. Valeu cada centavo apoiado!', TIMESTAMP '2026-06-21 10:00:00'),

  ('Devlog #1 — Hades', '33333333-3333-3333-3333-333333333333'::uuid,
   'A narrativa que avança a cada morte é genial. Nunca cansa de repetir.', TIMESTAMP '2026-07-14 19:40:00'),
  ('Devlog #1 — Hades', '55555555-5555-5555-5555-555555555555'::uuid,
   'Dublagem e arte impecáveis. Já quero apoiar!', TIMESTAMP '2026-07-15 09:12:00'),

  ('Devlog #1 — Undertale', '55555555-5555-5555-5555-555555555555'::uuid,
   'Fui de rota pacifista e chorei. Que jogo especial.', TIMESTAMP '2026-07-18 13:00:00'),

  ('Devlog #1 — Dead Cells', '22222222-2222-2222-2222-222222222222'::uuid,
   'Combate rápido e satisfatório. A progressão permanente prende demais.', TIMESTAMP '2026-06-10 18:00:00'),
  ('Devlog #1 — Dead Cells', '33333333-3333-3333-3333-333333333333'::uuid,
   'Cada run diferente por causa do castelo procedural. Genial.', TIMESTAMP '2026-06-11 11:20:00'),
  ('Devlog #1 — Dead Cells', '55555555-5555-5555-5555-555555555555'::uuid,
   'A pixel art animada é das mais bonitas que já vi.', TIMESTAMP '2026-06-12 20:05:00')
) AS c(post_titulo, user_id, texto, data) ON c.post_titulo = po.titulo;

-- ---------------------------------------------------------------------------
-- 10) DOAÇÕES  (Cuphead = 0; Stardew = 100%; Celeste/Dead Cells > 100%; resto parcial)
-- ---------------------------------------------------------------------------
INSERT INTO doacao (valor, data, user_id, jogo_id)
SELECT d.valor, d.data, d.user_id, j.id
FROM jogo j
JOIN (VALUES
  ('Celeste',        50000.0, '22222222-2222-2222-2222-222222222222'::uuid, TIMESTAMP '2026-06-25 10:00:00'),
  ('Celeste',        60000.0, '33333333-3333-3333-3333-333333333333'::uuid, TIMESTAMP '2026-06-28 14:00:00'),
  ('Celeste',        55000.0, '55555555-5555-5555-5555-555555555555'::uuid, TIMESTAMP '2026-07-02 16:30:00'),
  ('Hollow Knight',  20000.0, '22222222-2222-2222-2222-222222222222'::uuid, TIMESTAMP '2026-07-03 09:30:00'),
  ('Hollow Knight',  12000.0, '33333333-3333-3333-3333-333333333333'::uuid, TIMESTAMP '2026-07-05 19:00:00'),
  ('Stardew Valley', 25000.0, '22222222-2222-2222-2222-222222222222'::uuid, TIMESTAMP '2026-05-20 08:00:00'),
  ('Stardew Valley', 25000.0, '33333333-3333-3333-3333-333333333333'::uuid, TIMESTAMP '2026-06-01 12:00:00'),
  ('Hades',          40000.0, '22222222-2222-2222-2222-222222222222'::uuid, TIMESTAMP '2026-07-12 16:00:00'),
  ('Hades',          26000.0, '55555555-5555-5555-5555-555555555555'::uuid, TIMESTAMP '2026-07-13 11:00:00'),
  ('Undertale',      21000.0, '55555555-5555-5555-5555-555555555555'::uuid, TIMESTAMP '2026-07-17 18:00:00'),
  ('Dead Cells',     90000.0, '22222222-2222-2222-2222-222222222222'::uuid, TIMESTAMP '2026-06-15 10:00:00'),
  ('Dead Cells',     80000.0, '33333333-3333-3333-3333-333333333333'::uuid, TIMESTAMP '2026-06-20 14:00:00'),
  ('Dead Cells',     70000.0, '55555555-5555-5555-5555-555555555555'::uuid, TIMESTAMP '2026-06-25 09:00:00'),
  ('Katana ZERO',      500.0, '22222222-2222-2222-2222-222222222222'::uuid, TIMESTAMP '2026-07-23 10:00:00')
) AS d(jtitulo, valor, user_id, data) ON d.jtitulo = j.titulo;

-- ---------------------------------------------------------------------------
-- 11) CURTIDAS EM PUBLICAÇÕES  (LIKE e DISLIKE; Cuphead = sem reação)
-- ---------------------------------------------------------------------------
INSERT INTO curtida (tipo, user_id, postagem_id)
SELECT r.tipo, r.user_id, po.id
FROM postagem po
JOIN (VALUES
  ('Devlog #1 — Celeste',             'LIKE',    '22222222-2222-2222-2222-222222222222'::uuid),
  ('Devlog #1 — Celeste',             'LIKE',    '33333333-3333-3333-3333-333333333333'::uuid),
  ('Devlog #1 — Celeste',             'LIKE',    '55555555-5555-5555-5555-555555555555'::uuid),
  ('Devlog #2 — Celeste',             'LIKE',    '22222222-2222-2222-2222-222222222222'::uuid),
  ('Devlog #2 — Celeste',             'DISLIKE', '33333333-3333-3333-3333-333333333333'::uuid),
  ('Devlog #1 — Hollow Knight',       'LIKE',    '22222222-2222-2222-2222-222222222222'::uuid),
  ('Lançamento — Stardew Valley 1.0', 'LIKE',    '22222222-2222-2222-2222-222222222222'::uuid),
  ('Lançamento — Stardew Valley 1.0', 'LIKE',    '33333333-3333-3333-3333-333333333333'::uuid),
  ('Lançamento — Stardew Valley 1.0', 'LIKE',    '55555555-5555-5555-5555-555555555555'::uuid),
  ('Devlog #1 — Hades',               'DISLIKE', '55555555-5555-5555-5555-555555555555'::uuid),
  ('Devlog #1 — Undertale',           'LIKE',    '55555555-5555-5555-5555-555555555555'::uuid),
  ('Devlog #1 — Dead Cells',          'LIKE',    '22222222-2222-2222-2222-222222222222'::uuid),
  ('Devlog #1 — Dead Cells',          'LIKE',    '33333333-3333-3333-3333-333333333333'::uuid)
) AS r(post_titulo, tipo, user_id) ON r.post_titulo = po.titulo;

-- ---------------------------------------------------------------------------
-- 12) CURTIDAS EM COMENTÁRIOS  (LIKE e DISLIKE; alguns comentários sem reação)
--     Alvo = comentário identificado por (postagem, autor).
-- ---------------------------------------------------------------------------
INSERT INTO curtida (tipo, user_id, comentario_id)
SELECT v.tipo, v.reactor, cm.id
FROM comentario cm
JOIN postagem po ON po.id = cm.postagem_id
JOIN (VALUES
  -- comentário da Ana em Celeste #1: 2 likes
  ('Devlog #1 — Celeste', '22222222-2222-2222-2222-222222222222'::uuid, 'LIKE',    '33333333-3333-3333-3333-333333333333'::uuid),
  ('Devlog #1 — Celeste', '22222222-2222-2222-2222-222222222222'::uuid, 'LIKE',    '55555555-5555-5555-5555-555555555555'::uuid),
  -- comentário do Bruno em Celeste #1: 1 like + 1 dislike (misto)
  ('Devlog #1 — Celeste', '33333333-3333-3333-3333-333333333333'::uuid, 'LIKE',    '22222222-2222-2222-2222-222222222222'::uuid),
  ('Devlog #1 — Celeste', '33333333-3333-3333-3333-333333333333'::uuid, 'DISLIKE', '55555555-5555-5555-5555-555555555555'::uuid),
  -- comentário da Carla em Celeste #1: 0 reações (proposital)
  -- Dead Cells:
  ('Devlog #1 — Dead Cells', '22222222-2222-2222-2222-222222222222'::uuid, 'LIKE',    '33333333-3333-3333-3333-333333333333'::uuid),
  ('Devlog #1 — Dead Cells', '33333333-3333-3333-3333-333333333333'::uuid, 'DISLIKE', '22222222-2222-2222-2222-222222222222'::uuid),
  ('Devlog #1 — Dead Cells', '55555555-5555-5555-5555-555555555555'::uuid, 'LIKE',    '22222222-2222-2222-2222-222222222222'::uuid),
  ('Devlog #1 — Dead Cells', '55555555-5555-5555-5555-555555555555'::uuid, 'LIKE',    '33333333-3333-3333-3333-333333333333'::uuid)
) AS v(post_titulo, author, tipo, reactor)
  ON po.titulo = v.post_titulo AND cm.user_id = v.author;
