package org.kalki.doppelganger.controller;

import org.kalki.doppelganger.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simulate")
public class DoppelgangerSimulateController {


	@Autowired
	private SimulationService simulationService;

	@PostMapping(path = "/json/{apiName}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> simulateJson(@RequestBody String requestBody, @PathVariable String apiName) {
		return simulationService.execute("JSON", apiName, requestBody);
	}

	@GetMapping(path = "/json/{apiName}/{requestBody}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> simulateJsonGet(@PathVariable String apiName, @PathVariable String requestBody) {
		return simulationService.execute("GET", apiName, requestBody);
	}

	@PostMapping(path = "/xml/{apiName}", produces = { MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<String> simulateXml(@RequestBody String requestBody, @PathVariable String apiName) {
		return simulationService.execute("XML", apiName, requestBody);
	}

	@PutMapping(path = "/json/{apiName}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> simulateJsonPut(@RequestBody String requestBody, @PathVariable String apiName) {
		return simulationService.execute("JSON", apiName, requestBody);
	}

	@PutMapping(path = "/json/{apiName}/{requestBody}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> simulateJsonPutWithURLBody(@PathVariable String apiName, @PathVariable String requestBody) {
		return simulationService.execute("JSON", apiName, requestBody);
	}

}
