package org.kalki.doppelganger.repository;

import java.util.ArrayList;
import java.util.List;

import org.kalki.doppelganger.entity.RequestForwardConfigEntity;
import org.kalki.doppelganger.entity.SimulatorResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SimulatorConfigDaoService {

	@Autowired
	SimulatorConfigRepository simulatorConfigRepository;

	@Autowired
	RequestForwardConfigRepository reqForwardConfigRepo;

	public List<SimulatorResponseEntity> findByApiNameAndRequestType(String apiName, String requestType) {
		Object[] respArr = simulatorConfigRepository.findByApiNameAndRequestType(apiName, requestType);
		return populateSimResponseEntity(respArr);
	}

	private List<SimulatorResponseEntity> populateSimResponseEntity(Object[] respArray) {
		List<SimulatorResponseEntity> responseEntityList = new ArrayList<>();
		for (int i = 0; i < respArray.length; i++) {
			SimulatorResponseEntity simResponseEntity = new SimulatorResponseEntity();
			Object[] resp = (Object[]) respArray[i];
			simResponseEntity.setValidation((String) resp[0]);
			simResponseEntity.setResponseCode(getValue(resp[1], Integer.class));
			simResponseEntity.setResponse((String) resp[2]);
			simResponseEntity.setTimeOutPeriod(getValue(resp[3], Integer.class));
			responseEntityList.add(simResponseEntity);
		}
		return responseEntityList;
	}

	private <T> T getValue(Object value, Class<T> valueType) {
		if (value == null) {
			return null;
		}
		return valueType.cast(value);
	}

	public RequestForwardConfigEntity findForwardDetailsByRequestName(String requestName) {
		return reqForwardConfigRepo.findByRequestName(requestName);
	}

	public List<SimulatorResponseEntity> findByApiNameAndRequestBody(String apiName, String requestBody) {
		Object[] respArr = simulatorConfigRepository.findByApiNameAndRequestBody(apiName, requestBody);
		return populateSimResponseEntity(respArr);
	}

}
