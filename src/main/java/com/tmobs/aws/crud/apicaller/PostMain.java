package com.tmobs.aws.crud.apicaller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostMain {

	public static void main(String[] args) {
		for (int i = 0; i<100; i++) {

			try {
				String endpointUrl = "https://bozbzbzxn2.execute-api.us-east-2.amazonaws.com/default/put?msisdn=549116798123&id=" + System.currentTimeMillis() + "&producto=prod" + System.currentTimeMillis();
//				String endpointUrl = "https://bozbzbzxn2.execute-api.us-east-2.amazonaws.com/default/put?id=1&producto=prod" + System.currentTimeMillis();

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
				System.out.println("HTTP: " + responseCode + response.toString());

				connection.disconnect();

				Thread.currentThread().sleep(300);
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
