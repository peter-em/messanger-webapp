'use strict'

function clearInputDiv(container) {

    while(container.lastChild) {
        container.removeChild(container.lastChild);
    }
    // document.getElementById("placeholder").classList.remove("hiding");
}

var area = document.getElementById("text");
function contentChanged() {

    if (area.textContent.length > 0) {
        document.getElementById("placeholder").classList.add("hiding");
    } else {
        document.getElementById("placeholder").classList.remove("hiding");
    }
};

$("#text").keydown(function(event) {

    if (event.key === "Enter" && !event.shiftKey) {
        event.preventDefault();
        sendMessage();
    }
});

$("#scrollable").scroll(function() {
    if ($(this).scrollTop() === 0) {
        loadArchived();
    }
});
