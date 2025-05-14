package com.superwatch.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.superwatch.App;

/**
 * Classe pour gérer la mise à jour du site web depuis GitHub
 */
public class WebsiteUpdater {
    private static final String GITHUB_API_URL = "https://api.github.com/repos/Rudiak01/SuperWatch/releases";
    private static final String VERSION_FILE = "version.txt";
    private static final String DEFAULT_VERSION = "latest";
    private static final Pattern SEMVER_PATTERN = Pattern.compile("^v\\d+\\.\\d+\\.\\d+$");
    
    private final App plugin;
    
    /**
     * Constructeur
     * 
     * @param plugin L'instance principale du plugin
     */
    public WebsiteUpdater(App plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Vérifier si une mise à jour est nécessaire et l'installer si c'est le cas
     * 
     * @return true si une mise à jour a été effectuée, false sinon
     */
    public boolean checkAndUpdateWebsite() {
        String targetVersion = getTargetVersion();
        String currentVersion = getCurrentVersion();
        
        plugin.getLogger().info("Version actuellement installée : " + (currentVersion == null ? "aucune" : currentVersion));
        plugin.getLogger().info("Version cible : " + targetVersion);
        
        if (currentVersion == null || !currentVersion.equals(targetVersion)) {
            try {
                // Si targetVersion est "latest", obtenir le tag réel
                String actualTargetVersion = targetVersion;
                String downloadUrl = null;
                
                if ("latest".equalsIgnoreCase(targetVersion)) {
                    try {
                        actualTargetVersion = getLatestReleaseVersion();
                        plugin.getLogger().info("Version 'latest' résolue en : " + actualTargetVersion);
                        downloadUrl = getLatestReleaseDownloadUrl();
                    } catch (IOException e) {
                        plugin.getLogger().log(Level.WARNING, "Erreur lors de la récupération de la dernière version", e);
                        return fallbackToLocalWebsite();
                    }
                } else {
                    downloadUrl = getSpecificReleaseDownloadUrl(targetVersion);
                }
                
                if (downloadUrl != null) {
                    return downloadAndInstallWebsite(downloadUrl, actualTargetVersion);
                } else {
                    plugin.getLogger().warning("Impossible de trouver l'URL de téléchargement pour la version : " + targetVersion);
                    return fallbackToLocalWebsite();
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Erreur lors de la mise à jour du site web", e);
                return fallbackToLocalWebsite();
            }
        }
        
        plugin.getLogger().info("Le site web est à jour.");
        return false;
    }
    
    /**
     * Recours au site web local en cas d'échec de téléchargement
     * 
     * @return true si l'installation locale a réussi, false sinon
     */
    private boolean fallbackToLocalWebsite() {
        plugin.getLogger().info("Recours au site web local par défaut");
        try {
            // Utiliser WebsiteManager pour extraire le site par défaut
            WebsiteManager.reinstallWebsite(plugin);
            
            // Créer le fichier version.txt avec v0.0.0
            Path versionFile = getWebsitePath().resolve(VERSION_FILE);
            Files.write(versionFile, "v0.0.0".getBytes());
            
            plugin.getLogger().info("Site web local installé avec succès (version v0.0.0)");
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de l'installation du site web local", e);
            return false;
        }
    }
    
    /**
     * Réinitialise le dossier du site web
     * 
     * @return true si la réinitialisation a réussi, false sinon
     */
    public boolean resetWebsite() {
        try {
            Path websitePath = getWebsitePath();
            plugin.getLogger().info("Suppression du dossier : " + websitePath);
            
            if (Files.exists(websitePath)) {
                WebsiteManager.deleteDirectory(websitePath.toFile());
            }
            
            Files.createDirectories(websitePath);
            plugin.getLogger().info("Le dossier du site web a été réinitialisé avec succès.");
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la réinitialisation du dossier du site web", e);
            return false;
        }
    }
    
    /**
     * Vérifie si une mise à jour est disponible
     * 
     * @return Une chaîne contenant les informations sur la mise à jour, ou null si aucune mise à jour n'est disponible
     */
    public String checkForUpdates() {
        try {
            String latestVersion = getLatestReleaseVersion();
            String currentVersion = getCurrentVersion();
            
            if (currentVersion == null) {
                return "Aucune version installée. Dernière version disponible : " + latestVersion;
            }
            
            if ("latest".equals(getTargetVersion())) {
                if (currentVersion.equals(latestVersion)) {
                    return "Le site web est à jour avec la dernière version (" + latestVersion + ")";
                } else {
                    return "Une mise à jour est disponible ! Version actuelle : " + currentVersion + ", Dernière version : " + latestVersion;
                }
            }
            
            if (!currentVersion.equals(latestVersion)) {
                return "Une mise à jour est disponible ! Version actuelle : " + currentVersion + ", Dernière version : " + latestVersion;
            } else {
                return "Le site web est à jour (version " + currentVersion + ")";
            }
            
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la vérification des mises à jour", e);
            return "Erreur lors de la vérification des mises à jour : " + e.getMessage();
        }
    }
    
    /**
     * Force la mise à jour du site web
     * 
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean forceUpdate() {
        return checkAndUpdateWebsite();
    }
    
    /**
     * Définit la version cible du site web
     * 
     * @param version La version à définir (latest ou un tag spécifique comme v0.0.9)
     * @return true si la version a été définie avec succès, false sinon
     */
    public boolean setTargetVersion(String version) {
        if (!isValidVersion(version)) {
            return false;
        }
        
        try {
            plugin.getConfigManager().setProperty("WebsiteVersion", version);
            plugin.getConfigManager().saveConfig();
            plugin.getLogger().info("Version cible définie sur : " + version);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la définition de la version cible", e);
            return false;
        }
    }
    
    /**
     * Obtient les informations sur la version
     * 
     * @return Une chaîne contenant les informations sur la version
     */
    public String getVersionInfo() {
        String currentVersion = getCurrentVersion();
        String targetVersion = getTargetVersion();
        
        StringBuilder info = new StringBuilder();
        info.append("Version installée : ").append(currentVersion == null ? "aucune" : currentVersion).append("\n");
        info.append("Version cible : ").append(targetVersion);
        
        try {
            String latestVersion = getLatestReleaseVersion();
            info.append("\nDernière version disponible : ").append(latestVersion);
        } catch (IOException e) {
            info.append("\nImpossible de récupérer la dernière version disponible : ").append(e.getMessage());
        }
        
        return info.toString();
    }
    
    /**
     * Télécharge et installe le site web depuis l'URL donnée
     * 
     * @param downloadUrl L'URL de téléchargement du fichier ZIP
     * @param version La version à installer
     * @return true si l'installation a réussi, false sinon
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    private boolean downloadAndInstallWebsite(String downloadUrl, String version) throws IOException {
        plugin.getLogger().info("Téléchargement du site web depuis : " + downloadUrl);
        
        // Réinitialiser le dossier du site web
        resetWebsite();
        
        // Télécharger le fichier ZIP
        Path tempFile = Files.createTempFile("superwatch-website-", ".zip");
        try {
            downloadFile(downloadUrl, tempFile);
            
            // Extraire le fichier ZIP
            Path websitePath = getWebsitePath();
            extractZipFile(tempFile, websitePath);
            
            // Créer le fichier de version avec le tag réel (pas "latest")
            Path versionFile = websitePath.resolve(VERSION_FILE);
            Files.write(versionFile, version.getBytes());
            
            plugin.getLogger().info("Site web installé avec succès (version " + version + ")");
            return true;
        } finally {
            // Supprimer le fichier temporaire
            Files.deleteIfExists(tempFile);
        }
    }
    
    /**
     * Télécharge un fichier depuis une URL
     * 
     * @param fileUrl L'URL du fichier à télécharger
     * @param targetPath Le chemin où sauvegarder le fichier
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    private void downloadFile(String fileUrl, Path targetPath) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Accept", "application/octet-stream");
        connection.setRequestProperty("User-Agent", "SuperWatch-Plugin");
        
        try (InputStream in = connection.getInputStream();
             OutputStream out = Files.newOutputStream(targetPath)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
    
    /**
     * Extrait un fichier ZIP dans un répertoire
     * 
     * @param zipFile Le fichier ZIP à extraire
     * @param targetDir Le répertoire cible
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    private void extractZipFile(Path zipFile, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = targetDir.resolve(entry.getName());
                
                // Créer les répertoires parents si nécessaire
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    
                    // Extraire le fichier
                    try (OutputStream out = Files.newOutputStream(entryPath)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = zis.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                }
                
                zis.closeEntry();
            }
        }
    }
    
    /**
     * Obtient l'URL de téléchargement pour une version spécifique
     * 
     * @param version La version à télécharger (latest ou un tag spécifique)
     * @return L'URL de téléchargement, ou null si la version n'existe pas
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    private String getDownloadUrl(String version) throws IOException {
        if ("latest".equalsIgnoreCase(version)) {
            return getLatestReleaseDownloadUrl();
        } else {
            return getSpecificReleaseDownloadUrl(version);
        }
    }
    
    /**
     * Obtient l'URL de téléchargement de la dernière version
     * 
     * @return L'URL de téléchargement, ou null si aucune version n'est disponible
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    private String getLatestReleaseDownloadUrl() throws IOException {
        URL url = new URL(GITHUB_API_URL + "/latest");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setRequestProperty("User-Agent", "SuperWatch-Plugin");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            JSONParser parser = new JSONParser();
            try {
                JSONObject releaseObj = (JSONObject) parser.parse(reader);
                JSONArray assets = (JSONArray) releaseObj.get("assets");
                
                for (Object asset : assets) {
                    JSONObject assetObj = (JSONObject) asset;
                    String name = (String) assetObj.get("name");
                    if (name.endsWith(".zip")) {
                        return (String) assetObj.get("browser_download_url");
                    }
                }
            } catch (ParseException e) {
                plugin.getLogger().log(Level.SEVERE, "Erreur lors de l'analyse de la réponse JSON", e);
            }
        }
        
        return null;
    }
    
    /**
     * Obtient l'URL de téléchargement d'une version spécifique
     * 
     * @param version La version à télécharger
     * @return L'URL de téléchargement, ou null si la version n'existe pas
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    private String getSpecificReleaseDownloadUrl(String version) throws IOException {
        URL url = new URL(GITHUB_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setRequestProperty("User-Agent", "SuperWatch-Plugin");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            JSONParser parser = new JSONParser();
            try {
                JSONArray releases = (JSONArray) parser.parse(reader);
                
                for (Object release : releases) {
                    JSONObject releaseObj = (JSONObject) release;
                    String tagName = (String) releaseObj.get("tag_name");
                    
                    if (tagName.equals(version)) {
                        JSONArray assets = (JSONArray) releaseObj.get("assets");
                        
                        for (Object asset : assets) {
                            JSONObject assetObj = (JSONObject) asset;
                            String name = (String) assetObj.get("name");
                            if (name.endsWith(".zip")) {
                                return (String) assetObj.get("browser_download_url");
                            }
                        }
                    }
                }
            } catch (ParseException e) {
                plugin.getLogger().log(Level.SEVERE, "Erreur lors de l'analyse de la réponse JSON", e);
            }
        }
        
        return null;
    }
    
    /**
     * Obtient la version cible du site web depuis la configuration
     * 
     * @return La version cible (latest par défaut)
     */
    private String getTargetVersion() {
        return plugin.getConfigManager().getString("WebsiteVersion", DEFAULT_VERSION);
    }
    
    /**
     * Obtient la version actuellement installée
     * 
     * @return La version installée, ou null si aucune version n'est installée
     */
    private String getCurrentVersion() {
        Path versionFile = getWebsitePath().resolve(VERSION_FILE);
        
        if (Files.exists(versionFile)) {
            try {
                return new String(Files.readAllBytes(versionFile)).trim();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Erreur lors de la lecture du fichier de version", e);
                return null;
            }
        } else {
            return null;
        }
    }
    
    /**
     * Obtient la version de la dernière release GitHub
     * 
     * @return La version de la dernière release
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    private String getLatestReleaseVersion() throws IOException {
        URL url = new URL(GITHUB_API_URL + "/latest");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setRequestProperty("User-Agent", "SuperWatch-Plugin");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            JSONParser parser = new JSONParser();
            try {
                JSONObject releaseObj = (JSONObject) parser.parse(reader);
                return (String) releaseObj.get("tag_name");
            } catch (ParseException e) {
                plugin.getLogger().log(Level.SEVERE, "Erreur lors de l'analyse de la réponse JSON", e);
                throw new IOException("Erreur lors de l'analyse de la réponse JSON", e);
            }
        }
    }
    
    /**
     * Obtient le chemin du dossier du site web
     * 
     * @return Le chemin du dossier du site web
     */
    private Path getWebsitePath() {
        String websiteFolder = plugin.getConfigManager().getString("web_directory", "web/");
        
        // Supprimer le slash à la fin si présent
        if (websiteFolder.endsWith("/")) {
            websiteFolder = websiteFolder.substring(0, websiteFolder.length() - 1);
        }
        
        plugin.getLogger().fine("Utilisation du dossier web : " + websiteFolder);
        return Paths.get(plugin.getDataFolder().getAbsolutePath(), websiteFolder);
    }
    
    /**
     * Vérifie si une chaîne de version est valide
     * 
     * @param version La version à vérifier
     * @return true si la version est valide, false sinon
     */
    private boolean isValidVersion(String version) {
        if ("latest".equalsIgnoreCase(version)) {
            return true;
        }
        
        return SEMVER_PATTERN.matcher(version).matches();
    }
}