package com.vnet.demo.jms.servicebus;


import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

public class MessageListener implements javax.jms.MessageListener {

    public void onMessage(Message message) {
        TextMessage receivedMessage = (TextMessage) message;
        try {
            System.out.println(receivedMessage.getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
