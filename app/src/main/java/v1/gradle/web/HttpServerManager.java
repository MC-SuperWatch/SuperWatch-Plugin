package v1.gradle.web;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import org.bukkit.plugin.Plugin;

import com.sun.net.httpserver.HttpServer;

import v1.gradle.tools.PlayerDataManager;
import v1.gradle.web.api.ApiPlayerHandler;
import v1.gradle.web.api.ApiServerHandler;

public class HttpServerManager {
    private final Plugin plugin;
    private HttpServer server;
    private final PlayerDataManager playerDataManager;

    public HttpServerManager(Plugin plugin) {
        this.plugin = plugin;
        this.playerDataManager = new PlayerDataManager(plugin);
    }

    public void startServer() {
        try {
            // Configuration du serveur
            server = HttpServer.create(new InetSocketAddress(8001), 0);
            
            // Configuration du dossier racine pour les fichiers web statiques
            File websiteRoot = new File(plugin.getDataFolder(), "website");
            if (!websiteRoot.exists()) {
                websiteRoot.mkdirs();
            }

            // Gestionnaire pour les fichiers statiques
            server.createContext("/", new MainHandler(websiteRoot));
            
            // Gestionnaires API
            server.createContext("/ApiP", new ApiPlayerHandler(plugin, playerDataManager));
            server.createContext("/ApiS", new ApiServerHandler(plugin, playerDataManager));

            // Démarrage du serveur
            server.setExecutor(null);
            server.start();
            
            plugin.getLogger().info("Serveur HTTP démarré sur le port 8080");
        } catch (IOException e) {
            plugin.getLogger().severe("Erreur lors du démarrage du serveur HTTP: " + e.getMessage());
        }
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
            plugin.getLogger().info("Serveur HTTP arrêté");
        }
    }
}