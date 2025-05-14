package com.superwatch.data;

import com.superwatch.App;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Gère les données des joueurs
 */
public class PlayerDataManager {

    private final App plugin;
    private final File dataFolder;
    private final JSONParser jsonParser;

    /**
     * Constructeur
     * 
     * @param plugin L'instance principale du plugin
     */
    public PlayerDataManager(App plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        this.jsonParser = new JSONParser();
        
        // Créer le dossier de données si nécessaire
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    /**
     * Sauvegarde les données d'un joueur
     * 
     * @param player Le joueur
     */
    @SuppressWarnings("unchecked")
    public void savePlayerData(Player player) {
        JSONObject playerData = new JSONObject();
        
        // Données de base
        playerData.put("name", player.getName());
        playerData.put("uuid", player.getUniqueId().toString());
        playerData.put("level", player.getLevel());
        playerData.put("xp", player.getExp());
        playerData.put("health", player.getHealth());
        playerData.put("food", player.getFoodLevel());
        playerData.put("gamemode", player.getGameMode().toString());
        playerData.put("isOp", player.isOp());
        playerData.put("status", "offline");
        playerData.put("lastSeen", System.currentTimeMillis());
        
        // Sauvegarder dans un fichier JSON
        File playerFile = new File(dataFolder, player.getUniqueId().toString() + ".json");
        
        try (FileWriter writer = new FileWriter(playerFile)) {
            writer.write(playerData.toJSONString());
            plugin.getLogger().info("Données du joueur " + player.getName() + " sauvegardées");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la sauvegarde des données du joueur " + player.getName(), e);
        }
    }

    /**
     * Obtient les données d'un joueur à partir de son UUID
     * 
     * @param uuid L'UUID du joueur
     * @return Les données du joueur, ou null si le joueur n'existe pas
     */
    public JSONObject getPlayerData(UUID uuid) {
        File playerFile = new File(dataFolder, uuid.toString() + ".json");
        
        if (!playerFile.exists()) {
            return null;
        }
        
        try (FileReader reader = new FileReader(playerFile)) {
            return (JSONObject) jsonParser.parse(reader);
        } catch (IOException | ParseException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la lecture des données du joueur " + uuid, e);
            return null;
        }
    }

    /**
     * Obtient les données d'un joueur à partir de son nom
     * 
     * @param playerName Le nom du joueur
     * @return Les données du joueur, ou null si le joueur n'existe pas
     */
    public JSONObject getPlayerData(String playerName) {
        // Parcourir tous les fichiers pour trouver celui correspondant au nom
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".json"));
        
        if (files == null) {
            return null;
        }
        
        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                JSONObject playerData = (JSONObject) jsonParser.parse(reader);
                
                if (playerName.equalsIgnoreCase((String) playerData.get("name"))) {
                    return playerData;
                }
            } catch (IOException | ParseException e) {
                plugin.getLogger().warning("Erreur lors de la lecture du fichier " + file.getName() + ": " + e.getMessage());
            }
        }
        
        return null;
    }

    /**
     * Obtient les données de base de tous les joueurs hors ligne
     * 
     * @return Une carte des noms de joueurs vers leurs données de base
     */
    @SuppressWarnings("unchecked")
    public Map<String, JSONObject> getOfflinePlayersBasicData() {
        Map<String, JSONObject> players = new HashMap<>();
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".json"));
        
        if (files == null) {
            return players;
        }
        
        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                JSONObject playerData = (JSONObject) jsonParser.parse(reader);
                String playerName = (String) playerData.get("name");
                
                // Créer un objet de données simplifié
                JSONObject basicData = new JSONObject();
                basicData.put("uuid", playerData.get("uuid"));
                basicData.put("level", playerData.get("level"));
                basicData.put("status", "offline");
                basicData.put("lastSeen", playerData.get("lastSeen"));
                
                players.put(playerName, basicData);
            } catch (IOException | ParseException e) {
                plugin.getLogger().warning("Erreur lors de la lecture du fichier " + file.getName() + ": " + e.getMessage());
            }
        }
        
        return players;
    }

    /**
     * Obtient le nombre de joueurs hors ligne
     * 
     * @return Le nombre de joueurs hors ligne
     */
    public int getOfflinePlayersCount() {
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".json"));
        return files != null ? files.length : 0;
    }
}