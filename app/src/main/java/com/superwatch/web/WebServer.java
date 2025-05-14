        package com.superwatch.web;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import com.sun.net.httpserver.HttpServer;
import com.superwatch.App;
import com.superwatch.api.PlayerDetailEndpoint;
import com.superwatch.api.PlayersListEndpoint;
import com.superwatch.handlers.FormHandler;
import com.superwatch.handlers.InstallHandler;
import com.superwatch.website.WebsiteHandler;

/**
 * Classe gérant le serveur HTTP intégré
 */
public class WebServer {
    private final App plugin;
    private HttpServer httpServer;
    private boolean isRunning = false;

    /**
     * Constructeur
     * 
     * @param plugin L'instance principale du plugin
     */
    public WebServer(App plugin) {
        this.plugin = plugin;
    }

    /**
     * Démarre le serveur HTTP
     * 
     * @return true si le serveur a démarré avec succès, false sinon
     */
    public boolean start() {
        if (isRunning) {
            return true;
        }

        try {
            // Obtenir le port depuis la configuration
            int port = plugin.getConfigManager().getInt("web_port", 8080);
            
            // Créer et configurer le serveur HTTP
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            
            // Enregistrer les gestionnaires de contexte
            httpServer.createContext("/", new WebsiteHandler(plugin));
            httpServer.createContext("/install", new InstallHandler(plugin));
            
            // Endpoints API
            httpServer.createContext("/api/players", new PlayersListEndpoint(plugin.getPlayerDataManager()));
            httpServer.createContext("/api/player/", new PlayerDetailEndpoint(plugin.getPlayerDataManager()));
            
            // Configurer le pool de threads
            httpServer.setExecutor(Executors.newCachedThreadPool());
            
            // Démarrer le serveur
            httpServer.start();
            isRunning = true;
            
            plugin.getLogger().info("Serveur HTTP démarré sur le port " + port);
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors du démarrage du serveur HTTP", e);
            return false;
        }
    }

    /**
     * Arrête le serveur HTTP
     */
    public void stop() {
        if (!isRunning || httpServer == null) {
            return;
        }

        try {
            httpServer.stop(0);
            isRunning = false;
            httpServer = null;
            plugin.getLogger().info("Serveur HTTP arrêté");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de l'arrêt du serveur HTTP", e);
        }
    }

    /**
     * Redémarre le serveur HTTP
     */
    public void restart() {
        stop();
        start();
    }

    /**
     * Vérifie si le serveur HTTP est en cours d'exécution
     * 
     * @return true si le serveur est en cours d'exécution, false sinon
     */
    public boolean isRunning() {
        return isRunning;
    }
}