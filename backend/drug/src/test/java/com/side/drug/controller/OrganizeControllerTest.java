package com.side.drug.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.side.drug.model.OrganizeStatus;
import com.side.drug.repository.DrugProfileRepository;
import com.side.drug.repository.OrganizeStatusRepository;
import com.side.drug.repository.OrganizedDrugProfileRepository;
import com.side.drug.service.OrganizeStatusService;

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

	@Autowired
	private DrugProfileRepository drugRepo;

	@Autowired
	private OrganizeStatusService statusService;

	@BeforeEach
	void setUp() {
		organizedRepo.deleteAll();
		drugRepo.deleteAll();
		statusService.updateProgress(0L, false);
	}

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
		System.out.println(">>> 중단 후 isRunning: " + status.isRunning());

		// 3. 재시작
		mockMvc.perform(post("/organize"))
			.andExpect(status().isOk());

		// 재시작 상태 확인
		OrganizeStatus resumed = statusRepo.findById(1L).orElseThrow();
		assertTrue(resumed.isRunning());
		System.out.println(">>> 재시작 후 isRunning: " + resumed.isRunning());
	}
}
