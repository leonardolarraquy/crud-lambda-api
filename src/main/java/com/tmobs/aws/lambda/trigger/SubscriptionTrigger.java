package com.tmobs.aws.lambda.trigger;

import java.util.Iterator;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;

public class SubscriptionTrigger implements RequestHandler<DynamodbEvent, Void> {
	
	private void logImage(Context context, DynamodbStreamRecord record, Map image, String desc) {
		if(image == null)
			return;
		
		Iterator it = image.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry next = (Map.Entry) it.next();
			context.getLogger().log(record.getEventID() + " " + desc + " value key: " + next.getKey() + " value: " + next.getValue()); 
		}
	}
	
	@Override
	public Void handleRequest(DynamodbEvent event, Context context) {
		context.getLogger().log("function called: " + context.getFunctionName());
		context.getLogger().log("event triggered: " + event.toString());

		for (DynamodbStreamRecord record : event.getRecords()) {

			if (record == null) {
				continue;
			}

			context.getLogger().log(record.getEventID() + " record triggered: " + record.getEventName() + " region: " + record.getAwsRegion() + " source: " + record.getEventSource());

			logImage(context, record, record.getDynamodb().getOldImage(), "old");
			logImage(context, record, record.getDynamodb().getNewImage(), "new");
			
			if(record.getEventName().equalsIgnoreCase("MODIFY")) {
				DatabaseSync.updateSubscription(context, record);
			}

			if(record.getEventName().equalsIgnoreCase("INSERT")) {
				DatabaseSync.insertSubscription(context, record);
			}

			if(record.getEventName().equalsIgnoreCase("DELETE")) {

			}

		}

		return null;
	}
}