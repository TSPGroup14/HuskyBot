package huskybot;

import huskybot.cmdFramework.CommandLoader;
import huskybot.handlers.*;
import huskybot.utils.ConfigLoader;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.IOException;
import java.util.EnumSet;

public class HuskyBot {

    /* Bot startup */

    public static final long startTime = System.currentTimeMillis();

    //Logging
    public static final Logger log = LoggerFactory.getLogger("HuskyBot");
    private static final ConfigLoader config = ConfigLoader.Companion.load();

    public static final Color color = config.getEmbedColour();

    public static ShardManager shardManager;

    public static void main(String[] args) throws LoginException, IOException {

        Thread.currentThread().setName("HuskyBot");

        final String jarLocation = HuskyBot.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        System.setProperty("kotlin.script.classpath", jarLocation);

        final String Token = config.getToken();

        /* Build the bot */
        final DefaultShardManagerBuilder shardManagerBuilder = DefaultShardManagerBuilder.create(Token, EnumSet.allOf(GatewayIntent.class))
                .setShardsTotal(-1)                                                 //Lets discord automate the amount of shards we need
                .setMemberCachePolicy(MemberCachePolicy.ALL)                        //Cache all members
                .enableIntents(GatewayIntent.GUILD_MEMBERS)                         //Enable privileged gateway
                .addEventListeners( new CommandHandler(), new EventHandler())       //Start event listeners
                .setStatus(OnlineStatus.DO_NOT_DISTURB)                             //Set bot status
                .setActivity(Activity.watching("Protecting Your Server"));                   //Might as well rep the school


        shardManager = shardManagerBuilder.build();

        /* Load Commands */
        CommandLoader cmdLoader = new CommandLoader();
        cmdLoader.loadCommands(shardManager.retrieveApplicationInfo().getJDA());

    }
}
