package com.tayo.centos.scheduler;

public class UserActivity 
{
	private String userId;
	private String activityType;
	
	
	public UserActivity(String userId, String activityType) 
	
	{
		super();
		this.userId = userId;
		this.activityType = activityType;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) 
	{
		this.userId = userId;
	}
	public String getActivityType() 
	{
		return activityType;
	}
	public void setActivityType(String activityType) 
	{
		this.activityType = activityType;
	}
	
	

}
