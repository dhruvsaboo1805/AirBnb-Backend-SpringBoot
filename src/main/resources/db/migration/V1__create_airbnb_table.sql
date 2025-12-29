CREATE TABLE airbnb (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,

        name VARCHAR(255),
        description TEXT,
        location VARCHAR(255),
        price_per_night DECIMAL(6 , 2) NOT NULL,

        created_at TIMESTAMP NOT NULL,
        updated_at TIMESTAMP NOT NULL

        CONSTRAINT fk_user
        FOREIGN KEY (user_id) REFERENCES user(id),
);