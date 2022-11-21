package dev.rosewood.roseminions.manager;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.database.DataMigration;
import dev.rosewood.rosegarden.manager.AbstractDataManager;
import dev.rosewood.roseminions.database.migrations._1_Create_Tables;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.model.ChunkLocation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;

public class DataManager extends AbstractDataManager {

    public DataManager(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    public void saveMinion(Minion minion) {
        Location location = minion.getLocation();
        if (location.getWorld() == null)
            return;

        this.databaseConnector.connect(connection -> {
            String query = "INSERT INTO " + this.getTablePrefix() + "minions (owner_uuid, block_x, block_y, block_z, chunk_x, chunk_z, world, requires_chunk_load) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, minion.getOwner().toString());
                statement.setInt(2, location.getBlockX() % 16);
                statement.setInt(3, location.getBlockY());
                statement.setInt(4, location.getBlockZ() % 16);
                statement.setInt(5, location.getBlockX() >> 16);
                statement.setInt(6, location.getBlockZ() >> 16);
                statement.setString(7, location.getWorld().getName());
                statement.setBoolean(8, minion.isChunkLoaded());
                statement.executeUpdate();
            }
        });
    }

    public void updateMinion(Minion minion) {
        Location location = minion.getLocation();
        if (location.getWorld() == null)
            return;

        this.databaseConnector.connect(connection -> {
            String query = "UPDATE " + this.getTablePrefix() + "minions SET requires_chunk_load = ? WHERE block_x = ? AND block_y = ? AND block_z = ? AND chunk_x = ? AND chunk_z = ? AND world = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setBoolean(1, minion.isChunkLoaded());
                statement.setInt(2, location.getBlockX() % 16);
                statement.setInt(3, location.getBlockY());
                statement.setInt(4, location.getBlockZ() % 16);
                statement.setInt(5, location.getBlockX() >> 16);
                statement.setInt(6, location.getBlockZ() >> 16);
                statement.setString(7, location.getWorld().getName());
                statement.executeUpdate();
            }
        });
    }

    public void deleteMinion(Minion minion) {
        Location location = minion.getLocation();
        if (location.getWorld() == null)
            return;

        this.databaseConnector.connect(connection -> {
            String query = "DELETE FROM " + this.getTablePrefix() + "minions WHERE block_x = ? AND block_y = ? AND block_z = ? AND chunk_x = ? AND chunk_z = ? AND world = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, location.getBlockX() % 16);
                statement.setInt(2, location.getBlockY());
                statement.setInt(3, location.getBlockZ() % 16);
                statement.setInt(4, location.getBlockX() >> 16);
                statement.setInt(5, location.getBlockZ() >> 16);
                statement.setString(6, location.getWorld().getName());
                statement.executeUpdate();
            }
        });
    }

    /**
     * @return a multimap of world chunk locations that are being force loaded by minions
     */
    public Multimap<String, ChunkLocation> getChunkLoadedMinions() {
        Multimap<String, ChunkLocation> minionLocations = MultimapBuilder.hashKeys().hashSetValues().build();
        this.databaseConnector.connect(connection -> {
            try (Statement statement = connection.createStatement()) {
                ResultSet results = statement.executeQuery("SELECT chunk_x, chunk_z, world FROM " + this.getTablePrefix() + "minions WHERE requires_chunk_load = 1 GROUP BY chunk_x, chunk_z");
                while (results.next()) {
                    String world = results.getString("world");
                    minionLocations.put(world, new ChunkLocation(
                            results.getInt("chunk_x"),
                            results.getInt("chunk_z"),
                            world
                    ));
                }
            }
        });
        return minionLocations;
    }

    @Override
    public List<Class<? extends DataMigration>> getDataMigrations() {
        return Collections.singletonList(
                _1_Create_Tables.class
        );
    }

}
