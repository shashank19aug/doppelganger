package org.kalki.doppelganger.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="simulator_response")
public class SimulatorResponseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	@Column(name="simulator_config_id")
	private String simulatorConfigId;

	@Column(name="validation")
	private String validation;
	
	@Column(name="response_code")
	private Integer responseCode;

	@Column(name="response")
	private String response;
	
	@Column(name="timeout_period")
	private Integer timeOutPeriod;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSimulatorConfigId() {
		return simulatorConfigId;
	}

	public void setSimulatorConfigId(String simulatorConfigId) {
		this.simulatorConfigId = simulatorConfigId;
	}

	public String getValidation() {
		return validation;
	}

	public void setValidation(String validation) {
		this.validation = validation;
	}


	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(Integer responseCode) {
		this.responseCode = responseCode;
	}

	public int getTimeOutPeriod() {
		return timeOutPeriod;
	}

	public void setTimeOutPeriod(Integer timeOutPeriod) {
		this.timeOutPeriod = timeOutPeriod;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}


	
}
