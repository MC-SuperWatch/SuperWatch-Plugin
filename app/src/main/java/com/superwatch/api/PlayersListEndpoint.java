package com.superwatch.api;

import com.sun.net.httpserver.HttpExchange;
import com.superwatch.data.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.Map;

/**
 * Endpoint API pour obtenir la liste de tous les joueurs
 */
public class PlayersListEndpoint extends ApiEndpoint {

    private final PlayerDataManager playerDataManager;

    /**
     * Constructeur
     * 
     * @param playerDataManager Le gestionnaire de données des joueurs
     */
    public PlayersListEndpoint(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            sendJsonResponse(exchange, "{\"error\":\"Method not allowed\"}", 405);
            return;
        }

        JSONObject responseJson = new JSONObject();
        
        // Information sur les mods et le logiciel
        responseJson.put("mods", new JSONArray());
        responseJson.put("software", Bukkit.getName() + " " + Bukkit.getVersion());
        
        // Information sur les joueurs
        JSONObject playersInfo = new JSONObject();
        int onlineCount = Bukkit.getOnlinePlayers().size();
        int offlineCount = playerDataManager.getOfflinePlayersCount();
        int maxPlayers = Bukkit.getMaxPlayers();
        
        playersInfo.put("online", onlineCount);
        playersInfo.put("offline", offlineCount);
        playersInfo.put("max", maxPlayers);
        
        // Liste des joueurs
        JSONObject playersList = new JSONObject();
        
        // Joueurs en ligne
        for (Player player : Bukkit.getOnlinePlayers()) {
            JSONObject playerData = new JSONObject();
            playerData.put("level", player.getLevel());
            playerData.put("xp", (int) player.getExp());
            playerData.put("health", (int) player.getHealth());
            playerData.put("food", player.getFoodLevel());
            
            JSONObject permissions = new JSONObject();
            permissions.put("isOp", player.isOp());
            permissions.put("gameMode", player.getGameMode().toString());
            playerData.put("permissions", permissions);
            
            playerData.put("status", "online");
            playerData.put("uuid", player.getUniqueId().toString());
            
            playersList.put(player.getName(), playerData);
        }
        
        // Joueurs hors ligne
        for (Map.Entry<String, JSONObject> entry : playerDataManager.getOfflinePlayersBasicData().entrySet()) {
            playersList.put(entry.getKey(), entry.getValue());
        }
        
        playersInfo.put("list", playersList);
        responseJson.put("players", playersInfo);
        
        // Envoyer la réponse
        sendJsonResponse(exchange, responseJson.toJSONString(), 200);
    }
}