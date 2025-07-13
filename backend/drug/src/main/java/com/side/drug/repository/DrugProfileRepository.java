package com.side.drug.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.side.drug.model.DrugProfile;

@Repository
public interface DrugProfileRepository extends JpaRepository <DrugProfile, Long> {

}
