package db.migration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V4__ensure_flashcards_phonetic_audio_columns extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        if (!tableExists(context, "flashcards")) {
            return;
        }

        addColumnIfMissing(context, "phonetic", "VARCHAR(255) NULL");
        addColumnIfMissing(context, "audio_url", "VARCHAR(500) NULL");
    }

    private void addColumnIfMissing(Context context, String columnName, String columnDefinition) throws Exception {
        if (columnExists(context, "flashcards", columnName)) {
            return;
        }

        try (Statement statement = context.getConnection().createStatement()) {
            statement.execute(
                "ALTER TABLE flashcards ADD COLUMN " + columnName + " " + columnDefinition
            );
        }
    }

    private boolean tableExists(Context context, String tableName) throws Exception {
        try (PreparedStatement statement = context.getConnection().prepareStatement(
            """
            SELECT 1
            FROM information_schema.tables
            WHERE table_schema = DATABASE()
              AND table_name = ?
            """
        )) {
            statement.setString(1, tableName);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean columnExists(Context context, String tableName, String columnName) throws Exception {
        try (PreparedStatement statement = context.getConnection().prepareStatement(
            """
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = ?
              AND column_name = ?
            """
        )) {
            statement.setString(1, tableName);
            statement.setString(2, columnName);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}
