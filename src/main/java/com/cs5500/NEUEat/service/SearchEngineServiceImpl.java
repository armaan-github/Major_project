package com.cs5500.NEUEat.service;

import com.cs5500.NEUEat.model.Dish;
import com.cs5500.NEUEat.model.RestaurantInfo;
import com.cs5500.NEUEat.model.SearchEngine;
import com.cs5500.NEUEat.repository.SearchEngineRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchEngineServiceImpl implements SearchEngineService {

  @Autowired
  SearchEngineRepository searchEngineRepository;

  private SearchEngine getOrCreateSearchEngine() {
    return searchEngineRepository.findById("1")
        .orElseGet(() -> searchEngineRepository.save(new SearchEngine()));
  }

  @Override
  public void addRestaurant(String word, String restaurantId) {
    SearchEngine searchEngine = getOrCreateSearchEngine();
    searchEngine.add(word, restaurantId);
    searchEngineRepository.save(searchEngine);
  }

  @Override
  public List<String> searchRestaurant(String word) {
    SearchEngine searchEngine = getOrCreateSearchEngine();
    searchEngineRepository.save(searchEngine);
    return searchEngine.search(word);
  }

  @Override
  public void removeRestaurant(String word, String restaurantId) {
    SearchEngine searchEngine = getOrCreateSearchEngine();
    searchEngine.remove(word, restaurantId);
    searchEngineRepository.save(searchEngine);
  }

  @Override
  public void eraseInfo(RestaurantInfo info, String restaurantId) {
    this.removeRestaurant(info.getRestaurantName(), restaurantId);
    this.removeRestaurant(info.getTag1(), restaurantId);
    this.removeRestaurant(info.getTag2(), restaurantId);
    this.removeRestaurant(info.getTag3(), restaurantId);
  }

  @Override
  public void eraseDishes(List<Dish> dishes, String restaurantId) {
    for (Dish dish : dishes) {
      this.removeRestaurant(dish.getDishName(), restaurantId);
    }
  }

  @Override
  public void updateInfo(RestaurantInfo info, String restaurantId) {
    this.addRestaurant(info.getRestaurantName(), restaurantId);
    this.addRestaurant(info.getTag1(), restaurantId);
    this.addRestaurant(info.getTag2(), restaurantId);
    this.addRestaurant(info.getTag3(), restaurantId);
  }
}
