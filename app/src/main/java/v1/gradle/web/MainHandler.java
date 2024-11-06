// MainHandler.java
package v1.gradle.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class MainHandler implements HttpHandler {
    private final File websiteRoot;

    public MainHandler(File websiteRoot) {
        this.websiteRoot = websiteRoot;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        
        // Default to index.html if root path
        if (path.equals("/")) {
            path = "/index.html";
        }

        File file = new File(websiteRoot, path);

        if (!file.exists() || !file.isFile()) {
            String response = "404 File not found";
            exchange.sendResponseHeaders(404, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }

        // Get content type based on file extension
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // Set content type header
        exchange.getResponseHeaders().set("Content-Type", contentType);
        
        // Send file content
        exchange.sendResponseHeaders(200, file.length());
        try (OutputStream os = exchange.getResponseBody();
             FileInputStream fs = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = fs.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
        }
    }
}