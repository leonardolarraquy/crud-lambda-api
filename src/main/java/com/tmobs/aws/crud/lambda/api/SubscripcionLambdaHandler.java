package com.tmobs.aws.crud.lambda.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.tmobs.aws.crud.lambda.api.model.Subscripcion;

public class SubscripcionLambdaHandler implements RequestStreamHandler{

	private JSONObject logEventAndRead(InputStream input, Context context) throws Exception {
		context.getLogger().log(context.getAwsRequestId() + " function called: " + context.getFunctionName() + " ARN: " + context.getInvokedFunctionArn() + " (this): " + this.toString());
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		JSONParser parser = new JSONParser();

		JSONObject object = (JSONObject) parser.parse(reader);

		Iterator it = object.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry next = (Map.Entry) it.next();

			context.getLogger().log(context.getAwsRequestId() + " parameter arrived --> key: " + next.getKey() + " value: " + next.getValue());
		}

		reader.close();

		return object;
	}

	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		JSONObject responseObject = new JSONObject();
		OutputStreamWriter writer = new OutputStreamWriter(output);

		try {
			JSONObject object = this.logEventAndRead(input, context);		

			JSONObject responseBody = new JSONObject();

			AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();

			DynamoDB dynamoDB     = new DynamoDB(client);

			String id = this.getValue("id", object, context);

			Item resItem = dynamoDB.getTable("subscripciones").getItem("id", id);

			if(resItem != null) {
				Subscripcion product = new Subscripcion(resItem.toJSON());
				responseBody.put("subscription", product);
				responseBody.put("statusCode", 200);
			}
			else {
				responseBody.put("message", "no subscription found");
				responseBody.put("statusCode", 404);

			}

			responseObject.put("body", responseBody.toString());

		}
		catch(Exception e) {
			context.getLogger().log(context.getAwsRequestId() + " ERROR: " + e.getMessage());
			context.getLogger().log(context.getAwsRequestId() + " ERROR: " + e.getCause());
			
			for(StackTraceElement x :  e.getStackTrace()) {
				context.getLogger().log(context.getAwsRequestId() + " ERROR: " + x.toString());				
			}
			
			responseObject.put("body", e.getLocalizedMessage());
		}

		writer.write(responseObject.toString());
		writer.close();
	}

	private String getValue(String key, JSONObject object, Context context) {
		String value      = null;
		JSONObject temp   = null;
		
		context.getLogger().log(context.getAwsRequestId() + " searching: " + key);

		if(object.get("multiValueQueryStringParameters") != null)
			temp = (JSONObject) object.get("multiValueQueryStringParameters");

		if(object.get("pathParameters") != null)
			temp = (JSONObject) object.get("pathParameters");

		if(object.get("queryStringParameters") != null)
			temp = (JSONObject) object.get("queryStringParameters");

		if(object.get("body") != null) {
			context.getLogger().log("body found: " + object.get("body"));

			StringReader reader = new StringReader(object.get("body").toString());
			
			Gson gson = new Gson();
			Map map = gson.fromJson(reader, Map.class);
			
			value = (String) map.get(key);
		}
		else {
			if(temp != null && temp.get(key) != null)
				value = temp.get(key).toString();
			else 
				value = object.get(key).toString();			
		}

		context.getLogger().log(context.getAwsRequestId() + " found: " + key + ": " + value);

		return value;
	}

	public void handlePutRequest(InputStream input, OutputStream output, Context context) throws IOException {		
		JSONObject responseObject = new JSONObject();
		OutputStreamWriter writer = new OutputStreamWriter(output);

		try {
			JSONObject object = this.logEventAndRead(input, context);		

			JSONObject responseBody = new JSONObject();

			AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();

			DynamoDB dynamoDB     = new DynamoDB(client);

			String id = this.getValue("id", object, context);

			Item item = dynamoDB.getTable("subscripciones").getItem("id", id);

			Subscripcion sub = null;

			if(item == null) {
				sub = new Subscripcion(object.toString()); // construir objeto a partir del payload
				sub.setFecAlta(new Date().toString());
				sub.setMsisdn(this.getValue("msisdn", object, context));
				sub.setId(id);
			}
			else {
				sub = new Subscripcion(item.toJSON()); // construir el objecto a partir del JSON de DynamoDB
			}

			sub.setFecUpdated(new Date().toString());
			sub.setCantUpdated(sub.getCantUpdated() + 1);
			sub.setProducto(this.getValue("producto", object, context));

			dynamoDB.getTable("subscripciones").
			putItem(new PutItemSpec().withItem(new Item()
					.withString("id", sub.getId())
					.withString("msisdn", sub.getMsisdn())
					.withString("fecUpdated", sub.getFecUpdated().toString())
					.withString("fecAlta", sub.getFecAlta().toString())
					.withString("producto", sub.getProducto())
					.withNumber("cantUpdated", sub.getCantUpdated())
					));

			responseBody.put("message", "new subscription created/updated");
			responseObject.put("statusCode", 200);
			responseObject.put("body", responseBody.toString());

			context.getLogger().log(context.getAwsRequestId() + " body: " + responseBody.toString());
		}
		catch(Exception e ) {
			context.getLogger().log(context.getAwsRequestId() + " ERROR: " + e.getMessage());
			context.getLogger().log(context.getAwsRequestId() + " ERROR: " + e.getCause());
			
			for(StackTraceElement x :  e.getStackTrace()) {
				context.getLogger().log(context.getAwsRequestId() + " ERROR: " + x.toString());				
			}

			responseObject.put("statusCode", 400);
			responseObject.put("error", e.toString());
		}

		writer.write(responseObject.toString());
		writer.close();
	}


	public void handleDeleteRequest(InputStream input, OutputStream output, Context context) throws IOException {
		JSONObject responseObject = new JSONObject();
		OutputStreamWriter writer = new OutputStreamWriter(output);

		try {
			JSONObject object       = this.logEventAndRead(input, context);		
			JSONObject responseBody = new JSONObject();

			AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();

			DynamoDB dynamoDB     = new DynamoDB(client);

			String id = this.getValue("id", object, context);

			dynamoDB.getTable("subscripciones").deleteItem("id", id);

			responseBody.put("message", "item deleted");
			responseObject.put("statusCode", 200);
			responseObject.put("body", responseBody.toString());

			context.getLogger().log("body: " + responseBody.toString());
		}
		catch(Exception e) {
			context.getLogger().log(context.getAwsRequestId() + " ERROR: " + e.getMessage());
			context.getLogger().log(context.getAwsRequestId() + " ERROR: " + e.getCause());
			
			for(StackTraceElement x :  e.getStackTrace()) {
				context.getLogger().log(context.getAwsRequestId() + " ERROR: " + x.toString());				
			}

			responseObject.put("statusCode", 400);
			responseObject.put("error", e.toString());	
		}

		writer.write(responseObject.toString());
		writer.close();
	}
}