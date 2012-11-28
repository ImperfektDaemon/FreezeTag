package me.dimensio.ftx;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class FreezeTag extends JavaPlugin {
    
    //MySQL handlers
    
    
    //MySQL settings variables
	private final Config config = new Config(this);
    private final Helper helper = new Helper(this);
    public final GameHandler gameHandler = new GameHandler(this, config);
    
    private final EntityListener el = new EntityListener(this, gameHandler);
    private final PlayerListener pl = new PlayerListener(this, gameHandler, config);
    private final BlockListener bl = new BlockListener(this, config);
    
    private final CommandListener cmdHandler = new CommandListener(this, gameHandler, config, helper);
    
    public String logPrefix = "[FreezeTagX] "; // Prefix to go in front of all log entries
    public static final Logger log = Logger.getLogger("Minecraft"); // Minecraft log and console
    
    public boolean inGame = false;
    public boolean inCountdown = false;
    public boolean inRegistration = false;
    public boolean inAreaMode = false;
    public Player areaPlayer;
    public Arena arena;
    public enum areaMode { LOBBY_1, LOBBY_2, ARENA_1, ARENA_2, NONE }
    public areaMode mode = areaMode.NONE;
    public int numOfPlayers = 0;
    public int numOfFrozen = 0;
    public int numOfChasers = 0;
    public HashMap<Player, Location> oldLocations = new HashMap<Player, Location>();
    
    //Global player list.
    public HashMap<Player, String> players = new HashMap<Player, String>();
    
    @Override
    public void onDisable() {
        if (arena != null) {
            arena.tearDown();
            config.clearArena();
        }

        Bukkit.getServer().getScheduler().cancelTasks(this);
        gameHandler.cleanUpGame();
        log.info(logPrefix + "disabled.");
    }
    
    @Override
    public void onEnable() {
    	PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(bl, this);
        pm.registerEvents(el, this);
        pm.registerEvents(pl, this);
       
    	
    	cmdHandler.setupCommands();
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(logPrefix + "version v" + pdfFile.getVersion() + " is enabled.");
         config.doConfig();  
         config.doArena();
          
        
    }
  
  
    
}
