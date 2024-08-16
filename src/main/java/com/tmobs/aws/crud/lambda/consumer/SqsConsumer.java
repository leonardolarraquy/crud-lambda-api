package com.tmobs.aws.crud.lambda.consumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class SqsConsumer implements RequestHandler<Object, String>{

	private void logContext(Context context) {
		if(context.getClientContext() != null &&  context.getClientContext().getEnvironment() != null)
			context.getLogger().log("logging call: " + context.getClientContext().getEnvironment().toString());

		context.getLogger().log("logging call: " + context.getFunctionName());
		context.getLogger().log("logging call: " + context.toString());
		context.getLogger().log("logging call: " + context.getInvokedFunctionArn());

	}

	@Override
	public String handleRequest(Object event, Context context) {
		this.logContext(context);		
		context.getLogger().log("variable Event: " + event);

		WebClient webClient = new WebClient(BrowserVersion.CHROME);

		try {			
			context.getLogger().log("Java Version: " + System.getProperty("java.version"));
			context.getLogger().log("Java Runtime Version: " + System.getProperty("java.runtime.version"));
			context.getLogger().log("Java Vendor: " + System.getProperty("java.vendor"));
			context.getLogger().log("Java Home: " + System.getProperty("java.home"));
						
			java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.OFF);
			java.util.logging.Logger.getLogger("org.apache.http").setLevel(java.util.logging.Level.OFF);


			// Eliminar las llaves externas
			String input = event.toString().substring(1, event.toString().length() - 1);

			// Dividir la cadena por ", " para obtener cada par clave-valor
			String[] pairs = input.split(", ");

			// Crear un mapa para almacenar los pares clave-valor
			Map<String, String> map = new HashMap<>();

			// Iterar sobre los pares
			for (String pair : pairs) {
				// Dividir cada par por "="
				String[] keyValue = pair.split("=", 2);
				String key = keyValue[0].trim();
				String value = keyValue[1].trim();
				// Agregar al mapa
				map.put(key, value);
			}

			context.getLogger().log("URL to Get: " + map.get("url").toString());
			
			long now = System.currentTimeMillis();
			
			this.openUrlConnection(context, map.get("url").toString());

			context.getLogger().log("URL already called using HttpURLConnection: " + (System.currentTimeMillis() - now) + " ms.");
			
			//todo true
			webClient.getOptions().setJavaScriptEnabled(false);
			webClient.getOptions().setUseInsecureSSL(true);
			webClient.getOptions().setRedirectEnabled(true);
			webClient.getCookieManager().setCookiesEnabled(true);
			
			//todo false			
			webClient.getOptions().setCssEnabled(false);
			webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			webClient.getOptions().setPrintContentOnFailingStatusCode(false);
			
			//tiempos
			webClient.getOptions().setTimeout(15000); //socket connection and data retrieval
			webClient.waitForBackgroundJavaScriptStartingBefore(1000);
			webClient.waitForBackgroundJavaScript(1000);

			webClient.setAjaxController(new NicelyResynchronizingAjaxController());

			context.getLogger().log("WEBCLIENT CONFIGURED ABOUT TO MAKE DE CALL ");

			HtmlPage page = webClient.getPage((String) map.get("url").toString());

			context.getLogger().log("URL already called: " + page.getTitleText());

		}
		catch(Exception e) {
			context.getLogger().log("error parsing url: " + e.getMessage());
		}
		finally {
			webClient.close();			
		}


		return "OK";

	}

	private void openUrlConnection(Context context, String urlString ) {
		HttpURLConnection connection = null;

		try {
			// Crear objeto URL
			URL url = new URL(urlString);

			// Abrir conexión
			connection = (HttpURLConnection) url.openConnection();

			// Configurar el método de solicitud (GET por defecto)
			connection.setRequestMethod("GET");

			// Establecer un tiempo de espera para la conexión
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);

			// Obtener el código de respuesta
			int responseCode = connection.getResponseCode();
			context.getLogger().log("Response Code by URL Connect: " + responseCode);

			// Leer la respuesta si el código es 200 (OK)
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				StringBuilder content = new StringBuilder();

				while ((inputLine = in.readLine()) != null) {
					content.append(inputLine);
				}

				// Cerrar los streams
				in.close();

			} else {
				context.getLogger().log("Failed to connect: " + responseCode);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} 
		finally {
			if (connection != null) {
				connection.disconnect();  // Asegurarse de desconectar la conexión
			}
		}
	}
}