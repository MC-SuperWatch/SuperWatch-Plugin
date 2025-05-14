package com.superwatch.handlers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

import com.superwatch.App;
import com.superwatch.utils.PHPConstants;

/**
 * Classe utilitaire pour les opérations d'installation
 */
public class InstallHandlerHelper {

    /**
     * Télécharge le fichier PHP en utilisant l'URL appropriée pour le système d'exploitation
     * 
     * @param plugin L'instance du plugin
     * @param tempDir Le répertoire temporaire où stocker le fichier téléchargé
     * @return Le chemin du fichier téléchargé
     * @throws IOException En cas d'erreur lors du téléchargement
     */
    public static Path downloadPHP(App plugin, Path tempDir) throws IOException {
        // Obtenir l'URL appropriée pour le système d'exploitation
        String phpUrl = PHPConstants.getDownloadUrlForCurrentOS();
        
        // Vérifier si l'URL est valide avant de télécharger
        if (!PHPConstants.isValidDownloadUrl(phpUrl)) {
            plugin.getLogger().warning("L'URL de téléchargement n'est pas valide: " + phpUrl);
            // Si l'URL par défaut n'est pas valide, essayer avec une version spécifiée
            phpUrl = PHPConstants.WINDOWS_PHP_URL_BASE + "php-8.4.7-Win32-vs17-x64.zip";
            
            // Vérifier à nouveau
            if (!PHPConstants.isValidDownloadUrl(phpUrl)) {
                throw new IOException("Impossible de trouver une URL de téléchargement PHP valide");
            }
        }
        
        // Afficher des informations supplémentaires pour Windows
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            plugin.getLogger().info("Utilisation de la version PHP pour Windows: " + phpUrl.substring(phpUrl.lastIndexOf('/') + 1));
        }
        
        plugin.getLogger().info("Téléchargement de PHP depuis " + phpUrl);

        // Créer le répertoire temporaire s'il n'existe pas
        Files.createDirectories(tempDir);

        // Télécharger le fichier
        Path zipFilePath = tempDir.resolve("php.zip");
        
        try {
            URL url = new URL(phpUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            try (ReadableByteChannel readableByteChannel = Channels.newChannel(connection.getInputStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(zipFilePath.toFile());
                 FileChannel fileChannel = fileOutputStream.getChannel()) {

                fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                plugin.getLogger().info("Téléchargement terminé: " + zipFilePath);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors du téléchargement de PHP", e);
            throw e;
        }

        return zipFilePath;
    }
}