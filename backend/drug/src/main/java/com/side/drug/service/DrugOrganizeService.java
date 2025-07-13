package com.side.drug.service;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.side.drug.model.DrugProfile;
import com.side.drug.model.OrganizedDrugProfile;
import com.side.drug.repository.DrugProfileRepository;
import com.side.drug.repository.OrganizedDrugProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DrugOrganizeService {

	private static final Logger log = LoggerFactory.getLogger(DrugOrganizeService.class);

	private final DrugProfileRepository rawRepo;
	private final OrganizedDrugProfileRepository organizedRepo;

	@Transactional
	public void organize() {
		List<DrugProfile> rawList = rawRepo.findAll();
		List<OrganizedDrugProfile> organizedList = new ArrayList<>();

		for (DrugProfile raw : rawList) {
			Optional<OrganizedDrugProfile> matched = organizedList.stream()
				.filter(org -> isMatch(org, raw))
				.findFirst();

			if (matched.isPresent()) {
				OrganizedDrugProfile org = matched.get();
				org.setCompanyName(merge(org.getCompanyName(), raw.getCompanyName()));
				org.setBrandName(merge(org.getBrandName(), raw.getBrandName()));
				org.setInnName(merge(org.getInnName(), raw.getInnName()));
				org.setCodeName(merge(org.getCodeName(), raw.getCodeName()));

				log.info(">>> MERGE with existing group:");
			} else {
				organizedList.add(new OrganizedDrugProfile(
					null,
					raw.getCompanyName(),
					raw.getBrandName(),
					raw.getInnName(),
					raw.getCodeName()
				));
				log.info(">>> NEW group created:");
			}

			log.info(" - Company: {}", raw.getCompanyName());
			log.info(" - Brand  : {}", raw.getBrandName());
			log.info(" - INN    : {}", raw.getInnName());
			log.info(" - Code   : {}", raw.getCodeName());
		}

		organizedRepo.deleteAllInBatch();
		organizedRepo.saveAll(organizedList);
		log.info(">>> 저장 완료: 총 {}건", organizedList.size());
	}

	private boolean isMatch(OrganizedDrugProfile org, DrugProfile raw) {
		return hasCommonElement(org.getCompanyName(), raw.getCompanyName())
			&& (hasCommonElement(org.getBrandName(), raw.getBrandName())
			|| hasCommonElement(org.getInnName(), raw.getInnName())
			|| hasCommonElement(org.getCodeName(), raw.getCodeName()));
	}

	private boolean hasCommonElement(String s1, String s2) {
		if (s1 == null || s2 == null) return false;
		Set<String> set1 = new HashSet<>(Arrays.asList(s1.split("__")));
		Set<String> set2 = new HashSet<>(Arrays.asList(s2.split("__")));
		set1.retainAll(set2);
		return !set1.isEmpty();
	}

	private String merge(String original, String newValue) {
		Set<String> merged = new LinkedHashSet<>();
		if (original != null) merged.addAll(Arrays.asList(original.split("__")));
		if (newValue != null) merged.addAll(Arrays.asList(newValue.split("__")));
		return String.join("__", merged);
	}
}
