package com.planwith.planwith_fo_grade.adapter.in.web;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_grade.config.DeployProperties;

@RestController
@RequestMapping("/api/grade")
public class DeployController {

	private static final Logger log = LoggerFactory.getLogger(DeployController.class);

	private final DeployProperties deployProperties;

	public DeployController(DeployProperties deployProperties) {
		this.deployProperties = deployProperties;
	}

	// 배포 상태 확인
	@GetMapping("/deploy-check")
	public ResponseEntity<Map<String, String>> deployCheck() {
		log.info("DeployController : GET deployCheck : 배포 상태 확인 요청");
		Map<String, String> response = Map.of(
				"service", "grade-service",
				"marker", deployProperties.marker(),
				"message", "grade-service deploy pipeline ok"
		);
		log.info("DeployController : GET deployCheck : 배포 상태 확인 완료");
		return ResponseEntity.ok(response);
	}
}
