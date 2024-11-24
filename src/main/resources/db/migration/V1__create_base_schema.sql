-- Steam Users table first (because it's referenced by users)
CREATE TABLE IF NOT EXISTS steam_users
(
    steam_id     VARCHAR(255) PRIMARY KEY,
    persona_name VARCHAR(255),
    profile_url  VARCHAR(255),
    avatar_url   VARCHAR(255)
);

-- Then Users table (with the foreign key)
CREATE TABLE IF NOT EXISTS users
(
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(255) NOT NULL UNIQUE,
    email      VARCHAR(255),
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(50)  NOT NULL DEFAULT 'USER',
    steam_id   VARCHAR(255) REFERENCES steam_users (steam_id),
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Games and related tables
CREATE TABLE games
(
    id                BIGSERIAL PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    slug             VARCHAR(255) NOT NULL UNIQUE,
    api_id           VARCHAR(100),
    background_image VARCHAR(512),
    release_date     DATE,
    source           VARCHAR(20)  NOT NULL,
    rating           DOUBLE PRECISION,
    rawg_last_updated TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (api_id, source)
);

CREATE TABLE game_genres
(
    game_id BIGINT REFERENCES games (id) ON DELETE CASCADE,
    genre   VARCHAR(50),
    PRIMARY KEY (game_id, genre)
);

CREATE TABLE game_instances
(
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT REFERENCES users (id) ON DELETE CASCADE,
    game_id             BIGINT REFERENCES games (id) ON DELETE CASCADE,
    status              VARCHAR(20) NOT NULL,
    progress_percentage INTEGER CHECK (progress_percentage BETWEEN 0 AND 100),
    play_time           INTEGER,
    last_played         TIMESTAMP,
    notes               TEXT,
    added_at           TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, game_id)
);

-- Reviews table
CREATE TABLE reviews
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT REFERENCES users (id) ON DELETE CASCADE,
    game_id     BIGINT REFERENCES games (id) ON DELETE CASCADE,
    rating      INTEGER CHECK (rating BETWEEN 1 AND 5),
    review_text TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, game_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_games_title ON games(title);
CREATE INDEX idx_games_rating ON games(rating DESC);
CREATE INDEX idx_game_instances_user_status ON game_instances(user_id, status);
CREATE INDEX idx_reviews_game ON reviews(game_id);