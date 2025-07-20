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
import com.side.drug.websocket.LogWebSocketHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DrugOrganizeService {


	private final DrugProfileRepository rawRepo;
	private final OrganizedDrugProfileRepository organizedRepo;
	private final OrganizeStatusService statusService;
	private final LogWebSocketHandler logWebSocketHandler;

	public void organize() {

		// 1) 시작 플래그 ON
		long lastId = statusService.getLastProcessedId();
		statusService.updateProgress(lastId, true);

		// 2) 커서 기반 데이터 로드
		List<DrugProfile> rawList = rawRepo.findByIdGreaterThanOrderByIdAsc(lastId);
		List<OrganizedDrugProfile> organizedList = organizedRepo.findAll();

		// 3) 처리 루프
		for (DrugProfile raw : rawList) {
			// stop() 요청 시 바로 빠져나오기
			if (!statusService.isRunning()) {
				log(">>> Organize 중단됨 (ID: "+raw.getId()+")");
				break;
			}

			// 머지 로직
			Optional<OrganizedDrugProfile> matched = organizedList.stream()
				.filter(org -> isMatch(org, raw))
				.findFirst();

			if (matched.isPresent()) {
				OrganizedDrugProfile org = matched.get();
				org.setCompanyName(merge(org.getCompanyName(), raw.getCompanyName()));
				org.setBrandName(merge(org.getBrandName(), raw.getBrandName()));
				org.setInnName(merge(org.getInnName(), raw.getInnName()));
				org.setCodeName(merge(org.getCodeName(), raw.getCodeName()));
				log(">>> MERGE: "+raw.getCompanyName()+")");
			} else {
				organizedList.add(new OrganizedDrugProfile(
					null,
					raw.getCompanyName(),
					raw.getBrandName(),
					raw.getInnName(),
					raw.getCodeName()
				));
				log(">>> NEW GROUP: "+raw.getCompanyName()+")");
			}

			//  테스트/디버깅용 잠깐 멈춤
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			// 4) 진척도 업데이트 (플래그 유지)
			lastId = raw.getId();
			statusService.updateProgress(lastId, true);
		}

		// 5) 결과 저장
		organizedRepo.deleteAllInBatch();
		organizedRepo.saveAll(organizedList);
		log(">>> 저장 완료: 총 "+organizedList.size()+"건");

		// 6) 종료 플래그 OFF (lastId 유지)
		statusService.updateProgress(lastId, false);
		log(">>> Organize 종료 (최종 ID: "+lastId+")");
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

	public void log(String message) {
		logWebSocketHandler.broadcast(message); // WebSocket 로그
		log.info(message); // 콘솔 로그
	}

}
