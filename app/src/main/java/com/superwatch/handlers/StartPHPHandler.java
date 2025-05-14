package com.superwatch.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.superwatch.App;

public class StartPHPHandler implements HttpHandler {

    private final App plugin;
    private Process currentProcess;

    public StartPHPHandler(App plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            // Si un processus PHP est déjà en cours, l'arrêter
            if (currentProcess != null && currentProcess.isAlive()) {
                plugin.getLogger().info("Arrêt du serveur PHP en cours...");
                currentProcess.destroy();
                try {
                    currentProcess.waitFor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            String phpPath = plugin.getDataFolder().getAbsolutePath() + "/php/";
            String webDirectory = plugin.getConfigManager().getString("web_directory", "web/");
            String webPath = plugin.getDataFolder().getAbsolutePath() + "/" + webDirectory;

            // Ensure the website directory exists
            Path webDir = Paths.get(webPath);
            if (!Files.exists(webDir)) {
                Files.createDirectories(webDir);
                plugin.getLogger().info("Created website directory at " + webPath);
            }

            // Get the port from configuration
            int webPort = plugin.getConfigManager().getInt("web_port", 8080);

            // Utiliser 0.0.0.0 pour écouter sur toutes les interfaces
            // Cela permet l'accès depuis l'extérieur si le pare-feu le permet
            String bindAddress = "0.0.0.0:" + webPort;

            ProcessBuilder pb;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                pb = new ProcessBuilder(phpPath + "php.exe", "-S", bindAddress, "-t", webPath);
            } else {
                pb = new ProcessBuilder(phpPath + "bin/php", "-S", bindAddress, "-t", webPath);
            }

            pb.redirectErrorStream(true);
            currentProcess = pb.start();

            // Read output in a background thread to avoid blocking
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        plugin.getLogger().info("[PHP Server] " + line);
                    }
                } catch (IOException e) {
                    plugin.getLogger().severe("Error reading PHP server output: " + e.getMessage());
                }
            }).start();

            sendResponse(exchange, "Serveur PHP démarré sur http://localhost:" + webPort, 200);
            plugin.getLogger().info("Serveur PHP démarré sur " + bindAddress);
        } catch (Exception e) {
            plugin.getLogger().severe("Erreur lors du démarrage du serveur PHP: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, "Erreur: " + e.getMessage(), 500);
        }
    }

    private void sendResponse(HttpExchange exchange, String response, int status) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}