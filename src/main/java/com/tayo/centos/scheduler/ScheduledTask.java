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
		Date timeToSend = new Date(System.currentTimeMillis()-(7*60*1000));
		try
		{
		/*	ScheduledJob.printLast20Activities(timeToSend);
			ScheduledJob.printTop10UserActivitiesByTop10Users(timeToSend);*/
			ScheduledJob.printAllAnswers(timeToSend);
			
		}
		catch(Exception e)
		{
			log.error(e.toString());
		}
	
		
	}

}
