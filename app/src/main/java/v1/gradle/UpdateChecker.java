package v1.gradle;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateChecker {
    private static final String GITHUB_API_URL = "https://api.github.com/repos/TeALO36/SuperWatch/releases/latest";

    public static void checkForUpdates(JavaPlugin plugin) {
        String currentVersion = "v" + plugin.getDescription().getVersion();
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(GITHUB_API_URL)
                        .header("Accept", "application/vnd.github.v3+json")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        JsonObject json = new Gson().fromJson(responseBody, JsonObject.class);
                        String latestVersion = json.get("name").getAsString();

                        // Normaliser les versions pour la comparaison
                        String normalizedCurrentVersion = normalizeVersion(currentVersion);
                        String normalizedLatestVersion = normalizeVersion(latestVersion);

                        plugin.getLogger().info("Version actuelle: " + normalizedCurrentVersion);
                        plugin.getLogger().info("Dernière version: " + normalizedLatestVersion);

                        if (!normalizedCurrentVersion.equals(normalizedLatestVersion)) {
                            plugin.getLogger().warning("Une nouvelle version du plugin SuperWatch est disponible : " + latestVersion);
                            plugin.getLogger().warning("Téléchargez-la sur https://github.com/TeALO36/SuperWatch/releases");
                        } else {
                            plugin.getLogger().info("Votre plugin SuperWatch est à jour (version " + currentVersion + ")");
                        }
                    } else {
                        plugin.getLogger().severe("Erreur lors de la vérification des mises à jour : " + 
                            (response.code() + " " + response.message()));
                    }
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Erreur lors de la vérification des mises à jour : " + e.getMessage());
            }
        });
    }

    private static String normalizeVersion(String version) {
        // Supprime les préfixes "v" ou "V" et normalise la version
        return version.toLowerCase().replaceAll("^[vV]", "").trim();
    }
}