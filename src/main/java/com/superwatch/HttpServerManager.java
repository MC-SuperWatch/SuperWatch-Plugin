package com.superwatch;

import com.superwatch.MainHandler;
import com.superwatch.ApiHandlers;

import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpServerManager {
    private final Plugin plugin;
    private final ConfigManager configManager;
    private final PlayerDataManager playerDataManager;
    private HttpServer server;

    public HttpServerManager(Plugin plugin, ConfigManager configManager, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.playerDataManager = playerDataManager;
    }

    public void startServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(configManager.getPort()), 0);
            server.createContext("/", new MainHandler(new File(plugin.getDataFolder(), "website")));
            server.createContext("/apiP", new ApiHandlers.ApiPHandler(plugin, playerDataManager));
            server.createContext("/apiS", new ApiHandlers.ApiSHandler(plugin, playerDataManager));

            server.setExecutor(null);
            server.start();
            plugin.getLogger().info("HTTP Server started on port " + configManager.getPort());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to start HTTP server: " + e.getMessage());
        }
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
            plugin.getLogger().info("HTTP server stopped");
        }
    }
}