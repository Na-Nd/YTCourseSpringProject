CREATE SEQUENCE IF NOT EXISTS message_seq START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS usr_seq START 1 INCREMENT 1;

CREATE TABLE IF NOT EXISTS message (
                                       id BIGINT NOT NULL,
                                       filename VARCHAR(255),
    tag VARCHAR(255),
    text VARCHAR(2048) NOT NULL,
    user_id BIGINT,
    PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS user_role (
                                         user_id BIGINT NOT NULL,
                                         roles VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS usr (
                                   id BIGINT NOT NULL,
                                   activation_code VARCHAR(255),
    active BOOLEAN NOT NULL,
    email VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
    );


DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM   pg_constraint
        WHERE  conname = 'message_user_fk'
        AND    connamespace = (SELECT oid FROM pg_namespace WHERE nspname = current_schema())
    ) THEN
ALTER TABLE message
    ADD CONSTRAINT message_user_fk FOREIGN KEY (user_id) REFERENCES usr;
END IF;
END
$$;

DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM   pg_constraint
        WHERE  conname = 'user_role_fk'
        AND    connamespace = (SELECT oid FROM pg_namespace WHERE nspname = current_schema())
    ) THEN
ALTER TABLE user_role
    ADD CONSTRAINT user_role_fk FOREIGN KEY (user_id) REFERENCES usr;
END IF;
END
$$;
