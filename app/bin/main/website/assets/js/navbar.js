const menuBtn = document.getElementById("menu-btn"); // Sélection du bouton du menu
const overlayMenu = document.getElementById("overlay-menu"); // Sélection de l'overlay menu
const overlayBg = document.getElementById("overlay-bg"); // Sélection de l'overlay background
let isMenuOpen = false; // Variable de suivi pour l'état du menu

const searchBar = document.getElementById("searchbar");
const searchClick = document.getElementById("searchclick");
const navbar = document.getElementById("navbar-center");
const searchicon = document.getElementById("searchicon");
let isSearchOpen = false;

const sorterclick = document.getElementById("sorticon");
const sorterMenu = document.getElementById("sortergroup");
let isSorterOpen = false;

const isMobile = window.matchMedia("(max-width: 768px)").matches; // si l'écran est plus petit que 768px, on considère que l'utilisateur est sur mobile

menuBtn.addEventListener("click", () => {
  if (!isMenuOpen) {
    if(isMobile){
      searchClick.classList.add("classMagique");
    }
    overlayBg.classList.add("open"); // Fade in background
    overlayMenu.classList.add("open"); // Slide in menu
    menuBtn.innerHTML = "&times;"; // Change icon to "close"
    menuBtn.style = "padding-right:9px;padding-top:5px";
    $(function () {
      if ($("body").is(".IndexPage")) {
        if (isSearchOpen) {
          searchBar.classList.remove("open");
          searchClick.classList.remove("open");
          isSearchOpen = !isSearchOpen;
        }
        if (isSorterOpen) {
          sorterMenu.classList.remove("open");
          isSorterOpen = !isSorterOpen;
        }
      }
    });
  } else {
    if(isMobile){
      searchClick.classList.remove("classMagique");
    }
    overlayBg.classList.remove("open"); // Fade out background
    overlayMenu.classList.remove("open"); // Slide out menu
    menuBtn.innerHTML = "&#9776;"; // Change back to "menu" icon
    menuBtn.style = "padding-right:0";
  }
  isMenuOpen = !isMenuOpen; // Toggle the state
});

// Close the menu when clicking on the background
overlayBg.addEventListener("click", () => {
  if (isMenuOpen) {
    overlayBg.classList.remove("open");
    overlayMenu.classList.remove("open");
    menuBtn.innerHTML = "&#9776;";
    isMenuOpen = !isMenuOpen;
  }
});

const alreadyhere = document.getElementById("alreadyhere");
if (alreadyhere != null) {
  function clickalreadyhere() {
    alreadyhere.innerHTML = "You are already here :)";
    window.setTimeout(function () {
      alreadyhere.innerHTML = "Overview";
      alreadyhere.style = "font-size:xx-large; text-decoration:none";
    }, 2000);
  }
  alreadyhere.addEventListener("click", clickalreadyhere);
}

$(function () {
  if ($("body").is(".IndexPage")) {
    // -- Search bar -- //

    function searchclick() {
      if (!isMenuOpen) {
        if (!isSearchOpen) {
          searchClick.classList.add("open");
          searchBar.classList.add("open");
          document.getElementById("search").focus();
          isSearchOpen = !isSearchOpen;
        }
        else{
          document.getElementById("search").focus();
        }
      } else {
        overlayBg.classList.remove("open"); // Fade out background
        overlayMenu.classList.remove("open"); // Slide out menu
        menuBtn.innerHTML = "&#9776;"; // Change back to "menu" icon
        menuBtn.style = "padding-right:0";
        searchBar.classList.add("open");
        searchClick.classList.add("open");
        document.getElementById("search").focus();
        isMenuOpen = !isMenuOpen;
        isSearchOpen = !isSearchOpen;
      }
    }

    function resetSearch() {
      if (isSearchOpen) {
          searchBar.classList.remove("open");
          searchClick.classList.remove("open");
          isSearchOpen = !isSearchOpen;
      }
    }

    searchicon.addEventListener("click", searchclick);
    searchBar.addEventListener("click", searchclick);
    overlayBg.addEventListener("click", resetSearch);
    menuBtn.addEventListener("click", resetSearch);
    navbar.addEventListener("click", resetSearch);

    // -- Sorter Menu -- //

    sorterclick.addEventListener("click", sorterui);
    function sorterui() {
      if (!isMenuOpen) {
        if (!isSorterOpen) {
          sorterMenu.classList.add("open");
        } else {
          sorterMenu.classList.remove("open");
        }
        isSorterOpen = !isSorterOpen;
      } else {
        overlayBg.classList.remove("open"); // Fade out background
        overlayMenu.classList.remove("open"); // Slide out menu
        menuBtn.innerHTML = "&#9776;"; // Change back to "menu" icon
        menuBtn.style = "padding-right:0";
        sorterMenu.classList.add("open");
        isMenuOpen = !isMenuOpen;
        isSorterOpen = !isSorterOpen;
      }
    }

    function resetsorterui() {
      if (isSorterOpen) {
        sorterMenu.classList.remove("open");
        isSorterOpen = !isSorterOpen;
      }
    }

    overlayBg.addEventListener("click", resetsorterui);
  }
});

