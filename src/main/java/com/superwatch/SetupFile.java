public class SetupFile {
    private File websiteDir;
    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private HttpServerManager httpServerManager;
    private EventListeners eventListeners;

    public void Resource(){

        LOGGER.info("superwatch enabled");
        websiteDir = new File(getDataFolder(), "website");

        configManager = new ConfigManager(this);
        playerDataManager = new PlayerDataManager(this);
        httpServerManager = new HttpServerManager(this, configManager, playerDataManager);
        eventListeners = new EventListeners(this, playerDataManager);


        extractResource("index.html", new File(websiteDir, "index.html"));
        extractResource("styles.css", new File(websiteDir, "assets/css/styles.css"));
        extractResource("navbar.js", new File(websiteDir, "assets/js/navbar.js"));
        extractResource("apimain.js", new File(websiteDir, "assets/js/apimain.js"));
        extractResource("pagedetail.js", new File(websiteDir, "assets/js/pagedetail.js"));
        extractResource("skin.js", new File(websiteDir, "assets/js/skin.js"));
        extractResource("ico.png", new File(websiteDir, "assets/img/ico.png"));
        extractResource("body.png", new File(websiteDir, "assets/img/body.png"));
        extractResource("config.properties", new File(getDataFolder(), "config.properties"));



        configManager.loadConfig();
        playerDataManager.loadPlayerData();
        httpServerManager.startServer();
    }

    private void extractResource(String resourceName, File destination) {
        if (!destination.exists()) {
            destination.getParentFile().mkdirs();
            try (InputStream in = getResource(resourceName)) {
                if (in != null) {
                    Files.copy(in, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.info("Extracted " + resourceName + " to " + destination.getPath());
                } else {
                    LOGGER.severe("Resource not found: " + resourceName);
                }
            } catch (IOException e) {
                LOGGER.severe("Failed to extract resource: " + resourceName);
            }
        } else {
            LOGGER.info(resourceName + " already exists, skipping extraction.");
        }
    }
}