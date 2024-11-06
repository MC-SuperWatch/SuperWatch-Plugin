package v1.gradle.notify;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import v1.gradle.Update;

public class UpdateNotify implements Listener {
    private final JavaPlugin plugin;
    private final Update updateManager;

    public UpdateNotify(JavaPlugin plugin, Update updateManager) {
        this.plugin = plugin;
        this.updateManager = updateManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (player.isOp() || player.hasPermission("superwatch.update")) {
            if (updateManager.isUpdateAvailable()) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    player.sendMessage("§6[SuperWatch] §fUne nouvelle version est disponible: " + updateManager.getLatestVersion());
                    player.sendMessage("§6[SuperWatch] §fUtilisez /superwatch update pour mettre à jour le plugin.");
                }, 40L); // Délai de 2 secondes après la connexion
            }
        }
    }
}