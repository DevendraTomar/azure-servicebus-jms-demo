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
public class MessageRecevier {

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
            Thread.sleep(30000000);
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
