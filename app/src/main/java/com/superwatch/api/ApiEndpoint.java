package com.superwatch.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Interface de base pour tous les endpoints API
 */
public abstract class ApiEndpoint implements HttpHandler {

    /**
     * Méthode à implémenter par chaque endpoint pour traiter les requêtes
     * 
     * @param exchange L'échange HTTP
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    public abstract void handleRequest(HttpExchange exchange) throws IOException;
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            handleRequest(exchange);
        } catch (Exception e) {
            // En cas d'erreur, envoyer une réponse d'erreur 500
            String errorResponse = "{\"error\":\"" + e.getMessage() + "\"}";
            sendJsonResponse(exchange, errorResponse, 500);
        }
    }
    
    /**
     * Envoie une réponse JSON
     * 
     * @param exchange L'échange HTTP
     * @param response La réponse en format JSON
     * @param statusCode Le code de statut HTTP
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    protected void sendJsonResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}