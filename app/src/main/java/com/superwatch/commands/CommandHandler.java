package com.superwatch.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Interface pour les gestionnaires de commandes
 */
public interface CommandHandler {
    
    /**
     * Traite une commande
     * 
     * @param sender L'expéditeur de la commande
     * @param args Les arguments de la commande
     * @return true si la commande a été traitée, false sinon
     */
    boolean execute(CommandSender sender, String[] args);
    
    /**
     * Complète une commande (Tab completion)
     * 
     * @param sender L'expéditeur de la commande
     * @param args Les arguments de la commande
     * @return Une liste de suggestions pour compléter la commande
     */
    List<String> tabComplete(CommandSender sender, String[] args);
    
    /**
     * Obtient le nom de la commande
     * 
     * @return Le nom de la commande
     */
    String getName();
    
    /**
     * Obtient la description de la commande
     * 
     * @return La description de la commande
     */
    String getDescription();
    
    /**
     * Vérifie si l'expéditeur a la permission d'utiliser cette commande
     * 
     * @param sender L'expéditeur de la commande
     * @return true si l'expéditeur a la permission, false sinon
     */
    boolean hasPermission(CommandSender sender);
}