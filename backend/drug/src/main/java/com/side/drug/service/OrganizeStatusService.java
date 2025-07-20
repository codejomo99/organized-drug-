package com.side.drug.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.side.drug.model.OrganizeStatus;
import com.side.drug.repository.DrugProfileRepository;
import com.side.drug.repository.OrganizeStatusRepository;
import com.side.drug.websocket.LogWebSocketHandler;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrganizeStatusService {

	private final OrganizeStatusRepository repository;
	private final LogWebSocketHandler logWebSocketHandler;
	private final DrugProfileRepository drugProfileRepository;

	public OrganizeStatus getOrCreate() {
		return repository.findById(1L).orElseGet(() -> {
			OrganizeStatus status = new OrganizeStatus();
			status.setId(1L);
			status.setLastProcessedId(0L);
			status.setRunning(false);
			return repository.save(status);
		});
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void updateProgress(Long lastProcessedId, boolean running) {
		OrganizeStatus status = getOrCreate();
		status.setLastProcessedId(lastProcessedId);
		status.setRunning(running);
		repository.saveAndFlush(status);


		long total = drugProfileRepository.findAll().size();
		logWebSocketHandler.sendProgress(lastProcessedId, total);
	}

	public Long getLastProcessedId() {
		return getOrCreate().getLastProcessedId();
	}

}

