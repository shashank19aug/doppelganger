package org.kalki.doppelganger.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kalki.doppelganger.entity.SimulatorResponseEntity;
import org.kalki.doppelganger.repository.SimulatorConfigDaoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus.Series;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SimulationService {
	private static Logger LOG = LoggerFactory.getLogger(SimulationService.class);

	@Autowired
	SimulatorConfigDaoService simulatorConfigDaoService;

	public ResponseEntity<String> execute(String requestType, String apiName, String requestBody) {
		List<SimulatorResponseEntity> simRespList = simulatorConfigDaoService.findByApiNameAndRequestType(apiName, requestType);
		if (simRespList != null) {
			for (SimulatorResponseEntity simRespEntity : simRespList) {
				String validationString = simRespEntity.getValidation();
				if (StringUtils.isNotBlank(validationString)) {
					String[] validationArr = StringUtils.split(validationString, "|");
					boolean isValid = false;
					for (String validation : validationArr) {
						if (requestBody.contains(validation)) {
							isValid = true;
						} else {
							isValid = false;
							break;
						}
					}
					if (isValid) {
						Integer statusCode = simRespEntity.getResponseCode();
						String response = simRespEntity.getResponse();
						if (statusCode == 503) {
							Long timeOut = Long.valueOf(simRespEntity.getTimeOutPeriod());
							try {
								Thread.sleep(timeOut);
							} catch (InterruptedException e) {
								LOG.error(e.getMessage(), e);
							}
							return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
						} else {
							HttpStatus httpStatus;
							try {
								httpStatus = HttpStatus.valueOf(statusCode);
							} catch (IllegalArgumentException e) {
								LOG.error(e.getMessage());
								Series series = HttpStatus.Series.valueOf(statusCode);
								httpStatus = HttpStatus.valueOf(Integer.parseInt(series.value() + "00"));
								LOG.info("Changing HttpStatus to [" + httpStatus.value() + "]");
							}
							return new ResponseEntity<>(response, httpStatus);
						}
					}
				}
			}
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

}
