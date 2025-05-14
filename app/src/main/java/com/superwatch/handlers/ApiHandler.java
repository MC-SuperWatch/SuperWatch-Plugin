package com.superwatch.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.superwatch.App;
import com.superwatch.data.PlayerDataManager;

/**
 * Gestionnaire des requêtes de l'API REST
 */
public class ApiHandler implements HttpHandler {
    
    private final App plugin;
    private final PlayerDataManager playerDataManager;
    
    /**
     * Constructeur du gestionnaire d'API
     * 
     * @param plugin L'instance principale du plugin
     * @param playerDataManager Le gestionnaire de données des joueurs
     */
    public ApiHandler(App plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        
        // Vérifier le chemin de la requête
        if (path.equals("/api/players")) {
            handlePlayersRequest(exchange);
        } else if (path.startsWith("/api/player/")) {
            String playerIdentifier = path.substring("/api/player/".length());
            handlePlayerRequest(exchange, playerIdentifier);
        } else {
            // Réponse 404 pour les autres chemins
            String response = "{\"error\":\"Not found\"}";
            sendJsonResponse(exchange, response, 404);
        }
    }
    
    /**
     * Traite la requête pour obtenir la liste des joueurs
     * 
     * @param exchange L'échange HTTP
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    @SuppressWarnings("unchecked")
    private void handlePlayersRequest(HttpExchange exchange) throws IOException {
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
            playerData.put("player_version", getPlayerClientVersion(player));
            
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
    
    /**
     * Traite la requête pour obtenir les informations détaillées d'un joueur
     * 
     * @param exchange L'échange HTTP
     * @param playerIdentifier L'identifiant du joueur (nom ou UUID)
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    @SuppressWarnings("unchecked")
    private void handlePlayerRequest(HttpExchange exchange, String playerIdentifier) throws IOException {
        // Essayer de trouver le joueur par UUID d'abord
        Player player = null;
        JSONObject playerData = null;
        
        try {
            UUID uuid = UUID.fromString(playerIdentifier);
            player = Bukkit.getPlayer(uuid);
            
            if (player == null) {
                // Joueur hors ligne
                playerData = playerDataManager.getPlayerData(uuid);
            }
        } catch (IllegalArgumentException e) {
            // Si ce n'est pas un UUID valide, essayer par nom
            player = Bukkit.getPlayerExact(playerIdentifier);
            
            if (player == null) {
                // Joueur hors ligne
                playerData = playerDataManager.getPlayerData(playerIdentifier);
            }
        }
        
        // Si le joueur est en ligne, récupérer ses données en temps réel
        if (player != null) {
            playerData = new JSONObject();
            
            playerData.put("uuid", player.getUniqueId().toString());
            playerData.put("name", player.getName());
            playerData.put("status", "online");
            
            // Inventaire
            JSONArray inventory = new JSONArray();
            ItemStack[] contents = player.getInventory().getContents();
            
            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null) {
                    JSONObject item = new JSONObject();
                    item.put("slot", i);
                    item.put("type", contents[i].getType().toString());
                    
                    if (contents[i].getEnchantments().size() > 0) {
                        JSONObject enchants = new JSONObject();
                        contents[i].getEnchantments().forEach((enchant, level) -> {
                            enchants.put(enchant.getKey().getKey(), level);
                        });
                        item.put("enchantments", enchants);
                    }
                    
                    item.put("amount", contents[i].getAmount());
                    inventory.add(item);
                }
            }
            
            playerData.put("inventory", inventory);
            
            // Armure
            JSONObject armor = new JSONObject();
            ItemStack[] armorContents = player.getInventory().getArmorContents();
            String[] armorSlots = {"boots", "leggings", "chestplate", "helmet"};
            
            for (int i = 0; i < armorContents.length; i++) {
                if (armorContents[i] != null) {
                    JSONObject item = new JSONObject();
                    item.put("type", armorContents[i].getType().toString());
                    
                    if (armorContents[i].getEnchantments().size() > 0) {
                        JSONObject enchants = new JSONObject();
                        armorContents[i].getEnchantments().forEach((enchant, level) -> {
                            enchants.put(enchant.getKey().getKey(), level);
                        });
                        item.put("enchantments", enchants);
                    }
                    
                    armor.put(armorSlots[i], item);
                }
            }
            
            playerData.put("armor", armor);
            
            // Statistiques
            playerData.put("xp", (int) player.getExp());
            playerData.put("level", player.getLevel());
            playerData.put("food", player.getFoodLevel());
            playerData.put("health", (int) player.getHealth());
            playerData.put("gamemode", player.getGameMode().toString());
        }
        
        // Si le joueur n'a pas été trouvé
        if (playerData == null) {
            sendJsonResponse(exchange, "{\"error\":\"Player not found\"}", 404);
            return;
        }
        
        // Envoyer la réponse
        sendJsonResponse(exchange, playerData.toJSONString(), 200);
    }
    
    /**
     * Envoie une réponse JSON
     * 
     * @param exchange L'échange HTTP
     * @param response La réponse en format JSON
     * @param statusCode Le code de statut HTTP
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    private void sendJsonResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
    
    /**
     * Obtient la version du client du joueur
     * 
     * @param player Le joueur
     * @return La version du client, ou "Unknown" si elle ne peut pas être déterminée
     */
    private String getPlayerClientVersion(Player player) {
        try {
            // Cette méthode est une approximation, car il n'existe pas de méthode directe dans l'API Bukkit
            if (player.getClientViewDistance() >= 32) {
                return "1.18+";
            } else if (player.getClientViewDistance() >= 16) {
                return "1.14-1.17";
            } else if (player.isOp()) { // Heuristique basée sur le fait que les anciennes versions peuvent avoir des distances de vue plus faibles
                return "1.12-1.13";
            } else {
                return "1.8-1.11";
            }
        } catch (Exception e) {
            return "Unknown";
        }
    }
}