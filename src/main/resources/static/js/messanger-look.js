let textContainer, textareaSize, input;

function autoSize() {
    textareaSize.innerHTML = input.value + '\n';
}

document.addEventListener('DOMContentLoaded', function() {
    textContainer = document.querySelector('#textarea-container');
    textareaSize = textContainer.querySelector('.textarea-size');
    input = textContainer.querySelector('textarea');

    autoSize();
    input.addEventListener('input', autoSize);
});

$("#text").keypress(function(event) {
    if (event.which == 13 && !event.shiftKey) {
        event.preventDefault();
        sendMessage();
    }
});