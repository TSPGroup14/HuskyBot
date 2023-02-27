package huskybot.cmdFramework

enum class CommandCategory(val description: String) {
    MODERATION("Commands that execute or assist moderating functions"),
    MANAGEMENT("Commands that edit the bots functions and behaviour in the guild"),
    MODMAIL("Commands that are related to the ticketing function"),
    MISC("Commands that don't fit in the other categories");

    fun toTitleCase(): String {
        return this.toString().lowercase()
    }
}