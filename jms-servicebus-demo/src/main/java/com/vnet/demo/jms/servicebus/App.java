package com.vnet.demo.jms.servicebus;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 *
 *
 * using jms with azure service bus
 *
 */
public class App {

    private static final String USER = "RootManageSharedAccessKey";
    private static final String PASSWORD = "n6Kh4vcfEpSjD0oAZrmPtNF1oFEoVLsOra4FB4c36UM=";

    public static void main(String[] args) throws Exception {
        try {
            Context context = new InitialContext();

            ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
            Destination destination = (Destination) context.lookup("queue1");

            Connection connection = factory.createConnection(USER, PASSWORD);
            connection.setExceptionListener(new MyExceptionListener());
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageConsumer consumer = session.createConsumer(destination);
            consumer.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                    TextMessage receivedMessage = (TextMessage) message;
                    try {
                        System.out.println(receivedMessage.getText());
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            });

            MessageProducer messageProducer = session.createProducer(destination);
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

    private static class MyExceptionListener implements ExceptionListener {
        public void onException(JMSException exception) {
            System.out.println("Connection ExceptionListener fired, exiting.");
            exception.printStackTrace(System.out);
            System.exit(1);
        }
    }

}
