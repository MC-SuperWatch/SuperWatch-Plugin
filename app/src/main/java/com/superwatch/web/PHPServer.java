package com.superwatch.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import com.superwatch.App;

/**
 * Classe gérant le serveur PHP
 */
public class PHPServer {
    private final App plugin;
    private Process phpProcess;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private Thread outputMonitor;

    /**
     * Constructeur
     *
     * @param plugin L'instance principale du plugin
     */
    public PHPServer(App plugin) {
        this.plugin = plugin;
    }

    /**
     * Démarre le serveur PHP
     *
     * @return true si le serveur a démarré avec succès, false sinon
     */
    public boolean start() {
        if (isRunning.get()) {
            plugin.getLogger().info("Le serveur PHP est déjà en cours d'exécution");
            return true;
        }

        try {
            // Récupérer le chemin du PHP depuis la configuration
            String phpPath = plugin.getDataFolder().getAbsolutePath() + "/php/";
            String phpExecutable = System.getProperty("os.name").toLowerCase().contains("win") 
                                   ? phpPath + "php.exe" 
                                   : phpPath + "bin/php";

            // Vérifier si l'exécutable PHP existe
            Path phpExePath = Paths.get(phpExecutable);
            if (!Files.exists(phpExePath)) {
                plugin.getLogger().severe("Exécutable PHP non trouvé: " + phpExecutable);
                return false;
            }

            // Récupérer le répertoire du site web depuis la configuration
            String webDirectory = plugin.getConfigManager().getString("web_directory", "web/");
            String webPath = plugin.getDataFolder().getAbsolutePath() + "/" + webDirectory;

            // Vérifier si le répertoire web existe
            Path webDir = Paths.get(webPath);
            if (!Files.exists(webDir)) {
                plugin.getLogger().warning("Répertoire web non trouvé: " + webPath);
                Files.createDirectories(webDir);
                plugin.getLogger().info("Répertoire web créé: " + webPath);
            }

            // Récupérer le port depuis la configuration ou utiliser un port différent de celui du serveur HTTP
            // Le port HTTP est généralement 8080 ou 8090, donc on utilise 9000 par défaut pour PHP
            int phpPort = plugin.getConfigManager().getInt("php_port", 9000);
            
            // Utiliser 0.0.0.0 pour écouter sur toutes les interfaces
            String bindAddress = "0.0.0.0:" + phpPort;

            // Démarrer le serveur PHP
            ProcessBuilder pb = new ProcessBuilder(phpExecutable, "-S", bindAddress, "-t", webPath);
            pb.redirectErrorStream(true); // Rediriger stderr vers stdout
            phpProcess = pb.start();

            // Surveiller la sortie du processus dans un thread séparé
            outputMonitor = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(phpProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        plugin.getLogger().info("[PHP Server] " + line);
                    }
                } catch (IOException e) {
                    if (isRunning.get()) {
                        plugin.getLogger().log(Level.SEVERE, "Erreur lors de la lecture de la sortie PHP", e);
                    }
                }
            });
            outputMonitor.setDaemon(true);
            outputMonitor.start();

            isRunning.set(true);
            plugin.getLogger().info("Serveur PHP démarré sur " + bindAddress);
            
            // Vérifier l'état du processus après quelques instants
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    if (!phpProcess.isAlive()) {
                        int exitCode = phpProcess.exitValue();
                        plugin.getLogger().severe("Le serveur PHP s'est arrêté prématurément avec le code " + exitCode);
                        isRunning.set(false);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IllegalThreadStateException e) {
                    // Le processus est toujours en cours, c'est bon
                }
            }).start();

            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors du démarrage du serveur PHP", e);
            return false;
        }
    }

    /**
     * Arrête le serveur PHP
     */
    public void stop() {
        if (!isRunning.get()) {
            return;
        }

        try {
            // Arrêter le processus PHP
            if (phpProcess != null && phpProcess.isAlive()) {
                plugin.getLogger().info("Arrêt du serveur PHP...");
                phpProcess.destroy();
                
                // Attendre que le processus se termine (avec timeout)
                try {
                    boolean exited = phpProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
                    if (!exited) {
                        plugin.getLogger().warning("Le serveur PHP ne répond pas, forçage de l'arrêt...");
                        phpProcess.destroyForcibly();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    plugin.getLogger().warning("Interruption lors de l'attente de l'arrêt du serveur PHP");
                }
            }
            
            // Arrêter le thread de surveillance
            if (outputMonitor != null && outputMonitor.isAlive()) {
                outputMonitor.interrupt();
            }

            isRunning.set(false);
            plugin.getLogger().info("Serveur PHP arrêté");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Erreur lors de l'arrêt du serveur PHP", e);
        }
    }

    /**
     * Redémarre le serveur PHP
     */
    public void restart() {
        stop();
        start();
    }

    /**
     * Vérifie si le serveur PHP est en cours d'exécution
     *
     * @return true si le serveur est en cours d'exécution, false sinon
     */
    public boolean isRunning() {
        return isRunning.get() && phpProcess != null && phpProcess.isAlive();
    }
    
    /**
     * Obtient le port utilisé par le serveur PHP
     * 
     * @return le port utilisé par le serveur PHP
     */
    public int getPort() {
        return plugin.getConfigManager().getInt("php_port", 9000);
    }
}