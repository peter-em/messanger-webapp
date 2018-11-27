'use strict';


function createMessage(author, content, time) {
    var message = document.createElement("div");
    message.classList.add("message");
    var element = document.createElement("div");
    element.classList.add("author-div");
    element.innerText = author;
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

class Conversation {
    constructor(partner, previewContainer) {
        this.partner = partner;
        this.newMsgCounter = 0;
        this.oldestTime = $.now();
        this.previewContainer = previewContainer;
        this.messagesContainer = document.createElement("div");
        this.messagesContainer.classList.add("messages")
    }

    addUserMessage(author, content, time) {
        var timeFormatted = formatTime(time);
        this.messagesContainer.appendChild(createMessage(author, content, timeFormatted));
        this.previewContainer.updateContent(0, timeFormatted, content);
    }

    addIncomingMessage(author, content, time) {
        var timeFormatted = formatTime(time);
        this.messagesContainer.appendChild(createMessage(author, content, timeFormatted));
        var active = $("#conv-partner").text();
    
        if (this.partner != active) {
            this.newMsgCounter++;
        }
        this.previewContainer.updateContent(this.newMsgCounter, timeFormatted, content);
    }

    addArchivedMessage(author, content, time) {
        this.messagesContainer.firstChild.insertBefore(createMessage(author, content, time));
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
        this.newestTime = {};
        this.demo = {};
    }

    createContainer() {
        var details = document.createElement("div");
        details.classList.add("cupper");
        var user = document.createElement("div");
        user.classList.add("cuser");
        user.innerText = this.partner;
        details.appendChild(user);
        
        var counter = document.createElement("div");
        counter.classList.add("ccountr");
        details.appendChild(counter);
        this.counter = counter;
        
        var time = document.createElement("div");
        time.classList.add("ctime");
        details.appendChild(time);
        this.newestTime = time;

        var container = document.createElement("div");
        container.classList.add("conv");
        container.appendChild(details);
        var demo = document.createElement("div");
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
