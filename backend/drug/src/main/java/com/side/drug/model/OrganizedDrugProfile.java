package com.side.drug.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "organized_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizedDrugProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(columnDefinition = "TEXT")
	private String companyName;

	@Column(columnDefinition = "TEXT")
	private String brandName;

	@Column(columnDefinition = "TEXT")
	private String innName;

	@Column(columnDefinition = "TEXT")
	private String codeName;
}
