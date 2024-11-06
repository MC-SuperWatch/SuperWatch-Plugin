package v1.gradle.web.api;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import v1.gradle.tools.PlayerDataManager;

public class ApiServerHandler implements HttpHandler {
    private final Plugin plugin;
    private final PlayerDataManager playerDataManager;

    public ApiServerHandler(Plugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        JSONObject jsonResponse = new JSONObject();
        
        addMemoryInfo(jsonResponse);
        addCpuInfo(jsonResponse);
        addPlayerInfo(jsonResponse);
        addServerInfo(jsonResponse);
        addPluginInfo(jsonResponse);

        sendJsonResponse(exchange, jsonResponse);
    }

    private void addMemoryInfo(JSONObject jsonResponse) {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);
        
        JSONObject memory = new JSONObject();
        memory.put("used", usedMemory);
        memory.put("max", maxMemory);
        jsonResponse.put("memory", memory);
    }

    private void addCpuInfo(JSONObject jsonResponse) {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = osBean.getSystemLoadAverage();
        jsonResponse.put("cpuLoad", cpuLoad);
    }

    private void addPlayerInfo(JSONObject jsonResponse) {
        JSONObject players = new JSONObject();
        players.put("online", playerDataManager.getOnlineCount());
        players.put("total", playerDataManager.getOnlineCount());
        jsonResponse.put("players", players);
    }

    private void addServerInfo(JSONObject jsonResponse) {
        jsonResponse.put("serverVersion", Bukkit.getVersion());
    }

    private void addPluginInfo(JSONObject jsonResponse) {
        JSONObject plugins = new JSONObject();
        for (org.bukkit.plugin.Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            plugins.put(plugin.getName(), plugin.getDescription().getVersion());
        }
        jsonResponse.put("plugins", plugins);
    }

    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJsonResponse(HttpExchange exchange, JSONObject jsonResponse) throws IOException {
        String response = jsonResponse.toJSONString();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}