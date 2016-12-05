package com.vnet.demo.jms.servicebus;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;


/**
 * using jms with azure service bus
 *
 */
public class TopicTest {

    private static final String USER = "test";
    private static final String PASSWORD = "VzJUWqJ7iVfcCJN6UxmLLP/hzEqedQYN5d6BF098Usc=";

    public static void main(String[] args) throws Exception {
        try {
            Context context = new InitialContext();

            ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");

            Connection connection = factory.createConnection(USER, PASSWORD);
            connection.setExceptionListener(new ExceptionListener());
            connection.start();

            TopicSession session = (TopicSession) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic("test");
              // 对未启用partition的topic可以用TopicSubscriber订阅消息
//            TopicSubscriber subscriber = session.createDurableSubscriber(topic, "subscription1");
//            subscriber.setMessageListener(new MessageListener());

            // 对启用partition的topic 只能用MessageConsumer来订阅消息
            MessageConsumer messageConsumer = session.createConsumer(session.createQueue("test/Subscriptions/sub1"));
            messageConsumer.setMessageListener(new MessageListener());

            MessageProducer messageProducer = session.createProducer(topic);
            Message message = session.createTextMessage("Hello world1213!");
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
