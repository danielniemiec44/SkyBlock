package eu.hypecraft.skyblock;

/*
import java.sql.Connection;
import java.sql.ResultSet;
*/

import java.util.UUID;

import org.bukkit.entity.Player;

public class Island {

	public static int hasIsland(Player p) {
		UUID uuid = p.getUniqueId();
		if(Main.islandOwner.containsKey(uuid)) {
			return Main.islandOwner.get(uuid);
			/*
		}else {
			Connection conn = DBQuery.ConnectDB();
			ResultSet rs = DBQuery.getResults(conn, "select id from islands where owner='" + uuid + "'");
			int id = 0;
			if(rs.first()) {
				id = rs.getInt(1);
			}
			conn.close();
			return id;
			*/
		}else {
			return 0;
		}
	}
	
}
