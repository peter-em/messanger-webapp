'use strict';

// This file handles underground logic events
let stompClient = null;
let userName = "";
let convPartner = "";
let users = [];
let usersContainer = {};
let convsArray = [];
let textArea = {};
let localLang = "";


function createConversationObj(login) {
    let conversation = new Conversation(login, new Preview(login));
    let convsList = $("#convs-list")[0];
    let previewContainer = conversation.previewContainer.createContainer();
    previewContainer.addEventListener("click", () => {
        setConvWindow(conversation, login);
    });
    convsList.appendChild(previewContainer);
    return conversation;
}

function updateUsersCount() {
    let count = users.length - document.getElementsByClassName("offline").length;
    $("#active-count").text(count.toString());
}

function prepareUserNode(login) {
    let newUser = document.createElement("li")

    let div = document.createElement("div");
    div.classList.add("u-active", "offline");
    let span = document.createElement("span");
    span.classList.add("dot");
    div.appendChild(span);
    newUser.appendChild(div);
    div = document.createElement("div");
    div.classList.add("u-label");
    div.innerText = login;
    newUser.appendChild(div);

    newUser.classList.add('user');
    newUser.addEventListener("click", () => {
        setConvWindow(convsArray[login], login);
    });
    return newUser;
}

function toggleOffline(user) {
    user.firstElementChild.classList.toggle("offline");
}

function addUser(user) {

    let index = users.indexOf(user);
    if (index < 0) {
        users.push(user);
        users.sort();
        index = users.indexOf(user);
        if (index < users.length - 1) {
            let node = usersContainer.children[index];
            usersContainer.insertBefore(prepareUserNode(user), node);
        } else {
            usersContainer.appendChild(prepareUserNode(user));
        }
    }

    toggleOffline(usersContainer.children[index]);
    updateUsersCount();
}

function removeUser(user) {
    let index = users.indexOf(user);
    if (index < 0) {
        return;
    }

    toggleOffline(usersContainer.children[index]);
    updateUsersCount();
}

function readInitialData(body) {

    let initializer = JSON.parse(body);
    
    // initialize list of active users section
    users = initializer.onlineUsers.concat(initializer.offlineUsers);
    // let userIndex = users.indexOf(userName);
    // users.splice(userIndex, 1);
    users.sort();
    for (let i = 0; i < users.length; i++) {
        let node = prepareUserNode(users[i]);
        usersContainer.appendChild(node);
        if (initializer.onlineUsers.includes(users[i])) {
            toggleOffline(node);
        }
    }
    updateUsersCount();

    // initialize list of conv previews section
    for (let preview of initializer.conversations) {
        
        let conv = createConversationObj(preview.partner);
        convsArray[preview.partner] = conv;
        let msg = preview.message;
        preview.unread = msg.author === userName ? 0 : preview.unread;
        conv.setPreviewMessage(msg.content, msg.time, preview.unread);
    }
}


function showData(message) {
    console.log(message);
}

// initial method (connecting to server, initializing resources)
window.addEventListener("load", () => {
    userName = document.getElementById("userName").innerText;
    connect();
    usersContainer = this.document.getElementById("users-list");
    textArea = this.document.getElementById("text");

    document.querySelector("div[contenteditable]").addEventListener("paste", function(e) {
        e.preventDefault();
        var text = e.clipboardData.getData("text/plain");
        document.execCommand("insertText", false, text);
    });

    let mutationObserver = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
            contentChanged();
        });
    });
    mutationObserver.observe(textArea, {
        characterData: true,
        childList: true,
    });
    textArea.contentEditable = false;
    localLang = this.window.navigator.language || this.window.navigator.userLanguage;

    let close = document.getElementById("close");
    close.addEventListener("click", () => {
        setConvWindow(null, "");
        textArea.contentEditable = false;
        close.parentNode.classList.add("hiding");
    });
});


function connect() {
    let socket = new SockJS('/websocket');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {

        stompClient.subscribe("/app/activeclients", (data) => {
            readInitialData(data.body);
        });

        stompClient.subscribe("/server/user.login", (update) => {
            addUser(update.body);
        });

        stompClient.subscribe("/server/user.logout", (update) => {
            removeUser(update.body);
        });

        stompClient.subscribe("/conv/priv/" + userName, (newMessage) => {
            handleNewMessage(newMessage.body);
        });

        stompClient.subscribe("/conv/archive/" + userName, (archived) => {
            handleArchivedMessages(archived.body);
        })

        stompClient.subscribe('/server/update', (greeting) => {
            showData(greeting);
        });

    }, function () {
        console.log("Application was unable to connect or connection was closed");
    });

}


function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
}

function setConvWindow(conv, login) {
    if (convPartner === login) { return; }

    clearInputDiv(textArea);
    let container = document.getElementById("messages-container");
    while(container.lastChild) {
        container.removeChild(container.lastChild);
    }
    
    convPartner = login;
    $("#conv-partner").text(login);
    
    if (login === "") { return; }

    textArea.contentEditable = true;
    document.getElementById("header-panel").classList.remove("hiding");
    
    if (!conv) { return; }

    container.appendChild(conv.messagesContainer);
    conv.setActive();

    let scrollable = document.getElementById("scrollable");
    if (conv.messagesContainer.scrollHeight < scrollable.clientHeight) {
        loadArchived();
    }
    scrollable.scrollTop = conv.messagesContainer.scrollHeight;
}


function sendMessage() {
    let content = textArea.innerText.trim();
    clearInputDiv(textArea);
    if (content.length == 0) {
        return;
    }

    let conv = convsArray[convPartner];
    if (conv == undefined) {
        conv = createConversationObj(convPartner);
        convsArray[convPartner] = conv;
        document.getElementById("messages-container").appendChild(conv.messagesContainer);
    }
    
    conv.addNewMessage(userName, content, $.now());
    let destination = "/app/priv/" + convPartner;
    stompClient.send(destination, {}, JSON.stringify( {
        'user': userName,
        'content': content
    }));
}

function handleNewMessage(rawMessage) {

    let data = JSON.parse(rawMessage);

    let conv = convsArray[data.author];
    if (conv === undefined) {
        conv = createConversationObj(data.author);
        convsArray[data.author] = conv;
    }

    conv.addNewMessage(data.author, data.content, new Date(data.time));

    playSound("newmsg", {volume: .1});
}

function handleArchivedMessages(rawMessage) {
    let data = JSON.parse(rawMessage);
    let conv = convsArray[data.partner];

    data.messages.reverse();
    let container = conv.messagesContainer;
    let oldHeight = container.scrollHeight;
    for (let i = 0; i < data.messages.length; ++i) {
        conv.addArchivedMessage(data.messages[i].author,
            data.messages[i].content,
            data.messages[i].time);
    }

    if (data.messages.length > 6) {
        conv.hasOlderMsg = true;
    }

    let scrollable = document.getElementById("scrollable");
    if (container.parentNode != null) {
        scrollable.scrollTop = container.scrollHeight - oldHeight;

        if (container.scrollHeight < scrollable.clientHeight) {
            loadArchived();
        }
    }
}

function loadArchived() {

    if (convsArray[convPartner].hasOlderMsg) {

        convsArray[convPartner].hasOlderMsg = false;
        let time = convsArray[convPartner].oldestTime;
        let destination = "/app/priv/archive/" + userName;
        stompClient.send(destination, {}, JSON.stringify( {
            'user': convPartner,
            'content': time.toString()
        }));
    }
}

function playsound() {
    playSound("newmsg");
}
