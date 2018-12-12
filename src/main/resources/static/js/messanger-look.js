'use strict'

function clearInputDiv(container) {

    while(container.lastChild) {
        container.removeChild(container.lastChild);
    }
    document.getElementById("placeholder").classList.remove("hiding");
}

$("#text").keyup(function(event) {

    let area = document.getElementById("text");
    if (area.innerText.length < 1 || area.innerText === "\n") {
        clearInputDiv(area);
    }
});

$("#text").keypress(function(event) {

    if (event.which != 8) {
        document.getElementById("placeholder").classList.add("hiding");
    }

    if (event.which == 13 && !event.shiftKey) {
        event.preventDefault();
        sendMessage();
    }
});

$("#scrollable").scroll(function() {
    if ($(this).scrollTop() === 0) {
        loadArchived();
    }
})