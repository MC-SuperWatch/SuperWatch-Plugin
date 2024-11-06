fetch("http://arthonetwork.fr:8001/apiP")
  .then((response) => response.json())
  .then((data) => {
    API_Data = data; // stock les données de l'api dans une variable
    Name.classList.add("sortclicked"); // le bouton "name" est cliqué par défaut
    croissant.classList.add("sortclicked"); // le bouton "croissant" est cliqué par défaut
    sorted = 1; // permet aux fonctions de savoir qu'on tri par ordre croissant (l'autre est sortedreversed)
    sortedByName = 1; // permet aux fonctions de savoir qu'on tri par nom
    sortByName(); // appel tri par nom
    Init(API_Data); // chargement initial des joueurs
  });

/*
  API_Data={
    "mods": [],
    "software": null,
    "players": {
      "offline": 1,
      "max": 20,
      "online": 1,
      "list": {
        "TeALO36": {
          "level": 11,
          "permissions": {
            "isOp": true,
            "gameMode": "CREATIVE"
          },
          "xp": 34,
          "health": 8,
          "uuid": "f61c25f61cc74edbb5faed2b061a04ae",
          "food": 11,
          "status": "offline"
        },
        "Rudiak": {
          "level": 0,
          "permissions": {
            "isOp": true,
            "gameMode": "CREATIVE"
          },
          "xp": 0,
          "health": 20,
          "uuid": "2939f22fc68f4deca8b7c62d9de7d2e1",
          "food": 20,
          "status": "online"
        }
      }
    },
    "plugins": [],
    "icon": null
  };
  Init(API_Data);
  */

/*
setTimeout(function () {
    window.location.reload(1);
}, 5000);
*/
function Init(data) {
  // ----- Copy Pasta All js files ----- //
  const playerCounter = document.getElementById("navbar-counter"); //joueur en ligne
  playerCounter.innerHTML = data.players.online;
  const playerTotal = document.getElementById("navbar-total"); //joueur maximal autorisé par le serveur
  playerTotal.innerHTML = data.players.max;
  // ----- ----------------------------- //
  const isMobile = window.matchMedia("(max-width: 768px)").matches; // si l'écran est plus petit que 768px, on considère que l'utilisateur est sur mobile
  for (const playerName in data.players.list) {
    //repet pour chaque joueur existant dans l'api
    if (data.players.list.hasOwnProperty(playerName)) {
      const player = data.players.list[playerName];
      const parent = document.querySelector("section");

      var article = document.createElement("article");
      article.className = "etiquette";
      article.setAttribute("player-id", playerName.toLowerCase());

      var sousarticle = document.createElement("div");
      sousarticle.className = "sous-etiquette";

      var pseudo = document.createElement("p");
      pseudo.className = "username";
      pseudo.innerHTML = `${playerName}`;

      var uuid = document.createElement("p");
      uuid.className = "uuid";
      uuid.innerHTML = `UUID : ${player.uuid}`;

      var version = document.createElement("p");
      version.className = "version";
      version.innerHTML = `Version : ${player.player_version}`;

      var skin = document.createElement("img");
      skin.className = "skin";
      skin.title = `Page détaillé de ${playerName}`;
      skin.alt = `skin de ${playerName}`;

      var linkToDetailedView = document.createElement("a");
      linkToDetailedView.className = "LinkToDetailedView";
      var text = document.createElement("a");
      text.className = "text-etiquette";

      transferplayer();

      parent.appendChild(article);

      if (isMobile) {
        article.appendChild(linkToDetailedView);
        linkToDetailedView.appendChild(pseudo);
        skin.src = `https://mineskin.eu/helm/${playerName}/100.png`;
        linkToDetailedView.appendChild(skin);
        linkToDetailedView.appendChild(version);
        article.appendChild(sousarticle);
      } else {
        article.appendChild(linkToDetailedView);
        article.appendChild(sousarticle);

        skin.src = `https://mineskin.eu/armor/body/${playerName}/100.png`;
        linkToDetailedView.appendChild(skin);

        sousarticle.appendChild(text);
        text.appendChild(pseudo);
        text.appendChild(uuid);
        text.appendChild(version);
      }

      //   ------- online / op ------- //
      if (player.permissions.isOp == true) {
        pseudo.style = "color : purple";
      }
      if (player.status === "online") {
        article.style =
          "box-shadow: lime 0 0 0 3px, 0 2px 20px rgba(0, 0, 0, 1),inset 0 0 50px rgba(0, 0, 0, 0.5)";
      } else {
        article.style =
          "box-shadow: red 0 0 0 3px, 0 2px 20px rgba(0, 0, 0, 1),inset 0 0 50px rgba(0, 0, 0, 0.5)";
      }

      // ban / kick buttons
      var bottom = document.createElement("div");
      bottom.className = "bottom";
      sousarticle.append(bottom);

      // banbutton
      var banbox = document.createElement("div");
      banbox.className = `banbox${playerName}`;

      var bansousbox = document.createElement("div");
      bansousbox.className = `bansousbox${playerName}`;

      var ban = document.createElement("span");
      ban.id = `banbutton${playerName}`;
      ban.innerHTML = "ban";

      bottom.appendChild(banbox);
      banbox.appendChild(bansousbox);
      bansousbox.appendChild(ban);

      // kick button
      var kickbox = document.createElement("div");
      kickbox.className = `kickbox${playerName}`;

      var kicksousbox = document.createElement("div");
      kicksousbox.className = `kicksousbox${playerName}`;

      var kick = document.createElement("span");
      kick.innerHTML = "kick";
      kick.id = `kickbutton${playerName}`;

      bottom.appendChild(kickbox);
      kickbox.appendChild(kicksousbox);
      kicksousbox.appendChild(kick);

      var style = document.createElement("style");
      style.innerHTML = `
            .kickbox${playerName} {
                width: inherit;
                display: flex;
                justify-content: center;
                align-items: center;
                align-self: flex-end;
                padding: 5px;
                border-radius: 5px;
            }
            .kicksousbox${playerName} {
                line-height: 50px;
                height: 50px;
                text-align: center;
                width: inherit;
                cursor: pointer;
                color: #ebebeb;
                transition: all 0.5s;
                position: relative;
            }
            .kicksousbox${playerName} span {
                z-index: 998;
                display: block;
                position: absolute;
                width: inherit;
                height: inherit;
            }

            .kicksousbox${playerName}::before {
                content: '';
                position: absolute;
                top: 0;
                left: 0;
                width: inherit;
                height: inherit;
                z-index: 997;
                transition: all 0.5s;
                border: 1px solid rgba(255, 255, 255, 0.2);
                background-color: rgba(255, 255, 255, 0.1);
                border-radius: 5px;
            }

            .kicksousbox${playerName}::after {
                content: '';
                position: absolute;
                top: 0;
                left: 0;
                width: inherit;
                height: inherit;
                z-index: 997;
                transition: all 0.5s;
                border: 1px solid rgba(255, 255, 255, 0.2);
                background-color: rgba(255, 255, 255, 0.1);
                border-radius: 5px;
            }

            .kicksousbox-clicked${playerName}::before {
                transform: rotate(-225deg)  scaleX(1.2);
                background-color: rgba(255, 0, 0, 0.5);
                backdrop-filter: blur(2px);
                border-color: rgba(255, 0, 0, 0.5);
                border-radius: 0;
            }

            .kicksousbox-clicked${playerName}::after {
                transform: rotate(225deg) scaleX(1.2);
                background-color: rgba(255, 0, 0, 0.5);
                backdrop-filter: blur(2px);
                border-color: rgba(255, 0, 0, 0.5);
                border-radius: 0;
            }
            .banbox${playerName} {
                width: inherit;
                display: flex;
                justify-content: center;
                align-items: center;
                align-self: flex-end;
                padding: 5px;
                border-radius: 5px;
            }
            .bansousbox${playerName} {
                line-height: 50px;
                height: 50px;
                text-align: center;
                width: inherit;
                cursor: pointer;
                color: #ebebeb;
                transition: all 0.5s;
                position: relative;
            }
            .bansousbox${playerName} span {
                z-index: 998;
                display: block;
                position: absolute;
                width: inherit;
                height: inherit;
            }

            .bansousbox${playerName}::before {
                content: '';
                position: absolute;
                top: 0;
                left: 0;
                width: inherit;
                height: inherit;
                z-index: 997;
                transition: all 0.5s;
                border: 1px solid rgba(255, 255, 255, 0.2);
                background-color: rgba(255, 255, 255, 0.1);
                border-radius: 5px;
            }

            .bansousbox${playerName}::after {
                content: '';
                position: absolute;
                top: 0;
                left: 0;
                width: inherit;
                height: inherit;
                z-index: 997;
                transition: all 0.5s;
                border: 1px solid rgba(255, 255, 255, 0.2);
                background-color: rgba(255, 255, 255, 0.1);
                border-radius: 5px;
            }

            .bansousbox-clicked${playerName}::before {
                transform: rotate(-225deg)  scaleX(1.2);
                background-color: rgba(255, 0, 0, 0.5);
                backdrop-filter: blur(2px);
                border-color: rgba(255, 0, 0, 0.5);
                border-radius: 0;
            }

            .bansousbox-clicked${playerName}::after {
                transform: rotate(225deg) scaleX(1.2);
                background-color: rgba(255, 0, 0, 0.5);
                backdrop-filter: blur(2px);
                border-color: rgba(255, 0, 0, 0.5);
                border-radius: 0;
            }`;

      // -- kick section -- //

      document.getElementsByTagName("head")[0].appendChild(style);
      const kickbutton = document.getElementById(`kickbutton${playerName}`);
      // Variable pour compter les clics
      let kickclickCount = 0;
      // Fonction qui exécute l'action après 2 clics
      function handlekickClick() {
        kickclickCount++;
        if (kickclickCount == 1) {
          if (player.status == "offline") {
            if (isMobile) {
              kickbutton.innerText = "hors ligne";
              kickbutton.style = "font-size:small";
            } else {
              kickbutton.innerText = "Joueur hors ligne";
            }
            kickclickCount = 0;
          } else {
            if (isMobile) {
              kickbutton.innerText = `Vraiment ?`;
              kickbutton.style = "font-size:small";
            } else {
              kickbutton.innerText = `Kick ${playerName} ?`;
            }
            $(`.kicksousbox${playerName} `).toggleClass(
              `kicksousbox-clicked${playerName} `
            );
          }
        }
        if (kickclickCount === 2) {
          kickbutton.innerText = "Kicked";
          kickbutton.style = "font-size:large";

          /* -- A FAIRE -- WAITING ON API -- */

          fetch(`http://arthonetwork.fr:8001/api/kick`, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify({ uuid: player.uuid }),
          })
            .then((response) => response.json())
            .then((result) => {
              console.log(`${playerName} a été kick.`);
              //alert(`${playerName} a été kick.`);
            })
            .catch((error) => {
              console.error("Erreur, impossible de kick le joueur:", error);
              alert(`Erreur, impossible de kick ${playerName}.`);
            });

          /* -----------------------  */
        }
      }
      function resetkickButton() {
        window.setTimeout(function () {
          kickbutton.innerText = "kick";
          kickbutton.style = "font-size:extralarge";
          if (player.status == "online" && kickclickCount != 0) {
            $(`.kicksousbox${playerName}`).toggleClass(
              `kicksousbox-clicked${playerName}`
            );
            kickclickCount = 0;
          }
        }, 350);
      }
      // Ajoute les événements
      kickbutton.addEventListener("click", handlekickClick);
      kickbutton.addEventListener("mouseleave", resetkickButton);

      // -- ban section -- //

      const banbutton = document.getElementById(`banbutton${playerName}`);
      // Variable pour compter les clics
      let banclickCount = 0;
      // Fonction qui exécute l'action après 2 clics
      function handleBanClick() {
        banclickCount++;
        if (banclickCount == 1) {
          if (isMobile) {
            banbutton.innerText = `Vraiment ?`;
            banbutton.style = "font-size:small";
          } else {
            banbutton.innerText = `Ban ${playerName} ?`;
          }

          $(`.bansousbox${playerName} `).toggleClass(
            `bansousbox-clicked${playerName} `
          );
        }
        if (banclickCount === 2) {
          if (isMobile) {
            banbutton.innerText = `banni`;
            banbutton.style = "font-size:large";
          } else {
            banbutton.innerText = `${playerName} banni`;
            banbutton.style = "font-size:large";
          }

          /* -- A FAIRE -- WAITING ON API -- */

          fetch(`http://arthonetwork.fr:8001/api/ban`, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify({ uuid: player.uuid }),
          })
            .then((response) => response.json())
            .then((result) => {
              console.log(`${playerName} a été banni.`);
              //alert(`${playerName} a été banni.`);
            })
            .catch((error) => {
              console.error("Erreur, impossible de bannir le joueur:", error);
              alert(`Erreur, impossible de bannir ${playerName}.`);
            });
          /* -----------------------  */
        }
      }

      function resetBanButton() {
        if (banclickCount != 0) {
          window.setTimeout(function () {
            banbutton.innerText = "ban";
            banbutton.style = "font-size:extralarge";
            $(`.bansousbox${playerName}`).toggleClass(
              `bansousbox-clicked${playerName}`
            );
            banclickCount = 0;
          }, 350);
        }
      }
      banbutton.addEventListener("click", handleBanClick);
      banbutton.addEventListener("mouseleave", resetBanButton);

      // ----- Mobile Specific ----- //

      if (isMobile) {
        $(document).on("scroll", function () {
          if (banclickCount != 0) {
            banbutton.innerText = "ban";
            banbutton.style = "font-size:large";
            $(`.bansousbox${playerName}`).toggleClass(
              `bansousbox-clicked${playerName}`
            );
            banclickCount = 0;
          }
          kickbutton.innerText = "kick";
          kickbutton.style = "font-size:large";
          if (player.status == "online" && kickclickCount != 0) {
            $(`.kicksousbox${playerName}`).toggleClass(
              `kicksousbox-clicked${playerName}`
            );
            kickclickCount = 0;
          }
        });
      }
      //------- link beetween overview page and detailed page ------/
      function transferplayer() {
        url = "./player.html?name=" + encodeURIComponent(playerName);
        linkToDetailedView.href = url;
        text.href = url;
      }
    }
  }
}
// ------------- sorter ------------------ //

var sortedByName, sortedByOnline, sortedByCheater, sorted, sortedReversed;

const Name = document.getElementById("name");
const online = document.getElementById("online");
const cheating = document.getElementById("cheating");
const croissant = document.getElementById("croissant");
const decroissant = document.getElementById("decroissant");

Name.addEventListener("click", sortname);
online.addEventListener("click", sortonline);
cheating.addEventListener("click", sortcheating);
croissant.addEventListener("click", sortcroissant);
decroissant.addEventListener("click", sortdecroissant);

function sortname() {
  Name.classList.add("sortclicked");
  online.classList.remove("sortclicked");
  cheating.classList.remove("sortclicked");
  if (sorted == 1) {
    sortByName();
  } else if (sortedReversed == 1) {
    sortByNameReverse();
  }
  sortedByName = 1;
  sortedByOnline = 0;
  sortedByCheater = 0;
  reloadPlayers();
}
function sortonline() {
  Name.classList.remove("sortclicked");
  online.classList.add("sortclicked");
  cheating.classList.remove("sortclicked");
  if (sorted == 1) {
    sortByOnline();
  } else if (sortedReversed == 1) {
    sortByOnlineReverse();
  }
  sortedByName = 0;
  sortedByOnline = 1;
  sortedByCheater = 0;
  reloadPlayers();
}
function sortcheating() {
  Name.classList.remove("sortclicked");
  online.classList.remove("sortclicked");
  cheating.classList.add("sortclicked");
  sortedByName = 0;
  sortedByOnline = 0;
  sortedByCheater = 1;
  reloadPlayers();
}

function sortcroissant() {
  croissant.classList.add("sortclicked");
  decroissant.classList.remove("sortclicked");
  if (sortedByName == 1) {
    sortByName();
  } else if (sortedByOnline == 1) {
    sortByOnline();
  }
  //else if(sortedByCheater==1){sortByCheater();}
  sortedReversed = 0;
  sorted = 1;
  reloadPlayers();
}
function sortdecroissant() {
  croissant.classList.remove("sortclicked");
  decroissant.classList.add("sortclicked");
  if (sortedByName == 1) {
    sortByNameReverse();
  } else if (sortedByOnline == 1) {
    sortByOnlineReverse();
  }
  //else if(sortedByCheater==1){sortByCheaterReverse();}
  sortedReversed = 1;
  sorted = 0;
  reloadPlayers();
}

function sortByName() {
  const nameList = Object.keys(API_Data.players.list)
    .sort()
    .reduce((acc, key) => {
      acc[key] = API_Data.players.list[key];
      return acc;
    }, {});
  API_Data.players.list = nameList;
}
function sortByNameReverse(functionName) {
  const reverseNameList = Object.keys(API_Data.players.list)
    .sort()
    .reverse()
    .reduce((acc, key) => {
      acc[key] = API_Data.players.list[key];
      return acc;
    }, {});
  API_Data.players.list = reverseNameList;
}

function sortByOnline() {
  const sortedPlayers = Object.entries(API_Data.players.list)
    .sort(([playerA, dataA], [playerB, dataB]) => {
      if (dataA.status === "online" && dataB.status === "offline") {
        return -1; // Place 'online' avant 'offline'
      } else if (dataA.status === "offline" && dataB.status === "online") {
        return 1; // Place 'offline' après 'online'
      } else {
        return 0; // Garde l'ordre initial si les deux sont dans le même état
      }
    })
    .reduce((acc, [playerName, playerData]) => {
      acc[playerName] = playerData; // Reconstruire l'objet avec les joueurs triés
      return acc;
    }, {});

  API_Data.players.list = sortedPlayers;
}

function sortByOnlineReverse() {
  const sortedPlayers = Object.entries(API_Data.players.list)
    .sort(([playerA, dataA], [playerB, dataB]) => {
      if (dataA.status === "online" && dataB.status === "offline") {
        return -1; // Place 'online' avant 'offline'
      } else if (dataA.status === "offline" && dataB.status === "online") {
        return 1; // Place 'offline' après 'online'
      } else {
        return 0; // Garde l'ordre initial si les deux sont dans le même état
      }
    })
    .reverse()
    .reduce((acc, [playerName, playerData]) => {
      acc[playerName] = playerData; // Reconstruire l'objet avec les joueurs triés
      return acc;
    }, {});

  API_Data.players.list = sortedPlayers;
}

function reloadPlayers() {
  const parent = document.querySelector("section");
  parent.innerHTML = "";
  Init(API_Data);
  searchPlayers();
}

// --------------- research ------------------ //

function searchPlayers() {
  const input = document.getElementById("search").value.toLowerCase();
  const players = document.querySelectorAll(".playersection article");

  players.forEach(function (player) {
    const playerId = player.getAttribute("player-id");
    if (playerId.includes(input)) {
      player.style.display = "";
    } else {
      player.style.display = "none";
    }
  });
}
