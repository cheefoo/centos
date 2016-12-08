package com.tayo.centos.scheduler;

public class ActivityTimestamp 
{
	private String activityType;
	private String activityTimestamp;
	
	
	public ActivityTimestamp(String activityType, String activityTimestamp) 
	{
		super();
		this.activityType = activityType;
		this.activityTimestamp = activityTimestamp;
	}
	public String getActivityType() 
	{
		return activityType;
	}
	public void setActivityType(String activityType) 
	{
		this.activityType = activityType;
	}
	public String getActivityTimestamp() 
	{
		return activityTimestamp;
	}
	public void setActivityTimestamp(String activityTimestamp) 
	{
		this.activityTimestamp = activityTimestamp;
	}
	@Override
	public String toString() {
		return "activityType=" + activityType + ", activityTimestamp=" + activityTimestamp;
	}
	
	
	
		

}
