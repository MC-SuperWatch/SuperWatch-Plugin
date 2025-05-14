package com.superwatch.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.superwatch.App;
import com.superwatch.web.PHPServer;

/**
 * Commande pour démarrer le serveur PHP
 */
public class StartPHPCommand implements CommandHandler {

    private final App plugin;
    private final String name = "startphp";
    private final String description = "Démarre ou redémarre le serveur PHP";

    /**
     * Constructeur
     * 
     * @param plugin L'instance principale du plugin
     */
    public StartPHPCommand(App plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            sender.sendMessage("§c[SuperWatch] Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        PHPServer phpServer = plugin.getPHPServer();
        
        if (phpServer == null) {
            sender.sendMessage("§c[SuperWatch] Erreur: Le serveur PHP n'est pas initialisé.");
            return true;
        }
        
        if (phpServer.isRunning()) {
            sender.sendMessage("§e[SuperWatch] Le serveur PHP est déjà en cours d'exécution. Redémarrage...");
            phpServer.restart();
        } else {
            sender.sendMessage("§a[SuperWatch] Démarrage du serveur PHP...");
            phpServer.start();
        }
        
        if (phpServer.isRunning()) {
            int port = phpServer.getPort();
            sender.sendMessage("§a[SuperWatch] Serveur PHP démarré avec succès sur le port " + port);
        } else {
            sender.sendMessage("§c[SuperWatch] Échec du démarrage du serveur PHP. Consultez les logs pour plus d'informations.");
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