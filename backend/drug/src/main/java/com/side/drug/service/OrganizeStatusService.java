package com.side.drug.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.side.drug.model.OrganizeStatus;
import com.side.drug.repository.OrganizeStatusRepository;

@Service
@RequiredArgsConstructor
public class OrganizeStatusService {

	private final OrganizeStatusRepository repository;

	public OrganizeStatus getOrCreate() {
		return repository.findById(1L).orElseGet(() -> {
			OrganizeStatus status = new OrganizeStatus();
			status.setId(1L);
			status.setLastProcessedId(0L);
			status.setRunning(false);
			return repository.save(status);
		});
	}

	public void updateProgress(Long lastProcessedId, boolean running) {
		OrganizeStatus status = getOrCreate();
		status.setLastProcessedId(lastProcessedId);
		status.setRunning(running);
		repository.save(status);
	}

	public void stop() {
		OrganizeStatus status = getOrCreate();
		status.setRunning(false);
		repository.save(status);
	}

	public boolean isRunning() {
		return getOrCreate().isRunning();
	}

	public Long getLastProcessedId() {
		return getOrCreate().getLastProcessedId();
	}
}

