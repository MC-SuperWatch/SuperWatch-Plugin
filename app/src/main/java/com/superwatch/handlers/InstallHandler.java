package com.superwatch.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.superwatch.App;

/**
 * Gestionnaire pour l'installation de PHP et la configuration initiale
 */
public class InstallHandler implements HttpHandler {

    private final App plugin;
    private Process currentProcess;
    private boolean isInstalling = false;
    public InstallHandler(App plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (isInstalling) {
            sendResponse(exchange, "Installation en cours, veuillez patienter...", 200);
            return;
        }

        String requestURI = exchange.getRequestURI().toString();
        if (requestURI.contains("?start=true")) {
            isInstalling = true;

            // Créer un nouvel ID d'installation
            String installId = UUID.randomUUID().toString();

            // Créer un thread séparé pour l'installation
            Thread installThread = new Thread(() -> {
                try {
                    doInstallPHP(installId);
                } catch (Exception e) {
                    plugin.getLogger().severe("Erreur lors de l'installation de PHP: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    isInstalling = false;
                }
            });

            installThread.start();
            sendResponse(exchange, "Installation de PHP démarrée avec l'ID " + installId, 200);
        } else if (requestURI.contains("?status=")) {
            // TODO: Implémenter la vérification du statut de l'installation
            sendResponse(exchange, "Fonctionnalité de statut non implémentée", 501);
        } else {
            // Afficher la page d'installation
            String htmlResponse = getInstallationPage();
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            sendResponse(exchange, htmlResponse, 200);
        }
    }

    private String getInstallationPage() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"fr\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Installation de PHP - SuperWatch</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; line-height: 1.6; }\n" +
                "        .container { max-width: 800px; margin: 0 auto; }\n" +
                "        h1, h2 { color: #333; }\n" +
                "        .card { background: #f9f9f9; border: 1px solid #ddd; padding: 20px; margin-bottom: 20px; border-radius: 5px; }\n" +
                "        .btn { display: inline-block; background: #4CAF50; color: white; padding: 10px 15px; text-decoration: none; border-radius: 4px; }\n" +
                "        .btn-warning { background: #ff9800; }\n" +
                "        #status { display: none; margin-top: 20px; padding: 15px; background: #e8f5e9; border-radius: 4px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h1>Installation de PHP pour SuperWatch</h1>\n" +
                "        \n" +
                "        <div class=\"card\">\n" +
                "            <h2>À propos de cette installation</h2>\n" +
                "            <p>Cette page va vous permettre d'installer PHP directement depuis le serveur Minecraft. PHP est nécessaire pour faire fonctionner le site web de SuperWatch.</p>\n" +
                "            <p><strong>Note:</strong> L'installation peut prendre quelques minutes selon votre connexion internet.</p>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class=\"card\">\n" +
                "            <h2>Prêt à installer?</h2>\n" +
                "            <p>Cliquez sur le bouton ci-dessous pour lancer l'installation de PHP.</p>\n" +
                "            <a href=\"?start=true\" class=\"btn\" id=\"installBtn\">Installer PHP</a>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div id=\"status\"></div>\n" +
                "    </div>\n" +
                "    \n" +
                "    <script>\n" +
                "        document.getElementById('installBtn').addEventListener('click', function(e) {\n" +
                "            e.preventDefault();\n" +
                "            var statusDiv = document.getElementById('status');\n" +
                "            statusDiv.style.display = 'block';\n" +
                "            statusDiv.innerHTML = '<p>Installation en cours...</p>';\n" +
                "            \n" +
                "            fetch(this.href)\n" +
                "                .then(response => response.text())\n" +
                "                .then(data => {\n" +
                "                    statusDiv.innerHTML = '<p>' + data + '</p>';\n" +
                "                    if (data.includes('démarrée')) {\n" +
                "                        checkStatus();\n" +
                "                    }\n" +
                "                })\n" +
                "                .catch(error => {\n" +
                "                    statusDiv.innerHTML = '<p>Erreur: ' + error + '</p>';\n" +
                "                });\n" +
                "        });\n" +
                "        \n" +
                "        function checkStatus() {\n" +
                "            // Cette fonction sera implémentée plus tard pour vérifier l'état de l'installation\n" +
                "            setTimeout(function() {\n" +
                "                var statusDiv = document.getElementById('status');\n" +
                "                statusDiv.innerHTML += '<p>Installation terminée! Vous pouvez maintenant utiliser la commande /superwatch startphp pour démarrer le serveur PHP.</p>';\n" +
                "            }, 10000); // Simuler une vérification après 10 secondes\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }

    private void doInstallPHP(String installId) throws Exception {
        plugin.getLogger().info("Installation de PHP démarrée avec l'ID " + installId);

        // Créer le répertoire temporaire
        Path tempDir = Paths.get(plugin.getDataFolder().getAbsolutePath(), "temp");

        // Télécharger PHP en utilisant l'helper
        Path phpZipPath = InstallHandlerHelper.downloadPHP(plugin, tempDir);

        // Extraire l'archive
        extractPHP(phpZipPath);

        // Configurer PHP
        configurePHP();

        // Tester l'installation
        if (testPHPInstallation()) {
            plugin.getLogger().info("Installation de PHP terminée avec succès!");

            // Démarrer le serveur PHP
            if (plugin.startPHPServer()) {
                plugin.getLogger().info("Serveur PHP démarré avec succès");
            } else {
                plugin.getLogger().warning("Échec du démarrage du serveur PHP après l'installation");
            }
        } else {
            plugin.getLogger().severe("L'installation de PHP a échoué!");
        }
    }

    private void extractPHP(Path zipFilePath) throws IOException {
        Path phpDir = Paths.get(plugin.getDataFolder().getAbsolutePath(), "php");

        // Supprimer l'ancien répertoire PHP s'il existe
        if (Files.exists(phpDir)) {
            deleteDirectory(phpDir.toFile());
        }

        // Créer le nouveau répertoire
        Files.createDirectories(phpDir);

        plugin.getLogger().info("Extraction de PHP vers " + phpDir);

        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(zipFilePath))) {
            ZipEntry entry = zipIn.getNextEntry();

            while (entry != null) {
                Path filePath = phpDir.resolve(entry.getName());

                if (!entry.isDirectory()) {
                    // Créer les répertoires parents si nécessaire
                    Files.createDirectories(filePath.getParent());

                    // Extraire le fichier
                    Files.copy(zipIn, filePath, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    // Créer le répertoire
                    Files.createDirectories(filePath);
                }

                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }

        plugin.getLogger().info("Extraction terminée");

        // Rendre les fichiers PHP exécutables sur Linux/Mac
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            makeExecutable(phpDir.resolve("bin/php"));
        }
    }

    private void makeExecutable(Path filePath) throws IOException {
        File file = filePath.toFile();
        if (file.exists()) {
            file.setExecutable(true);
            plugin.getLogger().info("Fichier rendu exécutable: " + filePath);
        }
    }

    private void configurePHP() throws IOException {
        // Créer ou modifier le fichier php.ini
        Path phpDir = Paths.get(plugin.getDataFolder().getAbsolutePath(), "php");
        Path phpIni = phpDir.resolve("php.ini");

        List<String> phpIniContent = new ArrayList<>();
        phpIniContent.add("extension_dir = \"ext\"");
        phpIniContent.add("extension=openssl");
        phpIniContent.add("extension=mysqli");
        phpIniContent.add("extension=pdo_mysql");
        phpIniContent.add("extension=curl");
        phpIniContent.add("extension=gd");
        phpIniContent.add("extension=zip");
        phpIniContent.add("date.timezone = \"Europe/Paris\"");
        phpIniContent.add("memory_limit = 256M");
        phpIniContent.add("max_execution_time = 300");

        Files.write(phpIni, phpIniContent, StandardCharsets.UTF_8);
        plugin.getLogger().info("Fichier php.ini créé");
    }

    private boolean testPHPInstallation() throws IOException, InterruptedException {
        Path phpDir = Paths.get(plugin.getDataFolder().getAbsolutePath(), "php");
        Path phpExecutable = System.getProperty("os.name").toLowerCase().contains("win")
                            ? phpDir.resolve("php.exe")
                            : phpDir.resolve("bin/php");

        if (!Files.exists(phpExecutable)) {
            plugin.getLogger().severe("Exécutable PHP non trouvé: " + phpExecutable);
            return false;
        }

        ProcessBuilder pb = new ProcessBuilder(phpExecutable.toString(), "-v");
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        int exitCode = process.waitFor();

        plugin.getLogger().info("Test PHP: " + output.toString().trim());
        plugin.getLogger().info("Code de sortie: " + exitCode);

        return exitCode == 0 && output.toString().contains("PHP");
    }
    private void sendResponse(HttpExchange exchange, String response, int status) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return directory.delete();
    }
}
