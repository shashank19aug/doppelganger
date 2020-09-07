package org.kalki.doppelganger.service;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.kalki.doppelganger.entity.RequestForwardConfigEntity;
import org.kalki.doppelganger.repository.SimulatorConfigDaoService;
import org.kalki.doppelganger.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus.Series;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class RequestForwardService {

	private static Logger LOG = LoggerFactory.getLogger(RequestForwardService.class);

	@Autowired
	SimulatorConfigDaoService simConfigDaoService;

	public ResponseEntity<String> execute(String requestType, String requestName, String requestBody, HttpHeaders httpHeaders, String dynamicData) {

		String response = "";
		RequestForwardConfigEntity entity = simConfigDaoService.findForwardDetailsByRequestName(requestName);
		if (entity != null) {
			String timeout = entity.getTimeOut();
			if (StringUtils.isNotBlank(timeout)) {
				Long timeOut = Long.valueOf(timeout);
				try {
					Thread.sleep(timeOut);
				} catch (InterruptedException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			String url = entity.getForwardUrl();
			HashMap<String, String> header = new HashMap<>();

			for (Map.Entry<String, List<String>> entry : httpHeaders.entrySet()) {
				String headerName = entry.getKey();
				if (!"content-length".equalsIgnoreCase(headerName) && !"host".equalsIgnoreCase(headerName) && !"user-agent".equalsIgnoreCase(headerName)) {
					for (String headerValue : entry.getValue()) {
						header.put(headerName, headerValue);
					}
				}
			}
			Map<String, String> params = new HashMap<>();
			HttpUtil.StringResponse stringResponse = null;
			int connectionTimeout = 60000;
			int socketTimeout = 60000;
			try {
				if ("POST".equals(requestType)) {
					if (StringUtils.isNoneBlank(dynamicData)) {
						url = replaceDynamicData(url, dynamicData);
					}
					stringResponse = HttpUtil.post2(url, header, params, requestBody, connectionTimeout, socketTimeout, true);
				} else if ("PUT".equals(requestType)) {
					if (StringUtils.isNoneBlank(dynamicData)) {
						url = replaceDynamicData(url, dynamicData);
					}
					stringResponse = HttpUtil.put(url, header, params, requestBody, connectionTimeout, socketTimeout, true);
				} else {
					url = replaceDynamicData(url, requestBody);
					stringResponse = HttpUtil.get2(url, header, params, connectionTimeout, socketTimeout, true);
				}
				if (stringResponse != null) {
					int statusCode = stringResponse.getStatusCode();
					response = stringResponse.getContent();
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
				} else {
					LOG.info("No response.");
					return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
				}
			} catch (ConnectTimeoutException | SocketTimeoutException e) {
				LOG.error(e.getMessage(), e);
				return new ResponseEntity<>(HttpStatus.GATEWAY_TIMEOUT);
			}
		}
		return null;
	}

	public ResponseEntity<String> execute(String requestType, String requestName, String requestBody, HttpHeaders httpHeaders) {
		return execute(requestType, requestName, requestBody, httpHeaders, "");
	}

	private static String replaceDynamicData(String url, String dynamicDataList) {
		String[] dynamicDataArray = dynamicDataList.split("_");
		int i = 0;
		for (String dynamicData : dynamicDataArray) {
			if (i == 0) {
				url = StringUtils.replace(url, "[DYNAMIC_DATA]", dynamicData);
			} else {
				url = StringUtils.replace(url, "[DYNAMIC_DATA_" + String.valueOf(i) + "]", dynamicData);
			}
			i++;
		}
		return url;
	}

	public static void main(String[] args) {
		String requestBody = "123_456";
		String url = "https://103.1.113.185/accounts/[DYNAMIC_DATA]/[DYNAMIC_DATA_1]/balances?api_key=kyqak5muymxcrjhc5q57vz9v";
		url = replaceDynamicData(url, requestBody);
		System.out.println(url);
		// StringResponse stringResponse = HttpClientUtil.get2(url, header, params,
		// connectionTimeout, socketTimeout);
	}

}
