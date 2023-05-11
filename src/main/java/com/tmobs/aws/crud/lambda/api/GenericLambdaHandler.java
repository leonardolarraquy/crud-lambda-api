package com.tmobs.aws.crud.lambda.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.tmobs.aws.crud.lambda.api.model.Generic;

public class GenericLambdaHandler implements RequestStreamHandler{

	private void logAndTableName(Context context) {
		if(context.getClientContext() != null &&  context.getClientContext().getEnvironment() != null)
			context.getLogger().log("logging call: " + context.getClientContext().getEnvironment().toString());

		context.getLogger().log("logging call: " + context.getFunctionName());
		context.getLogger().log("logging call: " + context.toString());
		context.getLogger().log("logging call: " + context.getInvokedFunctionArn());

	}

	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		this.logAndTableName(context);		

		OutputStreamWriter writer = new OutputStreamWriter(output);
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		JSONParser parser = new JSONParser();
		JSONObject responseObject = new JSONObject();
		JSONObject responseBody = new JSONObject();

		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();

		DynamoDB dynamoDB     = new DynamoDB(client);

		int id;
		Item resItem = null;

		try {
			JSONObject object = (JSONObject) parser.parse(reader);
			JSONObject temp   = null;

			if(object.get("pathParameters") != null)
				temp = (JSONObject) object.get("pathParameters");

			if(object.get("queryStringParameters") != null)
				temp = (JSONObject) object.get("queryStringParameters");

			if(temp.get("id") != null) {
				id = Integer.parseInt(temp.get("id").toString());

				String entity = temp.get("entity").toString();


				resItem = dynamoDB.getTable(entity).getItem("id", id);
			}

			if(resItem != null) {
				Generic product = new Generic(resItem.toJSON());
				responseBody.put("product", product);
				responseBody.put("statusCode", 200);
			}
			else {
				responseBody.put("message", "no product found");
				responseBody.put("statusCode", 404);

			}

			responseObject.put("body", responseBody.toString());

		}
		catch(Exception e) {
			context.getLogger().log("ERROR: " + e.getMessage());

		}

		writer.write(responseObject.toString());

		reader.close();
		writer.close();


	}

	public void handlePutRequest(InputStream input, OutputStream output, Context context) throws IOException {		
		this.logAndTableName(context);		

		OutputStreamWriter writer = new OutputStreamWriter(output);
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		JSONParser parser = new JSONParser();
		JSONObject responseObject = new JSONObject();
		JSONObject responseBody = new JSONObject();

		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();

		DynamoDB dynamoDB     = new DynamoDB(client);

		boolean success = true;

		try {
			JSONObject object = (JSONObject) parser.parse(reader);

			if(object.get("body") != null) {

				Generic prod = new Generic(object.get("body").toString());

				dynamoDB.getTable(prod.getEntity()).
				putItem(new PutItemSpec().withItem(new Item()
						.withNumber("id", prod.getId())
						.withString("name", prod.getName())
						.withString("description", prod.getDescription())
						.withString("customStr1", prod.getCustomStr1())
						.withString("customStr2", prod.getCustomStr2())
						.withNumber("customInt1", prod.getCustomInt1())
						.withNumber("customInt2", prod.getCustomInt2())
						.withNumber("customDouble1", prod.getCustomDouble1())
						.withNumber("customDouble2", prod.getCustomDouble2())
						));

				responseBody.put("message", "new product created/updated");
				responseObject.put("statusCode", 200);
				responseObject.put("body", responseBody.toString());

				context.getLogger().log("body: " + responseBody.toString());

			}

		}
		catch(Exception e ) {
			context.getLogger().log("ERROR: " + e.getMessage());

			responseObject.put("statusCode", 400);
			responseObject.put("error", e.toString());

			success = false;

		}

		writer.write(responseObject.toString());
		reader.close();
		writer.close();

		context.getLogger().log("success:" + success);


	}


	public void handleDeleteRequest(InputStream input, OutputStream output, Context context) throws IOException {
		this.logAndTableName(context);		

		OutputStreamWriter writer = new OutputStreamWriter(output);
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		JSONParser parser = new JSONParser();
		JSONObject responseObject = new JSONObject();
		JSONObject responseBody = new JSONObject();

		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();

		DynamoDB dynamoDB     = new DynamoDB(client);

		try {
			JSONObject object = (JSONObject) parser.parse(reader);

			JSONObject temp   = null;

			if(object.get("pathParameters") != null)
				temp = (JSONObject) object.get("pathParameters");

			if(object.get("queryStringParameters") != null)
				temp = (JSONObject) object.get("queryStringParameters");


			if(temp.get("id") != null) {
				int id = Integer.parseInt(temp.get("id").toString());

				String entity = temp.get("entity").toString();

				dynamoDB.getTable(entity).deleteItem("id", id);
			}

			responseBody.put("message", "item deleted");
			responseObject.put("statusCode", 200);
			responseObject.put("body", responseBody.toString());

			context.getLogger().log("body: " + responseBody.toString());
		}
		catch(Exception e) {
			context.getLogger().log("ERROR: " + e.toString());
			context.getLogger().log("ERROR: " + e.getLocalizedMessage());

			responseObject.put("statusCode", 400);
			responseObject.put("error", e.toString());	
		}

		writer.write(responseObject.toString());
		reader.close();
		writer.close();
	}
}