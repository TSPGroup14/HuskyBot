package huskybot.modules.modmail

import huskybot.HuskyBot
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.dv8tion.jda.internal.interactions.component.StringSelectMenuImpl

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

        event.deferEdit().queue()   //Acknowledge the interaction

        /* Get existing selection menu */
        var selectionOld = StringSelectMenuImpl(event.message.actionRows[0].actionComponents[0].toData())

        var options = getPage(event, true, selectionOld.options)

        if (options == null) options = selectionOld.options

        /* Create new Selection Menu */
        val selection = StringSelectMenu.create("guildlist")
            .addOptions(options)
            .build()

        /* Append Selection Menu to Original Message */
        event.hook.editOriginalComponents(
            ActionRow.of(selection),
            ActionRow.of(
                Button.secondary("ticket:prevpage", "Previous"),
                Button.secondary("ticket:nextpage", "Next"),
                Button.danger("modmail:cancel", "Cancel")
            )
        ).queue()
    }

    /**
     * Function that returns an updated action row with the previous set of 25 guilds
     */
    fun getGuildsPrevious(event: ButtonInteractionEvent) {

        event.deferEdit().queue()   //Acknowledge the interaction

        /* Get existing selection menu */
        var selectionOld = StringSelectMenuImpl(event.message.actionRows[0].actionComponents[0].toData())

        var options = getPage(event, false, selectionOld.options)

        if (options == null) options = selectionOld.options

        /* Create Selection Menu */
        val selection = StringSelectMenu.create("guildlist")
            .addOptions(options)
            .build()

        event.hook.editOriginalComponents(
            ActionRow.of(selection),
            ActionRow.of(
                Button.secondary("ticket:prevpage", "Previous"),
                Button.secondary("ticket:nextpage", "Next"),
                Button.danger("modmail:cancel", "Cancel")
            )
        ).queue()
    }

    /**
     * Helper function for getting either the next or previous set of guilds
     * @param event Button interaction event
     * @param state Bool for determining which page to get (ex: False = Previous)
     * @param options ArrayList of SelectionMenu options
     */
    private fun getPage(event: ButtonInteractionEvent, state: Boolean, options: List<SelectOption>) : List<SelectOption>? {

        /* Get List of Mutual Guilds */
        var guilds = event.user.mutualGuilds

        //check if list is over 25 (limit for slection menu)
        if (guilds.size <= 25) {
            return null
        }

        /* Get Guild From Menu Value */
        val id = options[0].value.subSequence(12, options[0].value.length).toString()
        val targetGuild = event.jda.getGuildById(id.toLong())

        /* Create Subsets of Mutual Guilds */
        var selectedPage = 0
        val page = arrayOfNulls<List<Guild>>(10)

        for (i in page.indices) {
            page[i] = mutableListOf<Guild>()    //Instantiate the list of guilds
        }

        //Switch case for creating subsets
        when {
            (guilds.size > 25 && guilds.size <= 50) -> {
                page[0] = guilds.subList(0,24)
                page[1] = guilds.subList(25,guilds.size-1)
            }
            (guilds.size > 50 && guilds.size <= 75) -> {
                page[0] = guilds.subList(0,24)
                page[1] = guilds.subList(25,49)
                page[2] = guilds.subList(50, guilds.size-1)
            }
            (guilds.size > 75 && guilds.size <= 100) -> {
                page[0] = guilds.subList(0,24)
                page[1] = guilds.subList(25,49)
                page[2] = guilds.subList(50,74)
                page[3] = guilds.subList(75, guilds.size-1)
            }
            (guilds.size > 100 && guilds.size <= 125) -> {
                page[0] = guilds.subList(0,24)
                page[1] = guilds.subList(25,49)
                page[2] = guilds.subList(50,74)
                page[3] = guilds.subList(75,99)
                page[4] = guilds.subList(100, guilds.size-1)
            }
        }

        /* Find out what page the user is on */
        for (i in page.indices) {
            for (guild in page[i]!!) {
                if (guild.equals(targetGuild)) selectedPage = i
            }
        }

        /* Go to requested page */
        if (!state && selectedPage != 0)
            selectedPage--
            else
            selectedPage++

        /* Generate new array of SelectOptions */
        val tempOptions = ArrayList<SelectOption>()
        for (guild in page[selectedPage]!!) {
            tempOptions.add(SelectOption.of(guild.name, "guildSelect:${guild.idLong}"))
        }

        return tempOptions
    }
}