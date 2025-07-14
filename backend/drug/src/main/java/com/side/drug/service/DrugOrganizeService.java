package com.side.drug.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.side.drug.model.DrugProfile;
import com.side.drug.model.OrganizedDrugProfile;
import com.side.drug.repository.DrugProfileRepository;
import com.side.drug.repository.OrganizedDrugProfileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DrugOrganizeService {


	private final DrugProfileRepository rawRepo;
	private final OrganizedDrugProfileRepository organizedRepo;
	private final OrganizeStatusService statusService;

	@Transactional
	public void organize() {
		// 이미 실행 중이면 중복 방지
		if (statusService.isRunning()) {
			throw new IllegalStateException("이미 집계가 실행 중입니다.");
		}

		// 시작 전 실행 상태 true로 설정
		Long lastId = statusService.getLastProcessedId();
		statusService.updateProgress(lastId, true);

		// 마지막 처리된 ID 이후부터 조회 (커서 기반 처리)
		List<DrugProfile> rawList = rawRepo.findByIdGreaterThanOrderByIdAsc(lastId);

		// 기존 저장된 조직화된 데이터 불러오기 (메모리에 유지)
		List<OrganizedDrugProfile> organizedList = organizedRepo.findAll();

		for (DrugProfile raw : rawList) {
			// 중단 요청 확인 → 처리 중단
			if (!statusService.isRunning()) {
				log.info(">>> Organize 중단됨 (현재 ID: {})", raw.getId());
				break;
			}

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

			// 진행상황 저장 (재시작 시 이어서 처리 가능하게)
			statusService.updateProgress(raw.getId(), true);

			// 진행 로그 출력
			log.info(" - Company: {}", raw.getCompanyName());
			log.info(" - Brand  : {}", raw.getBrandName());
			log.info(" - INN    : {}", raw.getInnName());
			log.info(" - Code   : {}", raw.getCodeName());
		}

		// 결과 저장 (전체 재저장 방식)
		organizedRepo.deleteAllInBatch();
		organizedRepo.saveAll(organizedList);
		log.info(">>> 저장 완료: 총 {}건", organizedList.size());

		// 집계 완료 후 상태 초기화
		statusService.updateProgress(0L, false);
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
