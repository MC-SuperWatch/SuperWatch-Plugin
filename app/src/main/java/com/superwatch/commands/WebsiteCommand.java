package com.superwatch.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;

import com.superwatch.App;
import com.superwatch.web.WebsiteUpdater;

/**
 * Commande pour gérer le site web
 */
public class WebsiteCommand implements CommandHandler {

    private final App plugin;
    private final WebsiteUpdater websiteUpdater;
    private final String name = "website";
    private final String description = "Gère le site web du plugin";
    private final List<String> subCommands = Arrays.asList(
        "reset", "check", "update", "version", "set"
    );

    /**
     * Constructeur
     * 
     * @param plugin L'instance principale du plugin
     */
    public WebsiteCommand(App plugin) {
        this.plugin = plugin;
        this.websiteUpdater = new WebsiteUpdater(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            sender.sendMessage("§c[SuperWatch] Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }
        
        if (args.length <= 1) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[1].toLowerCase();
        
        switch (subCommand) {
            case "reset":
                return handleReset(sender);
            case "check":
                if (args.length > 2 && "update".equalsIgnoreCase(args[2])) {
                    return handleCheckUpdate(sender);
                } else {
                    sender.sendMessage("§c[SuperWatch] Commande inconnue: " + subCommand);
                    showHelp(sender);
                    return true;
                }
            case "update":
                return handleUpdate(sender);
            case "version":
                if (args.length > 2 && "set".equalsIgnoreCase(args[2])) {
                    if (args.length > 3) {
                        return handleVersionSet(sender, args[3]);
                    } else {
                        sender.sendMessage("§c[SuperWatch] Vous devez spécifier une version (latest ou un tag comme v0.0.9)");
                        return true;
                    }
                } else {
                    return handleVersion(sender);
                }
            default:
                sender.sendMessage("§c[SuperWatch] Commande inconnue: " + subCommand);
                showHelp(sender);
                return true;
        }
    }
    
    /**
     * Gère la commande reset
     * 
     * @param sender L'expéditeur de la commande
     * @return true pour indiquer que la commande a été traitée
     */
    private boolean handleReset(CommandSender sender) {
        sender.sendMessage("§a[SuperWatch] Réinitialisation du dossier WebSite en cours...");
        boolean success = websiteUpdater.resetWebsite();
        if (success) {
            sender.sendMessage("§a[SuperWatch] Le dossier WebSite a été réinitialisé avec succès.");
        } else {
            sender.sendMessage("§c[SuperWatch] Une erreur est survenue lors de la réinitialisation du dossier WebSite.");
        }
        return true;
    }
    
    /**
     * Gère la commande check update
     * 
     * @param sender L'expéditeur de la commande
     * @return true pour indiquer que la commande a été traitée
     */
    private boolean handleCheckUpdate(CommandSender sender) {
        sender.sendMessage("§a[SuperWatch] Vérification des mises à jour...");
        String updateInfo = websiteUpdater.checkForUpdates();
        sender.sendMessage("§a[SuperWatch] " + updateInfo);
        return true;
    }
    
    /**
     * Gère la commande update
     * 
     * @param sender L'expéditeur de la commande
     * @return true pour indiquer que la commande a été traitée
     */
    private boolean handleUpdate(CommandSender sender) {
        sender.sendMessage("§a[SuperWatch] Mise à jour du site web en cours...");
        boolean success = websiteUpdater.forceUpdate();
        if (success) {
            sender.sendMessage("§a[SuperWatch] Le site web a été mis à jour avec succès.");
        } else {
            sender.sendMessage("§c[SuperWatch] Une erreur est survenue lors de la mise à jour du site web.");
        }
        return true;
    }
    
    /**
     * Gère la commande version
     * 
     * @param sender L'expéditeur de la commande
     * @return true pour indiquer que la commande a été traitée
     */
    private boolean handleVersion(CommandSender sender) {
        String versionInfo = websiteUpdater.getVersionInfo();
        for (String line : versionInfo.split("\n")) {
            sender.sendMessage("§a[SuperWatch] " + line);
        }
        return true;
    }
    
    /**
     * Gère la commande version set
     * 
     * @param sender L'expéditeur de la commande
     * @param version La version à définir
     * @return true pour indiquer que la commande a été traitée
     */
    private boolean handleVersionSet(CommandSender sender, String version) {
        sender.sendMessage("§a[SuperWatch] Définition de la version cible à " + version + "...");
        boolean success = websiteUpdater.setTargetVersion(version);
        if (success) {
            sender.sendMessage("§a[SuperWatch] Version cible définie avec succès à " + version);
        } else {
            sender.sendMessage("§c[SuperWatch] Version invalide. Utilisez 'latest' ou un tag valide (ex: v0.0.9)");
        }
        return true;
    }
    
    /**
     * Affiche l'aide de la commande
     * 
     * @param sender Le destinataire du message
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage("§6===== §e[SuperWatch] Aide - Commandes Website §6=====");
        sender.sendMessage("§e/superwatch website reset §f- Supprime le dossier WebSite");
        sender.sendMessage("§e/superwatch website check update §f- Vérifie s'il existe une version plus récente");
        sender.sendMessage("§e/superwatch website update §f- Télécharge et installe la version spécifiée");
        sender.sendMessage("§e/superwatch website version §f- Affiche la version installée et celle ciblée");
        sender.sendMessage("§e/superwatch website version set <version> §f- Définit la version cible (latest ou un tag comme v0.0.9)");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return subCommands.stream()
                .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        } else if (args.length == 2) {
            if ("check".equalsIgnoreCase(args[0])) {
                return Arrays.asList("update").stream()
                    .filter(cmd -> cmd.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            } else if ("version".equalsIgnoreCase(args[0])) {
                return Arrays.asList("set").stream()
                    .filter(cmd -> cmd.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            if ("version".equalsIgnoreCase(args[0]) && "set".equalsIgnoreCase(args[1])) {
                return Arrays.asList("latest", "v0.0.1", "v0.0.2", "v0.0.3").stream()
                    .filter(version -> version.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
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