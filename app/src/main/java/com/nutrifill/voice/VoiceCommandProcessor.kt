package com.nutrifill.voice

class VoiceCommandProcessor {
    companion object {
        private val FOOD_RECOGNITION_COMMANDS = setOf(
            "scan food",
            "recognize food",
            "what is this food",
            "identify food"
        )

        private val NUTRITION_INFO_COMMANDS = setOf(
            "nutrition information",
            "calories",
            "nutritional value",
            "tell me about this food"
        )

        private val NAVIGATION_COMMANDS = setOf(
            "go back",
            "home screen",
            "main menu",
            "settings",
            "history"
        )

        private val HELP_COMMANDS = setOf(
            "help",
            "what can you do",
            "available commands",
            "instructions"
        )
    }

    sealed class CommandType {
        object FoodRecognition : CommandType()
        object NutritionInfo : CommandType()
        object Navigation : CommandType()
        object Help : CommandType()
        object Unknown : CommandType()
    }

    fun processCommand(command: String): CommandType {
        val normalizedCommand = command.toLowerCase().trim()

        return when {
            FOOD_RECOGNITION_COMMANDS.any { normalizedCommand.contains(it) } -> 
                CommandType.FoodRecognition
            NUTRITION_INFO_COMMANDS.any { normalizedCommand.contains(it) } -> 
                CommandType.NutritionInfo
            NAVIGATION_COMMANDS.any { normalizedCommand.contains(it) } -> 
                CommandType.Navigation
            HELP_COMMANDS.any { normalizedCommand.contains(it) } -> 
                CommandType.Help
            else -> CommandType.Unknown
        }
    }

    fun getHelpMessage(): String {
        return """Here are the available voice commands:
            |1. For food recognition, say: 'scan food' or 'recognize food'
            |2. For nutrition information, say: 'nutrition information' or 'calories'
            |3. For navigation, say: 'go back', 'home screen', or 'main menu'
            |4. For help, say: 'help' or 'what can you do'
            |You can activate voice control at any time by tapping the microphone button.""".trimMargin()
    }

    fun getUnknownCommandMessage(): String {
        return "I'm sorry, I didn't understand that command. Say 'help' to hear available commands."
    }
}