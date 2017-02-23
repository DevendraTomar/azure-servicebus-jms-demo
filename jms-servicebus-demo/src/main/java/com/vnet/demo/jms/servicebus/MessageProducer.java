package com.vnet.demo.jms.servicebus;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Created by kevin on 2/22/2017.
 */
public class MessageProducer {

    private static final String USER = "RootManageSharedAccessKey";
    private static final String PASSWORD = "/zYwYatlKh02HrN5TcU8QN0uPq4AW6lFX/C2EqlKT2s=";

    public static void main(String[] args) throws Exception {
        try {
            Context context = new InitialContext();

            ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
            Destination destination = (Destination) context.lookup("queue1");

            Connection connection = factory.createConnection(USER, PASSWORD);
            connection.setExceptionListener(new MyExceptionListener());
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            for (int i = 0; i < 2; i++) {
                javax.jms.MessageProducer messageProducer = session.createProducer(destination);
                TextMessage message = session.createTextMessage(i + " Hello world!");
                messageProducer.send(message);
            }
            connection.close();
        } catch (Exception exp) {
            System.out.println("Caught exception, exiting.");
            exp.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private static class MyExceptionListener implements ExceptionListener {
        public void onException(JMSException exception) {
            System.out.println("Connection ExceptionListener fired, exiting.");
            exception.printStackTrace(System.out);
            System.exit(1);
        }
    }
}
