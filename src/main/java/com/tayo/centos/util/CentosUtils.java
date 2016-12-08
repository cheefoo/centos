package com.tayo.centos.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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


}
