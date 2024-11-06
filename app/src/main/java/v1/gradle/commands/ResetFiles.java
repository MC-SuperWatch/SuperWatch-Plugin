package v1.gradle.commands;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import v1.gradle.tools.ExtractResource;

public class ResetFiles implements CommandExecutor {
    private final JavaPlugin plugin;
    private final ExtractResource resourceExtractor;

    public ResetFiles(JavaPlugin plugin, ExtractResource resourceExtractor) {
        this.plugin = plugin;
        this.resourceExtractor = resourceExtractor;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("superwatch")) {
            return false;
        }

        if (args.length < 1 || !args[0].equalsIgnoreCase("reset")) {
            return false;
        }

        // Vérifier les permissions
        if (!sender.hasPermission("superwatch.reset")) {
            sender.sendMessage("§c[SuperWatch] Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }

        // Confirmer la réinitialisation
        if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
            sender.sendMessage("§6[SuperWatch] §fAttention! Cette commande va réinitialiser tous les fichiers du plugin.");
            sender.sendMessage("§6[SuperWatch] §fPour confirmer, tapez: /superwatch reset confirm");
            return true;
        }

        // Exécuter la réinitialisation
        sender.sendMessage("§6[SuperWatch] §fDébut de la réinitialisation des fichiers...");
        
        try {
            // Supprimer le dossier SuperWatch
            File superWatchDir = new File(plugin.getDataFolder().getParentFile(), "SuperWatch");
            if (superWatchDir.exists()) {
                deleteDirectory(superWatchDir);
            }

            // Réextraire tous les fichiers
            resourceExtractor.checkAndExtractResources();
            
            sender.sendMessage("§a[SuperWatch] §fTous les fichiers ont été réinitialisés avec succès!");
            
        } catch (Exception e) {
            sender.sendMessage("§c[SuperWatch] §fUne erreur est survenue lors de la réinitialisation: " + e.getMessage());
            plugin.getLogger().severe("Erreur lors de la réinitialisation des fichiers: " + e.getMessage());
        }

        return true;
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}