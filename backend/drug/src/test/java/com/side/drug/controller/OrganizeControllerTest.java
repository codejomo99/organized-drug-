package com.side.drug.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.side.drug.model.OrganizeStatus;
import com.side.drug.model.OrganizedDrugProfile;
import com.side.drug.repository.OrganizeStatusRepository;
import com.side.drug.repository.OrganizedDrugProfileRepository;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class OrganizeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OrganizeStatusRepository statusRepo;

	@Autowired
	private OrganizedDrugProfileRepository organizedRepo;

	@Test
	@DisplayName("1. organize 실행 후 중단 → resume 되는지 확인")
	void testOrganizeAndResumeFlow() throws Exception {
		// 1. 실행
		mockMvc.perform(post("/organize"))
			.andExpect(status().isOk());

		// 2. 중단 요청
		mockMvc.perform(post("/organize/stop"))
			.andExpect(status().isOk());

		// 중단 상태 확인
		OrganizeStatus status = statusRepo.findById(1L).orElseThrow();
		assertFalse(status.isRunning());

		// 3. 재시작
		mockMvc.perform(post("/organize"))
			.andExpect(status().isOk());

		// 결과 데이터 확인
		List<OrganizedDrugProfile> results = organizedRepo.findAll();
		assertFalse(results.isEmpty());
	}
}
