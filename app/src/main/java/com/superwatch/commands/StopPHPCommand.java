package com.superwatch.commands;

import com.superwatch.App;
import com.superwatch.web.PHPServer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Commande pour arrêter le serveur PHP
 */
public class StopPHPCommand implements CommandHandler {

    private final App plugin;
    private final String name = "stopphp";
    private final String description = "Arrête le serveur PHP s'il est en cours d'exécution";

    /**
     * Constructeur
     * 
     * @param plugin L'instance principale du plugin
     */
    public StopPHPCommand(App plugin) {
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
            sender.sendMessage("§a[SuperWatch] Arrêt du serveur PHP...");
            phpServer.stop();
            sender.sendMessage("§a[SuperWatch] Serveur PHP arrêté avec succès.");
        } else {
            sender.sendMessage("§e[SuperWatch] Le serveur PHP n'est pas en cours d'exécution.");
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