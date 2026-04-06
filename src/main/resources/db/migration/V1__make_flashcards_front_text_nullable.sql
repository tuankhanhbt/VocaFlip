SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'flashcards'
              AND column_name = 'front_text'
              AND is_nullable = 'NO'
        ),
        'ALTER TABLE flashcards MODIFY COLUMN front_text VARCHAR(500) NULL',
        'SELECT 1'
    )
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
