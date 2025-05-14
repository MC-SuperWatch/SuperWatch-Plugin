package com.superwatch.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.superwatch.App;
import com.superwatch.web.PHPServer;

/**
 * Commande pour afficher l'état du serveur
 */
public class StatusCommandHandler implements CommandHandler {

    private final App plugin;
    private final String name = "status";
    private final String description = "Affiche l'état du serveur web et des fichiers de configuration";

    /**
     * Constructeur
     * 
     * @param plugin L'instance principale du plugin
     */
    public StatusCommandHandler(App plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            sender.sendMessage("§c[SuperWatch] Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        // Afficher les informations sur le serveur web
        String siteName = plugin.getConfigManager().getString("site_name", "SuperWatch");
        int webPort = plugin.getConfigManager().getInt("web_port", 8080);
        String webDirectory = plugin.getConfigManager().getString("web_directory", "web/");
        String domainName = plugin.getConfigManager().getString("domain_name", "");
        
        sender.sendMessage("§6===== §e[SuperWatch] Status §6=====");
        sender.sendMessage("§bNom du site: §f" + siteName);
        sender.sendMessage("§bPort HTTP: §f" + webPort);
        
        // Afficher le port PHP
        int phpPort = plugin.getConfigManager().getInt("php_port", 9000);
        sender.sendMessage("§bPort PHP: §f" + phpPort);
        
        if (!domainName.isEmpty()) {
            sender.sendMessage("§bDomaine personnalisé: §f" + domainName);
        }
        
        // Vérifier l'existence du répertoire web
        File webDir = new File(plugin.getDataFolder(), webDirectory);
        if (webDir.exists() && webDir.isDirectory()) {
            int filesCount = 0;
            if (webDir.listFiles() != null) {
                filesCount = webDir.listFiles().length;
            }
            sender.sendMessage("§bRépertoire web: §a✓ §f" + webDir.getAbsolutePath() + " (" + filesCount + " fichiers)");
        } else {
            sender.sendMessage("§bRépertoire web: §c✗ §f" + webDir.getAbsolutePath() + " (non trouvé)");
        }
        
        // Vérifier s'il y a un ancien dossier website
        File oldWebDir = new File(plugin.getDataFolder(), "website");
        if (oldWebDir.exists() && oldWebDir.isDirectory() && !oldWebDir.equals(webDir)) {
            int filesCount = 0;
            if (oldWebDir.listFiles() != null) {
                filesCount = oldWebDir.listFiles().length;
            }
            sender.sendMessage("§bAncien répertoire website: §e! §f" + oldWebDir.getAbsolutePath() + " (" + filesCount + " fichiers)");
            sender.sendMessage("§c  Attention: Ce dossier n'est plus utilisé, utilisez " + webDirectory + " à la place.");
        }
        
        // Vérifier l'existence du fichier de configuration
        File configFile = new File(plugin.getDataFolder(), "superwatch.properties");
        if (configFile.exists()) {
            sender.sendMessage("§bFichier de configuration: §a✓ §f" + configFile.getAbsolutePath());
        } else {
            sender.sendMessage("§bFichier de configuration: §c✗ §f" + configFile.getAbsolutePath() + " (non trouvé)");
        }
        
        // Vérifier l'installation de PHP
        File phpDir = new File(plugin.getDataFolder(), "php");
        
        if (phpDir.exists() && phpDir.isDirectory()) {
            File phpExecutable = null;
            
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                phpExecutable = new File(phpDir, "php.exe");
            } else {
                phpExecutable = new File(phpDir, "bin/php");
            }
            
            if (phpExecutable.exists() && phpExecutable.canExecute()) {
                sender.sendMessage("§bPHP: §a✓ §f" + phpExecutable.getAbsolutePath());
            } else {
                sender.sendMessage("§bPHP: §c✗ §fNon trouvé ou non exécutable");
            }
        } else {
            sender.sendMessage("§bPHP: §c✗ §fNon installé");
        }
        
        // Afficher l'état du serveur PHP
        PHPServer phpServer = plugin.getPHPServer();
        if (phpServer != null) {
            String status = phpServer.isRunning() ? "§a✓ En cours d'exécution" : "§c✗ Arrêté";
            sender.sendMessage("§bServeur PHP: §f" + status);
        } else {
            sender.sendMessage("§bServeur PHP: §c✗ Non initialisé");
        }
        
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>(); // Pas de complétion spécifique pour cette commande
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.isOp() || sender.hasPermission("superwatch.admin");
    }
}