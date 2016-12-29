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
		Date timeToSend = new Date(System.currentTimeMillis()-(7*60*1000));  //compute current time - 7 minutes
		try
		{
			ScheduledJob.printAllAnswers(timeToSend);
			
		}
		catch(Exception e)
		{
			log.error(e.toString());
		}
	
		
	}

}
