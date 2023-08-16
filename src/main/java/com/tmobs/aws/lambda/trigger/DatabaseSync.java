package com.tmobs.aws.lambda.trigger;

import java.sql.PreparedStatement;

import org.apache.commons.dbcp2.BasicDataSource;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;

public class DatabaseSync {
	
	private static BasicDataSource ds = new BasicDataSource();
	
	static {
		ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
//		ds.setUrl("jdbc:mysql://localhost:3306/ems"); // localhost
		ds.setUrl("jdbc:mysql://0.tcp.sa.ngrok.io:10344");
		ds.setUsername("root");
		ds.setPassword("Josefina1901$");
		ds.setMinIdle(5);
		ds.setMaxIdle(10);
		ds.setMaxOpenPreparedStatements(100);
	}
	
	public static void updateSubscription(Context context, DynamodbStreamRecord record) {
		context.getLogger().log(record.getEventID() + " updating subscription to external MYSQL"); 
				
		try {
			PreparedStatement ps = ds.getConnection().prepareStatement("update ems.subscripciones set msisdn = ?, fecAlta = ?, fecUpdated = ?, producto = ?, cantUpdated = ? where id = ? ");
			ps.setString(1, getValue(context, record, "msisdn"));
			ps.setString(2, getValue(context, record, "fecAlta"));
			ps.setString(3, getValue(context, record, "fecUpdated"));
			ps.setString(4, getValue(context, record, "producto"));
			ps.setString(5, getValue(context, record, "cantUpdated"));
			ps.setString(6, getValue(context, record, "id"));
						
			int rows = ps.executeUpdate();
			ps.close();
			
			context.getLogger().log(record.getEventID() + " updated subscription to external MYSQL. All good! id: " + record.getDynamodb().getNewImage().get("id").toString() + " rows updated: " + rows); 
		}
		catch(Exception e) {
			context.getLogger().log(record.getEventID() + " error updating subscription - " + e.toString() + " - " + e.getLocalizedMessage()); 
		}
	}
	
	public static void insertSubscription(Context context, DynamodbStreamRecord record) {
		context.getLogger().log(record.getEventID() + " inserting subscription to external MYSQL"); 
		
		try {
			PreparedStatement ps = ds.getConnection().prepareStatement("insert into ems.subscripciones (msisdn, fecAlta, fecUpdated, producto, cantUpdated, id) values (?,?,?,?,?,?) ");
			ps.setString(1, getValue(context, record, "msisdn"));
			ps.setString(2, getValue(context, record, "fecAlta"));
			ps.setString(3, getValue(context, record, "fecUpdated"));
			ps.setString(4, getValue(context, record, "producto"));
			ps.setString(5, getValue(context, record, "cantUpdated"));
			ps.setString(6, getValue(context, record, "id"));
						
			int rows = ps.executeUpdate();
			ps.close();
			
			context.getLogger().log(record.getEventID() + " inserted subscription to external MYSQL. All good! id: " + record.getDynamodb().getNewImage().get("id").toString() + " rows updated: " + rows); 
		}
		catch(Exception e) {
			context.getLogger().log(record.getEventID() + " error inserting subscription - " + e.toString() + " - " + e.getLocalizedMessage()); 
		}
	}
	
	private static String getValue(Context context, DynamodbStreamRecord record, String key) {
		String str = record.getDynamodb().getNewImage().get(key).toString();

		int pos1 = str.indexOf(":");
		int pos2 = str.indexOf(",");
		
		String res = str.substring(pos1 + 1, pos2).trim();
		
		context.getLogger().log(record.getEventID() + " getValue() extracting " + key + " - value: " + res); 
		
		return res;
	}
		
	public static void deleteSubscription(Context context, DynamodbStreamRecord record) {
		String id = "";
		
		try {
			PreparedStatement ps = ds.getConnection().prepareStatement("delete from ems.subscripciones where id = ? ");
			ps.setString(1, id);
			
			ps.executeUpdate();
			ps.close();
		}
		catch(Exception e) {
			context.getLogger().log(record.getEventID() + " error deleting subscription: " + id); 
		}
		
	}


}
