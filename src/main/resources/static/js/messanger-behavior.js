'use strict';

// This files handles underground logic events
let userName = "";
let users = [];
let usersContainer = {};
let convsArray = [];
let textArea = {};
let offsetInMilliSec = (new Date()).getTimezoneOffset() * 60 * 1000;

function loginClicked(login) {

    var loginStr = login.innerText;

    var conv = convsArray[loginStr];
    if (conv === undefined) {
        conv = createConversationObj(loginStr);
        convsArray[loginStr] = conv;
    }
    setConvWindow(conv, loginStr);
    $("#conv-partner").text(loginStr);
}

function createConversationObj(login) {
    var conversation = new Conversation(login, new Preview(login));
        // convsArray[loginStr] = conv;
    var convsList = $("#convs-list")[0];
    var previewContainer = conversation.previewContainer.createContainer();
    previewContainer.addEventListener("click", function() {
        setConvWindow(conversation, login);
        $("#conv-partner").text(login);
    });
    convsList.appendChild(previewContainer);
    return conversation;
}

function updateUsersCount() {
    $("#user-count").text(users.length.toString());
}

function prepareUserNode(userName) {
    var newUser = document.createElement('div');
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
    var index = users.indexOf(user);

    if (index < users.length - 1) {
        var node = usersContainer.children[index+1];
        usersContainer.insertBefore(prepareUserNode(user), node);
    } else {
        usersContainer.appendChild(prepareUserNode(user));
    }
    updateUsersCount();
}

function removeUser(user) {
    var index = users.indexOf(user);
    if (index < 0) {
        return;
    }
    users.splice(index);

    usersContainer.removeChild(usersContainer.children[index]);
    updateUsersCount();
}


function readUserList(body) {

    users = JSON.parse(body);
    var userIndex = users.indexOf(userName);
    users.splice(userIndex, 1);
    users.sort();
    for (var i = 0; i < users.length; i++) {
        usersContainer.appendChild(prepareUserNode(users[i]));
    }
    updateUsersCount();
}

// server connection logic
let stompClient = null;


function showData(message) {
    console.log(message);
}

window.addEventListener("load", function() {
    userName = document.getElementById('userName').innerText;
    connect();
    usersContainer = document.getElementById('users-list');
    textArea = this.document.getElementById("text");
    textArea.disabled = true;

});


function connect() {
    var socket = new SockJS('/websocket');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {

        stompClient.subscribe("/app/activeclients", function (message) {
            readUserList(message.body);
        });

        stompClient.subscribe("/server/user.login", function (message) {
            addUser(message.body);
        });

        stompClient.subscribe("/server/user.logout", function (message) {
            removeUser(message.body);
        });

        stompClient.subscribe("/conv/" + userName, function (incomingMessage) {
            handleIncomingMessage(incomingMessage.body);
        });

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

function setConvWindow(conversation, login) {

    textArea.disabled = false;
    
    if ($("#conv-partner").text() === login) {
        return;
    }
    
    textArea.value = "";

    var container = document.getElementById("messages-container");
    while(container.lastChild) {
        container.removeChild(container.lastChild);
    }
    container.appendChild(conversation.messagesContainer);
    conversation.setActive();
}


// credits for this format solution:
// https://stackoverflow.com/a/30272803
function formatTime(date) {
    return ("0" + date.getHours()).slice(-2) + ":" + ("0" + date.getMinutes()).slice(-2);
}

function sendMessage() {
    var content = textArea.value.trim();
    textArea.value = "";
    autoSize();

    if (content.length == 0) {
        return;
    }
    
    var partner = $("#conv-partner").text();
    convsArray[partner].addUserMessage(userName, content, new Date());
    
    var destination = "/app/priv/" + partner;
    stompClient.send(destination, {}, JSON.stringify( {
        'user': userName,
        'content': content
    }));
}

function handleIncomingMessage(rawMessage) {

    var data = JSON.parse(rawMessage);

    var conv = convsArray[data.partner];
    if (conv === undefined) {
        conv = createConversationObj(data.partner);
        convsArray[data.partner] = conv;
    }

    var dateFromUTC = data.messages[0].time + offsetInMilliSec;
    if (data.type === "NEWMSG") {
        conv.addIncomingMessage(data.partner, data.messages[0].content, new Date(dateFromUTC));
    }
}
