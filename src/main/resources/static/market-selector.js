function saveMarket(input) {
    localStorage.setItem("market", input.value);
}

function loadMarket() {
    var market = localStorage.getItem("market");
    if (!market) {
        market = "jita";
    }
    document.getElementById("market").value = market;
}
document.addEventListener("DOMContentLoaded", loadMarket);