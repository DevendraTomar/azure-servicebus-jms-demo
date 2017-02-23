package com.vnet.demo.servicebus;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.JmsQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.messaging.MessageHeaders;

import javax.jms.*;

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

    /**
     *
     * 配置connect factory, Spring也提供多种ConnectionFactory，如：SingleConnectionFactory，CachingConnectionFactory
     * 但本例使用Qpid默认提供的ConnectionFactory示例，可以参考Spring的文档，查看Spring ConnectionFactory的具体用法
     */
    @Bean
    ConnectionFactory getConnectionFactory() {
        ConnectionFactory connectionFactory = new JmsConnectionFactory(userName, password, hostName);
        return connectionFactory;
    }

    @Bean
    public SimpleMessageListenerContainer jmsListenerContainerFactory(ConnectionFactory connectionFactory, JmsQueue jmsQueue) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setConcurrency("1");
        container.setDestination(jmsQueue);
        container.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        container.setSessionTransacted(false);
        container.setMessageListener(new MessageListener() {
            public void onMessage(Message message) {
                try {
                    TextMessage receivedMessage = (TextMessage) message;
                    System.out.println("============================"+ receivedMessage.getText());
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
        return container;
    }

    @Bean
    JmsQueue newJmsQueue() {
        return new JmsQueue(queue);
    }

    public static void main( String[] args ) throws InterruptedException {
        ApplicationContext context = new AnnotationConfigApplicationContext(App.class);
        Thread.sleep(1000000000);
    }
}
