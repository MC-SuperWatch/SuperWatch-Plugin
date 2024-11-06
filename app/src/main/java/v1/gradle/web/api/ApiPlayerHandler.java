package v1.gradle.web.api;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import v1.gradle.tools.PlayerDataManager;

public class ApiPlayerHandler implements HttpHandler {
    private final Plugin plugin;
    private final PlayerDataManager playerDataManager;

    public ApiPlayerHandler(Plugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        URI requestURI = exchange.getRequestURI();
        String query = requestURI.getQuery();
        String playerName = null;
        if (query != null && query.startsWith("player=")) {
            playerName = query.substring(7);
        }

        JSONObject jsonResponse;
        if (playerName != null) {
            jsonResponse = getDetailedPlayerData(playerName);
        } else {
            jsonResponse = getSimplifiedPlayersData();
        }

        sendJsonResponse(exchange, jsonResponse);
    }

    @SuppressWarnings("unchecked")
    private JSONObject getSimplifiedPlayersData() {
        JSONObject jsonResponse = new JSONObject();
        JSONObject players = new JSONObject();
        JSONObject playerList = new JSONObject();

        int onlineCount = playerDataManager.getOnlineCount();
        int totalCount = playerDataManager.getPlayerDataMap().size();

        // Pour chaque joueur, créer une version simplifiée des données
        playerDataManager.getPlayerDataMap().forEach((name, data) -> {
            JSONObject simplifiedData = new JSONObject();
            simplifiedData.put("level", data.get("level"));
            simplifiedData.put("xp", data.get("xp"));
            simplifiedData.put("health", data.get("health"));
            simplifiedData.put("food", data.get("food"));
            simplifiedData.put("uuid", data.get("uuid"));
            simplifiedData.put("status", data.get("status"));
            simplifiedData.put("permissions", data.get("permissions"));
            
            playerList.put(name, simplifiedData);
        });

        players.put("offline", totalCount - onlineCount);
        players.put("online", onlineCount);
        players.put("max", plugin.getServer().getMaxPlayers());
        players.put("list", playerList);

        jsonResponse.put("mods", new JSONArray());
        jsonResponse.put("software", null);
        jsonResponse.put("players", players);
        jsonResponse.put("plugins", new JSONArray());
        jsonResponse.put("icon", null);

        return jsonResponse;
    }

    @SuppressWarnings("unchecked")
    private JSONObject getDetailedPlayerData(String playerName) {
        JSONObject jsonResponse = new JSONObject();
        JSONObject players = new JSONObject();
        JSONObject playerList = new JSONObject();

        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            JSONObject playerData = new JSONObject();
            
            // Informations détaillées du joueur
            playerData.put("name", player.getName());
            playerData.put("crack_uuid", player.getUniqueId().toString());
            playerData.put("uuid", player.getUniqueId().toString());
            playerData.put("lastSeen", System.currentTimeMillis());
            playerData.put("level", player.getLevel());
            playerData.put("xp", (int)(player.getExp() * 100));
            playerData.put("health", (int)player.getHealth());
            playerData.put("food", player.getFoodLevel());
            playerData.put("status", "online");

            // Permissions
            JSONObject permissions = new JSONObject();
            permissions.put("isOp", player.isOp());
            permissions.put("gameMode", player.getGameMode().toString());
            playerData.put("permissions", permissions);

            // Inventaire
            JSONObject inventory = new JSONObject();
            PlayerInventory playerInventory = player.getInventory();

            // Main et off hand
            inventory.put("main_hand", itemStackToJson(playerInventory.getItemInMainHand()));
            inventory.put("off_hand", itemStackToJson(playerInventory.getItemInOffHand()));

            // Armure
            JSONObject armor = new JSONObject();
            armor.put("helmet", itemStackToJson(playerInventory.getHelmet()));
            armor.put("chestplate", itemStackToJson(playerInventory.getChestplate()));
            armor.put("leggings", itemStackToJson(playerInventory.getLeggings()));
            armor.put("boots", itemStackToJson(playerInventory.getBoots()));
            inventory.put("armor", armor);

            // Contenu de l'inventaire
            JSONObject contents = new JSONObject();
            ItemStack[] items = playerInventory.getContents();
            for (int i = 0; i < items.length; i++) {
                if (items[i] != null && items[i].getType().name() != "AIR") {
                    contents.put(String.valueOf(i), itemStackToJson(items[i]));
                }
            }
            inventory.put("contents", contents);
            
            playerData.put("inventory", inventory);
            playerList.put(playerName, playerData);
        } else {
            // Si le joueur est hors ligne, récupérer les données sauvegardées
            JSONObject savedData = playerDataManager.getPlayerData(playerName);
            if (savedData != null) {
                playerList.put(playerName, savedData);
            }
        }

        players.put("offline", playerDataManager.getPlayerDataMap().size() - playerDataManager.getOnlineCount());
        players.put("online", playerDataManager.getOnlineCount());
        players.put("max", plugin.getServer().getMaxPlayers());
        players.put("list", playerList);

        jsonResponse.put("mods", new JSONArray());
        jsonResponse.put("software", null);
        jsonResponse.put("players", players);
        jsonResponse.put("plugins", new JSONArray());
        jsonResponse.put("icon", null);

        return jsonResponse;
    }

    @SuppressWarnings("unchecked")
    private static JSONObject itemStackToJson(ItemStack item) {
        if (item != null && item.getType().name() != "AIR") {
            JSONObject itemJson = new JSONObject();
            itemJson.put("type", item.getType().name());
            itemJson.put("amount", item.getAmount());
            return itemJson;
        }
        return null;
    }

    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJsonResponse(HttpExchange exchange, JSONObject jsonResponse) throws IOException {
        String response = jsonResponse.toJSONString();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}