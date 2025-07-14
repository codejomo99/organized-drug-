package com.side.drug.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "organize_status")
public class OrganizeStatus {

	@Id
	private Long id = 1L; // 고정 ID (1건만 존재)

	private Long lastProcessedId; // 마지막으로 처리한 DrugProfile ID

	private boolean running; // 현재 집계 중인지 여부

	private LocalDateTime updatedAt;

	@PrePersist
	@PreUpdate
	public void updateTimestamp() {
		updatedAt = LocalDateTime.now();
	}
}