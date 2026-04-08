package com.cs5500.NEUEat.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "search_engine")
public class SearchEngine {

  @Id
  private String id = "1";

  @Convert(converter = NodeJsonConverter.class)
  @Column(columnDefinition = "LONGTEXT")
  private Node root;

  public SearchEngine() {
    this.root = new Node();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Node getRoot() {
    return root;
  }

  public void setRoot(Node root) {
    this.root = root;
  }

  public void add(String word, String restaurantId) {
    Node cur = root;
    for (int i = 0; i < word.length(); i++) {
      char c = Character.toLowerCase(word.charAt(i));
      if(c == ' ') continue;
      if (!cur.childMap.containsKey(c)) {
        cur.childMap.put(c, new Node());
      }
      cur.insertRestaurant(restaurantId);
      cur = cur.childMap.get(c);
    }
    cur.insertRestaurant(restaurantId);
  }

  public List<String> search(String word) {
    Node cur = root;
    for (int i = 0; i < word.length(); i++) {
      char c = Character.toLowerCase(word.charAt(i));
      if(c == ' ') continue;
      if (!cur.childMap.containsKey(c)) {
        return null;
      }
      cur = cur.childMap.get(c);
    }
    return new ArrayList<>(cur.infoMap.keySet());
  }

  public void remove(String word, String restaurantId) {
    Node cur = root;
    if (word == null) return;
    for (int i = 0; i < word.length(); i++) {
      char c = Character.toLowerCase(word.charAt(i));
      if(c == ' ') continue;
      if (!cur.childMap.containsKey(c)) return;
      cur.deleteRestaurant(restaurantId);
      cur = cur.childMap.get(c);
    }
    cur.deleteRestaurant(restaurantId);
  }
}
