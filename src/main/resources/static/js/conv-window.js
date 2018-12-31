'use strict';


function createMessage(author, content, time, isOwner) {
    let message = document.createElement("div");
    message.classList.add("message");
    let element = document.createElement("div");
    element.classList.add("author-div");
    if (isOwner) {
        element.classList.add("owner");
    }
    element.innerText = author;
    element.addEventListener("click", () => {
        let visible = document.getElementsByClassName("unhide");
        if (visible.length > 0 && visible[0] != element) {
            visible[0].classList.remove("unhide");
        }
        message.lastElementChild.classList.toggle("unhide");
    });
    message.appendChild(element);

    element = document.createElement("div");
    element.classList.add("content-div");
    element.innerText = content;
    message.appendChild(element);

    element = document.createElement("div");
    element.classList.add("time-div");
    element.innerText = time;
    message.appendChild(element);
    return message;
}

function createTimeInfo(time) {
    let div = document.createElement("div");
    div.classList.add("time-info");
    div.innerText = time;
    return div;
}

function hasDayPassed(timeInMillis) {
    return Math.ceil(($.now() - timeInMillis) / 3600000) > 23;
}

function has10MinPassed(last, newest) {
    return (newest - last) > 600000;
}

function formatDateTime(timeInMillis, hasDayPassed) {
    let date = new Date(timeInMillis);
    if (hasDayPassed) {
        return date.toLocaleDateString(localLang, {day: "numeric", month: "short"});
    } 
    return date.toLocaleTimeString(localLang, {hour: "2-digit", minute: "2-digit"});
}

class Conversation {
    constructor(partner, previewContainer) {
        this.partner = partner;
        this.newMsgCounter = 0;
        this.oldestTime = $.now();
        this.latestTime = 0;
        this.hasOlderMsg = false;
        this.previewContainer = previewContainer;
        this.messagesContainer = document.createElement("div");
        this.messagesContainer.classList.add("messages")
    }

    setPreviewMessage(content, time, newCounter) {
        this.newMsgCounter = newCounter;
        this.hasOlderMsg = true;
        this.latestTime = time;
        this.previewContainer.updateContent(this.newMsgCounter, formatDateTime(time, hasDayPassed(time)), content);
    }

    addNewMessage(author, content, time) {
        let timeFormatted = formatDateTime(time, false);

        // insert time-info div, when last message was over 10 minutes ago
        if (has10MinPassed(this.latestTime, time)) {
            this.messagesContainer.appendChild(createTimeInfo(timeFormatted));
        }
        this.latestTime = time;

        this.messagesContainer.appendChild(
            createMessage(author, content, timeFormatted, author != this.partner));
        
        if (this.partner != convPartner) {
            this.newMsgCounter++;
        }
        this.previewContainer.updateContent(this.newMsgCounter, timeFormatted, content);

        if (this.messagesContainer.parentNode != null) {
            $("#scrollable").scrollTop(this.messagesContainer.scrollHeight);
        }
    }

    addArchivedMessage(author, content, time) {
        let formatted = formatDateTime(time, false);
        if (hasDayPassed(time)) {
            formatted = formatDateTime(time, true) + ", " + formatted;
        }

        if (has10MinPassed(time, this.oldestTime)) {

            if (this.messagesContainer.hasChildNodes()) {
                let oldestTime = this.messagesContainer.firstElementChild.lastElementChild.innerText;
                this.messagesContainer.insertBefore(
                    createTimeInfo(oldestTime), this.messagesContainer.firstChild);
            }
        }

        this.messagesContainer.insertBefore(
            createMessage(author, content, formatted, author != this.partner),
            this.messagesContainer.firstChild);
        this.oldestTime = time;
    }

    setActive() {
        this.newMsgCounter = 0;
        this.previewContainer.clearUnread();
    }
    
}

class Preview {
    constructor(partner) {
        this.partner = partner;
        this.counter = {};
        this.newestTime = "";
        this.demo = {};
    }

    createContainer() {
        let details = document.createElement("div");
        details.classList.add("cupper");
        let user = document.createElement("div");
        user.classList.add("cuser");
        user.innerText = this.partner;
        details.appendChild(user);
        
        let counter = document.createElement("div");
        counter.classList.add("ccountr");
        details.appendChild(counter);
        this.counter = counter;
        
        let time = document.createElement("div");
        time.classList.add("ctime");
        details.appendChild(time);
        this.newestTime = time;

        let container = document.createElement("div");
        container.classList.add("conv");
        container.appendChild(details);
        let demo = document.createElement("div");
        demo.classList.add("cdemo");
        container.appendChild(demo);
        this.demo = demo;
        return container;
    }

    updateContent(unreadCount, time, content) {
        this.newestTime.innerText = time;
        this.demo.innerText = content;
        if (unreadCount > 0) {
            this.counter.innerText = "(" + unreadCount.toString() + ")";
        }
    }

    clearUnread() {
        this.counter.innerText = "";
    }
}
