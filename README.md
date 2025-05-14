# SuperWatch - Plugin de surveillance Minecraft

SuperWatch est un plugin Bukkit/Spigot qui fournit une interface web pour surveiller les joueurs et l'état du serveur Minecraft.

## Fonctionnalités

- Interface web pour visualiser les joueurs en ligne et leur inventaire
- API REST pour accéder aux données des joueurs
- Serveur PHP intégré pour servir le site web
- Stockage des données des joueurs hors ligne
- Commandes de gestion intuitives

## Structure du projet

Le projet est organisé en packages fonctionnels :

- `com.superwatch` : Package principal contenant la classe App
- `com.superwatch.api` : Endpoints API REST
- `com.superwatch.commands` : Commandes Minecraft du plugin
- `com.superwatch.config` : Gestion de la configuration
- `com.superwatch.data` : Gestion des données des joueurs
- `com.superwatch.web` : Serveurs web HTTP et PHP
- `com.superwatch.utils` : Utilitaires divers

## Installation

1. Placez le fichier .jar dans le dossier `plugins/` de votre serveur
2. Redémarrez votre serveur ou chargez le plugin
3. Configurez le plugin en modifiant le fichier `plugins/SuperWatch/superwatch.properties`
4. Redémarrez le serveur ou rechargez la configuration avec `/superwatch reload-config`

## Configuration

Le fichier de configuration `superwatch.properties` contient les options suivantes :

```properties
# Nom du site web
site_name=SuperWatch

# Clé API (pour les utilisateurs Premium)
api_key=

# Port d'écoute pour le serveur web
web_port=8090

# Domaine personnalisé (optionnel)
domain_name=

# Dossier d'extraction du site web
web_directory=web/

# Démarrer automatiquement le serveur PHP au chargement du plugin
auto_start_php=false
```

## Commandes

- `/superwatch extract-website` - Réinstalle le site web depuis les ressources
- `/superwatch reload-config` - Recharge la configuration
- `/superwatch status` - Affiche l'état du serveur et des fichiers
- `/superwatch startphp` - Démarre ou redémarre le serveur PHP
- `/superwatch stopphp` - Arrête le serveur PHP

## API REST

Le plugin expose une API REST pour accéder aux données des joueurs :

- `GET /api/players` - Liste tous les joueurs (en ligne et hors ligne)
- `GET /api/player/{name}` - Récupère les détails d'un joueur spécifique

## Développement

### Prérequis

- Java 11 ou supérieur
- Maven
- Spigot/Bukkit 1.19 ou supérieur

### Compilation

```bash
mvn clean package
```

Le fichier JAR sera généré dans le dossier `target/`.

## Licence

Ce projet est sous licence MIT. Voir le fichier LICENSE pour plus de détails.