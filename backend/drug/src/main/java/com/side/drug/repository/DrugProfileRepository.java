package com.side.drug.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.side.drug.model.DrugProfile;

@Repository
public interface DrugProfileRepository extends JpaRepository <DrugProfile, Long> {
	List<DrugProfile> findByIdGreaterThanOrderByIdAsc(Long id);
}
