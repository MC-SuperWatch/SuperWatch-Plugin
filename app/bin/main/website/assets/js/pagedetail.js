// Fonction pour obtenir le nom du joueur depuis l'URL
function getPlayerNameFromURL() {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get("name");
}

// Fonction pour convertir la valeur numérique en icônes
function getIconString(value, maxValue, icon) {
  const iconCount = Math.round((value / maxValue) * 10);
  return icon.repeat(iconCount);
}

function formatDate(timestamp) {
  const date = new Date(timestamp);
  return date.toLocaleDateString("fr-FR", {
    day: "numeric",
    month: "long",
    year: "numeric",
  });
}

// Fonction pour mettre à jour l'interface utilisateur avec les données du joueur
function updatePlayerUI(playerData) {
  // Mise à jour du pseudo
  document.getElementById(
    "titre_pseudo_joueur"
  ).textContent = `Pseudo : ${playerData.name}`;

  // Mise à jour de l'UUID
  const infosJoueur = document.querySelector(".infos_joueur");
  const uuidText = `UUID : ${playerData.uuid}`;
  const statusText = `En ligne : ${
    playerData.status === "online" ? "Oui" : "Non"
  }`;

  // Calcul des niveaux avec icônes
  const protectionLevel = getIconString(20, 20, "🛡️"); // Maximum protection assumed as 20
  const healthLevel = getIconString(playerData.health, 20, "❤️");
  const foodLevel = getIconString(playerData.food, 20, "🍗");

  // Mise à jour du contenu
  infosJoueur.innerHTML = `
        <br><br><br>${uuidText}
        <br><br>${statusText}
        <br><br>Niveau de protection : ${protectionLevel}
        <br><br>Niveau de vie : ${healthLevel}
        <br><br>Niveau de faim : ${foodLevel}
        <br><br>Dernière connexion : ${formatDate(playerData.lastSeen)}
    `;

  // Mise à jour de l'image du skin
  const skinImage = document.querySelector(".img_joueur");
  if (skinImage) {
    // Vous pouvez utiliser un service de skin Minecraft comme ceci:
    skinImage.src = `https://crafatar.com/renders/body/${playerData.uuid}?overlay=true`;
  }
}

// Fonction principale pour charger les données du joueur
async function loadPlayerData() {
  const playerName = getPlayerNameFromURL();

  if (!playerName) {
    console.error("Nom du joueur non spécifié dans l'URL");
    return;
  }

  try {
    const response = await fetch(
      `http://arthonetwork.fr:8001/apiP?player=${playerName}`
    );
    if (!response.ok) {
      throw new Error("Erreur lors de la récupération des données");
    }

    const playerData = await response.json();
    updatePlayerUI(playerData);
  } catch (error) {
    console.error("Erreur:", error);
  }
}

document.addEventListener("DOMContentLoaded", loadPlayerData);
