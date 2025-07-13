package com.side.drug.config;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.side.drug.service.ExcelImportService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer {

	private final ExcelImportService excelImportService;

	public void loadExcel() throws IOException {
		ClassPathResource file = new ClassPathResource("data/drugProfile.xlsx");
		excelImportService.importExcel(file.getInputStream());
	}
}