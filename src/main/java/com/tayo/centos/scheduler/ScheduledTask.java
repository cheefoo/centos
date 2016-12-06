package com.tayo.centos.scheduler;

import java.util.Date;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduledTask extends TimerTask
{
	private static final Logger log = LoggerFactory.getLogger(ScheduledTask.class);
	Date now;
	@Override
	public void run() 
	{
		now = new Date();
		try
		{
			ScheduledJob.printLast20Activities();
			ScheduledJob.printTop10UserActivitiesByTop10Users();
		}
		catch(Exception e)
		{
			log.error(e.toString());
		}
	
		
	}

}
