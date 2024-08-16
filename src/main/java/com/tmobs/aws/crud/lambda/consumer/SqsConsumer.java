package com.tmobs.aws.crud.lambda.consumer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class SqsConsumer implements RequestHandler<Object, String>{

	/*
	public static void main(String[] args) {

		String body ="{\"url\": \"https://www.la.clover.com/p/DJ90NDQHYP7CY\", \"country\": \"Argentina\"}";
		body = body.replaceAll("\"", "");

		// Eliminar las llaves externas
		String input = body.substring(1, body.toString().length() - 1);

		// Dividir la cadena por ", " para obtener cada par clave-valor
		String[] pairs = input.split(", ");

		// Crear un mapa para almacenar los pares clave-valor
		Map<String, String> map = new HashMap<>();

		// Iterar sobre los pares
		for (String pair : pairs) {
			// Dividir cada par por "="
			String[] keyValue = pair.split(":", 2);
			String key = keyValue[0].trim();
			String value = keyValue[1].trim();
			// Agregar al mapa
			map.put(key, value);
		}

		System.out.println("mapa es: " +map);

	}
	 */

	private void logContext(Context context) {
		if(context.getClientContext() != null &&  context.getClientContext().getEnvironment() != null)
			context.getLogger().log("logging call: " + context.getClientContext().getEnvironment().toString());

		context.getLogger().log("Java Version: " + System.getProperty("java.version"));
		context.getLogger().log("Java Runtime Version: " + System.getProperty("java.runtime.version"));
		context.getLogger().log("Java Vendor: " + System.getProperty("java.vendor"));
		context.getLogger().log("Java Home: " + System.getProperty("java.home"));

		context.getLogger().log("logging call: " + context.getFunctionName());
		context.getLogger().log("logging call: " + context.toString());
		context.getLogger().log("logging call: " + context.getInvokedFunctionArn());

	}

	@Override
	public String handleRequest(Object event, Context context) {
		this.logContext(context);		
		context.getLogger().log("variable Event: " + event + " class: " + event.getClass().getName()) ;

		//		java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.OFF);
		//		java.util.logging.Logger.getLogger("org.apache.http").setLevel(java.util.logging.Level.OFF);

		LinkedHashMap mapa = (LinkedHashMap) event;

		Iterator it = mapa.keySet().iterator();
		while (it.hasNext()) {
			String x    = it.next().toString();

			context.getLogger().log("x: " + x);

			List list   = (List) mapa.get(x);

			context.getLogger().log("list: " + list + " size: " + list.size());

			Map obj     = (Map) list.get(0);

			context.getLogger().log("obj: " + obj + " size: " + obj.size());

			String body    = obj.get("body").toString();

			context.getLogger().log("body: " + body + " body: " + body.length());

			WebClient webClient = new WebClient(BrowserVersion.CHROME);

			try {							

				body = body.replaceAll("\"", "");

				// Eliminar las llaves externas
				String input = body.substring(1, body.toString().length() - 1);

				// Dividir la cadena por ", " para obtener cada par clave-valor
				String[] pairs = input.split(", ");

				// Crear un mapa para almacenar los pares clave-valor
				Map<String, String> map = new HashMap<>();

				// Iterar sobre los pares
				for (String pair : pairs) {
					// Dividir cada par por "="
					String[] keyValue = pair.split(":", 2);
					String key = keyValue[0].trim();
					String value = keyValue[1].trim();
					// Agregar al mapa
					map.put(key, value);
				}

				String url = (String) map.get("url").toString();

				this.openUrlConnection(context, url);

				//todo true
				webClient.getOptions().setJavaScriptEnabled(true);
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

				long now = System.currentTimeMillis();

				context.getLogger().log("WEBCLIENT CONFIGURED ABOUT TO MAKE DE CALL ");

				HtmlPage page = webClient.getPage(url);

				int code    = page.getWebResponse().getStatusCode();
				String resm = page.getWebResponse().getStatusMessage();

				context.getLogger().log("URL already called: " + url + " HTTP CODE: " + code + " RESPONSE: " + resm + " PAGE TITLE: " + page.getTitleText() + " time: " + (System.currentTimeMillis() - now) + " ms.");

			}
			catch(Exception e) {
				context.getLogger().log("error parsing url: " + e.toString());
			}
			finally {
				webClient.close();			
			}
		}


		return "OK";

	}

	public static void main(String[] args) {

		openUrlConnection(null, "https://amtae.co/qfrqx");

	}


	private static void  openUrlConnection(Context context, String urlString ) {		
		//		context.getLogger().log("URL to Get: " + urlString);

		long now = System.currentTimeMillis();


		HttpURLConnection connection = null;

		try {
			// Crear objeto URL
			URL url = new URL(urlString);

			// Abrir conexión
			connection = (HttpURLConnection) url.openConnection();
			SSLContext sc = SSLContext.getInstance("SSL");  
			sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());  
			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
			//           connection.setHostnameVerifier(allHostsValid);
			// Configurar el método de solicitud (GET por defecto)
			connection.setRequestMethod("GET");

			// Establecer un tiempo de espera para la conexión
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			connection.setFollowRedirects(true);
			connection.setUseCaches(false);

			connection.connect();

			// Obtener el código de respuesta
			int responseCode = connection.getResponseCode();
			//			context.getLogger().log("Response Code by URL Connect: " + responseCode);

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
				//				context.getLogger().log("Failed to connect: " + responseCode);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} 
		finally {
			if (connection != null) {
				connection.disconnect();  // Asegurarse de desconectar la conexión
			}
		}

		//		context.getLogger().log("URL already called using HttpURLConnection: " + urlString + " " + (System.currentTimeMillis() - now) + " ms.");

	}
}

class TrustAnyTrustManager implements X509TrustManager {

	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	}

	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}
}
