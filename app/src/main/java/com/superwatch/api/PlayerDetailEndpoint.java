package com.superwatch.api;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.superwatch.data.PlayerDataManager;

/**
 * Endpoint API pour obtenir les détails d'un joueur spécifique
 */
public class PlayerDetailEndpoint extends ApiEndpoint {

    private final PlayerDataManager playerDataManager;

    /**
     * Constructeur
     * 
     * @param playerDataManager Le gestionnaire de données des joueurs
     */
    public PlayerDetailEndpoint(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            sendJsonResponse(exchange, "{\"error\":\"Method not allowed\"}", 405);
            return;
        }
        
        String path = exchange.getRequestURI().getPath();
        String playerIdentifier = path.substring("/api/player/".length());
        
        if (playerIdentifier == null || playerIdentifier.isEmpty()) {
            sendJsonResponse(exchange, "{\"error\":\"Player identifier required\"}", 400);
            return;
        }

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
            org.bukkit.inventory.ItemStack[] contents = player.getInventory().getContents();
            
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
            org.bukkit.inventory.ItemStack[] armorContents = player.getInventory().getArmorContents();
            String[] armorSlots = {"boots", "leggings", "chestplate", "helmet"};
            
            for (int i = 0; i < armorContents.length; i++) {
                if (armorContents[i] != null && !armorContents[i].getType().isAir()) {
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
}