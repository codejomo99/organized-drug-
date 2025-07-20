package com.side.drug.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.side.drug.service.DrugOrganizeService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class OrganizeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private DrugOrganizeService organizeService;

	@BeforeEach
	void setUp() {
		// in‑memory flag 초기화
		AtomicBoolean flag =
			(AtomicBoolean) ReflectionTestUtils.getField(organizeService, "runningFlag");
		flag.set(false);
	}

	@Test
	@DisplayName("API 통해 start → stop → restart 시 in-memory flag 변화를 확인")
	void testInMemoryApi() throws Exception {
		AtomicBoolean flag =
			(AtomicBoolean) ReflectionTestUtils.getField(organizeService, "runningFlag");

		// 1) 시작
		mockMvc.perform(post("/organize"))
			.andExpect(status().isOk());
		// 바로 in-memory 플래그가 켜져야 한다
		assertTrue(flag.get(), "POST /organize 후 runningFlag=true 이어야 한다");

		// 2) 중단
		mockMvc.perform(post("/organize/stop"))
			.andExpect(status().isOk());
		// 즉시 꺼져야 한다
		assertFalse(flag.get(), "POST /organize/stop 후 runningFlag=false 이어야 한다");

		// 3) 재시작
		mockMvc.perform(post("/organize"))
			.andExpect(status().isOk());
		// 다시 켜져야 한다
		assertTrue(flag.get(), "두 번째 POST /organize 후 runningFlag=true 이어야 한다");
	}
}
