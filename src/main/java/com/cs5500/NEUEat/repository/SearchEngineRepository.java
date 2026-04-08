package com.cs5500.NEUEat.repository;

import com.cs5500.NEUEat.model.SearchEngine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchEngineRepository extends JpaRepository<SearchEngine, String> {

}
