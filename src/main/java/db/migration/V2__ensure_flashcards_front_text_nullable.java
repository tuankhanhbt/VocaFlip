package db.migration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V2__ensure_flashcards_front_text_nullable extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        if (!tableExists(context, "flashcards")) {
            return;
        }

        String isNullable = null;

        try (PreparedStatement statement = context.getConnection().prepareStatement(
            """
            SELECT is_nullable
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'flashcards'
              AND column_name = 'front_text'
            """
        )) {
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    isNullable = resultSet.getString("is_nullable");
                }
            }
        }

        if ("NO".equalsIgnoreCase(isNullable)) {
            try (Statement statement = context.getConnection().createStatement()) {
                statement.execute("ALTER TABLE flashcards MODIFY COLUMN front_text VARCHAR(500) NULL");
            }
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
}
