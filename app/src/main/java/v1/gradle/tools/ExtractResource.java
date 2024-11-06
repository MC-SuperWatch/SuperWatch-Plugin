package v1.gradle.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

public class ExtractResource {
    private final JavaPlugin plugin;
    private final String resourcesPath;

    public ExtractResource(JavaPlugin plugin) {
        this.plugin = plugin;
        this.resourcesPath = "SuperWatch";
    }

    /**
     * Vérifie si les fichiers nécessaires existent
     * @return true si tous les fichiers requis existent, false sinon
     */
    public boolean checkRequiredFiles() {
        File outDir = new File(plugin.getDataFolder().getParentFile(), resourcesPath);
        if (!outDir.exists()) {
            return false;
        }

        // Vérifier config.properties
        File configFile = new File(outDir, "config.properties");
        if (!configFile.exists()) {
            return false;
        }

        // Vérifier le dossier website
        File websiteDir = new File(outDir, "website");
        if (!websiteDir.exists() || !websiteDir.isDirectory()) {
            return false;
        }

        // Vérifier si le dossier website contient au moins un fichier
        File[] websiteFiles = websiteDir.listFiles();
        return websiteFiles != null && websiteFiles.length > 0;
    }

    /**
     * Extrait les ressources uniquement si elles n'existent pas
     */
    public void extractMissingResources() {
        File outDir = new File(plugin.getDataFolder().getParentFile(), resourcesPath);
        
        // Créer le dossier principal s'il n'existe pas
        if (!outDir.exists()) {
            outDir.mkdirs();
            plugin.getLogger().info("Dossier SuperWatch créé");
            
            // Extraire les fichiers car le dossier vient d'être créé
            createOrUpdateConfig("config.properties");
            extractFolder("website");
        } else {
            // Vérifier uniquement les fichiers manquants
            if (!new File(outDir, "config.properties").exists()) {
                createOrUpdateConfig("config.properties");
            }
            
            File websiteDir = new File(outDir, "website");
            if (!websiteDir.exists() || websiteDir.list().length == 0) {
                extractFolder("website");
            }
        }
    }

    /**
     * Extrait une ressource spécifique du plugin
     * @param resourcePath Chemin de la ressource dans le plugin
     * @return true si l'extraction a réussi, false sinon
     */
    public boolean extractResource(String resourcePath) {
        InputStream in = plugin.getResource(resourcePath);
        if (in == null) {
            plugin.getLogger().warning("Ressource non trouvée: " + resourcePath);
            return false;
        }

        File outDir = new File(plugin.getDataFolder().getParentFile(), resourcesPath);
        if (!outDir.exists() && !outDir.mkdirs()) {
            plugin.getLogger().warning("Impossible de créer le dossier: " + outDir.getPath());
            return false;
        }

        File outFile = new File(outDir, resourcePath);
        
        // Création des dossiers parents si nécessaire
        if (!outFile.getParentFile().exists() && !outFile.getParentFile().mkdirs()) {
            plugin.getLogger().warning("Impossible de créer le dossier parent pour: " + outFile.getPath());
            return false;
        }

        try (FileOutputStream out = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            plugin.getLogger().info("Ressource extraite avec succès: " + resourcePath);
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de l'extraction de " + resourcePath, e);
            return false;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Erreur lors de la fermeture du flux", e);
            }
        }
    }

    /**
     * Extrait un dossier et son contenu depuis les ressources du plugin
     * @param folderPath Chemin du dossier dans les ressources
     * @return true si l'extraction a réussi, false sinon
     */
    public boolean extractFolder(String folderPath) {
        try {
            boolean success = true;
            for (String resource : getResourcesInFolder(folderPath)) {
                success &= extractResource(resource);
            }
            return success;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de l'extraction du dossier " + folderPath, e);
            return false;
        }
    }

    /**
     * Liste toutes les ressources dans un dossier donné
     * @param folderPath Chemin du dossier à explorer
     * @return Liste des chemins des ressources
     */
    private List<String> getResourcesInFolder(String folderPath) throws IOException {
        List<String> resources = new ArrayList<>();
        
        // Obtenir le fichier JAR du plugin
        URL jarURL = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
        String jarPath = URLDecoder.decode(jarURL.getPath(), "UTF-8");
        JarFile jarFile = new JarFile(jarPath);

        // Explorer toutes les entrées du JAR
        Enumeration<JarEntry> entries = jarFile.entries();
        String searchPath = folderPath.endsWith("/") ? folderPath : folderPath + "/";

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();

            if (name.startsWith(searchPath) && !entry.isDirectory()) {
                resources.add(name);
                plugin.getLogger().info("Ressource trouvée: " + name);
            }
        }

        jarFile.close();
        return resources;
    }

    /**
     * Crée ou met à jour un fichier de configuration
     * @param fileName Nom du fichier de configuration
     * @return true si la création/mise à jour a réussi, false sinon
     */
    public boolean createOrUpdateConfig(String fileName) {
        File outDir = new File(plugin.getDataFolder().getParentFile(), resourcesPath);
        File configFile = new File(outDir, fileName);

        // Si le fichier n'existe pas, on l'extrait
        if (!configFile.exists()) {
            return extractResource(fileName);
        }

        // Si le fichier existe déjà, on le met à jour si nécessaire
        try (InputStream defaultConfig = plugin.getResource(fileName)) {
            if (defaultConfig == null) {
                plugin.getLogger().warning("Configuration par défaut non trouvée: " + fileName);
                return false;
            }
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de la mise à jour de " + fileName, e);
            return false;
        }
    }

    /**
     * Vérifie si les ressources nécessaires existent et les extrait si nécessaire
     */
    public void checkAndExtractResources() {
        File outDir = new File(plugin.getDataFolder().getParentFile(), resourcesPath);
        if (!outDir.exists()) {
            outDir.mkdirs();
            plugin.getLogger().info("Dossier SuperWatch créé");
        }

        // Extraire config.properties
        createOrUpdateConfig("config.properties");

        // Extraire le dossier website et son contenu
        extractFolder("website");
    }
}