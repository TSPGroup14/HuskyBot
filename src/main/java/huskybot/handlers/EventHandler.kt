package huskybot.handlers

import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.EventListener

class EventHandler : EventListener {
    override fun onEvent(event: GenericEvent) {
        //This is where we will add specific event triggers for everything that isn't a command/interaction.
    }
}