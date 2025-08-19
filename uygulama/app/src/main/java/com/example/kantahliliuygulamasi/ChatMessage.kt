data class ChatMessage(val text: String, val type: MessageType)

enum class MessageType {
    RESULT,
    SUGGESTION,
    SUMMARY
}
