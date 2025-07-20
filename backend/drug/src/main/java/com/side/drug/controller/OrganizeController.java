package com.side.drug.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.side.drug.config.DataInitializer;
import com.side.drug.service.DrugOrganizeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/organize")
@RequiredArgsConstructor
public class OrganizeController {

	private final DrugOrganizeService organizeService;
	private final DataInitializer dataInitializer;


	// 수동 데이터 추가
	@PostMapping("/import")
	public ResponseEntity<String> importData() throws IOException {
		dataInitializer.loadExcel();
		return ResponseEntity.ok("엑셀 데이터 DB로 import 완료");
	}


	@PostMapping
	public ResponseEntity<String> runOrganize() {
		organizeService.start();
		return ResponseEntity.ok().build();
	}

	@PostMapping("/stop")
	public ResponseEntity<String> stopOrganize() {
		organizeService.stop();
		return ResponseEntity.ok("Organize 중단 요청 완료");
	}
}