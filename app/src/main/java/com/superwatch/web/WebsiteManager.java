package com.superwatch.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

import com.superwatch.App;

/**
 * Classe utilitaire pour gérer le site web
 */
public class WebsiteManager {
    private static final String VERSION_FILE = "version.txt";
    private static final String DEFAULT_VERSION = "v0.0.0";

    /**
     * Vérifie si le site web est installé, sinon l'installe
     *
     * @param plugin L'instance principale du plugin
     */
    public static void checkWebsiteDirectory(App plugin) {
        String webDirPath = plugin.getConfigManager().getString("web_directory", "web/");
        Path websitePath = Paths.get(plugin.getDataFolder().getAbsolutePath(), webDirPath);
        Path indexFile = websitePath.resolve("index.php");

        // Vérifier si le fichier index.php existe
        if (!Files.exists(indexFile)) {
            reinstallWebsite(plugin);
        }
    }

    /**
     * Réinstalle le site web depuis les ressources
     *
     * @param plugin L'instance principale du plugin
     */
    public static void reinstallWebsite(App plugin) {
        String webDirPath = plugin.getConfigManager().getString("web_directory", "web/");
        
        // Supprimer le slash à la fin si présent
        if (webDirPath.endsWith("/")) {
            webDirPath = webDirPath.substring(0, webDirPath.length() - 1);
        }
        
        Path targetPath = Paths.get(plugin.getDataFolder().getAbsolutePath(), webDirPath);

        try {
            // Supprimer l'ancien dossier website s'il existe
            if (Files.exists(targetPath)) {
                plugin.getLogger().info("Suppression de l'ancien dossier " + targetPath);
                deleteDirectory(targetPath.toFile());
            }

            // Créer le répertoire cible
            Files.createDirectories(targetPath);
            plugin.getLogger().info("Répertoire créé: " + targetPath);

            // Extraction directe des ressources
            extractWebsiteResources(plugin, targetPath);
            
            // Créer le fichier version.txt avec la version par défaut
            Path versionFile = targetPath.resolve(VERSION_FILE);
            Files.write(versionFile, DEFAULT_VERSION.getBytes());
            plugin.getLogger().info("Fichier version.txt créé avec la version " + DEFAULT_VERSION);

            plugin.getLogger().info("Dossier website réinstallé avec succès dans " + targetPath);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la réinstallation du dossier website", e);
        }
    }

    /**
     * Extrait les ressources du site web depuis le JAR
     * 
     * @param plugin L'instance principale du plugin
     * @param targetPath Le chemin cible où copier les fichiers
     */
    private static void extractWebsiteResources(App plugin, Path targetPath) {
        String resourcePath = "website/";
        int filesExtracted = 0;
        
        try {
            // Extraire les fichiers de base
            String[] baseFiles = {"index.php", "2.html", "player.html", "README.md"};
            for (String file : baseFiles) {
                extractResource(plugin, resourcePath + file, targetPath.resolve(file));
                filesExtracted++;
            }
            
            // Créer les sous-dossiers nécessaires
            Files.createDirectories(targetPath.resolve("assets/css"));
            Files.createDirectories(targetPath.resolve("assets/js"));
            Files.createDirectories(targetPath.resolve("Hashage"));
            
            // Extraire les fichiers JavaScript
            String[] jsFiles = {"apimain.js", "navbar.js", "pagedetail.js", "skin.js"};
            for (String file : jsFiles) {
                extractResource(plugin, resourcePath + "assets/js/" + file, targetPath.resolve("assets/js/" + file));
                filesExtracted++;
            }
            
            // Extraire les fichiers CSS
            extractResource(plugin, resourcePath + "assets/css/styles.css", targetPath.resolve("assets/css/styles.css"));
            filesExtracted++;
            
            // Extraire les fichiers de hashage
            String[] hashFiles = {"SHA-256.js", "tt.js"};
            for (String file : hashFiles) {
                extractResource(plugin, resourcePath + "Hashage/" + file, targetPath.resolve("Hashage/" + file));
                filesExtracted++;
            }
            
            // Extraire le fichier de favicon (si présent)
            try {
                extractResource(plugin, resourcePath + "favicon.ico", targetPath.resolve("favicon.ico"));
                filesExtracted++;
            } catch (IOException e) {
                // Le favicon est optionnel, ignorer l'erreur
            }
            
            plugin.getLogger().info("Extraction terminée: " + filesExtracted + " fichiers extraits");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de l'extraction des ressources", e);
        }
    }
    
    /**
     * Extrait une seule ressource du JAR vers un fichier
     *
     * @param plugin L'instance du plugin
     * @param resourcePath Le chemin de la ressource dans le JAR
     * @param targetFile Le fichier cible
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    private static void extractResource(App plugin, String resourcePath, Path targetFile) throws IOException {
        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) {
                plugin.getLogger().warning("Ressource non trouvée: " + resourcePath);
                return;
            }
            
            Files.createDirectories(targetFile.getParent());
            Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info("Fichier extrait: " + targetFile);
        }
    }

    /**
     * Supprime un répertoire et tout son contenu
     *
     * @param directory Le répertoire à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public static boolean deleteDirectory(File directory) {
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