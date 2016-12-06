package com.tayo.centos.scheduler;

import java.util.Timer;

public class DashboardMonitor 
{
	public static void main (String args[])
	{
		Timer timer = new Timer();
		ScheduledTask task = new ScheduledTask();
		timer.schedule(task, 0, 1000);	
	}
	

}
