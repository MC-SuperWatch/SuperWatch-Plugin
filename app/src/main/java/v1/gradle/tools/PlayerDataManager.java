package v1.gradle.tools;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PlayerDataManager {
    private final Plugin plugin;
    private final Map<String, JSONObject> playerDataMap;
    private final Set<String> allPlayers;
    private final File dataFolder;
    private final JSONParser jsonParser;

    public PlayerDataManager(Plugin plugin) {
        this.plugin = plugin;
        this.playerDataMap = new ConcurrentHashMap<>(); // Thread-safe map
        this.allPlayers = ConcurrentHashMap.newKeySet(); // Thread-safe set
        this.dataFolder = new File(plugin.getDataFolder(), "website/player_data");
        this.jsonParser = new JSONParser();

        // Ensure data directory exists
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // Initial load of player data
        loadAllPlayerData();
    }

    private void loadAllPlayerData() {
        if (dataFolder.exists()) {
            File[] playerFiles = dataFolder.listFiles((dir, name) -> name.endsWith(".json"));
            if (playerFiles != null) {
                for (File file : playerFiles) {
                    try (FileReader reader = new FileReader(file)) {
                        JSONObject playerData = (JSONObject) jsonParser.parse(reader);
                        String playerName = (String) playerData.get("name");
                        if (playerName != null) {
                            playerDataMap.put(playerName, playerData);
                            allPlayers.add(playerName);
                        }
                    } catch (IOException | ParseException e) {
                        plugin.getLogger().warning("Failed to load player data from " + file.getName() + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    public void savePlayerData(String playerName, JSONObject playerData) {
        if (playerName == null || playerData == null) return;

        // Update in-memory maps
        playerDataMap.put(playerName, playerData);
        allPlayers.add(playerName);

        // Save to file
        String uuid = (String) playerData.get("crack_uuid");
        if (uuid != null) {
            File playerFile = new File(dataFolder, uuid + ".json");
            try (FileWriter writer = new FileWriter(playerFile)) {
                writer.write(playerData.toJSONString());
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save player data for " + playerName + ": " + e.getMessage());
            }
        }
    }

    public JSONObject getPlayerData(String playerName) {
        return playerDataMap.get(playerName);
    }

    public JSONObject getPlayerDataByUUID(UUID uuid) {
        File playerFile = new File(dataFolder, uuid.toString() + ".json");
        if (playerFile.exists()) {
            try (FileReader reader = new FileReader(playerFile)) {
                return (JSONObject) jsonParser.parse(reader);
            } catch (IOException | ParseException e) {
                plugin.getLogger().warning("Failed to load player data for UUID " + uuid + ": " + e.getMessage());
            }
        }
        return null;
    }

    public void updatePlayerStatus(String playerName, String status) {
        JSONObject playerData = playerDataMap.get(playerName);
        if (playerData != null) {
            playerData.put("status", status);
            playerData.put("lastSeen", System.currentTimeMillis());
            savePlayerData(playerName, playerData);
        }
    }

    public void cleanupOldData(long maxAge) {
        long currentTime = System.currentTimeMillis();
        File[] playerFiles = dataFolder.listFiles((dir, name) -> name.endsWith(".json"));
        
        if (playerFiles != null) {
            for (File file : playerFiles) {
                try (FileReader reader = new FileReader(file)) {
                    JSONObject playerData = (JSONObject) jsonParser.parse(reader);
                    Long lastSeen = (Long) playerData.get("lastSeen");
                    
                    if (lastSeen != null && (currentTime - lastSeen) > maxAge) {
                        String playerName = (String) playerData.get("name");
                        if (playerName != null) {
                            playerDataMap.remove(playerName);
                            allPlayers.remove(playerName);
                        }
                        file.delete();
                    }
                } catch (IOException | ParseException e) {
                    plugin.getLogger().warning("Error during cleanup of " + file.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    public Map<String, JSONObject> getPlayerDataMap() {
        return new HashMap<>(playerDataMap); // Return a copy for thread safety
    }

    public Set<String> getAllPlayers() {
        return new HashSet<>(allPlayers); // Return a copy for thread safety
    }

    public int getOnlineCount() {
        return (int) playerDataMap.values().stream()
            .filter(data -> "online".equals(data.get("status")))
            .count();
    }

    public void clearData() {
        playerDataMap.clear();
        allPlayers.clear();
        
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }
}