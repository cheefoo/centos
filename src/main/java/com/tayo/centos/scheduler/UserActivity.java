package com.tayo.centos.scheduler;

public class UserActivity 
{
	private String userId;
	private String activityType;
	private String activityCount;
	
	
	public UserActivity(String userId, String activityType) 	
	{
		super();
		this.userId = userId;
		this.activityType = activityType;
	}

	public UserActivity(String userId, String activityType, String activityCount)
	{
		super();
		this.userId = userId;
		this.activityType = activityType;
		this.activityCount = activityCount;
	}



	@Override
	public String toString() 
	{

		return "userId:" + userId + ", activityType:" + activityType;
	}
	
	

}
