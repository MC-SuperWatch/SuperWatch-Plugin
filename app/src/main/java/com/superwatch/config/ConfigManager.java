package com.superwatch.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

import com.superwatch.App;

/**
 * Gère la configuration du plugin
 */
public class ConfigManager {

    private final App plugin;
    private final Properties properties;
    private final File configFile;

    /**
     * Constructeur
     * 
     * @param plugin L'instance principale du plugin
     */
    public ConfigManager(App plugin) {
        this.plugin = plugin;
        this.properties = new Properties();
        this.configFile = new File(plugin.getDataFolder(), "superwatch.properties");
        
        loadConfig();
    }

    /**
     * Charge la configuration depuis le fichier
     */
    public void loadConfig() {
        try {
            // Créer le dossier du plugin s'il n'existe pas
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            // Vérifier si le fichier existe
            if (!configFile.exists()) {
                // Extraire le fichier de configuration par défaut depuis les ressources
                try (InputStream in = plugin.getResource("config.properties")) {
                    if (in != null) {
                        // Créer un fichier temporaire d'abord, puis le renommer
                        File tempFile = new File(plugin.getDataFolder(), "config.temp");
                        try (FileOutputStream out = new FileOutputStream(tempFile)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = in.read(buffer)) > 0) {
                                out.write(buffer, 0, length);
                            }
                        }
                        // Renommer le fichier temporaire en fichier de configuration
                        if (tempFile.renameTo(configFile)) {
                            plugin.getLogger().info("Fichier de configuration par défaut créé");
                        } else {
                            plugin.getLogger().warning("Impossible de renommer le fichier temporaire en fichier de configuration");
                        }
                    } else {
                        // Créer un fichier de configuration vide si la ressource n'existe pas
                        configFile.createNewFile();
                        plugin.getLogger().warning("Ressource config.properties non trouvée, fichier de configuration vide créé");
                    }
                }
            }
            
            // Charger les propriétés
            try (FileInputStream in = new FileInputStream(configFile)) {
                properties.load(in);
                plugin.getLogger().info("Configuration chargée depuis " + configFile.getAbsolutePath());
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors du chargement de la configuration", e);
        }
    }

    /**
     * Sauvegarde la configuration dans le fichier
     */
    public void saveConfig() {
        try {
            // Créer un fichier temporaire
            File tempFile = new File(plugin.getDataFolder(), "config.temp");
            
            // Écrire les propriétés dans le fichier temporaire
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                properties.store(out, "SuperWatch Configuration");
            }
            
            // Supprimer l'ancien fichier de configuration s'il existe
            if (configFile.exists()) {
                configFile.delete();
            }
            
            // Renommer le fichier temporaire
            if (tempFile.renameTo(configFile)) {
                plugin.getLogger().info("Configuration sauvegardée dans " + configFile.getAbsolutePath());
            } else {
                plugin.getLogger().warning("Impossible de renommer le fichier temporaire en fichier de configuration");
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la sauvegarde de la configuration", e);
        }
    }

    /**
     * Obtient une valeur de chaîne de caractères
     * 
     * @param key La clé de la propriété
     * @param defaultValue La valeur par défaut si la clé n'existe pas
     * @return La valeur de la propriété
     */
    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Obtient une valeur entière
     * 
     * @param key La clé de la propriété
     * @param defaultValue La valeur par défaut si la clé n'existe pas
     * @return La valeur de la propriété
     */
    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Valeur non entière pour la clé " + key + ", utilisation de la valeur par défaut");
            return defaultValue;
        }
    }

    /**
     * Obtient une valeur booléenne
     * 
     * @param key La clé de la propriété
     * @param defaultValue La valeur par défaut si la clé n'existe pas
     * @return La valeur de la propriété
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(properties.getProperty(key, String.valueOf(defaultValue)));
    }

    /**
     * Définit une valeur de propriété
     * 
     * @param key La clé de la propriété
     * @param value La valeur à définir
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
}