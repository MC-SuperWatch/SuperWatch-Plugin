package com.superwatch.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.superwatch.App;

/**
 * Gestionnaire d'installation de PHP
 */
public class PHPInstaller {
    private final App plugin;
    private boolean installInProgress = false;
    
    public PHPInstaller(App plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Vérifie si PHP est installé
     * 
     * @return true si PHP est correctement installé
     */
    public boolean isInstalled() {
        File phpDir = new File(plugin.getDataFolder(), "php");
        if (!phpDir.exists() || !phpDir.isDirectory()) {
            return false;
        }
        
        File phpExecutable;
        if (isWindows()) {
            phpExecutable = new File(phpDir, "php.exe");
        } else {
            phpExecutable = new File(phpDir, "bin/php");
        }
        
        return phpExecutable.exists() && phpExecutable.canExecute();
    }
    
    /**
     * Démarre l'installation de PHP
     * 
     * @param player Le joueur qui a demandé l'installation (pour les notifications), peut être null
     * @param onComplete Callback appelé après l'installation complète
     * @return un CompletableFuture qui sera complété une fois l'installation terminée
     */
    public CompletableFuture<Boolean> installPHP(Player player, Consumer<Boolean> onComplete) {
        if (installInProgress) {
            sendMessage(player, ChatColor.YELLOW + "Une installation de PHP est déjà en cours...");
            return CompletableFuture.completedFuture(false);
        }
        
        // Marquer l'installation comme en cours
        installInProgress = true;
        
        // Créer un CompletableFuture pour le résultat
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        
        // Démarrer l'installation dans un thread séparé
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                sendMessage(player, ChatColor.GOLD + "Début de l'installation de PHP...");
                
                // Utiliser l'URL appropriée pour le système d'exploitation
                String downloadUrl = PHPConstants.getDownloadUrlForCurrentOS();
                sendMessage(player, ChatColor.AQUA + "Téléchargement de PHP depuis " + downloadUrl);
                
                // Télécharger le fichier PHP
                Path downloadPath = downloadPHP(downloadUrl, player);
                if (downloadPath == null) {
                    throw new IOException("Échec du téléchargement de PHP");
                }
                
                // Extraire le fichier
                sendMessage(player, ChatColor.AQUA + "Extraction de PHP...");
                boolean extracted = extractPHP(downloadPath, player);
                if (!extracted) {
                    throw new IOException("Échec de l'extraction de PHP");
                }
                
                // Supprimer le fichier téléchargé
                Files.deleteIfExists(downloadPath);
                
                // Installation terminée avec succès
                sendMessage(player, ChatColor.GREEN + "Installation de PHP terminée avec succès !");
                
                // Attendre un peu pour assurer que tout est bien configuré
                Thread.sleep(1000);
                
                // Marquer l'installation comme terminée
                installInProgress = false;
                
                // Démarrer le serveur PHP
                Bukkit.getScheduler().runTask(plugin, () -> {
                    boolean started = plugin.startPHPServer();
                    sendMessage(player, started ? 
                        ChatColor.GREEN + "Serveur PHP démarré avec succès !" : 
                        ChatColor.RED + "Échec du démarrage du serveur PHP.");
                    
                    // Compléter le future avec le résultat
                    result.complete(started);
                    
                    // Appeler le callback si fourni
                    if (onComplete != null) {
                        onComplete.accept(started);
                    }
                });
                
            } catch (Exception e) {
                // Gérer l'erreur
                plugin.getLogger().severe("Erreur lors de l'installation de PHP: " + e.getMessage());
                e.printStackTrace();
                
                sendMessage(player, ChatColor.RED + "Erreur lors de l'installation de PHP: " + e.getMessage());
                
                // Marquer l'installation comme terminée (avec erreur)
                installInProgress = false;
                
                // Compléter le future avec l'échec
                result.complete(false);
                
                // Appeler le callback si fourni
                if (onComplete != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> onComplete.accept(false));
                }
            }
        });
        
        return result;
    }
    
    /**
     * Télécharge PHP depuis l'URL spécifiée
     * 
     * @param url L'URL de téléchargement
     * @param player Le joueur pour les notifications
     * @return Le chemin du fichier téléchargé ou null en cas d'erreur
     */
    private Path downloadPHP(String url, Player player) {
        try {
            // Créer un répertoire temporaire si nécessaire
            Path tempDir = Paths.get(plugin.getDataFolder().getAbsolutePath(), "temp");
            Files.createDirectories(tempDir);
            
            // Déterminer le nom du fichier
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            Path targetPath = tempDir.resolve(fileName);
            
            // Ouvrir la connexion HTTP
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "SuperWatch Plugin");
            
            // Obtenir la taille du fichier pour le rapport de progression
            int fileSize = connection.getContentLength();
            
            // Configurer les streams
            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream fos = new FileOutputStream(targetPath.toFile());
                 BufferedOutputStream out = new BufferedOutputStream(fos, 1024)) {
                
                byte[] buffer = new byte[1024];
                int bytesRead;
                int totalRead = 0;
                int lastReportedProgress = 0;
                
                // Lire et écrire le fichier par blocs
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                    
                    // Rapporter la progression environ toutes les 10%
                    if (fileSize > 0) {
                        int progress = (int) ((totalRead * 100L) / fileSize);
                        if (progress >= lastReportedProgress + 10) {
                            lastReportedProgress = progress;
                            sendMessage(player, ChatColor.AQUA + "Téléchargement: " + progress + "% terminé");
                        }
                    }
                }
                
                out.flush();
            }
            
            sendMessage(player, ChatColor.GREEN + "Téléchargement terminé: " + targetPath);
            return targetPath;
            
        } catch (IOException e) {
            plugin.getLogger().severe("Erreur lors du téléchargement de PHP: " + e.getMessage());
            sendMessage(player, ChatColor.RED + "Erreur lors du téléchargement: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Extrait l'archive PHP téléchargée
     * 
     * @param archivePath Le chemin de l'archive
     * @param player Le joueur pour les notifications
     * @return true si l'extraction a réussi
     */
    private boolean extractPHP(Path archivePath, Player player) {
        try {
            // Créer le répertoire PHP s'il n'existe pas
            Path phpDir = Paths.get(plugin.getDataFolder().getAbsolutePath(), "php");
            Files.createDirectories(phpDir);
            
            String archiveName = archivePath.toString().toLowerCase();
            
            if (archiveName.endsWith(".zip")) {
                // Extraction ZIP (Windows)
                extractZip(archivePath, phpDir, player);
            } else if (archiveName.endsWith(".tar.gz") || archiveName.endsWith(".tgz")) {
                // Extraction TAR.GZ (Linux)
                // Pour simplifier, nous utilisons une approche différente pour Linux
                // Cette partie devrait être améliorée avec une vraie extraction tar.gz
                // ou utiliser une distribution PHP précompilée pour Linux
                sendMessage(player, ChatColor.YELLOW + "L'extraction automatique des fichiers tar.gz n'est pas encore implémentée.");
                sendMessage(player, ChatColor.YELLOW + "Veuillez installer PHP manuellement sur votre système Linux.");
                return false;
            } else {
                sendMessage(player, ChatColor.RED + "Format d'archive non supporté: " + archivePath);
                return false;
            }
            
            return true;
            
        } catch (IOException e) {
            plugin.getLogger().severe("Erreur lors de l'extraction de PHP: " + e.getMessage());
            sendMessage(player, ChatColor.RED + "Erreur lors de l'extraction: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Extrait une archive ZIP
     * 
     * @param zipPath Le chemin de l'archive ZIP
     * @param targetDir Le répertoire cible
     * @param player Le joueur pour les notifications
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    private void extractZip(Path zipPath, Path targetDir, Player player) throws IOException {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            int totalEntries = zipFile.size();
            int extractedEntries = 0;
            int lastReportedProgress = 0;
            
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path targetPath = targetDir.resolve(entry.getName());
                
                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    // S'assurer que le dossier parent existe
                    Files.createDirectories(targetPath.getParent());
                    
                    // Extraire le fichier
                    try (BufferedInputStream in = new BufferedInputStream(zipFile.getInputStream(entry));
                         FileOutputStream fos = new FileOutputStream(targetPath.toFile());
                         BufferedOutputStream out = new BufferedOutputStream(fos, 1024)) {
                        
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                        out.flush();
                    }
                    
                    // Conserver les permissions d'exécution pour les binaires
                    if (entry.getName().endsWith(".exe") || entry.getName().endsWith(".dll") ||
                        entry.getName().equals("php") || entry.getName().contains("/bin/")) {
                        targetPath.toFile().setExecutable(true);
                    }
                }
                
                extractedEntries++;
                int progress = (int) ((extractedEntries * 100L) / totalEntries);
                if (progress >= lastReportedProgress + 10) {
                    lastReportedProgress = progress;
                    sendMessage(player, ChatColor.AQUA + "Extraction: " + progress + "% terminée");
                }
            }
        }
    }
    
    /**
     * Détermine si le système d'exploitation est Windows
     * 
     * @return true si le système est Windows
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
    
    /**
     * Envoie un message au joueur et au journal
     * 
     * @param player Le joueur (peut être null)
     * @param message Le message à envoyer
     */
    private void sendMessage(Player player, String message) {
        // Envoyer au journal
        plugin.getLogger().info(ChatColor.stripColor(message));
        
        // Envoyer au joueur s'il est connecté
        if (player != null && player.isOnline()) {
            Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage("[SuperWatch] " + message));
        }
    }
}