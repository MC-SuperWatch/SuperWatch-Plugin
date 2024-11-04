package com.superwatch;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.logging.Logger;

public class ConfigManager {
    private final Plugin plugin;
    private final Logger LOGGER;
    private int port;
    private String dns;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.LOGGER = plugin.getLogger();
    }

    public void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.properties");
        Properties config = new Properties();

        if (!configFile.exists()) {
            try (InputStream in = plugin.getResource("config.properties")) {
                if (in != null) {
                    Files.copy(in, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.info("Default config.properties created.");
                } else {
                    LOGGER.severe("Default config.properties not found.");
                }
            } catch (IOException e) {
                LOGGER.severe("Failed to create config.properties: " + e.getMessage());
            }
        }

        try (FileInputStream fis = new FileInputStream(configFile)) {
            config.load(fis);
            port = Integer.parseInt(config.getProperty("port", "8001"));
            dns = config.getProperty("dns");
            if (dns != null && dns.trim().isEmpty()) {
                dns = null;
            }
            LOGGER.info("Config loaded. HTTP Server will run on port: " + port);
            if (dns != null) {
                LOGGER.info("Using DNS: " + dns);
            } else {
                LOGGER.info("Using public IP address");
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to load config.properties: " + e.getMessage());
        } catch (NumberFormatException e) {
            LOGGER.severe("Invalid port number in config.properties. Using default port 8001.");
            port = 8001;
        }
    }

    public int getPort() {
        return port;
    }

    public String getDns() {
        return dns;
    }
}