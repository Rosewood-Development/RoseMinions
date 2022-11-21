package dev.rosewood.roseminions.database.migrations;

import dev.rosewood.rosegarden.database.DataMigration;
import dev.rosewood.rosegarden.database.DatabaseConnector;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _1_Create_Tables extends DataMigration {

    public _1_Create_Tables() {
        super(1);
    }

    @Override
    public void migrate(DatabaseConnector connector, Connection connection, String tablePrefix) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE " + tablePrefix + "minions (" +
                    "owner_uuid VARCHAR(36) NOT NULL, " +
                    "block_x TINYINT NOT NULL," +
                    "block_y SMALLINT NOT NULL," +
                    "block_z TINYINT NOT NULL," +
                    "chunk_x INT NOT NULL, " +
                    "chunk_z INT NOT NULL, " +
                    "world VARCHAR(255) NOT NULL, " +
                    "requires_chunk_load TINYINT NOT NULL, " +
                    "PRIMARY KEY (block_x, block_y, block_z, chunk_x, chunk_z, world)" +
                    ")");
        }
    }

}
