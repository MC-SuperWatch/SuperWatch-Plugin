package com.superwatch.website;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.superwatch.App;
import com.superwatch.handlers.FormHandler;

/**
 * Gestionnaire pour servir les fichiers du site web à partir du dossier WebSite
 */
public class WebsiteHandler implements HttpHandler {

    private final App plugin;
    private final Map<String, String> mimeTypes;
    private final FormHandler fallbackHandler;

    /**
     * Constructeur
     * 
     * @param plugin L'instance principale du plugin
     */
    public WebsiteHandler(App plugin) {
        this.plugin = plugin;
        this.mimeTypes = initMimeTypes();
        this.fallbackHandler = new FormHandler();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Path websiteDir = Paths.get(plugin.getDataFolder().getAbsolutePath(), "WebSite");
        
        // Si le chemin est "/" ou s'il n'y a pas de dossier WebSite, on utilise le FormHandler comme fallback
        if (path.equals("/") || !Files.exists(websiteDir) || !Files.isDirectory(websiteDir)) {
            fallbackHandler.handle(exchange);
            return;
        }

        // Traiter le chemin pour servir les fichiers correctement
        if (path.equals("/")) {
            path = "/index.html";
        }
        
        Path filePath = websiteDir.resolve(path.substring(1));
        
        // Vérifier si le fichier existe
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            // Si le fichier n'existe pas, essayer index.html dans ce dossier
            if (Files.isDirectory(filePath) && Files.exists(filePath.resolve("index.html"))) {
                filePath = filePath.resolve("index.html");
            } else {
                // Sinon, revenir au FormHandler
                fallbackHandler.handle(exchange);
                return;
            }
        }

        try {
            // Déterminer le type MIME
            String mimeType = getMimeType(filePath.toString());
            File file = filePath.toFile();

            // Définir les en-têtes de réponse
            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.sendResponseHeaders(200, file.length());

            // Envoyer le contenu du fichier
            try (OutputStream os = exchange.getResponseBody();
                 FileInputStream fis = new FileInputStream(file)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors du service du fichier: " + filePath, e);
            String response = "Erreur lors du service du fichier demandé.";
            exchange.sendResponseHeaders(500, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    /**
     * Initialise les types MIME
     * 
     * @return Une map des extensions de fichiers et de leurs types MIME
     */
    private Map<String, String> initMimeTypes() {
        Map<String, String> types = new HashMap<>();
        types.put("html", "text/html");
        types.put("htm", "text/html");
        types.put("js", "application/javascript");
        types.put("css", "text/css");
        types.put("jpg", "image/jpeg");
        types.put("jpeg", "image/jpeg");
        types.put("png", "image/png");
        types.put("gif", "image/gif");
        types.put("ico", "image/x-icon");
        types.put("svg", "image/svg+xml");
        types.put("json", "application/json");
        types.put("xml", "application/xml");
        types.put("txt", "text/plain");
        types.put("pdf", "application/pdf");
        types.put("zip", "application/zip");
        types.put("woff", "font/woff");
        types.put("woff2", "font/woff2");
        types.put("ttf", "font/ttf");
        types.put("eot", "application/vnd.ms-fontobject");
        return types;
    }

    /**
     * Détermine le type MIME d'un fichier basé sur son extension
     * 
     * @param path Le chemin du fichier
     * @return Le type MIME correspondant ou "application/octet-stream" par défaut
     */
    private String getMimeType(String path) {
        String extension = "";
        int lastDot = path.lastIndexOf('.');
        if (lastDot > 0) {
            extension = path.substring(lastDot + 1).toLowerCase();
        }
        return mimeTypes.getOrDefault(extension, "application/octet-stream");
    }
}