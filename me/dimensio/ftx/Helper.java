package me.dimensio.ftx;

import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

public class Helper {
    
    private final FreezeTag plugin;
    
    public ChatColor GREEN = ChatColor.DARK_GREEN;
    public String PREFIX = ChatColor.DARK_GREEN + "[FreezeTagX] "; // Prefix to go in front of all log entries
    public Logger log = Logger.getLogger("Minecraft"); // Minecraft log and console
    
    public Helper(FreezeTag instance) {
        plugin = instance;
    }
    
    public void getRules(Player player) {
        PluginDescriptionFile pdfFile = plugin.getDescription();
        player.sendMessage(" ");
        player.sendMessage(GREEN + "FreezeTagX v" + ChatColor.WHITE + pdfFile.getVersion() + GREEN + " rules:");
        player.sendMessage(" ");
        player.sendMessage(GREEN + "Remember playing Freeze Tag as a kid? This is the same, but in Minecraft!");
        player.sendMessage(GREEN + "If there is a game in progress, type '/ftx join' (without quotes) to register for the game.");
        player.sendMessage(" ");
        player.sendMessage(GREEN + "When the game begins, you will be told who the " + ChatColor.WHITE + "CHASERS" + GREEN + " are.");
        player.sendMessage(" ");
        player.sendMessage(GREEN + "If you're a chaser, find un-frozen players and tag them to freeze them. You win by freezing every player.");
        player.sendMessage(" ");
        player.sendMessage(GREEN + "If you're not a chaser, find your frozen brothers and tag them to unfreeze them! You win by surviving until the game ends.");
        player.sendMessage(" ");
    }
    
    
}
