package com.vnet.demo.servicebus;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.JmsQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.util.ErrorHandler;

import javax.jms.*;

@Configuration
@ComponentScan
@EnableJms
@PropertySource("servicebus.properties")
public class Application {

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
	 * 必须设置，@Value 取值依赖于此
	 * 
	 */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }
	
	/**
	 * 
	 * 配置connect factory, Spring也提供多种ConnectionFactory，如：SingleConnectionFactory，CachingConnectionFactory
	 * 但本例使用Qpid默认提供的ConnectionFactory示例，可以参考Spring的文档，查看Spring ConnectionFactoryd的具体用法
	 * 
	 */
    @Bean
    ConnectionFactory getConnectionFactory() {
        ConnectionFactory connectionFactory = new JmsConnectionFactory(userName, password, hostName);
        return connectionFactory;
    }

	/**
	 * 
	 * 通常情况下，我们会借助Spring提供的JmsTemplate来实现消息发送
	 * 
	 */
    @Bean
    JmsTemplate newJmsTemplate(ConnectionFactory connectionFactory) {
        return new JmsTemplate(connectionFactory);
    }

	/**
	 * 
	 * 注册消息消费者和错误监听器
	 * 
	 */
    @Bean
    MessageListenerContainer newListenerContainer(ConnectionFactory connectionFactory, Queue queue, MessageConsumerListener messageConsumerListener) {
        DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();
        messageListenerContainer.setConnectionFactory(connectionFactory);
        messageListenerContainer.setMessageListener(messageConsumerListener);
        messageListenerContainer.setDestination(queue);
        messageListenerContainer.setExceptionListener(new ExceptionListener() {
            public void onException(JMSException exception) {
                System.out.println("Connection ExceptionListener fired, exiting.");
                exception.printStackTrace(System.out);
                System.exit(1);
            }
        });
        return messageListenerContainer;
    }

    @Bean
    Queue queue() {
        return new JmsQueue(queue);
    }

	
    public static void main(String[] args) throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext(Application.class);
        JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
        Queue queue = context.getBean(Queue.class);
        jmsTemplate.send(queue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage("Hello World");
            }
        });
        Thread.sleep(2000);
        System.exit(1);
    }

}
