package com.side.drug.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.side.drug.model.DrugProfile;
import com.side.drug.model.OrganizedDrugProfile;
import com.side.drug.repository.DrugProfileRepository;
import com.side.drug.repository.OrganizedDrugProfileRepository;

@SpringBootTest
@Transactional
class DrugOrganizeServiceTest {

	@Autowired
	private DrugProfileRepository drugRepo;

	@Autowired
	private OrganizedDrugProfileRepository organizedRepo;

	@Autowired
	private DrugOrganizeService organizeService;

	@Autowired
	private OrganizeStatusService statusService;

	@BeforeEach
	void setUp() {
		organizedRepo.deleteAll();
		drugRepo.deleteAll();
		statusService.updateProgress(0L, false);
	}

	@Test
	@DisplayName("PDF 요구사항에 따른 병합 결과 테스트")
	void testMergeExampleFromRequirement() {
		// A 데이터
		drugRepo.save(new DrugProfile(null,
			"Qpex Biopharma__Brii Biosciences",
			"Lucentis (US)__Susvimo",
			"Botulinum toxin A__Botulinum neurotoxin type A__abobotulinumtoxinA",
			"ADC product candidate"
		));

		// B 데이터
		drugRepo.save(new DrugProfile(null,
			"Brii Biosciences__FFF",
			"Lucentis (US)__Susvimo CC",
			"Botulinum toxin A",
			"" // code name 없음
		));

		// 집계 실행
		organizeService.organize();

		List<OrganizedDrugProfile> result = organizedRepo.findAll();

		assertEquals(1, result.size());

		OrganizedDrugProfile org = result.get(0);

		assertThat(org.getCompanyName())
			.contains("Qpex Biopharma")
			.contains("Brii Biosciences")
			.contains("FFF");

		assertThat(org.getBrandName())
			.contains("Lucentis (US)")
			.contains("Susvimo")
			.contains("Susvimo CC");

		assertThat(org.getInnName())
			.contains("Botulinum toxin A")
			.contains("Botulinum neurotoxin type A")
			.contains("abobotulinumtoxinA");

		assertThat(org.getCodeName())
			.contains("ADC product candidate");
	}
}
