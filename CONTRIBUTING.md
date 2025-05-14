# Guide de contribution à SuperWatch

Merci de votre intérêt pour contribuer au projet SuperWatch ! Ce document fournit des directives pour contribuer efficacement au projet.

## Structure du projet

Le code est organisé en packages fonctionnels :

- `com.superwatch` : Package principal
- `com.superwatch.api` : Endpoints API REST
- `com.superwatch.commands` : Commandes Minecraft
- `com.superwatch.config` : Gestion de la configuration
- `com.superwatch.data` : Gestion des données des joueurs
- `com.superwatch.web` : Serveurs web HTTP et PHP
- `com.superwatch.utils` : Utilitaires divers

## Style de code

- Utilisez des indentations de 4 espaces (pas de tabulations)
- Suivez les conventions de nommage Java standard
- Commentez votre code avec des commentaires Javadoc
- Utilisez des messages de commit clairs et descriptifs

## Soumettre des modifications

1. Fork le projet
2. Créez une branche pour votre fonctionnalité (`git checkout -b feature/amazing-feature`)
3. Committez vos changements (`git commit -m 'Ajout d'une fonctionnalité incroyable'`)
4. Poussez vers votre branche (`git push origin feature/amazing-feature`)
5. Ouvrez une Pull Request

## Normes de qualité

- Écrivez des tests unitaires pour les nouvelles fonctionnalités
- Assurez-vous que tous les tests passent avant de soumettre une PR
- Vérifiez la compatibilité avec les versions de Minecraft supportées

## Processus de développement

1. Choisissez une issue sur laquelle travailler ou créez-en une nouvelle
2. Discutez des changements majeurs avant de les implémenter
3. Suivez le style du code existant
4. Documentez les nouvelles fonctionnalités dans le README.md

## Ressources

- [Documentation Bukkit/Spigot](https://hub.spigotmc.org/javadocs/bukkit/)
- [Wiki du projet](https://github.com/votre-utilisateur/superwatch/wiki) (si disponible)

Merci pour votre contribution au projet SuperWatch !