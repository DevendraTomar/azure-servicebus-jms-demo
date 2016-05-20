## Spring JMS with Azure Service Bus

Spring JMS ����Azure Service Bus��JMS APIʵ�� ��Ȼ������Qpid JMS Client��ʵ�֡�

Qpid JMS Client ���� Azure Service Bus ���ĵ�����ο���
http://arui.me/index.php/archives/120/

���ȣ�����Maven ��Ŀ����Spring JMS��Qpid���������뵽POM�С�
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
֮�����������ļ�����ͨ��Spring��ȡ�����ļ�
```
servicebus.hostname=amqps://kevinsb.servicebus.chinacloudapi.cn?amqp.idleTimeout=200000
servicebus.username=RootManageSharedAccessKey
servicebus.password=n6Kh4vcfEpSjD0oAZrmPtNF1oFEoVLsOra4FB4c36UM=
servicebus.queue=queue1
```
����ʾ��
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
     * �������ã�@Value ȡֵ�����ڴ�
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }
	
    /**
     * 
     * ����connect factory, SpringҲ�ṩ����ConnectionFactory���磺SingleConnectionFactory��
     * CachingConnectionFactory, ������ʹ��QpidĬ���ṩ��ConnectionFactoryʾ�������Բο�Spring���ĵ���
     * �鿴Spring ConnectionFactoryd�ľ����÷�
     */
    @Bean
    ConnectionFactory getConnectionFactory() {
        ConnectionFactory connectionFactory = new JmsConnectionFactory(userName, password, hostName);
        return connectionFactory;
    }

    /**
     * 
     * ͨ������£����ǻ����Spring�ṩ��JmsTemplate��ʵ����Ϣ����
     */
    @Bean
    JmsTemplate newJmsTemplate(ConnectionFactory connectionFactory) {
        return new JmsTemplate(connectionFactory);
    }

    /**
    * 
    * ע����Ϣ�����ߺʹ��������
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

