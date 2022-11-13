data class Chatroom(
    val firstUser: String,
    val secondUser: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chatroom
        return (firstUser == other.firstUser && secondUser == other.secondUser) ||
            (firstUser == other.secondUser && secondUser == other.firstUser)
    }

    override fun hashCode(): Int =
        firstUser.hashCode() + secondUser.hashCode()
}
