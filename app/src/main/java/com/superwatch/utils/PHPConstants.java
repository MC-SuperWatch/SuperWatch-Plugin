package com.superwatch.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Constantes pour l'installation et la gestion de PHP
 */
public class PHPConstants {
    private static final Logger LOGGER = Logger.getLogger(PHPConstants.class.getName());
    
    /**
     * URL de base pour le téléchargement de PHP pour Windows
     */
    public static final String WINDOWS_PHP_URL_BASE = "https://windows.php.net/downloads/releases/";
    
    /**
     * URL de la page de téléchargement de PHP pour Windows
     */
    public static final String WINDOWS_PHP_DOWNLOAD_PAGE = "https://windows.php.net/download/";
    
    /**
     * Pattern pour le nom de fichier PHP pour Windows
     */
    public static final String WINDOWS_PHP_FILENAME_PATTERN = "php-\\d+\\.\\d+\\.\\d+-Win32-vs17-x64\\.zip";
    
    /**
     * URL de téléchargement de PHP pour Linux
     */
    public static final String LINUX_PHP_URL = "https://www.php.net/distributions/php-8.2.13.tar.gz";
    
    /**
     * URL de téléchargement de PHP pour macOS
     */
    public static final String MAC_PHP_URL = "https://www.php.net/distributions/php-8.2.13.tar.gz";
    
    /**
     * Port par défaut pour le serveur PHP
     */
    public static final int DEFAULT_PHP_PORT = 9000;
    
    /**
     * Obtient l'URL de téléchargement appropriée pour le système d'exploitation actuel
     * 
     * @return L'URL de téléchargement
     */
    public static String getDownloadUrlForCurrentOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        
        if (osName.contains("win")) {
            return getLatestWindowsPHPUrl();
        } else if (osName.contains("mac")) {
            return MAC_PHP_URL;
        } else {
            return LINUX_PHP_URL;
        }
    }
    
    /**
     * Obtient l'URL de la dernière version de PHP pour Windows
     * 
     * @return L'URL complète pour télécharger la dernière version de PHP pour Windows
     */
    public static String getLatestWindowsPHPUrl() {
        // Essayer d'abord la page de téléchargement qui est plus fiable
        String latestVersion = findLatestVersionFromDownloadPage();
        if (latestVersion != null) {
            LOGGER.info("Dernière version PHP trouvée sur la page de téléchargement: " + latestVersion);
            return WINDOWS_PHP_URL_BASE + latestVersion;
        }
        
        // Si cela échoue, essayer de parcourir le répertoire des releases
        latestVersion = findLatestVersionFromReleasesDirectory();
        if (latestVersion != null) {
            LOGGER.info("Dernière version PHP trouvée dans le répertoire des releases: " + latestVersion);
            return WINDOWS_PHP_URL_BASE + latestVersion;
        }
        
        // Si tout a échoué, essayer l'URL spécifiée manuellement
        LOGGER.warning("Aucune version PHP n'a été trouvée automatiquement, utilisation de la version spécifiée manuellement");
        return WINDOWS_PHP_URL_BASE + "php-8.4.7-Win32-vs17-x64.zip";
    }
    
    /**
     * Tente de trouver la dernière version de PHP sur la page de téléchargement principale
     */
    private static String findLatestVersionFromDownloadPage() {
        try {
            URL url = new URL(WINDOWS_PHP_DOWNLOAD_PAGE);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    Pattern pattern = Pattern.compile("href=\"" + WINDOWS_PHP_URL_BASE + "(" + WINDOWS_PHP_FILENAME_PATTERN + ")\"");
                    
                    while ((line = reader.readLine()) != null) {
                        Matcher matcher = pattern.matcher(line);
                        while (matcher.find()) {
                            return matcher.group(1);
                        }
                    }
                }
            } else {
                LOGGER.warning("Erreur lors de l'accès à la page de téléchargement de PHP. Code: " + responseCode);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Erreur lors de l'analyse de la page de téléchargement de PHP", e);
        }
        return null;
    }
    
    /**
     * Tente de trouver la dernière version de PHP en parcourant le répertoire des releases
     */
    private static String findLatestVersionFromReleasesDirectory() {
        try {
            URL url = new URL(WINDOWS_PHP_URL_BASE);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                List<String> phpVersions = new ArrayList<>();
                
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    Pattern pattern = Pattern.compile("href=\"(" + WINDOWS_PHP_FILENAME_PATTERN + ")\"");
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        Matcher matcher = pattern.matcher(line);
                        while (matcher.find()) {
                            String filename = matcher.group(1);
                            phpVersions.add(filename);
                        }
                    }
                }
                
                if (!phpVersions.isEmpty()) {
                    // Trier les versions et prendre la plus récente
                    phpVersions.sort((v1, v2) -> compareVersions(v1, v2));
                    return phpVersions.get(phpVersions.size() - 1);
                }
            } else {
                LOGGER.warning("Erreur lors de l'accès au répertoire des releases PHP. Code: " + responseCode);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Erreur lors de l'analyse du répertoire des releases PHP", e);
        }
        
        return null;
    }
    
    /**
     * Vérifie si une URL de téléchargement est valide
     * 
     * @param url L'URL à vérifier
     * @return true si l'URL est valide et accessible, false sinon
     */
    public static boolean isValidDownloadUrl(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            return (responseCode == HttpURLConnection.HTTP_OK);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Erreur lors de la vérification de l'URL " + url, e);
            return false;
        }
    }
    
    /**
     * Compare deux versions de PHP
     * 
     * @param version1 Première version (nom de fichier complet)
     * @param version2 Deuxième version (nom de fichier complet)
     * @return Négatif si version1 < version2, positif si version1 > version2, 0 si égales
     */
    private static int compareVersions(String version1, String version2) {
        Pattern pattern = Pattern.compile("php-(\\d+)\\.(\\d+)\\.(\\d+)-Win32-vs17-x64\\.zip");
        
        Matcher matcher1 = pattern.matcher(version1);
        Matcher matcher2 = pattern.matcher(version2);
        
        if (matcher1.find() && matcher2.find()) {
            int major1 = Integer.parseInt(matcher1.group(1));
            int minor1 = Integer.parseInt(matcher1.group(2));
            int patch1 = Integer.parseInt(matcher1.group(3));
            
            int major2 = Integer.parseInt(matcher2.group(1));
            int minor2 = Integer.parseInt(matcher2.group(2));
            int patch2 = Integer.parseInt(matcher2.group(3));
            
            if (major1 != major2) {
                return major1 - major2;
            }
            if (minor1 != minor2) {
                return minor1 - minor2;
            }
            return patch1 - patch2;
        }
        
        return version1.compareTo(version2);
    }
}