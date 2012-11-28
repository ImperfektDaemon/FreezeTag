package me.dimensio.ftx;

import java.text.DecimalFormat;
import java.util.Random;

import me.dimensio.ftx.FreezeTag.areaMode;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import org.bukkit.entity.Player;

public class CommandListener {
    
    private final FreezeTag plugin;
    private final GameHandler gameHandler;
    private final Config config;
 
    private final Helper helper;
    
    public String PREFIX = ChatColor.DARK_GREEN + "[FreezeTagX] ";
    public String ERR_PREFIX = ChatColor.RED + "[FreezeTagX] ";
    
    public CommandListener(FreezeTag instance, GameHandler game, Config config, Helper helper) {
        plugin = instance;
        gameHandler = game;
        this.config = config;
      
        this.helper = helper;
    }
    
    public void setupCommands() {
        PluginCommand ftx = plugin.getCommand("ftx");
        CommandExecutor commandExecutor = new CommandExecutor() {
            public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
                if (sender instanceof Player) {
                    if (args.length > 0) {
                        commandHandler((Player)sender, args);
                    }
                }
                
                  
                return true;
            }
        };
        if (ftx != null) {
            ftx.setExecutor(commandExecutor);
        }
    }
    
    public void commandHandler(Player player, String[] args) {
        if (args[0].equalsIgnoreCase("reg")) {
            gameHandler.startGame(player);
        } else if (args[0].equalsIgnoreCase("join")) {
            gameHandler.joinGame(player);
        } else if (args[0].equalsIgnoreCase("unreg")) {
            gameHandler.unreg(player);
        } else if (args[0].equalsIgnoreCase("begin")) {
            if (args.length > 1) {
                gameHandler.beginGame(player, Integer.parseInt(args[1]));
            } else {
                gameHandler.beginGame(player, config.defaultTime);
            }
        } else if (args[0].equalsIgnoreCase("cancel")) {
            gameHandler.cancelCountdown(player);
        } else if (args[0].equalsIgnoreCase("list")) {
            gameHandler.listPlayers(player);
        } else if (args[0].equalsIgnoreCase("define")) {
            if (args.length < 1) {
                player.sendMessage(ERR_PREFIX + "You must specify an argument.");
            } else {
                if (player.hasPermission("ftx.admin.define")) {
                    if (args[1].equalsIgnoreCase("lobby")) {
                        plugin.inAreaMode = true;
                        plugin.mode = areaMode.LOBBY_1;
                        plugin.areaPlayer = player;
                        player.sendMessage(PREFIX + "You're in lobby define mode! Punch the first block of your cuboid.");
                    } else if (args[1].equalsIgnoreCase("arena")) {
                        plugin.inAreaMode = true;
                        plugin.mode = areaMode.ARENA_1;
                        plugin.areaPlayer = player;
                        player.sendMessage(PREFIX + "You're in arena define mode! Punch the first block of your cuboid.");
                    }
                }
            }
        } else if (args[0].equalsIgnoreCase("generate")) {
            if (player.hasPermission("ftx.admin.generate"));
            
            if (args.length == 4) {
                Location loc = player.getLocation();
                plugin.arena = Arena.build(new Location(loc.getWorld(), loc.getX(), loc.getY()-1, loc.getZ()), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), new Random());
                String[] bounds = plugin.arena.getBounds();
                config.arena = true;
                config.arena_area1 = bounds[0];
                config.arena_area2 = bounds[1];
                config.arena_world = player.getWorld().getName();
                config.saveArena();
                player.sendMessage(PREFIX + "Arena generated!");
            }
        } else if (args[0].equalsIgnoreCase("teardown")) {
            if (player.hasPermission("ftx.admin.teardown"));
            
            if (plugin.arena == null) return;
            
            plugin.arena.tearDown();
            config.clearArena();
            plugin.arena = null;
        } else if (args[0].equalsIgnoreCase("freeze")) {
            if (args.length == 1) { 
                player.sendMessage(ERR_PREFIX + "You must define a player to freeze."); 
                return; 
            }
            
            if (player.hasPermission("ftx.admin.freeze"));
            
            if (!plugin.inGame && !plugin.inRegistration) return;
            
            Player p = Bukkit.getServer().getPlayer(args[1]);
            
            if (!plugin.players.containsKey(p) || plugin.players.get(p).equalsIgnoreCase("FROZEN")) return;
            
            plugin.players.put(p, "FROZEN");
            p.sendMessage(PREFIX + "You've been frozen by " + ChatColor.YELLOW + player.getName() + ChatColor.DARK_GREEN + "!");
            player.sendMessage(PREFIX + "You've frozen " + ChatColor.YELLOW + args[1] + ChatColor.DARK_GREEN + "!");
        } else if (args[0].equalsIgnoreCase("unfreeze")) {
            if (args.length == 1) { 
                player.sendMessage(ERR_PREFIX + "You must define a player to unfreeze."); 
                return; 
            }
            
            if (player.hasPermission("ftx.admin.freeze")) ;
            
            if (!plugin.inGame && !plugin.inRegistration) return;
            
            Player p = Bukkit.getServer().getPlayer(args[1]);
            
            if (!plugin.players.containsKey(p) || plugin.players.get(p).equalsIgnoreCase("Regular")) return;
            
            plugin.players.put(p, "Regular");
            p.sendMessage(PREFIX + "You've been un-frozen by " + ChatColor.YELLOW + player.getName() + ChatColor.DARK_GREEN + "!");
            player.sendMessage(PREFIX + "You've un-frozen " + ChatColor.YELLOW + args[1] + ChatColor.DARK_GREEN + "!");
        } else if (args[0].equalsIgnoreCase("forcereg")) {
            if (args.length == 1) {
                player.sendMessage(ERR_PREFIX + "You must define a player to force register.");
                return;
            }
            
            if (player.hasPermission("ftx.admin.forcereg"));
            
            if (!plugin.inRegistration) return;
            
            Player p = Bukkit.getServer().getPlayer(args[1]);
            
            if (!p.isOnline()) return;
            
            if (plugin.players == null || plugin.players.isEmpty()) return;
            
            if (plugin.players.containsKey(p)) {
                player.sendMessage(ERR_PREFIX + "That player is already registered!");
                return;
            }
            
            gameHandler.joinGame(p);
            player.sendMessage(PREFIX + "You've successfully forced " + ChatColor.YELLOW + p.getName() + ChatColor.DARK_GREEN + " to register.");
        } else if (args[0].equalsIgnoreCase("endgame")) {
            if (player.hasPermission("ftx.admin.endgame"));
            
            if (plugin.inGame || plugin.inRegistration) {
                gameHandler.cleanUpGame();
                Bukkit.getServer().broadcastMessage(PREFIX + ChatColor.YELLOW + player.getName() + ChatColor.DARK_GREEN + " has ended the current game!");
            }
       
        } else if (args[0].equalsIgnoreCase("lobby")) {
            if (!plugin.inRegistration || !plugin.players.containsKey(player));
            
            gameHandler.telePlayerToLobby(player);
        } else if (args[0].equalsIgnoreCase("rules")) {
            helper.getRules(player);

        }
    }
    
    double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }
    
}
