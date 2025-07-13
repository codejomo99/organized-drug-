package com.side.drug;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.side.drug.service.ExcelImportService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer {

	private final ExcelImportService excelImportService;

	@PostConstruct
	public void init() throws IOException {
		ClassPathResource file = new ClassPathResource("data/drugProfile.xlsx");
		excelImportService.importExcel(file.getInputStream());
	}
}