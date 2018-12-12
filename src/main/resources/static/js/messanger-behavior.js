'use strict';

// This file handles underground logic events
let stompClient = null;
let userName = "";
let convPartner = "";
let users = [];
let usersContainer = {};
let convsArray = [];
let textArea = {};
let offsetInMilliSec = (new Date()).getTimezoneOffset() * 60 * 1000;
let localLang = "";

function loginClicked(login) {

    let loginStr = login.lastElementChild.innerText;

    let conv = convsArray[loginStr];
    if (conv === undefined) {
        conv = createConversationObj(loginStr);
        convsArray[loginStr] = conv;
    }
    setConvWindow(conv, loginStr);
}

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
        loginClicked(newUser);
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
        preview.unreadCount = msg.author === userName ? 0 : preview.unreadCount;
        conv.setPreviewMessage(msg.content, msg.time, preview.unreadCount);
    }
}


function showData(message) {
    console.log(message);
}

window.addEventListener("load", () => {
    userName = document.getElementById("userName").innerText;
    connect();
    usersContainer = this.document.getElementById("users-list");
    textArea = this.document.getElementById("text");
    textArea.disabled = true;
    localLang = this.window.navigator.language || this.window.navigator.userLanguage;
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

    textArea.disabled = false;
    if (convPartner === login) {
        return;
    }

    convPartner = login;
    $("#conv-partner").text(login);

    textArea.innerText = "";
    clearInputDiv(textArea);

    let container = document.getElementById("messages-container");
    while(container.lastChild) {
        container.removeChild(container.lastChild);
    }
    container.appendChild(conv.messagesContainer);
    conv.setActive();

    let scrollable = document.getElementById("scrollable");
    if (conv.messagesContainer.scrollHeight < scrollable.clientHeight) {
        loadArchived();
    }
    scrollable.scrollTop = conv.messagesContainer.scrollHeight;
}


// credits for this format solution:
// https://stackoverflow.com/a/30272803
function formatTime(date) {
    return ("0" + date.getHours()).slice(-2) + ":" + ("0" + date.getMinutes()).slice(-2);
}

function sendMessage() {
    let content = textArea.innerText.trim();
    textArea.innerText = "";
    clearInputDiv(textArea);

    if (content.length == 0) {
        return;
    }
    
    convsArray[convPartner].addNewMessage(userName, content, $.now());
    
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

// function toggleSend() {
//     document.getElementById("button-container").classList.toggle("hiding");
// }


// function showTime(author) {
//     author.parentNode.lastElementChild.classList.toggle("hiding");
// }

// function toggleStatus(el) {
//     el.firstElementChild.classList.toggle("offline");
// }