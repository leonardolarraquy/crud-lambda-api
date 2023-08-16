package com.tmobs.aws.crud.apicaller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetMain {

	public static void main(String[] args) throws Exception {
		for (int i = 0; i<1000; i++) {
			
			Caller caller = new Caller();
			caller.start();
		}
	}
}

class Caller extends Thread{

	@Override
	public void run() {
		super.run();
		
		try {
			String endpointUrl = "https://b5l6fsuct0.execute-api.us-east-2.amazonaws.com/default/get?id=1";

			URL url = new URL(endpointUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			StringBuilder response = new StringBuilder();

			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close(); 

			int responseCode = connection.getResponseCode();
			System.out.println("HTTP (id=" + System.currentTimeMillis() + ") -" + responseCode + response.toString());

			connection.disconnect();

		} 
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
}
