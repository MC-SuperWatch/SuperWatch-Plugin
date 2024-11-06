package v1.gradle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Update {
    private static final String GITHUB_API_URL = "https://api.github.com/repos/TeALO36/SuperWatch/releases/latest";
    private final JavaPlugin plugin;
    private final OkHttpClient client;
    private String latestVersion;
    private boolean updateAvailable = false;

    public Update(JavaPlugin plugin) {
        this.plugin = plugin;
        this.client = new OkHttpClient();
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void checkForUpdates() {
        checkAndUpdate(null);
    }

    public void checkAndUpdate(CommandSender sender) {
        String currentVersion = plugin.getDescription().getVersion();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Request request = new Request.Builder()
                        .url(GITHUB_API_URL)
                        .header("Accept", "application/vnd.github.v3+json")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        sendMessage(sender, "§cErreur lors de la vérification des mises à jour.");
                        return;
                    }

                    JsonObject json = new Gson().fromJson(response.body().string(), JsonObject.class);
                    latestVersion = json.get("name").getAsString();
                    String downloadUrl = json.getAsJsonArray("assets")
                            .get(0)
                            .getAsJsonObject()
                            .get("browser_download_url")
                            .getAsString();

                    if (!normalizeVersion(currentVersion).equals(normalizeVersion(latestVersion))) {
                        updateAvailable = true;
                        sendMessage(sender, "§6Nouvelle version disponible: " + latestVersion);
                        if (sender != null && sender.hasPermission("superwatch.update")) {
                            downloadUpdate(downloadUrl, sender);
                        }
                    } else {
                        updateAvailable = false;
                        sendMessage(sender, "§aVotre plugin est à jour! (Version: " + currentVersion + ")");
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Erreur lors de la vérification des mises à jour: " + e.getMessage());
                if (sender != null) {
                    sender.sendMessage("§c[SuperWatch] Erreur lors de la vérification des mises à jour.");
                }
            }
        });
    }

    private void downloadUpdate(String downloadUrl, CommandSender sender) {
        try {
            sendMessage(sender, "§6Téléchargement de la mise à jour...");
            
            Request request = new Request.Builder()
                    .url(downloadUrl)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    sendMessage(sender, "§cErreur lors du téléchargement de la mise à jour.");
                    return;
                }

                File pluginsFolder = plugin.getDataFolder().getParentFile();
                
                // Supprimer l'ancienne version si elle existe
                File[] files = pluginsFolder.listFiles((dir, name) -> 
                    name.startsWith("SuperWatch-") && name.endsWith(".jar"));
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }

                // Créer le nouveau fichier avec la version dans le nom
                File newJar = new File(pluginsFolder, "SuperWatch-" + latestVersion + ".jar");

                // Télécharger le nouveau fichier
                try (InputStream in = response.body().byteStream();
                     FileOutputStream out = new FileOutputStream(newJar)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }

                sendMessage(sender, "§aMise à jour téléchargée avec succès!");
                sendMessage(sender, "§6Redémarrez le serveur pour appliquer la mise à jour.");

            }
        } catch (Exception e) {
            sendMessage(sender, "§cErreur lors du téléchargement de la mise à jour: " + e.getMessage());
        }
    }

    private String normalizeVersion(String version) {
        return version.toLowerCase().replaceAll("^[vV]", "").trim();
    }

    private void sendMessage(CommandSender sender, String message) {
        if (sender != null) {
            sender.sendMessage("§6[SuperWatch] §f" + message);
        }
        plugin.getLogger().info(message);
    }
}
