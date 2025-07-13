package com.side.drug.service;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.side.drug.model.DrugProfile;
import com.side.drug.repository.DrugProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExcelImportService {
	private final DrugProfileRepository drugProfileRepository;

	public void importExcel(InputStream inputStream) throws IOException {
		Workbook workbook = new XSSFWorkbook(inputStream);
		Sheet sheet = workbook.getSheetAt(0);

		for (Row row : sheet) {
			if(row.getRowNum() == 0) continue; // 첫 줄은 헤더

			DrugProfile profile = new DrugProfile();
			profile.setCompanyName(getCellValue(row, 0));
			profile.setBrandName(getCellValue(row, 1));
			profile.setInnName(getCellValue(row, 2));
			profile.setCodeName(getCellValue(row, 3));

			drugProfileRepository.save(profile);
		}
	}

	private String getCellValue(Row row, int index) {
		Cell cell = row.getCell(index);
		return (cell != null) ? cell.toString().trim() : "";
	}
}
