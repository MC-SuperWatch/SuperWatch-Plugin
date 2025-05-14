package com.superwatch.commands;

import com.superwatch.App;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Commande pour gérer la configuration
 */
public class ConfigCommandHandler implements CommandHandler {

    private final App plugin;
    private final String name = "reload-config";
    private final String description = "Recharge la configuration du plugin";

    /**
     * Constructeur
     * 
     * @param plugin L'instance principale du plugin
     */
    public ConfigCommandHandler(App plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            sender.sendMessage("§c[SuperWatch] Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        plugin.reloadConfig();
        
        // Redémarrer les services si nécessaire
        plugin.restartServices();
        
        sender.sendMessage("§a[SuperWatch] Configuration rechargée avec succès.");
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