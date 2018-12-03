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

function loginClicked(login) {

    let loginStr = login.innerText;

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
    previewContainer.addEventListener("click", function() {
        setConvWindow(conversation, login);
    });
    convsList.appendChild(previewContainer);
    return conversation;
}

function updateUsersCount() {
    $("#user-count").text(users.length.toString());
}

function prepareUserNode(userName) {
    let newUser = document.createElement('div');
    newUser.classList.add('user');
    newUser.innerText = userName;
    newUser.addEventListener("click", function() {
        loginClicked(this);
    });
    return newUser;
}

function addUser(user) {

    users.push(user);
    users.sort();
    let index = users.indexOf(user);

    if (index < users.length - 1) {
        let node = usersContainer.children[index+1];
        usersContainer.insertBefore(prepareUserNode(user), node);
    } else {
        usersContainer.appendChild(prepareUserNode(user));
    }
    updateUsersCount();
}

function removeUser(user) {
    let index = users.indexOf(user);
    if (index < 0) {
        return;
    }
    users.splice(index);

    usersContainer.removeChild(usersContainer.children[index]);
    updateUsersCount();
}


function readInitialData(body) {

    let initializer = JSON.parse(body);
    
    // initialize list of active users section
    users = initializer.activeUsers;
    let userIndex = users.indexOf(userName);
    users.splice(userIndex, 1);
    users.sort();
    for (let i = 0; i < users.length; i++) {
        usersContainer.appendChild(prepareUserNode(users[i]));
    }
    updateUsersCount();

    // initialize list of conv previews section
    for (let preview of initializer.conversations) {
        
        let conv = createConversationObj(preview.partner);
        convsArray[preview.partner] = conv;
        let msg = preview.message;
        conv.setPreviewMessage(msg.author, msg.content, msg.time, 0);
    }
}


function showData(message) {
    console.log(message);
}

window.addEventListener("load", function() {
    userName = document.getElementById("userName").innerText;
    connect();
    usersContainer = this.document.getElementById("users-list");
    textArea = this.document.getElementById("text");
    textArea.disabled = true;

});


function connect() {
    let socket = new SockJS('/websocket');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {

        stompClient.subscribe("/app/activeclients", function (data) {
            readInitialData(data.body);
        });

        stompClient.subscribe("/server/user.login", function (update) {
            addUser(update.body);
        });

        stompClient.subscribe("/server/user.logout", function (update) {
            removeUser(update.body);
        });

        stompClient.subscribe("/conv/priv/" + userName, function (newMessage) {
            handleNewMessage(newMessage.body);
        });

        stompClient.subscribe("/conv/archive/" + userName, function(archived) {
            handleArchivedMessages(archived.body);
        })

        stompClient.subscribe('/server/update', function (greeting) {
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

    textArea.value = "";
    textArea.focus();

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
    let content = textArea.value.trim();
    textArea.value = "";
    autoSize();

    if (content.length == 0) {
        return;
    }
    
    convsArray[convPartner].addNewMessage(userName, content, new Date());
    
    let destination = "/app/priv/" + convPartner;
    stompClient.send(destination, {}, JSON.stringify( {
        'user': userName,
        'content': content
    }));
}

function handleNewMessage(rawMessage) {

    let data = JSON.parse(rawMessage);

    let conv = convsArray[data.partner];
    if (conv === undefined) {
        conv = createConversationObj(data.partner);
        convsArray[data.partner] = conv;
    }

    conv.addNewMessage(data.message.author,
        data.message.content,
        new Date(data.message.time));
}

function handleArchivedMessages(rawMessage) {
    let data = JSON.parse(rawMessage);
    let conv = convsArray[data.partner];

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
