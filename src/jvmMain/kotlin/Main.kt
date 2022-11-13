// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

val server = Server()

@Composable
@Preview
fun App() {
    MaterialTheme {
        ServerWindow(server)
        val sessions by server.activeUsers.collectAsState()
        for (session in sessions) {
            ClientWindow(Client(server, session))
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

@Composable
fun ServerWindow(server: Server) {
    val selected by server.selectedChat.collectAsState()
    val chatrooms by server.chatrooms.collectAsState()
    val broadcastChat by server.broadcast.collectAsState()
    Column(modifier = Modifier.fillMaxSize(1f)) {
        ChatsWindow(
            selected,
            chatrooms,
            broadcastChat,
            modifier = Modifier.fillMaxHeight(0.8f).fillMaxWidth(1f),
            selectChat = { chatroom ->
                server.selectChat(chatroom)
            }
        )
        var username by remember { mutableStateOf("") }
        Row(modifier = Modifier.fillMaxSize(1f)) {
            TextField(
                username,
                { text ->
                    username = text
                },
                modifier = Modifier.fillMaxWidth(0.8f).fillMaxHeight(1f),
                label = {
                    Text("Username")
                }
            )
            Button(modifier = Modifier.fillMaxSize(1f), onClick = {
                if (username.isNotBlank()) {
                    server.createUser(username.trim())
                    username = ""
                }
            }) {
                Text("Create user")
            }
        }
    }
}

@Composable
fun ClientWindow(client: Client) = Window(onCloseRequest = { client.finishSession() }, title = client.username) {
    val selected by client.selectedChat.collectAsState()
    val chatrooms by client.chatrooms.collectAsState()
    val broadcastChat by client.broadcast.collectAsState()
    Column(modifier = Modifier.fillMaxSize(1f)) {
        ChatsWindow(
            selected,
            chatrooms,
            broadcastChat,
            modifier = Modifier.fillMaxHeight(0.8f).fillMaxWidth(1f),
            selectChat = { chatroom ->
                println("selected $chatroom")
                client.selectChat(chatroom)
            }
        )
        var receiver by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        Row(modifier = Modifier.fillMaxSize(1f)) {
            TextField(
                receiver,
                { text ->
                    receiver = text
                },
                modifier = Modifier.fillMaxWidth(0.2f).padding(end = 5.dp).fillMaxHeight(1f),
                label = { Text("Username or empty") }
            )
            TextField(
                message,
                { text ->
                    message = text
                },
                modifier = Modifier.fillMaxWidth(0.8f).fillMaxHeight(1f),
                label = { Text("Message") }
            )
            Button(
                modifier = Modifier.fillMaxSize(1f),
                enabled = (client.server.activeUsers.value.contains(receiver) || receiver.isBlank()) &&
                    message.trim().isNotEmpty(),
                onClick = {
                    client.sendMessage(
                        message.trim(),
                        if (receiver.isBlank()) {
                            Receiver.All
                        } else {
                            Receiver.Person(receiver)
                        }
                    )
                    message = ""
                }
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
fun ChatsWindow(
    selected: Chatroom?,
    chatrooms: Map<Chatroom, List<String>>,
    broadcastChat: List<String>,
    selectChat: (Chatroom?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier) {
        LazyColumn(modifier = Modifier.fillMaxWidth(0.3f).fillMaxHeight(1f)) {
            item {
                val backgroundColor =
                    if (selected == null) {
                        println("true $selected == 0")
                        Color.Cyan
                    } else {
                        println("false $selected != 0")
                        Color.White
                    }
                Column(
                    modifier = Modifier
                        .background(backgroundColor)
                        .clickable {
                            selectChat(null)
                        }
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Text("Broadcast")
                }
            }
            for ((chatroom, _) in chatrooms) {
                item {
                    val backgroundColor =
                        if (selected == chatroom) {
                            println("true $selected == $chatroom")
                            Color.Cyan
                        } else {
                            println("false $selected != $chatroom")
                            Color.White
                        }
                    Column(
                        modifier = Modifier
                            .background(backgroundColor)
                            .clickable {
                                selectChat(chatroom)
                            }
                            .padding(20.dp)
                            .fillMaxWidth()
                    ) {
                        Text(chatroom.firstUser)
                        Text(chatroom.secondUser)
                    }
                }
            }
        }
        LazyColumn(modifier = Modifier.fillMaxWidth(1f).fillMaxHeight(1f)) {
            itemsIndexed(
                if (selected == null) {
                    broadcastChat
                } else {
                    chatrooms[selected] ?: emptyList()
                }
            ) { _, message ->
                Text(message)
            }
        }
    }
}
