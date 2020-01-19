package eu.hypecraft.skyblock;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.math.transform.Transform;


public class Main extends JavaPlugin {
	public static HashMap<UUID,ArrayList<Integer>> islandsMembers = new HashMap<UUID,ArrayList<Integer>>();
	public static HashMap<UUID,Integer> islandOwner = new HashMap<UUID,Integer>();
	public static HashMap<Integer,Integer[]> islandsCuboidPositions = new HashMap<Integer,Integer[]>();
	public static HashMap<Integer,Double[]> islandsHomePositions = new HashMap<Integer,Double[]>();
	public static HashMap<Integer,ArrayList<Integer>> visitorsPermissions = new HashMap<Integer,ArrayList<Integer>>();
	
	public static HashMap<UUID,Integer> teleportRequests = new HashMap<UUID,Integer>();
	public static ArrayList<UUID> waitingForTeleport = new ArrayList<UUID>();
	public static ArrayList<UUID> abortedRequests = new ArrayList<UUID>();
	public static HashMap<UUID,ArrayList<UUID>> ignoreTeleportations = new HashMap<UUID,ArrayList<UUID>>();
	public static HashMap<Integer,HashMap<UUID,Integer>> invitesToIsland = new HashMap<Integer,HashMap<UUID,Integer>>();
	public static HashMap<UUID,ArrayList<UUID>> ignoreInvitations = new HashMap<UUID,ArrayList<UUID>>();
	public static World islandworld = Bukkit.getWorld("SkyBlock");
	
	public static ArrayList<String> arguments = new ArrayList<String>();
	
	
	@Override
	public void onEnable() {
		System.out.println("Loading data...");
		try {
			Connection conn = DBQuery.ConnectDB();
			ResultSet results = DBQuery.getResults(conn, "select * from members");
			while(results.next()) {
				ArrayList<Integer> list;
					if(islandsMembers.containsKey(UUID.fromString(results.getString("uuid")))){
						list = islandsMembers.get(UUID.fromString(results.getString("uuid")));
					} else {
						list = new ArrayList<Integer>();
					}
					list.add(results.getInt("id"));
					islandsMembers.put(UUID.fromString(results.getString("uuid")), list);
			}
			results = DBQuery.getResults(conn, "select * from islands where owner != '';");
			Integer[] cuboid = new Integer[4];
			Double[] home = new Double[3];
			while(results.next()) {
				islandOwner.put(UUID.fromString(results.getString("owner")), results.getInt("id"));
				cuboid[0] = results.getInt("x_pos") - (results.getInt("size") / 2);
				cuboid[1] = results.getInt("z_pos") - (results.getInt("size") / 2);
				cuboid[2] = results.getInt("x_pos") + (results.getInt("size") / 2);
				cuboid[3] = results.getInt("z_pos") + (results.getInt("size") / 2);
				islandsCuboidPositions.put(results.getInt("id"), cuboid);
				home[0] = results.getDouble("home_x_pos");
				home[1] = results.getDouble("home_y_pos");
				home[2] = results.getDouble("home_z_pos");
				islandsHomePositions.put(results.getInt("id"), home);
				
				int temp = results.getInt("visitor_permissions");
     			String temp2 = Integer.toBinaryString(temp);
     			while(temp2.length() < 3) {
     				temp2 = "0" + temp2;
     			}
     			ArrayList<Integer> vperms = new ArrayList<Integer>();
     			for(int a = 0; a < 3;a++) {
         			if(Integer.parseInt(String.valueOf(temp2.charAt(a))) == 1) {
         				vperms.add(a + 1);
         			}
     			}
     			Main.visitorsPermissions.put(results.getInt("id"), vperms);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
        System.out.println("Data has been loaded.");
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerListener(), this);
        /*
        Permission p = new Permission("skyblock.managespawnbuilders");
        pm.addPermission(p);
        */
        arguments.add("<cp|controlpanel>");
        arguments.add("create");
        arguments.add("delete");
        arguments.add("<home|go>");
        arguments.add("sethome");
        arguments.add("spawn");
        arguments.add("fixhome");
        arguments.add("tp");
        arguments.add("add");
        arguments.add("remove");
        arguments.add("join");
        arguments.add("reject");
        arguments.add("ignore");
        arguments.add("unignore");
        arguments.add("tpaccept");
        arguments.add("tpdeny");
        arguments.add("tpignore");
        arguments.add("tpunignore");
	}
	
	
	@Override
	public void onDisable() {
		System.out.println("Disabling SkyBlock...");
	}
	
	
	/*
	public static void pasteSchematic(Location loc, Schematic schematic)
	    {
	        Material[] blocks = schematic.getBlocks();
	        BlockData[] blockData = schematic.getData();
	 
	        short length = schematic.getLenght();
	        short width = schematic.getWidth();
	        short height = schematic.getHeight();
	 
	        for (int x = 0; x < width; ++x) {
	            for (int y = 0; y < height; ++y) {
	                for (int z = 0; z < length; ++z) {
	                    int index = y * width * length + z * width + x;
	                    Block block = new Location(loc.getWorld(), x + loc.getX(), y + loc.getY(), z + loc.getZ()).getBlock();
	                    block.setType(blocks[index], true);
	                    block.setBlockData(blockData[index], true);
	                }
	            }
	        }
	    }
	    
	    
	    public static Schematic loadSchematic(File file) throws IOException
	    {
	        FileInputStream stream = new FileInputStream(file);
	        NBTInputStream nbtStream = new NBTInputStream(new GZIPInputStream(stream));
	 
	        CompoundTag schematicTag = (CompoundTag) nbtStream.readTag();
	 
	        Map<String, Tag> schematic = schematicTag.getValue();
	        if (!schematic.containsKey("Blocks")) {
	        	nbtStream.close();
	            throw new IllegalArgumentException("Schematic file is missing a \"Blocks\" tag");
	        }
	 
	        short width = (short) getChildTag(schematic, "Width").getValue();
	        short length = (short) getChildTag(schematic, "Length").getValue();
	        short height = (short) getChildTag(schematic, "Height").getValue();
	 
	        String materials = (String) getChildTag(schematic, "Materials").getValue();
	        if (!materials.equals("Alpha")) {
	        	nbtStream.close();
	            throw new IllegalArgumentException("Schematic file is not an Alpha schematic");
	        }
	 
	        Material[] blocks = (Material[]) getChildTag(schematic, "Blocks").getValue();
	        BlockData[] blockData = (BlockData[]) getChildTag(schematic, "Data").getValue();
	        
	        nbtStream.close();
	        
	        return new Schematic(blocks, blockData, width, length, height);
	    }
	 
	    /**
	    * Get child tag of a NBT structure.
	    *
	    * @param items The parent tag map
	    * @param key The name of the tag to get
	    * @param expected The expected type of the tag
	    * @return child tag casted to the expected type
	    * @throws DataException if the tag does not exist or the tag is not of the
	    * expected type
	    
	    private static <T extends Tag> Tag getChildTag(Map<String, Tag> items, String key) throws IllegalArgumentException
	    {
	        if (!items.containsKey(key)) {
	            throw new IllegalArgumentException("Schematic file is missing a \"" + key + "\" tag");
	        }
	        Tag tag = items.get(key);
	        return tag;
	    }
    */
    
    
	    public static void errorOccured(Player player) {
	    	player.sendMessage("Unexpected error occurred while attempting to perform this command. Please contact our stuff as soon as possible!");
	    }
	    
	    
	public static int assignIsland(Player player) throws SQLException {
			int islandid = 0;
			Connection conn = DBQuery.ConnectDB();
			ResultSet results = DBQuery.getResults(conn, "select id from islands where owner = '' order by id limit 1");
			if(results.first()) {
				int freeid = results.getInt(1);
				results.close();
				DBQuery.makeChanges(conn,"delete from islands where id=" + freeid);
				islandid = freeid;
			}else {
				results = DBQuery.getResults(conn,"SELECT MAX(id) FROM islands");
				results.first();
				int islands = results.getInt(1);
				islandid = islands + 1;
			}
			conn.close();
			return islandid;
	}
	
	public static int[] designateIslandPosition(int id){
		int a = 1; //current id
		int x = 0; //current x shift
		int z = 1; //current z shift
		int d = 1; //circle number
		int[] xz;
		xz = new int[2];
		while(a != id) {
			for(int c = 0;c < d;c++) {
				if(a == id) {
					xz[0] = x;
					xz[1] = z;
					return xz;
				}
				x++;
				a++;
			}
			for(int c = 0;c < 2*d;c++) {
				if(a == id) {
					xz[0] = x;
					xz[1] = z;
					return xz;
				}
				z--;
				a++;
			}
			for(int c = 0;c < 2*d;c++) {
				if(a == id) {
					xz[0] = x;
					xz[1] = z;
					return xz;
				}
				x--;
				a++;
			}
			for(int c = 0;c < 2*d;c++) {
				if(a == id) {
					xz[0] = x;
					xz[1] = z;
					return xz;
				}
				z++;
				a++;
			}
			for(int c = 0;c < d-1;c++) {
				if(a == id) {
					xz[0] = x;
					xz[1] = z;
					return xz;
				}
				x++;
				a++;
			}
			x++;
			z++;
			d++;
			a++;
		}
		xz[0] = x * 500;
		xz[1] = z * 500;
		return xz;
	}
	
	
	
	public void createIsland(Player player, String type) throws Exception {
			if(Island.hasIsland(player) == 0) {
				if(type.equalsIgnoreCase("player") || type.equalsIgnoreCase("vip") || type.equalsIgnoreCase("svip") || type.equalsIgnoreCase("sponsor") || type.equalsIgnoreCase("yt")) {
					int islandid = assignIsland(player);
					int[] islandposition = designateIslandPosition(islandid);
					int islandx = islandposition[0];
					int islandz = islandposition[1];
					int size = 100;
					
						if(!player.hasPermission("island.create." + type.toLowerCase())) {
							player.sendMessage("Nie posiadasz tej lub wyzszej rangi!");
							return;
						}
						prepareIsland(islandposition, size, type.toLowerCase());
					
					getServer().getScheduler().runTaskAsynchronously(this, new Runnable(){
		                public void run() {
					Connection conn;
					try {
						conn = DBQuery.ConnectDB();
						DBQuery.makeChanges(conn,"insert into islands values(" + islandid + ",'" + player.getUniqueId().toString() + "'," + size + "," + islandx + "," + islandz + "," + islandx + ",101," + islandz + ",0)");
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
		                }
					});
					
					islandOwner.put(player.getUniqueId(), islandid);
					Integer[] cuboid = new Integer[4];
					cuboid[0] = islandx - (size / 2);
					cuboid[1] = islandz - (size / 2);
					cuboid[2] = islandx + (size / 2);
					cuboid[3] = islandz + (size / 2);
					
					islandsCuboidPositions.put(islandid, cuboid);
					islandOwner.put(player.getUniqueId(), islandid);
					
					Double[] home = new Double[3];
					home[0] = (double) islandx;
					home[1] = (double) 101;
					home[2] = (double) islandz;
					islandsHomePositions.put(islandid, home);
					
					player.sendMessage("Your new island is ready! You will be teleported there.");
				    teleportToIsland(player, player);
				    	
					} else {
						player.sendMessage("There is no such island type!");
					}
				} else {
					player.sendMessage("You already have your own island! You will be teleported there. Use /is home next time.");
					teleportToIsland(player, player);
				}
			
	}
	    
	
	
	
	public void clearIsland(int x1, int z1, int x2, int z2) {
		World world = Bukkit.getWorld("Skyblock");
		for(int y = 0;y < 256;y++) {
			for(int z = z1;z <= z2;z++) {
				for(int x = x1;x <= x2;x++) {
					Block block = world.getBlockAt(x, y, z);
					if(block.getType() != Material.AIR)
					block.setType(Material.AIR);
				}
			}
		}
	}
	
	
	
	
	public void prepareIsland(int[] islandposition, int size, String type) throws IOException {
		int islandx = islandposition[0];
		int islandz = islandposition[1];
		clearIsland(islandx - (size / 2), islandz - (size / 2), islandx + (size / 2), islandz + (size / 2));
		pasteIsland(islandposition, type);
	}
	
	
	
	public boolean teleportToIsland(Player player, Player to) throws Exception {
					if(Island.hasIsland(to)!=0) {
						int id = Island.hasIsland(to);
						if(player == to) {
							Double[] array = islandsHomePositions.get(id);
							Location location = new Location(Bukkit.getWorld("SkyBlock"), (double) array[0], (double) array[1], (double) array[2]);
							teleportWithCooldown(player, location, to.getUniqueId());
						} else {
							getServer().getScheduler().runTaskAsynchronously(this, new Runnable(){
					                public void run() {
					                	if(ignoreTeleportations.containsKey(to.getUniqueId())) {
					                		ArrayList<UUID> list = ignoreTeleportations.get(to.getUniqueId());
					                		if(list.contains(player.getUniqueId())) {
					                				player.sendMessage("This player ignores your teleportation requests until he left the game.");
					                				return;
					                		}
					                	}
					                	if(teleportRequests.containsKey(player.getUniqueId())) {
						                	if(teleportRequests.get(player.getUniqueId()) == id) {
						                		player.sendMessage("You are still waiting for this player...");
						                	} else {
						                		abortedRequests.add(player.getUniqueId());
						                		teleportRequests.remove(player.getUniqueId());
						                		player.sendMessage("Teleportation request to player " + to.getName() + " was cancelled!");
						                		to.sendMessage("Teleportation request from player " + player.getName() + " was cancelled!");
						                	}
					                	} else {
					                	teleportRequests.put(player.getUniqueId(), id);
										to.sendMessage("Player " + player.getName() + " is asking, if he can visit your island. If you agree, click here,if not, do nothing.");
										player.sendMessage("Teleport request has been sent and will be valid for 30 seconds.");
					                	for(int a = 0;a < 20;a++) {
					                    try {
											Thread.sleep(1500);
											if(abortedRequests.contains(player.getUniqueId())) {
												abortedRequests.remove(player.getUniqueId());
												
											} else {
												if(teleportRequests.containsKey(player.getUniqueId())) {
													if(teleportRequests.get(player.getUniqueId()) == 0) {
														teleportRequests.remove(player.getUniqueId());
														player.sendMessage("Pending teleport request has been accepted.");
														Double[] array = islandsHomePositions.get(id);
														Location location = new Location(Bukkit.getWorld("SkyBlock"), (double) array[0], (double) array[1], (double) array[2]);
														teleportWithCooldown(player, location, to.getUniqueId());
														return;
													}
												} else {
													player.sendMessage("Your teleport request to player " + to.getName() + " was cancelled.");
													to.sendMessage("Pending teleport request from player " + player.getName() +  " has been canceled.");
													return;
												}
											}
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
					                	}
					                	teleportRequests.remove(player.getUniqueId());
										player.sendMessage("Pending teleport request to player " + to.getName() + " has expired!");
										to.sendMessage("Pending teleport request from player " + player.getName() + " has expired!");
					                }
					                }
					            });
						}
					} else {
						if(player == to) {
							player.sendMessage("You don't have island!");
						} else {
							player.sendMessage("Player " + to.getName() + " doesn't have island!");
						}
					}
				return true;
	}
	
	
	public static void setIslandHome(Player player, double locationx, double locationy, double locationz, String message) throws Exception {
			if(Island.hasIsland(player)!=0) {
				int id = Island.hasIsland(player);
				Location loc = player.getLocation();
				double locx = loc.getX();
				double locz = loc.getZ();
					Integer[] cuboid = Main.islandsCuboidPositions.get(id);
					int x1 = cuboid[0];
					int z1 = cuboid[1];
					int x2 = cuboid[2];
					int z2 = cuboid[3];
					if(locx >= x1 && locz >= z1 && locx <= x2 && locz <= z2) {
						Connection conn = DBQuery.ConnectDB();
						DBQuery.makeChanges(conn,"update islands set home_x_pos = " + locationx + ", home_y_pos = " + locationy + ", home_z_pos = " + locationz + " where id = " + id);
						conn.close();
						Double[] home = new Double[3];
						home[0] = locationx;
						home[1] = locationy;
						home[2] = locationz;
						islandsHomePositions.put(id, home);
						player.sendMessage(message);
					} else {
						player.sendMessage("You can't set your island home outside your island!");
					}
			} else {
				player.sendMessage("You don't have island!");
			}
	}
	
	
	public void deleteIsland(Player player) throws SQLException {
		int id = Island.hasIsland(player);
		if(id!=0) {
			getServer().getScheduler().runTaskAsynchronously(this, new Runnable(){
                public void run() {
			Connection conn;
			try {
				conn = DBQuery.ConnectDB();
				DBQuery.makeChanges(conn,"update islands set owner = '' where owner = '" + player.getUniqueId().toString() + "'");
				DBQuery.makeChanges(conn, "delete from members where id = " + id);
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
                }
			});
			islandsHomePositions.remove(id);
			islandOwner.remove(player.getUniqueId());
			
			for(UUID uuid : islandsMembers.keySet()) {
				ArrayList<Integer> ids = islandsMembers.get(uuid);
				if(ids.contains(id)) {
					ids.remove(id);
					islandsMembers.put(uuid, ids);
				}
			}
			
			islandsCuboidPositions.remove(id);
			
			player.teleport(Bukkit.getWorld("SkyBlock").getSpawnLocation());
			player.sendMessage("Your island has been deleted successfully and you were teleported to spawn!");
		} else {
			player.sendMessage("You don't have island!");
		}
	}
	
	
	/*
	public static boolean canBuild(Player player) {
		
	}
	*/
	
	public void teleportWithCooldown(Player player, Location location, UUID uuid) throws InterruptedException {
		getServer().getScheduler().runTaskAsynchronously(this, new Runnable(){
                public void run() {
                    try {
                    	if(!player.isOnGround()) {
                			player.sendMessage("You can't teleport while you're not standing on the ground!");
                		} else {
                			player.sendMessage("Don't move! You will be teleported in 5 seconds.");
                			waitingForTeleport.add(player.getUniqueId());
                			for(int a = 0;a < 20;a++) {
                			Thread.sleep(250);
                			
                			if(!waitingForTeleport.contains(player.getUniqueId())) {
                				player.sendMessage("Teleportation was canceled because you moved!");
                				return;
                			}
                			}
                			player.teleport(location);
                			if(player.getUniqueId() == uuid) {
                				player.sendMessage("You have been teleported to your island!");
                				//player.setMetadata("canBuild", new FixedMetadataValue());
                			} else if(uuid == null) {
                				player.sendMessage("You have been teleported to spawn!");
                			} else {
                				player.sendMessage("You have been teleported to " + Bukkit.getPlayer(uuid).getName() + "'s island!");
                			}
                		}
                    	
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
                    try {
						this.finalize();
					} catch (Throwable e) {
						e.printStackTrace();
					}
                }
            });
	}
	
	
	public void teleportToSpawn(Player player) throws InterruptedException {
		teleportWithCooldown(player, Bukkit.getWorld("SkyBlock").getSpawnLocation(), null);
	}
	
	
	
	
	
	public static void fixIslandHome(Player player) throws Exception {
		Integer[] cuboid = islandsCuboidPositions.get(Island.hasIsland(player));
		Block highestblock = getHighestBlock(cuboid);
		if(highestblock == null) {
			player.sendMessage("Na twojej wyspie nie ma zadnego solidnego bloku! Skontaktuj sie jak najszybciej z administracja!");
			return;
		}
		int highestx = highestblock.getX();
		int highesty = highestblock.getY();
		int highestz = highestblock.getZ();
		setIslandHome(player, highestx, highesty, highestz, "Teleport na wyspe zostal ustawiony na najwyzszy solidny blok!");
	}
	
	public static Block getHighestBlock(Integer[] cuboid) {
		World islandworld = Bukkit.getWorld("SkyBlock");
		int x1 = cuboid[0];
		int z1 = cuboid[1];
		int x2 = cuboid[2];
		int z2 = cuboid[3];
		
		int highestx = 0;
		int highesty = -1;
		int highestz = 0;
		
		for(int z = z1;z <= z2;z++) {
			for(int x = x1;x <= x2;x++) {
				Block block = islandworld.getHighestBlockAt(x, z);
				if(block.getType().isSolid()) {
					int blocky = block.getY();
					if(blocky > highesty) {
						highestx = block.getX();
						highesty = blocky;
						highestz = block.getZ();
					}
				}
			}
		}
		
		if(highesty != -1) {
			return islandworld.getBlockAt(highestx, highesty, highestz);
		}
		
		return null;
	}
	
	
	/*
	public static int getBlockIslandID(Location location) {
		int a = 1; //current id
		int x = 0; //current x shift
		int z = 200; //current z shift
		int d = 1; //circle number
		int blockx = location.getBlockX();
		int blockz = location.getBlockZ();
		for(;;) {
			for(int c = 0;c < d;c++) {
				if(blockx > x - 100 && blockx < x + 100 && blockz > z - 100 && blockz < z + 100) {
					return a;
				}
				x+=200;
				a++;
			}
			for(int c = 0;c < 2*d;c++) {
				if(blockx > x - 100 && blockx < x + 100 && blockz > z - 100 && blockz < z + 100) {
					return a;
				}
				z-=200;
				a++;
			}
			for(int c = 0;c < 2*d;c++) {
				if(blockx > x - 100 && blockx < x + 100 && blockz > z - 100 && blockz < z + 100) {
					return a;
				}
				x-=200;
				a++;
			}
			for(int c = 0;c < 2*d;c++) {
				if(blockx > x - 100 && blockx < x + 100 && blockz > z - 100 && blockz < z + 100) {
					return a;
				}
				z+=200;
				a++;
			}
			for(int c = 0;c < d-1;c++) {
				if(blockx > x - 100 && blockx < x + 100 && blockz > z - 100 && blockz < z + 100) {
					return a;
				}
				x+=200;
				a++;
			}
			x+=200;
			z+=200;
			d++;
			a++;
		}
	}
	*/
	
	
	
	public static void addIslandMember(int id, Player player) {
		
	}
	
	
	
	
	
	
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
            Player player = (Player) sender;
            try {
	            if(cmd.getName().equalsIgnoreCase("is")) {
	            	int id = Island.hasIsland(player);
	            	if(args.length == 0 || args[0].equalsIgnoreCase("cp") || args[0].equalsIgnoreCase("controlpanel")) {
	            		Inventory inv = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + "Choose what you want to do:");
	            		if(id != 0) {
	            			ItemStack is = new ItemStack(Material.GRASS);
		        			ItemMeta im = is.getItemMeta();
		        			im.setDisplayName("Teleport to your island.");
		        			ArrayList<String> list = new ArrayList<String>();
		        			list.add(ChatColor.DARK_GREEN + "Click here to teleport to your island.");
		        			im.setLore(list);
		        			is.setItemMeta(im);
		                	inv.setItem(0, is);
		                	
		                	is = new ItemStack(Material.DIRT);
		        			im = is.getItemMeta();
		        			im.setDisplayName("Set island's home");
		        			list = new ArrayList<String>();
		        			list.add(ChatColor.DARK_GREEN + "Click here to set your island's home to your currect location");
		        			im.setLore(list);
		        			is.setItemMeta(im);
		                	inv.setItem(1, is);
	            		} else {
	            			ItemStack is = new ItemStack(Material.GRASS);
		        			ItemMeta im = is.getItemMeta();
		        			im.setDisplayName("Create island");
		        			ArrayList<String> list = new ArrayList<String>();
		        			list.add(ChatColor.DARK_GREEN + "Click here to create your island");
		        			im.setLore(list);
		        			is.setItemMeta(im);
		                	inv.setItem(0, is);
	            		}
	            		
	            		ItemStack is = new ItemStack(Material.STONE);
	        			ItemMeta im = is.getItemMeta();
	        			im.setDisplayName("Teleport to spawn");
	        			ArrayList<String> list = new ArrayList<String>();
	        			list.add(ChatColor.DARK_GREEN + "Click here to teleport to spawn");
	        			im.setLore(list);
	        			is.setItemMeta(im);
	                	inv.setItem(8, is);
	                	
	                	player.openInventory(inv);
	            	} else {
		            	switch(args[0].toLowerCase()) {
		            	case "create":
		            			if(args.length > 1) {
		            				createIsland(player, args[1]);
		            			} else {
		            				player.sendMessage("You have specify which starting island do you want!");
		            			}
		                		break;
		            	case "delete":
								deleteIsland(player);
								break;
		            	case "home": case "go":
		                		teleportToIsland(player, player);
		                		break;
		            	case "sethome":
		                		setIslandHome(player, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), "Your island home has been moved to your current location successfully!");
		                		break;
		            	case "spawn":
								teleportToSpawn(player);
		                		break;
		            	case "fixhome":
								fixIslandHome(player);
								break;
		            	case "tp":
		                		if(args.length > 1) {
									teleportToIsland(player,Bukkit.getPlayer(args[1]));
		                		} else {
		                			player.sendMessage("You have to specify where do you want to teleport!");
		                		}
		                		break;
		            	case "add":
		            		if(id == 0) {
		            			player.sendMessage("You don't have island!");
		            		} else {
			            		if(args.length > 1) {
			            			if(player.getName().equalsIgnoreCase(args[1])) {
			            				player.sendMessage("Nie mozesz dodac samego siebie!");
			            				return false;
			            			}
			            			ArrayList<Integer> check;
			            			if(Bukkit.getOfflinePlayer(args[1]).isOnline()) {
			            				check = islandsMembers.get(Bukkit.getPlayer(args[1]).getUniqueId());
			            			} else {
			            			check = islandsMembers.get(Bukkit.getOfflinePlayer(args[1]).getUniqueId());
			            			}
			            			if(check.contains(id)) {
			            				player.sendMessage("This player is already added to your island!");
			            			} else {
			            			if(Bukkit.getOfflinePlayer(args[1]).isOnline()) {
			            				if(ignoreInvitations.containsKey(Bukkit.getPlayer(args[1]).getUniqueId())) {
			            					ArrayList<UUID> ignores = ignoreInvitations.get(Bukkit.getPlayer(args[1]).getUniqueId());
			            					if(ignores.contains(player.getUniqueId())) {
			            						player.sendMessage("Player " + args[1] + " ignores your invitations untill he leave the game!");
			            						return true;
			            					}
			            				}
			            				HashMap<UUID,Integer> list;
			            				if(invitesToIsland.containsKey(id)) {
			            					 list = invitesToIsland.get(id);
			            					if(list.containsKey(Bukkit.getPlayer(args[1]).getUniqueId())) {
			            						player.sendMessage("You are still waiting for this player to join!");
			            						return true;
			            					}
			            				} else {
			            					list = new HashMap<UUID,Integer>();
			            				}
			            				list.put(Bukkit.getPlayer(args[1]).getUniqueId(),2);
			            				invitesToIsland.put(id, list);
			            				player.sendMessage("Invitation to the island was successfully sent to the player " + args[1] + " and will be valid for 30 seconds!");
			            				Bukkit.getPlayer(args[1]).sendMessage("You have received invitation to island from player " + player.getName() + "! Click here to join!");
			            				getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
			            	                public void run() {
			            	                	for(int a = 0;a < 20;a++) {
			            	                		try {
														Thread.sleep(1500);
														if(Main.invitesToIsland.containsKey(id)) {
															switch(list.get(Bukkit.getPlayer(args[1]).getUniqueId())) {
						            						case 0:
						            							Bukkit.getPlayer(args[1]).sendMessage("You joined to  " + player.getName() + "'s island!");
						            							player.sendMessage("Player " + args[1] + " joined your island!");
						            							list.remove(Bukkit.getPlayer(args[1]).getUniqueId());
						            							invitesToIsland.put(id, list);
						            							ArrayList<Integer> ids = islandsMembers.get(Bukkit.getPlayer(args[1]).getUniqueId());
						            							ids.add(id);
						            							islandsMembers.put(Bukkit.getPlayer(args[1]).getUniqueId(),ids);
						            							break;
						            						case 1:
						            							Bukkit.getPlayer(args[1]).sendMessage("Odrzuciles zaproszenie do wyspy od gracza " + player.getName() + "!");
						            							player.sendMessage("Your invitation to player " + args[1] + " was rejected!");
						            							list.remove(Bukkit.getPlayer(args[1]).getUniqueId());
						            							invitesToIsland.put(id, list);
						            							break;
						            						case 2:
						            							player.sendMessage("Wciaz czekasz na odpowiedz na twoja prosbe o dodanie ciebie do wyspy tego gracza!");
						            							break;
						            						}
															
														} else {
															return;
														}
													} catch (InterruptedException e) {
														e.printStackTrace();
													}
			            	                	}
			            	                	Bukkit.getPlayer(args[1]).sendMessage("Minal czas na odpowiedzenie graczowi " + player.getName() + " na prosbe o dodanie go do swojej wyspy!");
			            	                	player.sendMessage("Gracz " + args[1] + " nie odpowiedzial na twoja prosbe o dodanie ciebie do jego wyspy!");
		            							list.remove(Bukkit.getPlayer(args[1]).getUniqueId());
		            							invitesToIsland.put(id, list);
			            	                }
			            				});
			            			} else {
			            				player.sendMessage("Gracza " + args[1] + " nie ma teraz na serwerze!");
			            			}
			            			}
			            		} else {
			            			player.sendMessage("Musisz podac kogo chcesz dodac!");
			            		}
		            		}
		            		break;
		            	case "remove":
		            		if(id == 0) {
		            			player.sendMessage("You don't have island!");
		            		} else {
		            			if(args.length > 1) {
		            				if(islandsMembers.containsKey(Bukkit.getOfflinePlayer(args[1]).getUniqueId())) {
		            					ArrayList<Integer> ids = islandsMembers.get(Bukkit.getOfflinePlayer(args[1]).getUniqueId());
		            					for(int islandid : ids) {
		            						if(islandid == id) {
		            							ids.remove(id);
		            							getServer().getScheduler().runTaskAsynchronously(this, new Runnable(){
		            				                public void run() {
		            				                	try {
		            				                		Connection conn = DBQuery.ConnectDB();
			            				                	DBQuery.makeChanges(conn, "delete from members where uuid = '" + Bukkit.getOfflinePlayer(args[1]).getUniqueId().toString() + "' and id != 0");
															conn.close();
														} catch (SQLException e) {
															e.printStackTrace();
														}
		            				                }
		            							});
		            							return true;
		            						}
		            					}
		            				}
		            				player.sendMessage("Player " + args[1] + " is not member of your island!");
		            			} else {
		            				player.sendMessage("You have to specify who you want to remove!");
		            			}
		            		}
		            		break;
		            	case "join":
		            		if(args.length > 1) {
		            			int joinid = Island.hasIsland(Bukkit.getPlayer(args[1]));
		            			if(joinid == 0) {
		            				player.sendMessage("Player " + args[1] + " doesn't have island!");
		            			} else {
		            				if(invitesToIsland.containsKey(joinid)) {
		            					HashMap<UUID,Integer> map = new HashMap<UUID,Integer>();
		            					if(map.containsKey(player.getUniqueId()) && map.get(player.getUniqueId()) == 2) {
		            						map.put(player.getUniqueId(), 0);
		            						invitesToIsland.put(joinid, map);
		            						Connection conn = DBQuery.ConnectDB();
		            						getServer().getScheduler().runTaskAsynchronously(this, new Runnable(){
		            			                public void run() {
		            			                	try {
														DBQuery.makeChanges(conn, "insert into members values('" + Bukkit.getPlayer(args[1]).getUniqueId().toString() + "',id,0)");
													} catch (SQLException e) {
														e.printStackTrace();
													}
		            			                }
		            						});
		            						return true;
		            					}
		            				}
		            				player.sendMessage("Player " + args[1] + " doesn't invite you to his island!");
		            			}
		            		} else {
		            			player.sendMessage("You have to specify which island do you want to join!");
		            		}
		            		break;
		            	case "reject":
		            		if(args.length > 1) {
		            			int joinid = Island.hasIsland(Bukkit.getPlayer(args[1]));
		            			if(joinid == 0) {
		            				player.sendMessage("Player " + args[1] + " doesn't have island!");
		            			} else {
		            				if(invitesToIsland.containsKey(joinid)) {
		            					HashMap<UUID,Integer> map = new HashMap<UUID,Integer>();
		            					if(map.containsKey(player.getUniqueId()) && map.get(player.getUniqueId()) == 2) {
		            						map.put(player.getUniqueId(), 1);
		            						invitesToIsland.put(joinid, map);
		            						return true;
		            					}
		            				}
		            				player.sendMessage("Player " + args[1] + " doesn't invite you to his island!");
		            			}
		            		} else {
		            			player.sendMessage("You have to specify which invitation do you want to reject!");
		            		}
		            		break;
		            	case "ignore":
		            		if(args.length > 1) {
		            			if(Bukkit.getOfflinePlayer(args[1]).isOnline()) {
			            			ArrayList<UUID> ignores;
			            			if(ignoreTeleportations.containsKey(player.getUniqueId())) {
			            				ignores = ignoreTeleportations.get(player.getUniqueId());
			            			} else {
			            				ignores = new ArrayList<UUID>();
			            			}
			            			if(ignores.contains(Bukkit.getPlayer(args[1]).getUniqueId())) {
			            				player.sendMessage("You are already ignoring this player's invitations until you leave the game.");
			            				return true;
			            			}
			            			ignores.add(Bukkit.getPlayer(args[1]).getUniqueId());
		            				ignoreTeleportations.put(player.getUniqueId(), ignores);
		            				player.sendMessage("Since now, you are ignoring this player's invitations until you leave the game.");
		            			} else {
		            				player.sendMessage("Player " + args[1] + " is not online!");
		            			}
		            		} else {
		            			player.sendMessage("You have to specify which invitations do you want to ignore!");
		            		}
		            		break;
		            	case "unignore":
		            		if(args.length > 1) {
		            			ArrayList<UUID> ignores;
		            			if(ignoreTeleportations.containsKey(player.getUniqueId())) {
		            				ignores = ignoreTeleportations.get(player.getUniqueId());
		            				if(ignores.contains(Bukkit.getPlayer(args[1]).getUniqueId())) {
		            					ignores.remove(Bukkit.getPlayer(args[1]).getUniqueId());
			            				ignoreTeleportations.put(player.getUniqueId(), ignores);
			            				player.sendMessage("You don't ignore this player anymore!");
			            				return true;
			            			}
		            			}
		            			player.sendMessage("You aren't ignoring this player!");
		            		} else {
		            			player.sendMessage("You have to specify which invitations do you want to stop ignore!");
		            		}
		            		break;
		            	case "tpaccept":
		            		if(id == 0) {
		            			player.sendMessage("You don't have island!");
		            		} else {
			            		if(args.length > 1) {
				            			if(Bukkit.getOfflinePlayer(args[1]).isOnline()) {
				            				Player asking = Bukkit.getPlayer(args[1]);
				            				if(teleportRequests.containsKey(asking.getUniqueId()) && teleportRequests.get(asking.getUniqueId()) == id) {
				            					teleportRequests.put(asking.getUniqueId(),0);
				            					player.sendMessage("You have accepted teleport request from " + asking.getName() + ".");
				            				} else {
				            					player.sendMessage("There is no teleport request received from this player.");
				            				}
				            			}else {
				            				player.sendMessage("Player " + args[1] + " is not online!");
				            			}
			            		}else {
			            			player.sendMessage("For security reasons, you must specify which request you want to accept. Simply click on the previously received request or enter the player's name.");
			            		}
		            		}
		            		break;
		            	case "tpdeny":
		            		if(id == 0) {
		            			player.sendMessage("You don't have island!");
		            		} else {
			            		if(args.length > 1) {
			            			if(id!=0) {
				            			if(Bukkit.getOfflinePlayer(args[1]).isOnline()) {
				            				Player asking = Bukkit.getPlayer(args[1]);
					            			if(teleportRequests.containsKey(asking.getUniqueId()) && teleportRequests.get(asking.getUniqueId()) == id) {
								            	abortedRequests.add(Bukkit.getPlayer(args[1]).getUniqueId());
							                	teleportRequests.remove(Bukkit.getPlayer(args[1]).getUniqueId());
					            			} else {
					            				player.sendMessage("There is no pending teleportation request from this player.");
					            			}
				            			} else {
				            				player.sendMessage("Player " + args[1] + " is not online!");
				            			}
			            			}
			            		} else {
			            			player.sendMessage("You have to specify which teleportation request you want to cancel.");
			            		}
		            		}
		            		break;
		            	case "tpignore":
			            		if(id == 0) {
			            			player.sendMessage("You don't have island!");
			            		} else {
			            			if(Bukkit.getOfflinePlayer(args[1]) == player) {
			            				player.sendMessage("You can't ignore yourself!");
			            			} else {
					            		if(args.length > 1) {
					            			ArrayList<UUID> list;
					            			if(ignoreTeleportations.containsKey(player.getUniqueId())) {
				            					list = ignoreTeleportations.get(player.getUniqueId());
				            				} else {
				            					list = new ArrayList<UUID>();
				            				}
					            			if(list.contains(Bukkit.getOfflinePlayer(args[1]).getUniqueId())) {
					            				player.sendMessage("You are already ignoring this player.");
					            				return true;
					            			}
					            			if(Bukkit.getOfflinePlayer(args[1]).isOnline()) {
					            				list.add(Bukkit.getPlayer(args[1]).getUniqueId());
				            					ignoreTeleportations.put(player.getUniqueId(), list);
				            					player.sendMessage("Since now you ignore this player, until you leave the game.");
					            			} else {
					            				player.sendMessage("Player " + args[1] + " is not online!");
					            			}
					            		} else {
					            			player.sendMessage("You have to specify the player you want to ignore.");
				            		}
			            			}
			            		}
		            		break;
		            	case "tpunignore":
		            		if(args.length > 1) {
		            			if(Bukkit.getOfflinePlayer(args[1]) == player) {
		            				player.sendMessage("You can't ignore or unignore yourself!");
		            				return true;
		            			}
			            		if(ignoreTeleportations.containsKey(player.getUniqueId())) {
			            			ArrayList<UUID> list = ignoreTeleportations.get(player.getUniqueId());
			            			if(list.contains(Bukkit.getOfflinePlayer(args[1]).getUniqueId())) {
			            				list.remove(Bukkit.getOfflinePlayer(args[1]).getUniqueId());
			            				ignoreTeleportations.put(player.getUniqueId(), list);
			            				player.sendMessage("Since now you are not ignoring this player.");
			            			} else {
			            				player.sendMessage("You are not ignoring this player.");
			            			}
			            		} else {
			            			player.sendMessage("You are not ignoring this player.");
			            		}
		            		} else {
		            			player.sendMessage("You have to specify the player you want to stop ignoring.");
		            		}
		            		break;
		            	default:
		            		String string = "Komendy dotyczace wysp:";
		            		for(String argument : arguments) {
		            			string += "\n/is " + argument;
		            		}
		            		player.sendMessage(string);
		            	}
	            	}
	            }
            } catch (Exception e) {
				e.printStackTrace();
				errorOccured(player);
			}
		}
		return true;
	}
	
	
	
	public void pasteIsland(int[] islandlocation, String type) throws IOException {
		int islandx = islandlocation[0];
		int islandz = islandlocation[1];
		boolean allowUndo = false;
		File file = new File(getDataFolder().getAbsolutePath(),"schematics/" + type + ".schematic");
		Vector position = new Vector(islandx, 100, islandz);
		com.sk89q.worldedit.world.World world = (com.sk89q.worldedit.world.World) islandworld;
		ClipboardFormat.findByFile(file).load(file).paste(world, position, allowUndo, false, (Transform) null);
	}
	
	
	
}
