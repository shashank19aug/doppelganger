package org.kalki.doppelganger.controller;


import org.kalki.doppelganger.service.RequestForwardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

public class DoppelgangerForwardController {

	@Autowired
	RequestForwardService requestForwardService;

	@PostMapping(path = "/json/{requestName}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> forwardPostJsonRequest(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, @PathVariable String requestName) {
		return requestForwardService.execute("POST", requestName, requestBody, headers);
	}

	@PutMapping(path = "/json/{requestName}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> forwardPutJsonRequest(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, @PathVariable String requestName) {
		return requestForwardService.execute("PUT", requestName, requestBody, headers);
	}
	
	
	@PutMapping(path = "/json/{requestName}/{dynamicData}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> forwardPutJsonRequestWithDynamicData(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, @PathVariable String requestName,@PathVariable String dynamicData) {
		return requestForwardService.execute("PUT", requestName, requestBody, headers,dynamicData);
	}
	
	@PostMapping(path = "/json/{requestName}/{dynamicData}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> forwardPostJsonRequestWithDynamicData(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, @PathVariable String requestName,@PathVariable String dynamicData) {
		return requestForwardService.execute("POST", requestName, requestBody, headers,dynamicData);
	}
	

	@GetMapping(path = "/json/{requestName}/{requestBody}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> forwardGetJsonRequest(@RequestHeader HttpHeaders headers, @PathVariable String requestName, @PathVariable String requestBody) {
		return requestForwardService.execute("GET", requestName, requestBody, headers);
	}

	@PostMapping(path = "/xml/{requestName}", produces = { MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<String> forwardXmlRequest(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, @PathVariable String requestName) {
		return requestForwardService.execute("POST", requestName, requestBody, headers);
	}

}
