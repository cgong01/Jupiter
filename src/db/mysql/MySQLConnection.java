package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterAPI;

public class MySQLConnection implements DBConnection {
	private Connection conn;
	
	public MySQLConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch(Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			return;
		}
		try {
			// here "insert ignore" used to check the the primary key, if it is duplicated, ignore it instead of throw exceptions. 
			String sql = "INSERT IGNORE INTO history (user_id, item_id) VALUES (?, ?)";	// prevent SQL injection.
			
			PreparedStatement statement = conn.prepareStatement(sql);
			for (String itemId : itemIds) {
				statement.setString(1, userId);
				statement.setString(2, itemId);
				statement.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			return;
		}
		try {
			// here "insert ignore" used to check the the primary key, if it is duplicated, ignore it instead of throw exceptions. 
			String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";	// prevent SQL injection.
			
			PreparedStatement statement = conn.prepareStatement(sql);
			for (String itemId : itemIds) {
				statement.setString(1, userId);
				statement.setString(2, itemId);
				statement.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		if (conn == null) {
			return null;
		}
		Set<String> favoriteItemIds = new HashSet<>();
		try {
			String sql = "SELECT item_id from history WHERE user_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				favoriteItemIds.add(rs.getString("item_id"));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return favoriteItemIds;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		if (conn == null) {
			return null;
		}
		Set<String> favoriteItemIds = getFavoriteItemIds(userId);
		Set<Item> favoriteItems = new HashSet<>();
		try {
			for (String itemId : favoriteItemIds) {
				String sql = "SELECT * from items WHERE item_id = ?";
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setString(1, itemId);
				ResultSet rs = stmt.executeQuery();
				ItemBuilder builder = new ItemBuilder();
				
				// Because itemId is unique and given one item id there should have only one result returned.
				if (rs.next()) {
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setRating(rs.getDouble("rating"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setCategories(getCategories(itemId));
				}
				favoriteItems.add(builder.build());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		if (conn == null) {
			return null;
		}
		Set<String> categories = new HashSet<>();
		try {
			String sql = "SELECT category from categories WHERE item_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, itemId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				categories.add(rs.getString("category"));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return categories;
	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		List<Item> items = tmAPI.search(lat, lon, term);
		for (Item item : items) {
			saveItem(item);
		}
		return items;
	}

	@Override
	public void saveItem(Item item) {
		if (conn == null) {
			return;
		}
		try {
			// here "insert ignore" used to check the the primary key, if it is duplicated, ignore it instead of throw exceptions. 
			String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";	// prevent SQL injection.
			
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			statement.setString(2, item.getName());
			statement.setDouble(3, item.getRating());
			statement.setString(4, item.getAddress());
			statement.setString(5, item.getAddress());
			statement.setString(6, item.getUrl());
			statement.setDouble(7, item.getDistance());
			statement.execute();
			
			
			sql = "INSERT IGNORE INTO categories VALUES (?, ?)";	// prevent SQL injection.
			for (String category : item.getCategories()) {
				statement = conn.prepareStatement(sql);
				statement.setString(1, item.getItemId());
				statement.setString(2, category);
				statement.execute();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getFullname(String userId) {
		if (conn == null) {
			return null;
		}
		String name = "";
		try {
			String sql = "SELECT first_name, last_name from users WHERE user_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				name = String.join(" ", rs.getString("first_name"), rs.getString("last_name"));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return name;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
			return false;
		}
		try {
			String sql = "SELECT user_id, password from users WHERE user_id = ? and password = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			stmt.setString(2, password);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
	}

	@Override
	public void signup(String userId, String password, String firstName, String lastName) {
		if (conn == null) {
			return;
		}
		try {
			String sql = "INSERT INTO users (user_id, password, first_name, last_name) VALUES (?, ?, ?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			stmt.setString(2, password);
			stmt.setString(3, firstName);
			stmt.setString(4, lastName);
			stmt.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean verifySignup(String userId) {
		if (conn == null) {
			return false;
		}
		try {
			String sql = "SELECT user_id FROM users WHERE user_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, userId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return false;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return true;
	}


	

}





