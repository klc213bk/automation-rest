package com.transglobe.streamingetl.automation.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpUtils {

	public static String restService(String urlStr, String requestMethod) throws Exception {
		
		HttpURLConnection httpConn = null;
		URL url = null;
		try {
			url = new URL(urlStr);
			httpConn = (HttpURLConnection)url.openConnection();
			httpConn.setRequestMethod(requestMethod);
			int responseCode = httpConn.getResponseCode();
			//			LOG.info(">>>>>  responseCode={}",responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
			StringBuffer response = new StringBuffer();
			String readLine = null;
			while ((readLine = in.readLine()) != null) {
				response.append(readLine);
			}
			in.close();

			return response.toString();
		} finally {
			if (httpConn != null ) httpConn.disconnect();
		}
	}
	
	public static String writeListToJsonString(List<String> list) throws IOException {  
		  
	    final ByteArrayOutputStream out = new ByteArrayOutputStream();
	    final ObjectMapper mapper = new ObjectMapper();

	    mapper.writeValue(out, list);

	    final byte[] data = out.toByteArray();
	    
	    return new String(data);
	    
	}
	public static String restPostService(String urlStr, String jsonStr) throws Exception {

		HttpURLConnection httpConn = null;
		URL url = null;
		OutputStream os = null;
		BufferedReader in = null;
		try {
			url = new URL(urlStr);
			httpConn = (HttpURLConnection)url.openConnection();
			httpConn.setRequestMethod("POST");
			httpConn.setRequestProperty("Content-Type", "application/json;utf-8" );
			httpConn.setRequestProperty("Accept", "application/json" );
			httpConn.setDoOutput(true);

			os = httpConn.getOutputStream();
			byte[] input = jsonStr.getBytes("utf-8");
			os.write(input, 0, input.length);
			
			
//			httpConn.setRequestMethod(requestMethod);
			int responseCode = httpConn.getResponseCode();
			
			in = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "utf-8"));
			StringBuffer response = new StringBuffer();
			String readLine = null;
			while ((readLine = in.readLine()) != null) {
				response.append(readLine.trim());
			}
			in.close();

			return response.toString();
		} finally {
			if (os != null) os.close();
			if (in != null) in.close();
			if (httpConn != null ) httpConn.disconnect();
		}
	}
}
