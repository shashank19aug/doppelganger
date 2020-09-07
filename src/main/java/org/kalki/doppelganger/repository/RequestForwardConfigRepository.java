package org.kalki.doppelganger.repository;

import org.kalki.doppelganger.entity.RequestForwardConfigEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public interface RequestForwardConfigRepository extends CrudRepository<RequestForwardConfigEntity, Integer>{

	
	@Query(value="select * from request_forward where request_name=?1 ",nativeQuery=true)
	RequestForwardConfigEntity findByRequestName(String requestName);
}
