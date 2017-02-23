package com.vnet.demo.servicebus;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.JmsQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;

/**
 *
 *
 */
@Configuration
@ComponentScan
@EnableJms
@PropertySource("servicebus.properties")
public class App {

    @Value("${servicebus.hostname}")
    private String hostName;

    @Value("${servicebus.username}")
    private String userName;

    @Value("${servicebus.password}")
    private String password;

    @Value("${servicebus.queue}")
    private String queue;

    @Bean
    ConnectionFactory getConnectionFactory() {
        ConnectionFactory connectionFactory = new JmsConnectionFactory(userName, password, hostName);
        return connectionFactory;
    }

    /**
     *
     * 通常情况下，我们会借助Spring提供的JmsTemplate来实现消息发送
     */
    @Bean
    JmsTemplate newJmsTemplate(ConnectionFactory connectionFactory) {
        return new JmsTemplate(connectionFactory);
    }

    @Bean
    Queue newJmsQueue() {
        return new JmsQueue(queue);
    }

    public static void main( String[] args ) throws InterruptedException {
        ApplicationContext context = new AnnotationConfigApplicationContext(App.class);
        JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
        Queue queue = context.getBean(Queue.class);
        jmsTemplate.send(queue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage("Hello World");
            }
        });
        Thread.sleep(10000);
        System.exit(0);
    }
}
