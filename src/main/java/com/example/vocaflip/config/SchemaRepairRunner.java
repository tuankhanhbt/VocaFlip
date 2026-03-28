package com.example.vocaflip.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchemaRepairRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        String isNullable = jdbcTemplate.query(
            """
            SELECT is_nullable
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'flashcards'
              AND column_name = 'front_text'
            """,
            rs -> rs.next() ? rs.getString("is_nullable") : null
        );

        if ("NO".equalsIgnoreCase(isNullable)) {
            jdbcTemplate.execute("ALTER TABLE flashcards MODIFY COLUMN front_text VARCHAR(500) NULL");
        }
    }
}
