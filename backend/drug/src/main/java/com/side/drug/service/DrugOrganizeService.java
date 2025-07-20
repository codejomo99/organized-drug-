package com.side.drug.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Service;

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

	// 메모리 플래그
	private final AtomicBoolean runningFlag = new AtomicBoolean(false);

	private final DrugProfileRepository rawRepo;
	private final OrganizedDrugProfileRepository organizedRepo;
	private final OrganizeStatusService statusService;
	private final LogWebSocketHandler logWebSocketHandler;

	public synchronized void start() {
		if (runningFlag.get()) return;
		runningFlag.set(true);

		// 백그라운드로 실제 작업 실행
		new Thread(this::organize).start();
	}

	/** 중단 요청 */
	public void stop() {
		runningFlag.set(false);
		statusService.updateProgress(statusService.getLastProcessedId(), false);
	}

	/** 실제 집계 로직 */
	public void organize() {
		// 1) 시작 플래그 ON, DB에 기록
		long lastId = statusService.getLastProcessedId();
		statusService.updateProgress(lastId, true);

		// 2) 커서 기반 데이터 로드
		List<DrugProfile> rawList = rawRepo.findByIdGreaterThanOrderByIdAsc(lastId);
		List<OrganizedDrugProfile> organizedList = organizedRepo.findAll();

		// 3) 처리 루프
		for (DrugProfile raw : rawList) {
			if (!runningFlag.get()) {
				statusService.updateProgress(raw.getId(), false);
				log(">>> Organize 중단됨 (ID: "+ raw.getId() +")");
				return;
			}

			// 머지 로직
			Optional<OrganizedDrugProfile> opt = organizedList.stream()
				.filter(org -> isMatch(org, raw))
				.findFirst();

			if (opt.isPresent()) {
				OrganizedDrugProfile org = opt.get();
				org.setCompanyName(merge(org.getCompanyName(), raw.getCompanyName()));
				org.setBrandName(merge(org.getBrandName(), raw.getBrandName()));
				org.setInnName(merge(org.getInnName(), raw.getInnName()));
				org.setCodeName(merge(org.getCodeName(), raw.getCodeName()));

				log(">>> MERGE: "
					+ raw.getCompanyName()
					+ ", " + raw.getBrandName()
					+ ", " + raw.getInnName()
					+ ", " + raw.getCodeName());

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


			// 진행 위치(DB 기록)
			lastId = raw.getId();
			statusService.updateProgress(lastId, true);
		}

		// 4) 결과 저장
		organizedRepo.deleteAllInBatch();
		organizedRepo.saveAll(organizedList);
		log(">>> 저장 완료: 총 "+organizedList.size()+"건");

		// 5) 종료 플래그 OFF
		statusService.updateProgress(lastId, false);
		runningFlag.set(false);
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
		log.info(message);
	}

}
