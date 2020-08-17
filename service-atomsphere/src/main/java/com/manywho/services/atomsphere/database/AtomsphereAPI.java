package com.manywho.services.atomsphere.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Base64;

import org.json.JSONObject;

import com.manywho.services.atomsphere.ServiceConfiguration;

public class AtomsphereAPI {
	public static JSONObject executeAPI(ServiceConfiguration configuration, String entityName, String method, String resource, JSONObject payload) 
	{
		String payloadString=null;
		if (payload!=null) payloadString=payload.toString();
		return executeAPI(configuration, entityName, method, resource, payloadString, false);
	}
	public static JSONObject executeAPIQueryMore(ServiceConfiguration configuration, String entityName, String queryToken, boolean isAPIMEntity) 
	{
		return executeAPI(configuration, entityName, "POST", "queryMore", queryToken, isAPIMEntity);
	}
    //TODO queryMore queryToken uses just a text blob, not jsonobject so we need to support passing a string payload
    //APIM https://api.boomi.com/apim/api/rest/v1/{accountID}
    public static JSONObject executeAPI(ServiceConfiguration configuration, String entityName, String method, String resource, String payload, boolean isAPIMEntity) 
	{
		if (resource!=null)
			resource="/"+resource;
		else 
			resource="";
		String urlString = "https://api.boomi.com/api/rest/v1/";
		if (isAPIMEntity)
			urlString = "https://api.boomi.com/apim/api/rest/v1/";
        StringBuffer response= new StringBuffer();
        HttpURLConnection conn=null;
        String responseCode = null;
		
        try {
Thread.sleep(300); //Boomi Default Rate Limit is 1 call per 200ms   	
    		URL url = new URL(urlString+String.format("%s/%s%s", configuration.getAccount(), entityName, resource));
//    		logger.info(method + " " + url.toString());
            conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(method);
	        conn.setDoOutput(true);
	        conn.setRequestProperty("Content-Type", "application/json");
	        conn.setRequestProperty("Accept", "application/json");
	    	String userpass = configuration.getUsername() + ":" + configuration.getPassword();
	    	String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
	    	conn.setRequestProperty ("Authorization", basicAuth);
	    	if (payload!=null)
	    	{
		        conn.setDoInput(true);
	    		String body = payload;
//		        LOGGER.info(body);
		        byte[] input = body.getBytes();
		 		conn.setRequestProperty("Content-Length", ""+input.length);
		        OutputStream os = conn.getOutputStream();
		        os.write(input, 0, input.length);  
	    	}  
	    		
	 
			responseCode = conn.getResponseCode() +"";
	        
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream())) ;
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
			    response.append(responseLine.trim());
			}
		} catch (ProtocolException e1) {
			throw new RuntimeException(e1);
		} catch (InterruptedException e1) {
			throw new RuntimeException(e1);
		} catch (IOException e) {
			try {
				if (conn!=null)
				{
					responseCode = conn.getResponseCode() +"";
					response.append("Error code: " + conn.getResponseCode());
					response.append(" " + conn.getResponseMessage() + " ");
					InputStream errorStream = conn.getErrorStream();
					if (errorStream!=null)
					{
						BufferedReader br = new BufferedReader(new InputStreamReader(errorStream)) ;
						String responseLine = null;
						while ((responseLine = br.readLine()) != null) {
						    response.append(responseLine.trim());
						}
					}
//					logger.error(response.toString());
					throw new RuntimeException(response.toString());
				} else throw new RuntimeException(e);
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
		}
//	    LOGGER.info(response.toString());
        String responseString = response.toString();
        JSONObject responseObj = null;
        if (responseString.length()==0)
        {	
        	responseObj = new JSONObject();
        	responseObj.put("statusCode", responseCode);
        } else {
        	responseObj = new JSONObject(response.toString());
        }
        return responseObj;
	}
}