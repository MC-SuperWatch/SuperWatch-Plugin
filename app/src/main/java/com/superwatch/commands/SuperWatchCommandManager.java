package com.superwatch.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.superwatch.App;

/**
 * Gestionnaire principal des commandes
 */
public class SuperWatchCommandManager implements CommandExecutor, TabCompleter {

    private final App plugin;
    private final Map<String, CommandHandler> commands = new HashMap<>();

    /**
     * Constructeur
     * 
     * @param plugin L'instance principale du plugin
     */
    public SuperWatchCommandManager(App plugin) {
        this.plugin = plugin;
        
        // Enregistrer les commandes
        registerCommand(new WebsiteCommand(plugin));
        registerCommand(new ConfigCommandHandler(plugin));
        registerCommand(new StatusCommandHandler(plugin));
        registerCommand(new StartPHPCommand(plugin));
        registerCommand(new StopPHPCommand(plugin));
        
        // Ajouter d'autres commandes ici au besoin
    }

    /**
     * Enregistre une commande
     * 
     * @param handler Le gestionnaire de commande
     */
    public void registerCommand(CommandHandler handler) {
        commands.put(handler.getName().toLowerCase(), handler);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Aucun argument, afficher l'aide
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        CommandHandler handler = commands.get(subCommand);
        
        if (handler != null) {
            // Transmettre la commande au gestionnaire approprié
            return handler.execute(sender, args);
        } else {
            // Commande inconnue
            sender.sendMessage("§c[SuperWatch] Commande inconnue : " + subCommand);
            showHelp(sender);
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Suggestions pour la première partie de la commande
            String prefix = args[0].toLowerCase();
            return commands.entrySet().stream()
                .filter(entry -> entry.getValue().hasPermission(sender))
                .map(Map.Entry::getKey)
                .filter(cmd -> cmd.startsWith(prefix))
                .collect(Collectors.toList());
        } else if (args.length > 1) {
            // Transmettre au gestionnaire approprié
            CommandHandler handler = commands.get(args[0].toLowerCase());
            if (handler != null && handler.hasPermission(sender)) {
                // Créer un nouveau tableau sans le premier élément
                String[] subArgs = new String[args.length - 1];
                System.arraycopy(args, 1, subArgs, 0, args.length - 1);
                return handler.tabComplete(sender, subArgs);
            }
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Affiche l'aide de la commande
     * 
     * @param sender Le destinataire du message
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage("§6===== §e[SuperWatch] Aide §6=====");
        
        for (CommandHandler handler : commands.values()) {
            if (handler.hasPermission(sender)) {
                sender.sendMessage("§e/superwatch " + handler.getName() + " §f- " + handler.getDescription());
            }
        }
    }
}