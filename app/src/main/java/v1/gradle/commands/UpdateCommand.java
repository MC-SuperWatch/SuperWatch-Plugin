package v1.gradle.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import v1.gradle.Update;

public class UpdateCommand implements CommandExecutor {
    private final Update updateManager;

    public UpdateCommand(Update updateManager) {
        this.updateManager = updateManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("superwatch")) {
            return false;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("update")) {
            sender.sendMessage("§6[SuperWatch] §fUtilisation: /superwatch update");
            return true;
        }

        // Vérifier les permissions
        if (!sender.hasPermission("superwatch.update")) {
            sender.sendMessage("§c[SuperWatch] Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }

        // Exécuter la mise à jour
        sender.sendMessage("§6[SuperWatch] §fVérification des mises à jour...");
        updateManager.checkAndUpdate(sender);
        
        return true;
    }
}
