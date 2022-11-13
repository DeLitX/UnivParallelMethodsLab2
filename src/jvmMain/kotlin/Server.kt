import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class Server {

    val broadcast: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    val chatrooms: MutableStateFlow<Map<Chatroom, List<String>>> = MutableStateFlow(emptyMap())
    val activeUsers: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    val selectedChat: MutableStateFlow<Chatroom?> = MutableStateFlow(null)

    fun selectChat(chatroom: Chatroom?) {
        selectedChat.update { chatroom }
    }

    fun createUser(username: String) {
        activeUsers.update { (it + username).distinct() }
    }

    fun finishSession(username: String) {
        activeUsers.update { it - username }
        chatrooms.update { it.filter { (key, _) -> key.firstUser != username && key.secondUser != username } }
        if (selectedChat.value?.firstUser == username || selectedChat.value?.secondUser == username) {
            selectedChat.update { null }
        }
    }

    fun sendMessage(message: String, username: String, receiver: Receiver) {
        when (receiver) {
            Receiver.All -> broadcast.update { it + makeMessage(message, username) }
            is Receiver.Person -> chatrooms.update { previousMap ->
                val newMap = previousMap.toMutableMap()
                val chatroom = Chatroom(username, receiver.username)
                newMap[chatroom] = previousMap.getOrDefault(chatroom, emptyList()) + makeMessage(message, username)
                newMap
            }
        }
    }

    private fun makeMessage(message: String, username: String): String =
        "$username: $message"
}
