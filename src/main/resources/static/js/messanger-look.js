'use strict'

var maskKeys = ["Shift", "Ctrl", "Alt",];

function clearInputDiv(container) {

    while(container.lastChild) {
        container.removeChild(container.lastChild);
    }
    document.getElementById("placeholder").classList.remove("hiding");
}

$("#text").keyup(function(event) {

    let area = document.getElementById("text");
    
    if (area.innerHTML.length > 0) {
        document.getElementById("placeholder").classList.add("hiding");
    }

    if (area.innerText.length < 1 || area.innerText === "\n") {
        clearInputDiv(area);
    }
});


$("#text").keydown(function(event) {

    if (event.key.length == 1) {
        document.getElementById("placeholder").classList.add("hiding");
    }
   
    if (event.key === "Enter" && !event.shiftKey) {
        event.preventDefault();
        sendMessage();
    }
});

$("#scrollable").scroll(function() {
    if ($(this).scrollTop() === 0) {
        loadArchived();
    }
})