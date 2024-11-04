package com.superwatch;

import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

public class Plugin extends JavaPlugin {
    private static final Logger LOGGER = Logger.getLogger("superwatch");
    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private HttpServerManager httpServerManager;
    private File websiteDir;
    private EventListeners eventListeners;

    @Override
    public void onEnable() {
        LOGGER.info("superwatch enabled");
        websiteDir = new File(getDataFolder(), "website");

        configManager = new ConfigManager(this);
        playerDataManager = new PlayerDataManager(this);
        httpServerManager = new HttpServerManager(this, configManager, playerDataManager);
        eventListeners = new EventListeners(this, playerDataManager);


        extractResource("index.html", new File(websiteDir, "index.html"));
        extractResource("styles.css", new File(websiteDir, "assets/css/styles.css"));
        extractResource("navbar.js", new File(websiteDir, "assets/js/navbar.js"));
        extractResource("apimain.js", new File(websiteDir, "assets/js/apimain.js"));
        extractResource("pagedetail.js", new File(websiteDir, "assets/js/pagedetail.js"));
        extractResource("skin.js", new File(websiteDir, "assets/js/skin.js"));
        extractResource("ico.png", new File(websiteDir, "assets/img/ico.png"));
        extractResource("body.png", new File(websiteDir, "assets/img/body.png"));
        extractResource("config.properties", new File(getDataFolder(), "config.properties"));



        configManager.loadConfig();
        playerDataManager.loadPlayerData();
        httpServerManager.startServer();

        getServer().getPluginManager().registerEvents(eventListeners, this);

        

    }

    @Override
    public void onDisable() {
        LOGGER.info("superwatch disabled");
        httpServerManager.stopServer();
        playerDataManager.savePlayerData();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }



    private void extractResource(String resourceName, File destination) {
        if (!destination.exists()) {
            destination.getParentFile().mkdirs();
            try (InputStream in = getResource(resourceName)) {
                if (in != null) {
                    Files.copy(in, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.info("Extracted " + resourceName + " to " + destination.getPath());
                } else {
                    LOGGER.severe("Resource not found: " + resourceName);
                }
            } catch (IOException e) {
                LOGGER.severe("Failed to extract resource: " + resourceName);
            }
        } else {
            LOGGER.info(resourceName + " already exists, skipping extraction.");
        }
    }

        public static String getPremiumUUID(String playerName) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
    
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
    
                String jsonText = response.toString();
                if (!jsonText.isEmpty()) {
                    JSONParser parser = new JSONParser();
                    JSONObject obj = (JSONObject) parser.parse(jsonText);
                    return (String) obj.get("id"); // UUID officiel de Mojang
                } else {
                    LOGGER.warning("Empty response from Mojang API for player: " + playerName);
                }
            } else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                LOGGER.info("No premium account found for player: " + playerName);
            } else {
                LOGGER.warning("HTTP error when querying Mojang API. Response code: " + responseCode);
            }
        } catch (IOException e) {
            LOGGER.warning("Network error when querying Mojang API: " + e.getMessage());
        } catch (ParseException e) {
            LOGGER.warning("Error parsing JSON response from Mojang API: " + e.getMessage());
        }
        return null; // Pas de compte premium trouvé ou erreur lors de la requête
    }

}