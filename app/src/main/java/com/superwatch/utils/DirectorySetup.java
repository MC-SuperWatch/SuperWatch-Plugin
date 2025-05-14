package com.superwatch.utils;

import java.io.File;

import com.superwatch.App;
import com.superwatch.web.WebsiteManager;

/**
 * Classe utilitaire pour configurer les répertoires du plugin
 */
public class DirectorySetup {

    /**
     * Configure les répertoires nécessaires au plugin
     *
     * @param plugin L'instance principale du plugin
     */
    public static void setupDirectories(App plugin) {
        // Créer le répertoire de données des joueurs
        new File(plugin.getDataFolder(), "playerdata").mkdirs();

        // Créer le répertoire PHP si nécessaire
        new File(plugin.getDataFolder(), "php").mkdirs();

        // Créer le répertoire du site web - Use config value from ConfigManager
        String webDirectory = plugin.getConfigManager().getString("web_directory", "web/");
        File webDir = new File(plugin.getDataFolder(), webDirectory);
        webDir.mkdirs();

        // Vérifier si un ancien répertoire "website" existe et s'il diffère du répertoire configuré
        if (!webDirectory.equals("website/")) {
            File oldWebDir = new File(plugin.getDataFolder(), "website");
            if (oldWebDir.exists() && !oldWebDir.equals(webDir)) {
                // Supprimer l'ancien répertoire
                if (oldWebDir.isDirectory()) {
                    WebsiteManager.deleteDirectory(oldWebDir);
                    plugin.getLogger().info("Ancien répertoire 'website' supprimé pour éviter la confusion");
                }
            }
        }

        // Extraire le site web si nécessaire
        WebsiteManager.checkWebsiteDirectory(plugin);
    }
}



