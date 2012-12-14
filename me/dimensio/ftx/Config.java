package me.dimensio.ftx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config extends JavaPlugin {
    
    private final FreezeTag plugin;
    public FileConfiguration customConfig = null ;
	public File customConfigFile= null;
	public FileConfiguration customConfig2= null ;
	public File customConfigFile2= null;
	public boolean verbose = false;
    
    

 
    
    //Various variables
    public int item;
    public int defaultTime;
    
    //Lobby variables
    public boolean lobby;
    public String lobby_world;
    public String lobby_area1;
    public String lobby_area2;
    
    //Arena variables
    public boolean arena;
    public String arena_world;
    public String arena_area1;
    public String arena_area2;
    public int numOfSpawns;
    public List<String> spawns;
    
    public Config(FreezeTag instance) {
        plugin = instance;
    }
    
    public void doConfig() {
      this.getCustomConfig().options().copyDefaults(true);
      saveCustomConfig();
 
 
        item = customConfig.getInt("listItem", 359);
        defaultTime = customConfig.getInt("defaultTimeLimit", 5);}
    
    public void doArena() {
     this.getArenaConfig().options().copyDefaults(true);
     saveArenaConfig();
  
        arena = customConfig2.getBoolean("arena.area.defined");
        lobby = customConfig2.getBoolean("arena.lobby.defined");
        numOfSpawns = customConfig2.getInt("arena.spawns.amount");
        
 
        if (arena) {
            arena_world = customConfig2.getString("arena.area.world");
            arena_area1 = customConfig2.getString("arena.area.p1");
            arena_area2 = customConfig2.getString("arena.area.p2");
            if (arena_world == null || arena_area1 == null || arena_area2 == null) arena = false;}
        if (lobby) {
            lobby_world = customConfig2.getString("arena.lobby.world");
            lobby_area1 = customConfig2.getString("arena.lobby.p1");
            lobby_area2 = customConfig2.getString("arena.lobby.p2");
            if (lobby_world == null || lobby_area1 == null || lobby_area2 == null) lobby = false;}
        if (numOfSpawns > 0) {
            spawns = customConfig2.getStringList("arena.spawns");
            spawns.remove("amount");
            numOfSpawns = spawns.size();}}
    
    public boolean saveLobby() {
            if (lobby_area1 == null || lobby_area2 == null || lobby_world == null) return false;
            customConfig2.set("arena.lobby.defined", lobby);
            customConfig2.set("arena.lobby.world", lobby_world);
            customConfig2.set("arena.lobby.p1", lobby_area1);
            customConfig2.set("arena.lobby.p2", lobby_area2);
            									 return true;
            									 }
    
    public boolean saveArena() {
            if (arena_area1 == null || arena_area2 == null || arena_world == null) return false;
            customConfig2.set("arena.area.defined", arena);
            customConfig2.set("arena.area.world", arena_world);
            customConfig2.set("arena.area.p1", arena_area1);
            customConfig2.set("arena.area.p2", arena_area2);
        return true;
   }
    
    public void clearArena() {
        

        if (new File(plugin.getDataFolder(), "arena.yml").exists()) {
            customConfig2.set("arena.area.defined", false);
            customConfig2.set("arena.area.world", 0);
            customConfig2.set("arena.area.p1", 0);
            customConfig2.set("arena.area.p2", 0);
            arena = false;
        }
 public void clearLobby() {
        

        if (new File(plugin.getDataFolder(), "arena.yml").exists()) {
            customConfig2.set("arena.lobby.defined", false);
            customConfig2.set("arena.lobby.world", 0);
            customConfig2.set("arena.lobby.p1", 0);
            customConfig2.set("arena.lobby.p2", 0);
            lobby = false;
        }
    }
    public void reloadCustomConfig() {
        if (customConfigFile == null) {
        customConfigFile = new File(plugin.getDataFolder(), "config.yml");
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
     
        // Look for defaults in the jar
        InputStream defConfigStream = plugin.getResource("config.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            customConfig.setDefaults(defConfig);
        }
    }
    public void reloadArenaConfig() {
        if (customConfigFile2 == null) {
        customConfigFile2 = new File(plugin.getDataFolder(), "arena.yml");
        }
        customConfig2 = YamlConfiguration.loadConfiguration(customConfigFile2);
     
        // Look for defaults in the jar
        InputStream defConfigStream = plugin.getResource("arena.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            customConfig2.setDefaults(defConfig);
        }
    }
    public FileConfiguration getCustomConfig() {
        if (customConfig == null) {
            this.reloadCustomConfig();
        }
        return customConfig;
    }
    public FileConfiguration getArenaConfig() {
        if (customConfig2 == null) {
            this.reloadArenaConfig();
        }
        return customConfig2;
    }
    public void saveCustomConfig() {
        if (customConfig == null || customConfigFile == null) {
        return;
        }
        try {
            getCustomConfig().save(customConfigFile);
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
        }
    }
    public void saveArenaConfig() {
        if (customConfig2 == null || customConfigFile2 == null) {
        return;
        }
        try {
            getArenaConfig().save(customConfigFile2);
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile2, ex);
        }
    }
    
}

