package com.vnet.demo.jms.servicebus;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * using jms with azure service bus
 *
 */
public class QueueTest {

    private static final String USER = "RootManageSharedAccessKey";
    private static final String PASSWORD = "TWJwaQugbYZfu3qnbgXIGNWSGayn1tq9pIXeN3MLRZE=";

    public static void main(String[] args) throws Exception {
        try {
            Context context = new InitialContext();

            ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
            Destination queue = (Destination) context.lookup("queue1");

            Connection connection = factory.createConnection(USER, PASSWORD);
            connection.setExceptionListener(new ExceptionListener());
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(queue);
            MessageConsumer messageConsumer = session.createConsumer(queue);
            messageConsumer.setMessageListener(new MessageListener());

            TextMessage message = session.createTextMessage("Hello world!");
            messageProducer.send(message);
            Thread.sleep(100000);
            connection.close();
        } catch (Exception exp) {
            System.out.println("Caught exception, exiting.");
            exp.printStackTrace(System.out);
            System.exit(1);
        }
    }

}
