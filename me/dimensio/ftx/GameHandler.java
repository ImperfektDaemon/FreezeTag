package me.dimensio.ftx;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GameHandler {
    
    private final FreezeTag plugin;
    private final Config config;
    
    public String PREFIX = ChatColor.DARK_GREEN + "[FreezeTagX] ";
    public String ERR_PREFIX = ChatColor.RED + "[FreezeTagX] ";
    
    int time = 5;
    
    public HashMap<Player, ItemStack[]> inventories = new HashMap<Player, ItemStack[]>();
    public HashMap<Player, GameMode> gamemode = new HashMap<Player, GameMode>();
    
    public GameHandler(FreezeTag instance, Config config) {
        plugin = instance;
        
        this.config = config;
    }
    
    public void startGame(Player player) {
    	int minimum = config.getCustomConfig().getInt("MinimumPlayer");
        if (!plugin.inRegistration && !plugin.inGame) {
            if (player.hasPermission("ftx.admin.reg")) {
                plugin.inRegistration = true;
                Bukkit.getServer().broadcastMessage(PREFIX + "A new game of Freeze Tag has begun! To join, type /ftx join");
                plugin.players.put(player, "Regular");
                plugin.numOfPlayers++;
                if (config.lobby) {
                    plugin.oldLocations.put(player, player.getLocation());
                    telePlayerToLobby(player);
                }
                player.sendMessage(ChatColor.RED + "[FreezeTagX] You've started a new game, and successfully registered to play. To start the game, type /ftx begin with at least " +minimum+ " players registered.");
            } else {
                player.sendMessage(ERR_PREFIX + "You do not have permission to do that.");
            }
        } else {
            if (plugin.inRegistration) {
                player.sendMessage(ERR_PREFIX + "There is already a game in the registration stage! To join, type /ftx join");
            } else {
                player.sendMessage(ERR_PREFIX + "There is already a game in progress! Wait for this game to finish before starting a new one.");
            }
        }
    }
    
    public void listPlayers(Player player) {
        if (!plugin.inGame && !plugin.inRegistration) return;
        
        Iterator<Player> i = plugin.players.keySet().iterator();
        player.sendMessage(" ");
        player.sendMessage(PREFIX + "Current player list:");
        
        while (i.hasNext()) {
            Player current = i.next();
            String status;
            if (plugin.players.get(current).equalsIgnoreCase("FROZEN")) {
                status = ChatColor.BLUE + "FROZEN";
            } else if (plugin.players.get(current).equalsIgnoreCase("Chaser")) {
                status = ChatColor.RED + "Chaser";
            } else {
                status = ChatColor.GREEN + "Un-frozen";
            }
            player.sendMessage(PREFIX + ChatColor.YELLOW + current.getName() + ChatColor.DARK_GREEN + " (Status: " + status + ChatColor.DARK_GREEN + ")");
            
        }
    }
    
    public void joinGame(Player player) {
    	
        if (plugin.inRegistration && !plugin.inGame) {
            if (player.hasPermission("ftx.users.join")) {
                if (plugin.players.containsKey(player) == false) {
                    plugin.players.put(player, "Regular");
                    if (config.lobby){
                       telePlayerToLobby(player);
                    } else{
                    	 plugin.oldLocations.put(player, player.getLocation());
                    }
                    plugin.numOfPlayers++;
                    Bukkit.getServer().broadcastMessage(PREFIX + ChatColor.YELLOW + player.getName() + ChatColor.DARK_GREEN + " has registered! There are currently " + ChatColor.YELLOW + plugin.players.size() + ChatColor.DARK_GREEN + " players registered.");
                    player.sendMessage(ERR_PREFIX + "You've successfully registered to play. There are currently " + ChatColor.YELLOW + plugin.players.size() + ChatColor.RED + " players registered.");
                } else {
                    player.sendMessage(ERR_PREFIX + "You're already registered! To un-register, type /ftx unreg");
                }
            } else {
                player.sendMessage(ERR_PREFIX + "You do not have permission to do that.");
            }
        } else {
            if (!plugin.inRegistration && !plugin.inGame) {
                player.sendMessage(ERR_PREFIX + "There isn't a game registration in progress. Get an admin to start the game for you.");
            } else if (!plugin.inRegistration && plugin.inGame) {
                player.sendMessage(ERR_PREFIX + "There's already a game in progress. Wait until the next game starts.");
            }
        }
    }
    
    public void cleanUpGame() {
       
        this.unTelePlayers(true);
        Bukkit.getServer().getScheduler().getPendingTasks().clear();
        Bukkit.getServer().getScheduler().cancelTasks(plugin);
        this.restoreInventories();
        plugin.numOfPlayers = 0;
        plugin.numOfChasers = 0;
        plugin.numOfFrozen = 0;
        plugin.inGame = false;
        plugin.inRegistration = false;
        plugin.inCountdown = false; 
        plugin.players.clear();
    }
    
    public void unreg(Player player) {
        if (!plugin.inRegistration) return;
        
        if (plugin.inRegistration && !plugin.inCountdown) {
            plugin.players.remove(player);
            Bukkit.getServer().broadcastMessage(ERR_PREFIX + ChatColor.YELLOW + player.getName() + ChatColor.RED + " has un-registered from the current game!");
            if (plugin.numOfPlayers == 1) {
                Bukkit.getServer().broadcastMessage(ERR_PREFIX + "There are no more players left in the game! The game has ended.");
                plugin.inRegistration = false;
            }
            this.unTelePlayer(player, true);
            plugin.numOfPlayers--;
        } else {
            player.sendMessage(ERR_PREFIX + "You can't leave a game in the countdown.");
        }
    }
    
    public void beginGame(Player player, int timeLimit) {
    	int minimum = config.getCustomConfig().getInt("MinimumPlayer");
        if (!plugin.inRegistration || plugin.inGame) return;
        
        if (player.hasPermission("ftx.admin.begin")) {
            if (plugin.numOfPlayers < minimum) {
                player.sendMessage(ERR_PREFIX + "There are not enough players to begin the game. You need at least " + ChatColor.YELLOW + "3" + ChatColor.RED + " players.");
            } else {
                plugin.inRegistration = false;
                plugin.inCountdown = true;

                //How many chasers will we have?
                plugin.numOfChasers = (int)Math.floor(plugin.numOfPlayers / minimum);
                Object[] key = plugin.players.keySet().toArray();
                
                HashSet<Player> chasers = new HashSet<Player>();

                //Assign the chasers
                for (int x = 1; x <= plugin.numOfChasers; x++) {
                    Random random = new Random();
                    int chaser = random.nextInt(plugin.numOfPlayers);

                    //Have we selected somebody who is already a chaser?
                    while (plugin.players.get((Player) key[chaser]).equalsIgnoreCase("Chaser")) {
                        chaser = random.nextInt(plugin.numOfPlayers);
                    }

                    Player chaserP = (Player) key[chaser];

                    plugin.players.put(chaserP, "Chaser");
                    chasers.add(chaserP);
                    chaserP.sendMessage(PREFIX + "You are a " + ChatColor.YELLOW + "CHASER" + ChatColor.DARK_GREEN + "! Freeze other players by punching them!");
                    chaserP.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1, 6));
                    chaserP.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1, 6));
                }
                
                String message = PREFIX + "The chasers are: ";
                Object[] arr = chasers.toArray();
                
                for (int i = 0; i < chasers.size(); i++) {
                    Player p = (Player) arr[i];
                    message += ChatColor.YELLOW + p.getName();
                    if (i == (chasers.size() - 1)) {
                        message += ChatColor.DARK_GREEN + ". ";
                    } else {
                        message += ChatColor.DARK_GREEN + ", ";
                    }
                }
                
                Bukkit.getServer().broadcastMessage(message);
                
                if (!config.lobby) {
                    plugin.oldLocations.put(player, player.getLocation());
                }
                
                telePlayersToArena();
                
                this.storeInventories();
                
                if (config.verbose) System.out.println(PREFIX + "Starting game!");
                
                this.startCountdown(timeLimit);
            }
        }
    }
    
    public void startCountdown(int timeLimit) {
        time = timeLimit;
        if (plugin.inCountdown) {
            Bukkit.getServer().broadcastMessage(PREFIX + "The game is about to begin!");
            Bukkit.getServer().broadcastMessage(PREFIX + "3..");
            Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    Bukkit.getServer().broadcastMessage(PREFIX + "2..");
                }
            }, 30L);
            Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    Bukkit.getServer().broadcastMessage(PREFIX + "1..");
                }
            }, 60L);
            Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    plugin.inGame = true;
                    Bukkit.getServer().broadcastMessage(PREFIX + "GO!");
                    plugin.inCountdown = false;
                    Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                        public void run() {
                            Bukkit.getServer().broadcastMessage(PREFIX + "Hurry, chasers! You only have " + ChatColor.RED + "ONE MINUTE" + ChatColor.DARK_GREEN + " left!");
                        }
                    }, ((time - 1) * 60) * 20);
                    Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                        public void run() {
                            plugin.gameHandler.regularVictory();
                        }
                    }, (time * 60) * 20);
                }
            }, 90L);
            
        }
    }
    
    public void cancelCountdown(Player player) {
        if (player.hasPermission("ftx.admin.cancel")) {
            if (plugin.inCountdown) {
                Bukkit.getServer().getScheduler().cancelTasks(plugin);
                plugin.inGame = false;
                plugin.inCountdown = false;
                plugin.inRegistration = true;
                plugin.numOfChasers = 0;
                plugin.numOfFrozen = 0;
                if (config.lobby) this.telePlayersToLobby();
                else this.unTelePlayers(false);
                Bukkit.getServer().broadcastMessage(PREFIX + ChatColor.YELLOW + player.getName() + " cancelled the countdown!");
            }
        }
    }
    
    public boolean checkVictory() {
        if (!plugin.inGame) return false;
        
        if (plugin.numOfFrozen == (plugin.numOfPlayers - plugin.numOfChasers)) return true;
        
        return false;
    }
    
    public void victory() {
        if (config.verbose) System.out.println(PREFIX + "The game is over! The chasers have won!");
        if (this.checkVictory()) {
            Bukkit.getServer().broadcastMessage(PREFIX + "The game is over! The chasers have won!");
            
          
                }
            
           
            this.cleanUpGame();
        }
    
    
    public void regularVictory() {
        if (config.verbose) System.out.println(PREFIX + "The game is over! The regulars have won!");
        if (!this.checkVictory()) {
            Bukkit.getServer().broadcastMessage(PREFIX + "The game is over! The regulars have won!");
            
           
            this.cleanUpGame();
        }
    }
    
    public boolean storeInventories() {
        if(plugin.players == null || plugin.players.isEmpty()) return false;
        
        Iterator<Player> i = plugin.players.keySet().iterator();
        
        while (i.hasNext()) {
            Player p = (Player) i.next();
            GameMode playermode = p.getGameMode();
            ItemStack[] pI = p.getInventory().getContents();
           
           
           
            inventories.put(p, pI); 
            gamemode.put(p, playermode);
            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().clear();
            p.getInventory().addItem(new ItemStack(config.item, 1));
        }
        return true;
    }
    
    public boolean restoreInventories() {
        if (inventories == null || inventories.isEmpty()) return false;
        
        Iterator<Player> i = inventories.keySet().iterator();
        
        while (i.hasNext()) {
            Player p = (Player) i.next();
            p.getInventory().setContents(inventories.get(p));
            p.setGameMode(gamemode.get(p));
        }
        inventories.clear();
        return true;
    }
    
    public boolean restoreInventory(Player player) {
        if (inventories == null || inventories.isEmpty() || !inventories.containsKey(player)) return false;
        
        player.getInventory().setContents(inventories.get(player));
        inventories.remove(player);
        return true;
    }
    
    public void telePlayersToArena() {
        if (!config.arena) return;
        Iterator<Player> i = plugin.players.keySet().iterator();
        
        while (i.hasNext()) {
            Player p = i.next();
            
            //if (config.numOfSpawns == 0 || config.spawns == null) {
                int[][] arr = new int[2][3];
                try {
                    String[] p1 = config.arena_area1.split(",");
                    String[] p2 = config.arena_area2.split(",");
                    arr = Arena.parseMinMax(p1, p2);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                Random random = new Random();
                int xGap = (arr[1][0] - arr[0][0]) - 2;
                int toX = (arr[0][0] + 1) + random.nextInt(xGap);
                int zGap = (arr[1][2] - arr[0][2]) - 2;
                int toZ = (arr[0][2] + 1) + random.nextInt(zGap);
                int toY = arr[0][1] + 1;
                World w = Bukkit.getServer().getWorld(config.arena_world);
                while (w.getBlockAt(toX, toY -1, toZ).isLiquid() || w.getBlockAt(toX, toY, toZ).isLiquid()) {
                    toX = arr[0][0] + random.nextInt(arr[1][0] - arr[0][0]);
                    toZ = arr[0][2] + random.nextInt(arr[1][2] - arr[0][2]);
                }
                
                while (w.getBlockAt(toX, toY, toZ).getType() != Material.AIR) {
                    toY = toY + 1;
                }
                p.teleport(new Location(w, toX, toY, toZ));
                p.setFlying(false);
        }
    }
    
    public void telePlayersToLobby() {
        if (!config.lobby) return;
        
        Iterator<Player> i = plugin.players.keySet().iterator();
        
        while (i.hasNext()) {
            Player p = i.next();
            
            int[][] arr = new int[2][3];
            try {
                String[] p1 = config.lobby_area1.split(",");
                String[] p2 = config.lobby_area2.split(",");
                arr = Arena.parseMinMax(p1, p2);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            Random random = new Random();
            int xGap = (arr[1][0] - arr[0][0]) - 2;
            int toX = (arr[0][0] + 1) + random.nextInt(xGap);
            int zGap = (arr[1][2] - arr[0][2]) - 2;
            int toZ = (arr[0][2] + 1) + random.nextInt(zGap);
            int toY = arr[0][1] + 1;
            World w = Bukkit.getServer().getWorld(config.lobby_world);
            while (w.getBlockAt(toX, toY, toZ).getType() == Material.AIR) {
                toY = toY - 1;
            }
            while (w.getBlockAt(toX, toY -1, toZ).getType() == Material.WATER || w.getBlockAt(toX, toY -1, toZ).getType() == Material.STATIONARY_WATER || w.getBlockAt(toX, toY -1, toZ).getType() == Material.STATIONARY_LAVA || w.getBlockAt(toX, toY -1, toZ).getType() == Material.LAVA) {
                toX = arr[0][0] + random.nextInt(arr[1][0] - arr[0][0]);
                toZ = arr[0][2] + random.nextInt(arr[1][2] - arr[0][2]);
            }

            while (w.getBlockAt(toX, toY, toZ).getType() != Material.AIR) {
                toY = toY + 1;
            }
            p.teleport(new Location(w, toX, toY, toZ));
        }
    }
    
    public void telePlayerToLobby(Player p) {
        if (!config.lobby) return;
            
        int[][] arr = new int[2][3];
        try {
            String[] p1 = config.lobby_area1.split(",");
            String[] p2 = config.lobby_area2.split(",");
            arr = Arena.parseMinMax(p1, p2);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        Random random = new Random();
        int xGap = (arr[1][0] - arr[0][0]) - 2;
        int toX = (arr[0][0] + 1) + random.nextInt(xGap);
        int zGap = (arr[1][2] - arr[0][2]) - 2;
        int toZ = (arr[0][2] + 1) + random.nextInt(zGap);
        int toY = arr[0][1] + 1;
        World w = Bukkit.getServer().getWorld(config.lobby_world);
        while (w.getBlockAt(toX, toY, toZ).getType() == Material.AIR) {
            toY = toY - 1;
        }
        while (w.getBlockAt(toX, toY -1, toZ).getType() == Material.WATER || w.getBlockAt(toX, toY -1, toZ).getType() == Material.STATIONARY_WATER || w.getBlockAt(toX, toY -1, toZ).getType() == Material.STATIONARY_LAVA || w.getBlockAt(toX, toY -1, toZ).getType() == Material.LAVA) {
            toX = arr[0][0] + random.nextInt(arr[1][0] - arr[0][0]);
            toZ = arr[0][2] + random.nextInt(arr[1][2] - arr[0][2]);
        }

        while (w.getBlockAt(toX, toY, toZ).getType() != Material.AIR) {
            toY = toY + 1;
        }
        p.teleport(new Location(w, toX, toY, toZ));
    }
    
    public void unTelePlayers(boolean clear) {
        if (plugin.oldLocations == null) return;
        
        Iterator<Player> i = plugin.oldLocations.keySet().iterator();
        
        while (i.hasNext()) {
            Player player = i.next();
            player.teleport(plugin.oldLocations.get(player));
        }
        plugin.oldLocations.clear();
    }
    
    public void unTelePlayer(Player p, boolean clear) {
        if (plugin.oldLocations == null || !plugin.oldLocations.containsKey(p)) return;
        
        p.teleport(plugin.oldLocations.get(p));
        if (clear) plugin.oldLocations.remove(p);
    }
    
}
