package com.tayo.centos.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
* Utilities class to load resource properties
 */
public class CentosUtils 
{
	
	
	public static java.util.Properties getProperties() throws IOException
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("db.properties");
        java.util.Properties prop = new Properties();
        prop.load(input);

        return prop;

    }

    public static String removeUnwantedToken(String record) throws Exception
    {
        if(record.length() == 0 || record == null)
            throw new Exception("record is null or has zero length");

        return record.substring(record.indexOf(",")+1, record.length());
    }


}
