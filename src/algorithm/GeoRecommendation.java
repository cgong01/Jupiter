package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

public class GeoRecommendation {
	// content-based recommendation algorithm.
	public List<Item> recommednItems(String userId, double lat, double lon) {
		DBConnection conn = DBConnectionFactory.getDBConnection();
		if (conn == null) {
			return new ArrayList<>();
		}
		List<Item> recommendedItems = new ArrayList<>();
		
		// step1: Get all favorited items
		Set<String> favoriteItems = conn.getFavoriteItemIds(userId);
		
		// step2: Get all categories of favorited items, sort by count from more to less
		Map<String, Integer> allCategories = new HashMap<>();	// step2
		for (String item : favoriteItems) {
			Set<String> categories = conn.getCategories(item);	// db queries
			for (String category : categories) {
				allCategories.put(category, allCategories.getOrDefault(category, 0) + 1);
			}
		}
		
		List<Entry<String, Integer>> categoryList = new ArrayList<Entry<String, Integer>>(allCategories.entrySet());
		Collections.sort(categoryList, new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
				return Integer.compare(e2.getValue(), e1.getValue()); 	// return by descending order.
			}
		});
		
		// step3: do search based on category, filter out favorited events, sort by distance from close to far
		
		// set used to de-duplicated,  Ex: category: music-3, art-2, one item belongs to music and art.
		// since hashset uses reference of Item object to check equals or not, but each of them are different,
		// so we need to override the hashcode and equals method to use itemId to de-duplicate.
		Set<Item> visitedItems = new HashSet<>();
		for (Entry<String, Integer> category : categoryList) {
			List<Item> items = conn.searchItems(lat, lon, category.getKey());
			List<Item> filteredItems = new ArrayList<>();
			for (Item item : items) {
				if (!favoriteItems.contains(item.getItemId()) && !visitedItems.contains(item)) {
					filteredItems.add(item);
				}
			}
			Collections.sort(filteredItems, new Comparator<Item>() {
				@Override
				public int compare(Item item1, Item item2) {
					// return the increasing order of distance.
					return Double.compare(item1.getDistance(), item2.getDistance());
				}
			});
			visitedItems.addAll(items);
			recommendedItems.addAll(filteredItems);
		}
		return recommendedItems;
	}
}





















