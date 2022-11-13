import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*

class Client(val server: Server, val username: String) {
    val broadcast: StateFlow<List<String>> = server.broadcast
    val chatrooms: StateFlow<Map<Chatroom, List<String>>> = server.chatrooms.map {
        it.filter { (key, _) ->
            key.firstUser == username || key.secondUser == username
        }
    }.stateIn(CoroutineScope(IO), SharingStarted.Eagerly, emptyMap())
    val selectedChat: MutableStateFlow<Chatroom?> = MutableStateFlow(null)

    fun selectChat(chatroom: Chatroom?) {
        selectedChat.update { chatroom }
    }

    fun sendMessage(message: String, receiver: Receiver) {
        server.sendMessage(message, username, receiver)
    }

    fun finishSession() {
        server.finishSession(username)
    }
}
