package com.superwatch;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.superwatch.commands.SuperWatchCommandManager;
import com.superwatch.config.ConfigManager;
import com.superwatch.data.PlayerDataManager;
import com.superwatch.utils.DirectorySetup;
import com.superwatch.web.PHPServer;
import com.superwatch.web.WebServer;
import com.superwatch.web.WebsiteUpdater;

/**
 * Classe principale du plugin SuperWatch
 */
public class App extends JavaPlugin {
    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private WebServer webServer;
    private PHPServer phpServer;
    private WebsiteUpdater websiteUpdater;
    private SuperWatchCommandManager commandManager;

    @Override
    public void onDisable() {
        getLogger().info("SuperWatch désactivé !");
        
        // Sauvegarder les données des joueurs connectés
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerDataManager.savePlayerData(player);
        }
        
        // Arrêter les serveurs
        if (webServer != null) {
            webServer.stop();
        }
        
        if (phpServer != null) {
            phpServer.stop();
        }
    }

    @Override
    public void onEnable() {
        getLogger().info("SuperWatch activé !");
        
        // Initialiser le gestionnaire de configuration
        configManager = new ConfigManager(this);
        
        // Initialiser le gestionnaire de données des joueurs
        playerDataManager = new PlayerDataManager(this);
        
        // Créer les répertoires nécessaires
        DirectorySetup.setupDirectories(this);
        
        // Initialiser le module de mise à jour du site web
        websiteUpdater = new WebsiteUpdater(this);
        
        // Vérifier et mettre à jour le site web si nécessaire
        boolean websiteUpdated = websiteUpdater.checkAndUpdateWebsite();
        if (websiteUpdated) {
            getLogger().info("Le site web a été mis à jour. Redémarrage des services...");
        }
        
        // Initialiser le serveur HTTP
        webServer = new WebServer(this);
        if (!webServer.start()) {
            getLogger().severe("Échec du démarrage du serveur HTTP");
        }

        // Initialiser le serveur PHP
        phpServer = new PHPServer(this);
        if (configManager.getBoolean("auto_start_php", false)) {
            if (!phpServer.start()) {
                getLogger().severe("Échec du démarrage automatique du serveur PHP");
            }
        }
        // Enregistrer la commande principale
        commandManager = new SuperWatchCommandManager(this);
        this.getCommand("superwatch").setExecutor(commandManager);
        this.getCommand("superwatch").setTabCompleter(commandManager);
    }
    
    /**
     * Obtient le gestionnaire de configuration
     * 
     * @return Le gestionnaire de configuration
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Obtient le gestionnaire de données des joueurs
     * 
     * @return Le gestionnaire de données des joueurs
     */
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    
    /**
     * Obtient le serveur web
     *
     * @return Le serveur web
     */
    public WebServer getWebServer() {
        return webServer;
    }

    /**
     * Obtient le serveur PHP
     *
     * @return Le serveur PHP
     */
    public PHPServer getPHPServer() {
        return phpServer;
    }
    
    /**
     * Obtient le gestionnaire de mise à jour du site web
     * 
     * @return Le gestionnaire de mise à jour du site web
     */
    public WebsiteUpdater getWebsiteUpdater() {
        return websiteUpdater;
    }

    /**
     * Démarre le serveur PHP
     * 
     * @return true si le serveur a démarré avec succès, false sinon
     */
    public boolean startPHPServer() {
        if (phpServer != null) {
            return phpServer.start();
        }
        return false;
    }

    /**
     * Redémarre les services du plugin
     */
    public void restartServices() {
        // Redémarrer le serveur HTTP
        if (webServer != null) {
            webServer.restart();
        }

        // Redémarrer le serveur PHP si nécessaire
        if (phpServer != null && phpServer.isRunning()) {
            phpServer.restart();
        }
    }

    /**
     * Récupère une valeur de configuration (méthode pratique)
     *
     * @param key La clé de la propriété
     * @param defaultValue La valeur par défaut si la clé n'existe pas
     * @return La valeur de la propriété
     */
    public String getConfig(String key, String defaultValue) {
        return configManager.getString(key, defaultValue);
    }

    /**
     * Récupère une valeur entière de configuration (méthode pratique)
     *
     * @param key La clé de la propriété
     * @param defaultValue La valeur par défaut si la clé n'existe pas
     * @return La valeur de la propriété
     */
    public int getConfig(String key, int defaultValue) {
        return configManager.getInt(key, defaultValue);
    }

    /**
     * Récupère une valeur booléenne de configuration (méthode pratique)
     *
     * @param key La clé de la propriété
     * @param defaultValue La valeur par défaut si la clé n'existe pas
     * @return La valeur de la propriété
     */
    public boolean getConfig(String key, boolean defaultValue) {
        return configManager.getBoolean(key, defaultValue);
    }

    /**
     * Recharge la configuration du plugin
     */
    public void reloadConfig() {
        configManager.loadConfig();
    }
}