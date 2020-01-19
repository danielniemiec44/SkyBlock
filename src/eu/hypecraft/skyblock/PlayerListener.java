package eu.hypecraft.skyblock;

import java.util.ArrayList;
import java.util.UUID;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class PlayerListener implements Listener {
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		try {
			Player p = e.getPlayer();
			if(p.isOp()) return;
			Location loc = e.getBlock().getLocation();
			World world = loc.getWorld();
			int locx = loc.getBlockX();
			int locz = loc.getBlockZ();
			if(world.getName() == "SkyBlock" && locx > -250 && locz > -250 && locx < 250 && locz < 250 && p.hasPermission("skyblock.build.spawn")) return;
			int islandid = Island.hasIsland(p);
			ArrayList<Integer> ids = new ArrayList<Integer>();
				ids.add(islandid);
				if(Main.islandsMembers.containsKey(e.getPlayer().getUniqueId())){
					ids.addAll(Main.islandsMembers.get(e.getPlayer().getUniqueId()));
				}
				for(int id : ids) {
					Integer[] cuboid = Main.islandsCuboidPositions.get(id);
					int x1 = cuboid[0];
					int z1 = cuboid[1];
					int x2 = cuboid[2];
					int z2 = cuboid[3];
					if(locx >= x1 && locz >= z1 && locx <= x2 && locz <= z2) return;
				}
			cancelTeleportation(p);
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		e.setCancelled(true);
		return;
	}
	
	
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		try {
			Player p = e.getPlayer();
			if(p.isOp()) return;
			Location loc = e.getBlock().getLocation();
			World world = loc.getWorld();
			int locx = loc.getBlockX();
			int locz = loc.getBlockZ();
			if(world.getName() == "SkyBlock" && locx > -250 && locz > -250 && locx < 250 && locz < 250 && p.hasPermission("skyblock.build.spawn")) return;
			int islandid = Island.hasIsland(p);
			ArrayList<Integer> ids = new ArrayList<Integer>();
				ids.add(islandid);
				if(Main.islandsMembers.containsKey(e.getPlayer().getUniqueId())){
					ids.addAll(Main.islandsMembers.get(e.getPlayer().getUniqueId()));
				}
				for(int id : ids) {
					Integer[] cuboid = Main.islandsCuboidPositions.get(id);
					int x1 = cuboid[0];
					int z1 = cuboid[1];
					int x2 = cuboid[2];
					int z2 = cuboid[3];
					if(locx >= x1 && locz >= z1 && locx <= x2 && locz <= z2) return;
				}
			cancelTeleportation(p);
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		e.setCancelled(true);
		return;
	}
	
	
	public void cancelTeleportation(Player p) {
		if(Main.waitingForTeleport.contains(p.getUniqueId())) {
			Main.waitingForTeleport.remove(p.getUniqueId());
		}
	}
	
	/*
	public interface PlayerLocationChangeEvent {
	    @SerializedName("name")
	    String name();
	    @Nullable
	    String typeVariable();
	}

	public abstract class PlayerTeleportEvent implements PlayerLocationChangeEvent {

	}

	public class PlayerMoveEvent implements PlayerLocationChangeEvent {
		@Override
		public String typeVariable() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String name() {
			// TODO Auto-generated method stub
			return null;
		}

	    
	}
	
	
	public static void onPlayerLocationChange(PlayerLocationChangeEvent e) {
			if(event instanceof PlayerTeleportEvent) {
				PlayerTeleportEvent e = (PlayerTeleportEvent) event;
			} else if(event instanceof PlayerMoveEvent) {
				PlayerMoveEvent e = (PlayerMoveEvent) event;
			} else {
				return;
			}
			*/
		

	
	
	
	
	
	
	
	
	
	
	
	/*
	 * 
	 * UWAGA! JEZELI ZOSTANIE COS ZMIENIONE W JEDNEJ Z 3 FUNKCJI PONIZEJ,
	 * NALEZY UPEWNIC SIE CZY NIE POWINNO SIE ICH WSZYSTKICH ZAKTUALIZOWAC!
	 * 
	 */
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		Player player = e.getPlayer();
		cancelTeleportation(player);
		
		World worldto = e.getTo().getWorld();
		
		World skyblockworld = Bukkit.getWorld("SkyBlock");
		
		Location spawn = skyblockworld.getSpawnLocation();
		
		double xfrom = e.getFrom().getX();
		double zfrom = e.getFrom().getZ();
		
		double xto = e.getTo().getX();
		double yto = e.getTo().getY();
		double zto = e.getTo().getZ();
		
		int xidfrom = (int)((Math.abs(xfrom) + 250) / 500) * (int)(Math.abs(xfrom) / xfrom);
		int zidfrom = (int)((Math.abs(zfrom) + 250) / 500) * (int)(Math.abs(zfrom) / zfrom);
		
		int xidto = (int)((Math.abs(xto) + 250) / 500) * (int)(Math.abs(xto) / xto);
		int zidto = (int)((Math.abs(zto) + 250) / 500) * (int)(Math.abs(zto) / zto);
		
		if(worldto == Bukkit.getWorld("SkyBlock")) {
			if(xidfrom != xidto || zidfrom != zidto) { //jezeli wchodzimy na teren innej wyspy
				if(xidto == 0 && zidto == 0) { //jezeli wchodzimy na spawn
					player.setWalkSpeed(0.5f);
				} else { //jezeli wchodzimy na inna wyspe niz spawn
					player.setWalkSpeed(0.2f);
					
						for(Integer id : Main.islandsCuboidPositions.keySet()) {
							Integer[] cuboid = Main.islandsCuboidPositions.get(id);
							if(xto >= cuboid[0] && zto >= cuboid[1] && xto <= cuboid[2] && zto <= cuboid[3]) {
								String owner = "";
								for(UUID uuid1 : Main.islandOwner.keySet()) {
									if(Main.islandOwner.get(uuid1) == id) {
										if(Bukkit.getOfflinePlayer(uuid1).isOnline()) {
											owner = Bukkit.getPlayer(uuid1).getName();
										} else {
											owner = Bukkit.getOfflinePlayer(uuid1).getName();
										}
										break;
									}
								}
								
								
								ArrayList<Integer> ids = Main.islandsMembers.get(player.getUniqueId());
								for(int islandid : ids) {
									if(id == islandid) {
										player.sendMessage("You entered  " + owner + "'s island.");
										break;
									}
								}
								ArrayList<Integer> vperms = Main.visitorsPermissions.get(id);
								if(vperms.contains(1)) {
									player.sendMessage("You entered  " + owner + "'s island, of which you member.");
								} else {
									e.setCancelled(true);
									player.sendMessage("You don't have permission to enter " + owner + "'s island!");
								}
								return;
							}
						}
				}
			} else if(xidto == 0 && zidto == 0 && yto < 80) { //jezeli jestesmy na spawnie ponizej 80 kratki
				player.teleport(spawn);
			}
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		cancelTeleportation(player);
		
		World worldto = e.getTo().getWorld();
		
		World skyblockworld = Bukkit.getWorld("SkyBlock");
		
		Location spawn = skyblockworld.getSpawnLocation();
		
		double xfrom = e.getFrom().getX();
		double zfrom = e.getFrom().getZ();
		
		double xto = e.getTo().getX();
		double yto = e.getTo().getY();
		double zto = e.getTo().getZ();
		
		int xidfrom = (int)((Math.abs(xfrom) + 250) / 500) * (int)(Math.abs(xfrom) / xfrom);
		int zidfrom = (int)((Math.abs(zfrom) + 250) / 500) * (int)(Math.abs(zfrom) / zfrom);
		
		int xidto = (int)((Math.abs(xto) + 250) / 500) * (int)(Math.abs(xto) / xto);
		int zidto = (int)((Math.abs(zto) + 250) / 500) * (int)(Math.abs(zto) / zto);
		
		if(worldto == Bukkit.getWorld("SkyBlock")) {
			if(xidfrom != xidto || zidfrom != zidto) { //jezeli wchodzimy na teren innej wyspy
				if(xidto == 0 && zidto == 0) { //jezeli wchodzimy na spawn
					player.setWalkSpeed(0.5f);
				} else { //jezeli wchodzimy na inna wyspe niz spawn
					player.setWalkSpeed(0.2f);
					
						for(Integer id : Main.islandsCuboidPositions.keySet()) {
							Integer[] cuboid = Main.islandsCuboidPositions.get(id);
							if(xto >= cuboid[0] && zto >= cuboid[1] && xto <= cuboid[2] && zto <= cuboid[3]) {
								String owner = "";
								for(UUID uuid1 : Main.islandOwner.keySet()) {
									if(Main.islandOwner.get(uuid1) == id) {
										if(Bukkit.getOfflinePlayer(uuid1).isOnline()) {
											owner = Bukkit.getPlayer(uuid1).getName();
										} else {
											owner = Bukkit.getOfflinePlayer(uuid1).getName();
										}
										break;
									}
								}
								
								
								ArrayList<Integer> ids = Main.islandsMembers.get(player.getUniqueId());
								for(int islandid : ids) {
									if(id == islandid) {
										player.sendMessage("You entered  " + owner + "'s island.");
										break;
									}
								}
								ArrayList<Integer> vperms = Main.visitorsPermissions.get(id);
								if(vperms.contains(1)) {
									player.sendMessage("You entered  " + owner + "'s island, of which you member.");
								} else {
									e.setCancelled(true);
									player.sendMessage("You don't have permission to enter " + owner + "'s island!");
								}
								return;
							}
						}
				}
			} else if(xidto == 0 && zidto == 0 && yto < 80) { //jezeli jestesmy na spawnie ponizej 80 kratki
				player.teleport(spawn);
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		player.setWalkSpeed(0.5f);
	}
	
	@EventHandler
	public void onRespawnJoin(PlayerRespawnEvent e) {
		Player player = e.getPlayer();
		player.setWalkSpeed(0.5f);
	}
	
	
	
	
	
	
	
	
	
	
	

	
	
	
	
	
	
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		
	}
	
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) throws Exception {
		Inventory inv = e.getInventory();
		Player p = (Player) e.getWhoClicked();
		if(inv.getTitle().equals(ChatColor.DARK_GREEN + "Choose what you want to do:")) {
			int slotnumber = e.getSlot();
			e.setCancelled(true);
			int id = Island.hasIsland(p);
			if(e.getCurrentItem().getType() != Material.AIR) {
				p.closeInventory();
			}
			switch(slotnumber) {
			case 0:
				if(id == 0) {
					Inventory inv2 = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + "Choose starting island:");
					
					ItemStack is = new ItemStack(Material.GRASS);
        			ItemMeta im = is.getItemMeta();
        			im.setDisplayName("Player Island");
        			ArrayList<String> list = new ArrayList<String>();
        			list.add(ChatColor.DARK_GREEN + "Click here to create your island");
        			im.setLore(list);
        			is.setItemMeta(im);
                	inv2.setItem(0, is);
                	
                	if(p.hasPermission("island.create.vip")) {
	                	is = new ItemStack(Material.GRASS);
	                	im = is.getItemMeta();
	        			im.setDisplayName("VIP Island");
	        			list = new ArrayList<String>();
	        			list.add(ChatColor.DARK_GREEN + "Click here to create your island");
	        			im.setLore(list);
	        			is.setItemMeta(im);
	                	inv2.setItem(1, is);
                	}
                	
                	if(p.hasPermission("island.create.svip")) {
	                	is = new ItemStack(Material.GRASS);
	                	im = is.getItemMeta();
	        			im.setDisplayName("SVIP Island");
	        			list = new ArrayList<String>();
	        			list.add(ChatColor.DARK_GREEN + "Click here to create your island");
	        			im.setLore(list);
	        			is.setItemMeta(im);
	                	inv2.setItem(2, is);
                	}
                	
                	
                	if(p.hasPermission("island.create.sponsor")) {
	                	is = new ItemStack(Material.GRASS);
	                	im = is.getItemMeta();
	        			im.setDisplayName("Sponsor Island");
	        			list = new ArrayList<String>();
	        			list.add(ChatColor.DARK_GREEN + "Click here to create your island");
	        			im.setLore(list);
	        			is.setItemMeta(im);
	                	inv2.setItem(3, is);
                	}
                	
                	if(p.hasPermission("island.create.yt")) {
	                	is = new ItemStack(Material.GRASS);
	                	im = is.getItemMeta();
	        			im.setDisplayName("Youtuber Island");
	        			list = new ArrayList<String>();
	        			list.add(ChatColor.DARK_GREEN + "Click here to create your island");
	        			im.setLore(list);
	        			is.setItemMeta(im);
	                	inv2.setItem(4, is);
                	}
                	
                	
                	p.openInventory(inv2);
				} else {
					Bukkit.dispatchCommand(p, "is home");
				}
				break;
			case 1:
				Bukkit.dispatchCommand(p, "is sethome");
				break;
			case 8:
				Bukkit.dispatchCommand(p, "is spawn");
				break;
			
			}
		} else if(inv.getTitle().equals(ChatColor.DARK_GREEN + "Choose starting island:")) {
			//int slotnumber = e.getSlot();
			String name = e.getCurrentItem().getItemMeta().getDisplayName();
			e.setCancelled(true);
			if(e.getCurrentItem().getType() != Material.AIR) {
				p.closeInventory();
			}
			switch(name) {
			case "Player Island":
				Bukkit.dispatchCommand(p, "is create player");
				break;
			case "VIP Island":
				Bukkit.dispatchCommand(p, "is create vip");
				break;
			case "SVIP Island":
				Bukkit.dispatchCommand(p, "is create svip");
				break;
			case "Donator Island":
				Bukkit.dispatchCommand(p, "is create sponsor");
				break;
			case "Sponsor Island":
				Bukkit.dispatchCommand(p, "is create yt");
				break;
			}
		}
	}
	
	
	/*
	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) throws Exception {
		try {
		if(e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			World world = p.getWorld();
			Double x = p.getLocation().getX();
			Double z = p.getLocation().getZ();
			int id = Island.hasIsland(p);
			if(world == Bukkit.getWorld("SkyBlock")) {
				ArrayList<Integer> ids = new ArrayList<Integer>();
				if(id != 0) {
					ids.add(id);
				} else if(Main.islandsMembers.containsKey(p.getUniqueId())) {
					ids.addAll(Main.islandsMembers.get(p.getUniqueId()));
				}
				for(int islandid : ids) {
					Integer[] cuboid = Main.islandsCuboidPositions.get(islandid);
					int x1 = cuboid[0];
					int z1 = cuboid[1];
					int x2 = cuboid[2];
					int z2 = cuboid[3];
					if(x >= x1 && z >= z1 && x <= x2 && z <= z2) {
						return;
					}
				}
			}
		} else {
			return;
		}
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		e.setCancelled(true);
	}
	*/
	
	
	/*
	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) throws Exception {
		if(e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			World world = p.getWorld();
			Double x = p.getLocation().getX();
			Double z = p.getLocation().getZ();
			int id = Island.hasIsland(p);
			if(!(e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
				if(world == Bukkit.getWorld("SkyBlock")) {
					ArrayList<Integer> ids = new ArrayList<Integer>();
					if(id != 0) {
						ids.add(id);
					} else if(Main.islandsMembers.containsKey(p.getUniqueId())) {
						ids.addAll(Main.islandsMembers.get(p.getUniqueId()));
					}
					for(int islandid : ids) {
						Integer[] cuboid = Main.islandsCuboidPositions.get(islandid);
						int x1 = cuboid[0];
						int z1 = cuboid[1];
						int x2 = cuboid[2];
						int z2 = cuboid[3];
						if(x >= x1 && z >= z1 && x <= x2 && z <= z2) {
							return;
						}
					}
					if(e.getCause() == DamageCause.STARVATION || e.getCause() == DamageCause.SUICIDE) {
						return;
					}
				} else {
					return;
				}
			} else {
				return;
			}
		} else {
			return;
		}
		e.setCancelled(true);
	}
	*/
	
	
	
	
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e) throws Exception {
		try {
			if(e.getEntity() instanceof Player) {
				Player p = (Player) e.getEntity();
				World world = p.getWorld();
				Double x = p.getLocation().getX();
				Double z = p.getLocation().getZ();
				int id = Island.hasIsland(p);
				Entity damager = e.getDamager();
				if(world == Bukkit.getWorld("SkyBlock")) {
					if(p.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
						ArrayList<Integer> ids = new ArrayList<Integer>();
						if(id != 0) {
							ids.add(id);
						} else if(Main.islandsMembers.containsKey(p.getUniqueId())) {
							ids.addAll(Main.islandsMembers.get(p.getUniqueId()));
						}
						for(int islandid : ids) {
							Integer[] cuboid = Main.islandsCuboidPositions.get(islandid);
							int x1 = cuboid[0];
							int z1 = cuboid[1];
							int x2 = cuboid[2];
							int z2 = cuboid[3];
							if(x >= x1 && z >= z1 && x <= x2 && z <= z2) {
								if(damager instanceof Player) {
									Player attacker = (Player) damager;
									int attackerislandid = Island.hasIsland(attacker);
									ids = new ArrayList<Integer>();
									if(attackerislandid != 0) {
										ids.add(attackerislandid);
									}
									if(Main.islandsMembers.containsKey(attacker.getUniqueId())) {
										ids.addAll(Main.islandsMembers.get(attacker.getUniqueId()));
									}
									for(int attackerid : ids) {																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																													
										if(attackerid == id) {
											return;
										}
									}
								} else {
									return;
								}
								break;
							}
						}
					} else {
						if(e.getCause() == DamageCause.STARVATION || e.getCause() == DamageCause.SUICIDE) {
							return;
						}
					}
				} else {
					return;
				}
			} else {
				return;
			}
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		e.setCancelled(true);
	}
	
	
	/*
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = (Player) e.getEntity();
		((CraftServer)Bukkit.getServer()).getHandle().moveToWorld(((CraftPlayer)p).getHandle(), 0, false);
	}
	*/
	
}
