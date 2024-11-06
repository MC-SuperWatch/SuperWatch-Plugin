package v1.gradle;

import org.bukkit.plugin.java.JavaPlugin;

import v1.gradle.commands.ResetFiles;
import v1.gradle.commands.UpdateCommand;
import v1.gradle.notify.UpdateNotify;
import v1.gradle.tools.ExtractResource;
import v1.gradle.tools.PlayerDataManager;
import v1.gradle.web.HttpServerManager;

public class App extends JavaPlugin {
    private Update updateManager;
    private ExtractResource resourceExtractor;
    private HttpServerManager httpServerManager;
    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        getLogger().info("Plugin activé !");

        // Initialiser le gestionnaire d'extraction
        resourceExtractor = new ExtractResource(this);

        // Vérifier si les fichiers requis existent, les extraire uniquement si nécessaire
        if (!resourceExtractor.checkRequiredFiles()) {
            getLogger().info("Fichiers manquants détectés, extraction en cours...");
            resourceExtractor.extractMissingResources();
        }

        playerDataManager = new PlayerDataManager(this);


        // Initialiser le gestionnaire de mise à jour
        updateManager = new Update(this);
        
        // Vérifier les mises à jour au démarrage
        updateManager.checkForUpdates();
        
        // Initialiser et démarrer le serveur HTTP
        httpServerManager = new HttpServerManager(this);
        httpServerManager.startServer();

        UpdateCommand updateCommand = new UpdateCommand(updateManager);
        ResetFiles resetCommand = new ResetFiles(this, resourceExtractor);
        
        // Créer un CommandExecutor qui gère les deux commandes
        getCommand("superwatch").setExecutor((sender, command, label, args) -> {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("update")) {
                    return updateCommand.onCommand(sender, command, label, args);
                } else if (args[0].equalsIgnoreCase("reset")) {
                    return resetCommand.onCommand(sender, command, label, args);
                }
            }
            // Message d'aide si aucune sous-commande valide n'est fournie
            sender.sendMessage("§6[SuperWatch] §fCommandes disponibles:");
            sender.sendMessage("§6/superwatch update §f- Vérifie et installe les mises à jour");
            sender.sendMessage("§6/superwatch reset §f- Réinitialise les fichiers du plugin");
            return true;
        });
        
        // Enregistrer le listener de notification
        getServer().getPluginManager().registerEvents(new UpdateNotify(this, updateManager), this);
    }

    @Override
    public void onDisable() {
        // Arrêter le serveur HTTP
        if (httpServerManager != null) {
            httpServerManager.stopServer();
        }
        getLogger().info("Plugin désactivé !");
    }
}