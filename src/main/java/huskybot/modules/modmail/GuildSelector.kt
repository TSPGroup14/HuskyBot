package huskybot.modules.modmail

import huskybot.HuskyBot
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

object GuildSelector {

    /**
     * Function that returns an embedded message with a selection menu of mutual guilds
     * to the user
     */
    fun getGuilds(event: ButtonInteractionEvent) {

        event.message.delete().queue()      //Delete original message

        /* Get List of Mutual Guilds */
        var guilds = event.user.mutualGuilds

        //check if list is over 25 (limit for slection menu)
        if (guilds.size > 25) {
            guilds = guilds.subList(0, 24)
        }

        /* Make Array of Options */
        val options = ArrayList<SelectOption>()
        for (guild in guilds) {
            options.add(SelectOption.of(guild.name, "guildSelect:${guild.idLong}"))
        }

        /* Create new Embed and Selection Menu */
        val embed = EmbedBuilder()
            .setTitle("❗Select Guild ❗")
            .setDescription("Please use the selection menu below to slect the desired guild.")
            .setColor(HuskyBot.color)
            .build()

        val selection = StringSelectMenu.create("guildlist")
            .addOptions(options)
            .build()

        /* Send Embed and Apply Selection Menu */

        event.replyEmbeds(embed)
            .addActionRow(selection)
            .addActionRow(
                Button.secondary("ticket:prevpage", "Previous"),
                Button.secondary("ticket:nextpage", "Next"),
                Button.danger("modmail:cancel", "Cancel")
            )
            .queue()
    }

    /**
     * Function that returns an updated action row with the next set of 25 guilds
     */
    fun getGuildsNext(event: ButtonInteractionEvent) {

    }

    /**
     * Function that returns an updated action row with the previous set of 25 guilds
     */
    fun getGuildsPrevious(event: ButtonInteractionEvent) {

        event.deferEdit().queue()   //Acknowledge the interaction

        /* Get List of Mutual Guilds */
        var guilds = event.user.mutualGuilds

        //check if list is over 25 (limit for slection menu)
        if (guilds.size > 25) {
            guilds = guilds.subList(0, 24)
        }

        /* Make Array of Options */
        val options = ArrayList<SelectOption>()
        for (guild in guilds) {
            options.add(SelectOption.of(guild.name, "guildSelect:${guild.idLong}"))
        }

        /* Create Selection Menu */
        val selection = StringSelectMenu.create("guildlist")
            .addOptions(options)
            .build()

        println("honk")
        event.hook.editOriginalComponents(
            ActionRow.of(selection),
            ActionRow.of(
                Button.secondary("ticket:prevpage", "Previous"),
                Button.secondary("ticket:nextpage", "Next"),
                Button.danger("modmail:cancel", "Cancel")
            )
        ).queue()
    }
}