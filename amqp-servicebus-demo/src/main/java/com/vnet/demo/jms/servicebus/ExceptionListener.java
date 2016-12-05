package com.vnet.demo.jms.servicebus;

import javax.jms.JMSException;

/**
 * Created by chen.rui on 12/5/2016.
 */
public class ExceptionListener implements javax.jms.ExceptionListener {
    public void onException(JMSException exception) {
        System.out.println("Connection ExceptionListener fired, exiting.");
        exception.printStackTrace(System.out);
        System.exit(1);
    }
}
