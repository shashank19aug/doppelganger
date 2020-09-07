package org.kalki.doppelganger.repository;

import org.kalki.doppelganger.entity.SimulatorConfigEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


public interface SimulatorConfigRepository extends CrudRepository<SimulatorConfigEntity, Integer>{

	
	@Query(value="select sr.validation ,sr.response_code,sr.response,sr.timeout_period from simulator_response sr ,simulator_config sc  "
			+ "where sc.id=sr.simulator_config_id and sc.request_type=?2 and sc.api_name=?1 ",nativeQuery=true)
	Object[] findByApiNameAndRequestType(String apiName,String requestType);
	
	@Query(value="select sr.validation ,sr.response_code,sr.response,sr.timeout_period from simulator_response sr ,simulator_config sc  "
			+ "where sc.id=sr.simulator_config_id and sc.request_type=?2 and sc.api_name=?1 ",nativeQuery=true)
	Object[] findByApiNameAndRequestBody(String apiName,String requestBody);
}
