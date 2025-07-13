package com.side.drug.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.side.drug.model.OrganizedDrugProfile;

@Repository
public interface OrganizedDrugProfileRepository extends JpaRepository<OrganizedDrugProfile, Long> {
}
