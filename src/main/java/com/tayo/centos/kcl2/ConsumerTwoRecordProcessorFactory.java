package com.tayo.centos.kcl2;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;

/**
 * Created by temitayo on 12/1/16.
 */
public class ConsumerTwoRecordProcessorFactory implements IRecordProcessorFactory
{

    @Override
    public IRecordProcessor createProcessor()
    {
        return new ConsumerTwoRecordProcessor();

    }
}
