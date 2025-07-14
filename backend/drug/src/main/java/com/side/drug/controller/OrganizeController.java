package com.side.drug.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.side.drug.config.DataInitializer;
import com.side.drug.service.DrugOrganizeService;
import com.side.drug.service.OrganizeStatusService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrganizeController {

	private final DrugOrganizeService organizeService;
	private final DataInitializer dataInitializer;
	private final OrganizeStatusService organizeStatusService;

	// 수동 데이터 추가
	@PostMapping("import")
	public ResponseEntity<String> importData() throws IOException {
		dataInitializer.loadExcel();

		return ResponseEntity.ok("엑셀 데이터 DB로 import 완료");
	}


	@PostMapping("/organize")
	public ResponseEntity<String> runOrganize() {
		organizeService.organize();
		return ResponseEntity.ok("Organizing completed!");
	}

	@PostMapping("/organize/stop")
	public ResponseEntity<String> stopOrganize() {
		organizeStatusService.stop();
		return ResponseEntity.ok("Organize 중단 요청 완료");
	}
}