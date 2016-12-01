package com.tayo.centos.kcl1;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;


/**
 * Created by temitayo on 11/30/16.
 */
public class ConsumerOneRecordProcessorFactory implements IRecordProcessorFactory
{

    @Override
    public IRecordProcessor createProcessor()
    {
        return new ConsumerOneRecordProcessor();
    }

}
