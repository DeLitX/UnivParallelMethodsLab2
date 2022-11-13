sealed class Receiver {
    object All : Receiver()
    data class Person(val username: String) : Receiver()
}
