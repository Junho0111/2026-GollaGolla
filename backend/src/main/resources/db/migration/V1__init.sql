CREATE TABLE region (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    parent_id BIGINT,
    name      VARCHAR(50)  NOT NULL,
    depth     INT          NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE member (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255),
    nickname    VARCHAR(50)  NOT NULL,
    provider    VARCHAR(20)  NOT NULL,
    provider_id VARCHAR(255),
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE refresh_token (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    member_id  BIGINT        NOT NULL,
    token      VARCHAR(255)  NOT NULL,
    expires_at DATETIME(6)   NOT NULL,
    is_revoked TINYINT(1)    NOT NULL DEFAULT 0,
    created_at DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_token (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE poi (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    version          BIGINT,
    region_id        BIGINT         NOT NULL,
    category         VARCHAR(20)    NOT NULL,
    name             VARCHAR(150)   NOT NULL,
    description      TEXT,
    lat              DECIMAL(10, 7) NOT NULL,
    lng              DECIMAL(10, 7) NOT NULL,
    rating           DECIMAL(2, 1)  NOT NULL DEFAULT 0.0,
    review_count     INT            NOT NULL DEFAULT 0,
    wish_count       INT            NOT NULL DEFAULT 0,
    view_count       INT            NOT NULL DEFAULT 0,
    popularity_score DECIMAL(10, 4) NOT NULL DEFAULT 0.0000,
    thumbnail_url    VARCHAR(500),
    image_urls       JSON,
    open_hours       JSON,
    closed_days      VARCHAR(30),
    naver_map_url    VARCHAR(500),
    address          VARCHAR(255),
    event_start_date DATE,
    event_end_date   DATE,
    break_time       JSON,
    source           VARCHAR(20)    NOT NULL,
    created_at       DATETIME(6)    NOT NULL,
    updated_at       DATETIME(6)    NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE wishlist (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    member_id  BIGINT      NOT NULL,
    poi_id     BIGINT      NOT NULL,
    is_public  TINYINT(1)  NOT NULL DEFAULT 1,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_wishlist_member_poi (member_id, poi_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE review (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    poi_id     BIGINT      NOT NULL,
    member_id  BIGINT      NOT NULL,
    rating     INT         NOT NULL,
    content    TEXT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_review_poi_member (poi_id, member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE itinerary (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    member_id      BIGINT      NOT NULL,
    title          VARCHAR(100) NOT NULL,
    region_id      BIGINT      NOT NULL,
    start_date     DATE        NOT NULL,
    end_date       DATE        NOT NULL,
    transport_mode VARCHAR(10) NOT NULL DEFAULT 'CAR',
    gen_type       VARCHAR(10) NOT NULL,
    share_token    VARCHAR(64),
    created_at     DATETIME(6) NOT NULL,
    updated_at     DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_itinerary_share_token (share_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE itinerary_item (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    itinerary_id BIGINT      NOT NULL,
    poi_id       BIGINT      NOT NULL,
    day_no       INT         NOT NULL,
    seq          INT         NOT NULL,
    start_time   TIME,
    end_time     TIME,
    is_anchor    TINYINT(1)  NOT NULL DEFAULT 0,
    memo         VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_itinerary_item_itinerary
        FOREIGN KEY (itinerary_id) REFERENCES itinerary (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
