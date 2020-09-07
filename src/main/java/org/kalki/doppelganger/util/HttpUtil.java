package org.kalki.doppelganger.util;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtil {

	private static Logger LOG = LoggerFactory.getLogger(HttpUtil.class);

	private HttpUtil() {
	}

	public static class ByteResponse {
		private int statusCode;
		private byte[] content;

		public ByteResponse(int statusCode, byte[] content) {
			super();
			this.statusCode = statusCode;
			if (content != null) {
				this.content = Arrays.copyOf(content, content.length);
			}
		}

		public int getStatusCode() {
			return statusCode;
		}

		public byte[] getContent() {
			return content;
		}
	}

	public static class StringResponse {
		private int statusCode;
		private String content;

		public StringResponse(int statusCode) {
			this(statusCode, null);
		}

		public StringResponse(int statusCode, String content) {
			super();
			this.statusCode = statusCode;
			this.content = content;
		}

		public int getStatusCode() {
			return statusCode;
		}

		public String getContent() {
			return content;
		}
	}

	public static byte[] post(String uri, byte[] data, int connectionTimeout, int socketTimeout) throws ConnectTimeoutException, SocketTimeoutException {

		HttpClient client = new DefaultHttpClient();
		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
		HttpConnectionParams.setSoTimeout(params, socketTimeout);
		enableHttpsTunnelIfRequired(uri, client);

		HttpPost post = new HttpPost(uri);
		post.setEntity(new ByteArrayEntity(data));

		HttpResponse httpResponse;
		try {
			httpResponse = client.execute(post);
			return IOUtils.toByteArray(httpResponse.getEntity().getContent());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] post(String uri, byte[] data) {

		HttpClient client = new DefaultHttpClient();
		enableHttpsTunnelIfRequired(uri, client);

		HttpPost post = new HttpPost(uri);
		post.setEntity(new ByteArrayEntity(data));

		HttpResponse httpResponse;
		try {
			httpResponse = client.execute(post);
			return IOUtils.toByteArray(httpResponse.getEntity().getContent());
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null && client.getClass() != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}

	public static ByteResponse post2(String uri, byte[] data) {
		HttpClient client = new DefaultHttpClient();
		enableHttpsTunnelIfRequired(uri, client);
		HttpPost post = new HttpPost(uri);
		post.setEntity(new ByteArrayEntity(data));

		HttpResponse httpResponse;
		try {
			httpResponse = client.execute(post);
			return new ByteResponse(httpResponse.getStatusLine().getStatusCode(), IOUtils.toByteArray(httpResponse.getEntity().getContent()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null && client.getClass() != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}

	public static StringResponse post2(String uri, Map<String, String> headers, String data) {
		return post2(uri, headers, data, false, null);
	}

// If Responsedata is needed make this flag true
	public static StringResponse post2(String uri, Map<String, String> headers, String data, boolean needErrorReponseData, HttpHost proxy) {
		HttpClient client = new DefaultHttpClient();
		if (proxy != null) {
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}
		enableHttpsTunnelIfRequired(uri, client);

		HttpResponse httpResponse;
		try {
			HttpPost post = new HttpPost(uri);
			for (Map.Entry<String, String> header : headers.entrySet()) {
				post.setHeader(header.getKey(), header.getValue());
			}
			post.setEntity(new StringEntity(data, "UTF-8"));

			httpResponse = client.execute(post);
			if (httpResponse != null) {
				StatusLine statusLine = httpResponse.getStatusLine();
				if (statusLine != null) {
					int statusCode = statusLine.getStatusCode();
					LOG.info("Status Code: " + statusCode);

					String reponseData = EntityUtils.toString(httpResponse.getEntity());
					LOG.info("Response : " + reponseData);
					if (statusCode == 200) {
						return new StringResponse(statusCode, reponseData);
					}
					if (needErrorReponseData) {
						return new StringResponse(statusCode, reponseData);
					}
					return new StringResponse(statusCode);
				} else {
					LOG.error("No status in response");
					return null;
				}
			} else {
				LOG.error("No response");
				return null;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null && client.getClass() != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}

	public static StringResponse post2(String uri, Map<String, String> headers, Map<String, String> params, String data, int connectionTimeout, int socketTimeout,
			boolean needErrorReponseData) throws ConnectTimeoutException, SocketTimeoutException {
		return post2(uri, headers, params, data, needErrorReponseData, connectionTimeout, socketTimeout, null);
	}

	public static StringResponse post2(String uri, Map<String, String> headers, Map<String, String> params, String data, boolean needErrorReponseData, int connectionTimeout,
			int socketTimeout, HttpHost proxy) throws ConnectTimeoutException, SocketTimeoutException {

		HttpClient client = new DefaultHttpClient();
		HttpParams httpParams = client.getParams();
		if (proxy != null) {
			httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}
		for (String key : params.keySet()) {
			httpParams.setParameter(key, params.get(key));
		}
		HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeout);
		HttpConnectionParams.setSoTimeout(httpParams, socketTimeout);
		enableHttpsTunnelIfRequired(uri, client);

		HttpResponse httpResponse;
		try {
			HttpPost post = new HttpPost(uri);
			for (Map.Entry<String, String> header : headers.entrySet()) {
				post.setHeader(header.getKey(), header.getValue());
			}
			post.setEntity(new StringEntity(data, "UTF-8"));
			Header[] allHeaders = post.getAllHeaders();
			LOG.info("Request Header : ");
			for (Header header : allHeaders) {
				LOG.info("'" + header.getName() + "':'" + header.getValue() + "'");
			}
			LOG.info("request url :" + post.getURI());
			LOG.info("request content :" + EntityUtils.toString(post.getEntity()));
			httpResponse = client.execute(post);
			if (httpResponse != null) {
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				String content = EntityUtils.toString(httpResponse.getEntity());
				LOG.info("Response Http Status :" + statusCode);
				LOG.info("response content :" + content);
				if (statusCode == 200) {
					return new StringResponse(statusCode, content);
				}
				if (needErrorReponseData) {
					return new StringResponse(statusCode, content);
				}
				return new StringResponse(statusCode);
			}
		} catch (SocketTimeoutException e) {
			if (e.getMessage().contains("Read timed out")) {
				throw e;
			} else if (e.getMessage().contains("connect timed out")) {// TODO to be removed, since placed for testing
				throw e;
			}
			throw new SocketTimeoutException("Service is down");
		} catch (IOException e) {
			Throwable cause = e.getCause();
			if (cause != null) {
				throw new RuntimeException(cause);
			} else {
				throw new RuntimeException(e);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null && client.getClass() != null) {
				client.getConnectionManager().shutdown();
			}
		}
		return null;
	}

	public static StringResponse post3(String uri, Map<String, String> headers, String data, String userName, String password) {
		DefaultHttpClient client = new DefaultHttpClient();

		HttpResponse httpResponse;
		try {
			enableHttpsTunnelIfRequired(uri, client);
			HttpPost post = new HttpPost(uri);
			for (Map.Entry<String, String> header : headers.entrySet()) {
				post.setHeader(header.getKey(), header.getValue());
			}

			URI uriObject = new URI(uri);
			client.getCredentialsProvider().setCredentials(new AuthScope(new HttpHost(uriObject.getHost())), new UsernamePasswordCredentials(userName, password));
			if (data != null) {
				post.setEntity(new StringEntity(data));
			}
			httpResponse = client.execute(post);
			return new StringResponse(httpResponse.getStatusLine().getStatusCode(), EntityUtils.toString(httpResponse.getEntity()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null && client.getClass() != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}

	public static StringResponse post2(String uri, Map<String, String> headers, Map<String, String> data) {
		return post2(uri, headers, data);
	}

	public static StringResponse post2(String uri, Map<String, String> headers, Map<String, String> data, HttpHost proxy) {
		HttpClient client = new DefaultHttpClient();
		if (proxy != null) {
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}
		HttpResponse httpResponse;
		try {
			enableHttpsTunnelIfRequired(uri, client);
			HttpPost post = new HttpPost(uri);
			for (Map.Entry<String, String> header : headers.entrySet()) {
				post.setHeader(header.getKey(), header.getValue());
			}
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			for (String paramName : data.keySet()) {
				nvps.add(new BasicNameValuePair(paramName, data.get(paramName)));
			}
			post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			httpResponse = client.execute(post);
			return new StringResponse(httpResponse.getStatusLine().getStatusCode(), EntityUtils.toString(httpResponse.getEntity()));
		} catch (Exception e) {
			LOG.error("Error while sending the request to URL: " + uri, e);
			throw new RuntimeException(e);
		}
	}

	public static StringResponse post(String uri, String data) {

		HttpClient client = new DefaultHttpClient();
		enableHttpsTunnelIfRequired(uri, client);
		HttpPost post = new HttpPost(uri);

// add the params to post request
		try {
			post.setEntity(new StringEntity(data));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Error while adding post parameters to request :: ", e);
		}

		HttpResponse httpResponse;
		try {
			httpResponse = client.execute(post);
			return new StringResponse(httpResponse.getStatusLine().getStatusCode(), EntityUtils.toString(httpResponse.getEntity()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null && client.getClass() != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}

	/**
	 *
	 * Http post call with header content-type set as application/json
	 *
	 * @param uri  URI where http post call has to made
	 * @param data body of the http post
	 *
	 * @return The response from server
	 */
	public static StringResponse jsonPost(String uri, String data) {

		HttpClient client = new DefaultHttpClient();
		enableHttpsTunnelIfRequired(uri, client);
		HttpPost post = new HttpPost(uri);

// add the params to post request
		try {
			post.setEntity(new StringEntity(data));
			post.setHeader("Content-type", "application/json");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Error while adding post parameters to request :: ", e);
		}

		HttpResponse httpResponse;
		try {
			httpResponse = client.execute(post);
			return new StringResponse(httpResponse.getStatusLine().getStatusCode(), EntityUtils.toString(httpResponse.getEntity()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null && client.getClass() != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}

	public static StringResponse post(String uri, String data, int connectionTimeout, int socketTimeout) throws ConnectTimeoutException, SocketTimeoutException {

		HttpClient client = new DefaultHttpClient();
		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
		HttpConnectionParams.setSoTimeout(params, socketTimeout);
		enableHttpsTunnelIfRequired(uri, client);
		HttpPost post = new HttpPost(uri);

// add the params to post request
		try {
			post.setEntity(new StringEntity(data));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Error while adding post parameters to request :: ", e);
		}

		HttpResponse httpResponse;
		try {
			httpResponse = client.execute(post);
			return new StringResponse(httpResponse.getStatusLine().getStatusCode(), EntityUtils.toString(httpResponse.getEntity()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null && client.getClass() != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}

	public static StringResponse post(String uri, Map<String, String> data) {

		HttpClient client = new DefaultHttpClient();
		enableHttpsTunnelIfRequired(uri, client);
		HttpPost post = new HttpPost(uri);

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (String paramName : data.keySet()) {
			nvps.add(new BasicNameValuePair(paramName, data.get(paramName)));
		}

// add the params to post request
		try {
			post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Error while adding post parameters to request :: ", e);
		}

		HttpResponse httpResponse;
		try {
			httpResponse = client.execute(post);
			return new StringResponse(httpResponse.getStatusLine().getStatusCode(), EntityUtils.toString(httpResponse.getEntity()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null && client.getClass() != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}

// public static StringResponse post(String uri, Map<String, Object> data) {
// HttpClient client = new DefaultHttpClient();
// HttpPost post = new HttpPost(uri);
//
// HttpParams params = new BasicHttpParams();
//
// for (String paramName : data.keySet()) {
// Object value = data.get(paramName);
// params.setParameter(paramName, value);
// }
//
// post.setParams(params);
// HttpResponse httpResponse;
// try {
// httpResponse = client.execute(post);
// return new StringResponse(httpResponse.getStatusLine().getStatusCode(), EntityUtils.toString(httpResponse.getEntity()));
// } catch (Exception e) {
// throw new RuntimeException(e);
// }
// }

	/**
	 * enable an https channel for secure communications
	 *
	 * @param client
	 */
	public static void enableHttpsTunnelIfRequired(String url, HttpClient client) {

// enable https tunneling only for https urls
		if (!url.toLowerCase().startsWith("https"))
			return;

		X509TrustManager xtm = new X509TrustManager() {

			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				return;
			}

			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				return;
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};
		TrustManager mytm[] = { xtm };
		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession sslSession) {
				return true;
			}
		};
		SSLContext ctx = null;
		try {
			ctx = SSLContext.getInstance("SSL");
			ctx.init(null, mytm, null);
			final org.apache.http.conn.ssl.SSLSocketFactory factory = new org.apache.http.conn.ssl.SSLSocketFactory(ctx, new AllowAllHostnameVerifier());
			final Scheme https = new Scheme("https", 443, factory);
			final SchemeRegistry schemeRegistry = client.getConnectionManager().getSchemeRegistry();
			schemeRegistry.register(https);

		} catch (Exception e) {
			throw new RuntimeException("Error during setting up of HTTPS channel ::", e);
		}
	}

	public static StringResponse delete(String uri, Map<String, Object> data) {
		HttpClient client = new DefaultHttpClient();
		HttpDelete delete = new HttpDelete(uri);

		HttpParams params = new BasicHttpParams();

		for (String paramName : data.keySet()) {
			Object value = data.get(paramName);
			params.setParameter(paramName, value);
		}
		delete.setParams(params);

		HttpResponse httpResponse;
		try {
			httpResponse = client.execute(delete);
			return new StringResponse(httpResponse.getStatusLine().getStatusCode(), EntityUtils.toString(httpResponse.getEntity()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null && client.getClass() != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}

	public static byte[] get(String uri) {
		HttpClient client = new DefaultHttpClient();
		enableHttpsTunnelIfRequired(uri, client);
		HttpGet get = new HttpGet(uri);

		HttpResponse httpResponse;
		try {
			httpResponse = client.execute(get);
			return IOUtils.toByteArray(httpResponse.getEntity().getContent());
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null && client.getClass() != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}

	/**
	 * Gets the contents of URL that is protected by HTTP Basic Auth
	 *
	 * @param uri      URI to fetch
	 * @param userName HTTP Basic Auth - User Name
	 * @param password HTTP Basic Auth - Password
	 * @return
	 */
	public static byte[] get(String uri, String userName, String password) {
		DefaultHttpClient client = new DefaultHttpClient();
		try {

			URI uriObject = new URI(uri);
			client.getCredentialsProvider().setCredentials(new AuthScope(new HttpHost(uriObject.getHost())), new UsernamePasswordCredentials(userName, password));

			enableHttpsTunnelIfRequired(uri, client);
			HttpGet get = new HttpGet(uri);

			HttpResponse httpResponse;
			httpResponse = client.execute(get);
			return IOUtils.toByteArray(httpResponse.getEntity().getContent());
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null && client.getClass() != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}

	public static StringResponse get2(String uri, Map<String, String> headers, String userName, String password) {
		DefaultHttpClient client = new DefaultHttpClient();
		try {
			enableHttpsTunnelIfRequired(uri, client);
			URI uriObject = new URI(uri);
			client.getCredentialsProvider().setCredentials(new AuthScope(new HttpHost(uriObject.getHost())), new UsernamePasswordCredentials(userName, password));

			HttpGet get = new HttpGet(uri);
			for (Map.Entry<String, String> header : headers.entrySet()) {
				get.setHeader(header.getKey(), header.getValue());
			}
			HttpResponse httpResponse;
			httpResponse = client.execute(get);
			return new StringResponse(httpResponse.getStatusLine().getStatusCode());
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null && client.getClass() != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}

	public static StringResponse get3(String uri, Map<String, String> headers, String userName, String password) {
		DefaultHttpClient client = new DefaultHttpClient();
		try {
			enableHttpsTunnelIfRequired(uri, client);
			URI uriObject = new URI(uri);
			client.getCredentialsProvider().setCredentials(new AuthScope(new HttpHost(uriObject.getHost())), new UsernamePasswordCredentials(userName, password));

			HttpGet get = new HttpGet(uri);
			for (Map.Entry<String, String> header : headers.entrySet()) {
				get.setHeader(header.getKey(), header.getValue());
			}
			HttpResponse httpResponse;
			httpResponse = client.execute(get);

			return new StringResponse(httpResponse.getStatusLine().getStatusCode(), EntityUtils.toString(httpResponse.getEntity()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null && client.getClass() != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}

	public static StringResponse get2(String uri, Map<String, String> headers, Map<String, String> params) {
		HttpClient client = new DefaultHttpClient();
		enableHttpsTunnelIfRequired(uri, client);
		HttpGet get = new HttpGet(uri);
		for (Map.Entry<String, String> header : headers.entrySet()) {
			get.setHeader(header.getKey(), header.getValue());
		}
		HttpParams parameters = new BasicHttpParams();
		for (String key : params.keySet()) {
			parameters.setParameter(key, params.get(key));
		}
		get.setParams(parameters);

		HttpResponse httpResponse;
		try {
			httpResponse = client.execute(get);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				return new StringResponse(httpResponse.getStatusLine().getStatusCode(), EntityUtils.toString(httpResponse.getEntity()));
			}
			return new StringResponse(httpResponse.getStatusLine().getStatusCode());
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null && client.getClass() != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}

	public static StringResponse get2(String uri, Map<String, String> headers, Map<String, String> params, int connectionTimeout, int socketTimeout, boolean needErrorReponseData)
			throws SocketTimeoutException {
		HttpClient client = new DefaultHttpClient();
		enableHttpsTunnelIfRequired(uri, client);
		HttpGet get = new HttpGet(uri);
		for (Map.Entry<String, String> header : headers.entrySet()) {
			get.setHeader(header.getKey(), header.getValue());
		}
		HttpParams parameters = client.getParams();
		HttpConnectionParams.setConnectionTimeout(parameters, connectionTimeout);
		HttpConnectionParams.setSoTimeout(parameters, socketTimeout);
		for (String key : params.keySet()) {
			parameters.setParameter(key, params.get(key));
		}
		get.setParams(parameters);

		HttpResponse httpResponse;
		try {
			Header[] allHeaders = get.getAllHeaders();
			LOG.info("Request Header : ");
			for (Header header : allHeaders) {
				LOG.info("'" + header.getName() + "':'" + header.getValue() + "'");
			}
			LOG.info("request url :" + get.getURI());
			httpResponse = client.execute(get);
			if (httpResponse != null) {
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				String content = EntityUtils.toString(httpResponse.getEntity());
				LOG.info("Response Http Status :" + statusCode);
				LOG.info("response content :" + content);
				if (statusCode == 200) {
					return new StringResponse(statusCode, content);
				}
				if (needErrorReponseData) {
					return new StringResponse(statusCode, content);
				}
				return new StringResponse(statusCode);
			}
		} catch (SocketTimeoutException e) {
			if (e.getMessage().contains("Read timed out")) {
				throw e;
			} else if (e.getMessage().contains("connect timed out")) {// TODO to be removed, since placed for testing
				throw e;
			}
			throw new SocketTimeoutException("Service is down");
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null && client.getClass() != null) {
				client.getConnectionManager().shutdown();
			}
		}
		return null;
	}

	public static byte[] get(String uri, Map<String, Object> params) {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(uri);

		HttpParams parameters = new BasicHttpParams();
		for (String key : params.keySet()) {
			parameters.setParameter(key, params.get(key));
		}

		get.setParams(parameters);
		HttpResponse httpResponse;
		try {
			httpResponse = client.execute(get);
			return IOUtils.toByteArray(httpResponse.getEntity().getContent());
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null && client.getClass() != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}

	public static byte[] post(String uri) {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(uri);

		HttpResponse httpResponse;
		try {
			httpResponse = client.execute(post);
			return IOUtils.toByteArray(httpResponse.getEntity().getContent());
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null && client.getClass() != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}

	public static void main(String[] args) {
		StringResponse res = get2("http://www.junk.com/fdc/fjhsjk", null, null);
		System.out.println(res.getStatusCode());
	}

	public static StringResponse put(String uri, String data) {

		HttpClient client = new DefaultHttpClient();
		enableHttpsTunnelIfRequired(uri, client);
		HttpPut put = new HttpPut(uri);

// add the params to post request
		try {
			put.setEntity(new StringEntity(data));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Error while adding post parameters to request :: ", e);
		}

		HttpResponse httpResponse;
		try {
			httpResponse = client.execute(put);
			return new StringResponse(httpResponse.getStatusLine().getStatusCode(), EntityUtils.toString(httpResponse.getEntity()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null && client.getClass() != null) {
				client.getConnectionManager().shutdown();
			}
		}
	}

	public static StringResponse put(String url, Map<String, String> headers, Map<String, String> params, String request, int connectionTimeout, int socketTimeout,
			boolean needErrorReponseData) throws ConnectTimeoutException, SocketTimeoutException {
		HttpClient client = new DefaultHttpClient();
		enableHttpsTunnelIfRequired(url, client);
		HttpPut put = new HttpPut(url);
		for (Map.Entry<String, String> header : headers.entrySet()) {
			put.setHeader(header.getKey(), header.getValue());
		}
		HttpParams parameters = client.getParams();
		HttpConnectionParams.setConnectionTimeout(parameters, connectionTimeout);
		HttpConnectionParams.setSoTimeout(parameters, socketTimeout);
		for (String key : params.keySet()) {
			parameters.setParameter(key, params.get(key));
		}
		put.setParams(parameters);
		try {
			put.setEntity(new StringEntity(request));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Error while adding post parameters to request :: ", e);
		}
		HttpResponse httpResponse;
		try {
			Header[] allHeaders = put.getAllHeaders();
			LOG.info("Request Header : ");
			for (Header header : allHeaders) {
				LOG.info("'" + header.getName() + "':'" + header.getValue() + "'");
			}
			LOG.info("request url :" + put.getURI());
			LOG.info("request content :" + EntityUtils.toString(put.getEntity()));
			httpResponse = client.execute(put);
			if (httpResponse != null) {
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				String content = EntityUtils.toString(httpResponse.getEntity());
				LOG.info("Response Http Status :" + statusCode);
				LOG.info("response content :" + content);
				if (statusCode == 200) {
					return new StringResponse(statusCode, content);
				}
				if (needErrorReponseData) {
					return new StringResponse(statusCode, content);
				}
				return new StringResponse(statusCode);
			}
		} catch (SocketTimeoutException e) {
			if (e.getMessage().contains("Read timed out")) {
				throw e;
			} else if (e.getMessage().contains("connect timed out")) {// TODO to be removed, since placed for testing
				throw e;
			}
			throw new SocketTimeoutException("Service is down");
		} catch (IOException e) {
			Throwable cause = e.getCause();
			if (cause != null) {
				throw new RuntimeException(cause);
			} else {
				throw new RuntimeException(e);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (client != null && client.getClass() != null) {
				client.getConnectionManager().shutdown();
			}
		}
		return null;
	}


}
