## Spring JMS with Azure Service Bus

Spring JMS 整合Azure Service Bus，JMS API实现 依然借助于Qpid JMS Client的实现。

Qpid JMS Client 连接 Azure Service Bus 的文档，请参考：
http://arui.me/index.php/archives/120/

首先，创建Maven 项目，将Spring JMS和Qpid的依赖加入到POM中。
```xml
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-context</artifactId>
	<version>4.2.6.RELEASE</version>
	</dependency>
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-jms</artifactId>
	<version>4.2.6.RELEASE</version>
</dependency>
<dependency>
	<groupId>org.apache.qpid</groupId>
	<artifactId>qpid-jms-client</artifactId>
	<version>0.9.0</version>
</dependency>
```
之后，设置配置文件，并通过Spring读取配置文件
```
servicebus.hostname=amqps://kevinsb.servicebus.chinacloudapi.cn?amqp.idleTimeout=200000
servicebus.username=RootManageSharedAccessKey
servicebus.password=n6Kh4vcfEpSjD0oAZrmPtNF1oFEoVLsOra4FB4c36UM=
servicebus.queue=queue1
```
代码示例
```java
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
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }
	
    /**
     * 
     * 配置connect factory, Spring也提供多种ConnectionFactory，如：SingleConnectionFactory，
     * CachingConnectionFactory, 但本例使用Qpid默认提供的ConnectionFactory示例，可以参考Spring的文档，
     * 查看Spring ConnectionFactoryd的具体用法
     */
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

    /**
    * 
    * 注册消息消费者和错误监听器
    */
    @Bean
    MessageListenerContainer newListenerContainer(ConnectionFactory connectionFactory, Queue queue, 
		    MessageConsumerListener messageConsumerListener) {
    
        DefaultMessageListenerContainer messageListenerContainer = 
			        new DefaultMessageListenerContainer();
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
```

