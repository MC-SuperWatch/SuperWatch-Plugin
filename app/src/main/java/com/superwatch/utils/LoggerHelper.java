package com.superwatch.utils;

import com.superwatch.App;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe utilitaire pour les opérations de journalisation
 */
public class LoggerHelper {
    private final Logger logger;

    /**
     * Constructeur
     * 
     * @param plugin L'instance principale du plugin
     */
    public LoggerHelper(App plugin) {
        this.logger = plugin.getLogger();
    }

    /**
     * Journalise un message d'information
     * 
     * @param message Le message à journaliser
     */
    public void info(String message) {
        logger.info(message);
    }

    /**
     * Journalise un message d'avertissement
     * 
     * @param message Le message à journaliser
     */
    public void warning(String message) {
        logger.warning(message);
    }

    /**
     * Journalise un message d'erreur
     * 
     * @param message Le message à journaliser
     */
    public void error(String message) {
        logger.severe(message);
    }

    /**
     * Journalise un message d'erreur avec exception
     * 
     * @param message Le message à journaliser
     * @param exception L'exception associée
     */
    public void error(String message, Throwable exception) {
        logger.log(Level.SEVERE, message, exception);
    }

    /**
     * Journalise un message de débogage
     * Note: Ce message ne sera journalisé que si le niveau de journalisation est FINE ou inférieur
     * 
     * @param message Le message à journaliser
     */
    public void debug(String message) {
        logger.fine(message);
    }
}