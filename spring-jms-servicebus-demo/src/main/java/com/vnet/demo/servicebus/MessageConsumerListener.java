package com.vnet.demo.servicebus;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;


@Component
public class MessageConsumerListener implements MessageListener {

    public void onMessage(Message message) {
        TextMessage receivedMessage = (TextMessage) message;
        try {
            System.out.println(receivedMessage.getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
