##Using JMS with Azure Service Bus and AMQP 1.0 

>Microsoft Azure Service Bus ��΢���ṩ����Ϣ���������Ʒ���֧����Ϣ���У� ���ⶩ�ģ���Ե���Ϣ�� Event Hubs���ܡ�������JAVA��ActiveMQ�Ĺ��ܣ� Service Bus��֧��AMQP 1.0Э��ģ����Ի���AMQP 1.0Э��ʵ�ֵ�JMS�ͻ��˾����������� Service Bus���ͽ�����Ϣ��

���Ľ�����Apache Qpid ������Service Bus ����Ϣ���У���ʵ��һ�����ͽ�����Ϣ���еĹ��ܡ�
Azure �����ṩ��ʾ�����ĵ�������ʹ�ýϾɵ�Qpid�ͻ��ˣ����Ľ�ʹ�����µ� Qpid JMS client ȥ����Azure ��Service Bus.
���Դ�Qpid �����У��������µ�Qpid JMS client ��֧��AMQP1.0Э��ġ�
![Alt text](./1463585185254.png)
#### using AMQP with Service Bus Queue
���ȣ�����Qpid JMS client lib��ʹ��Maven��������
```xml
    <dependency>
        <groupId>org.apache.qpid</groupId>
        <artifactId>qpid-jms-client</artifactId>
        <version>0.9.0</version>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-log4j12</artifactId>
        <version>1.7.5</version>
    </dependency>
```
���߹�������Qpid�İ� https://qpid.apache.org/releases/qpid-jms-0.9.0/
��Σ� ����jndi.properties �����ļ���
```coffeescript
# Set the InitialContextFactory class to use
java.naming.factory.initial = org.apache.qpid.jms.jndi.JmsInitialContextFactory
# Define the required ConnectionFactory instances
# connectionfactory.<JNDI-lookup-name> = <URI>
connectionfactory.myFactoryLookup = amqps://kevinsb.servicebus.chinacloudapi.cn?amqp.idleTimeout=200000
# Configure the necessary Queue and Topic objects
queue.queue1 = queue1
topic.topic = topic1
```
�����ļ����Ӻ���ԼӲ�����
amqp.idleTimeout  : ��ʾ��������timeoutʱ��, Ĭ�����ӵ�ʱ����30000��Azure  Service Bus Ҫ������ʱ��Ϊ60000��
����������ο� https://qpid.apache.org/releases/qpid-jms-0.2.0/docs/index.html

���°汾��Qpid��ʹ�ú�֮ǰ���ƣ���������Queue��ʾ�����룺
```java
private static final String USER = "RootManageSharedAccessKey";
private static final String PASSWORD = "your key";

 public static void main(String[] args) throws Exception {
     try {
         Context context = new InitialContext();

         ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
         Destination queue = (Destination) context.lookup("queue1");

         Connection connection = factory.createConnection(USER, PASSWORD);
         connection.setExceptionListener(new MyExceptionListener());
         connection.start();

         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageProducer messageProducer = session.createProducer(queue);
         MessageConsumer messageConsumer = session.createConsumer(queue);
         messageConsumer.setMessageListener(new MessageListener() {
             public void onMessage(Message message) {
                 TextMessage receivedMessage = (TextMessage) message;
                 try {
                     System.out.println(receivedMessage.getText());
                 } catch (JMSException e) {
                     e.printStackTrace();
                 }
             }
         });

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
 ```
#### using AMQP with Service Bus Topic
 JMS API with AMQP 1.0 �ǲ�֧��Service bus ��topic subscrptions
������΢��ٷ��ĵ����г��� ���޻��߲�֧������
![Alt text](./1463585634270.png)

#### ��ι����Ƚ��ĳ���
JMS�淶��������α�д����JMS�쳣�ķ����������м�����Ҫע�⣬ʾ��������ֻ�Ǽ�ʵ�֣���δʵ�ֶ��쳣�Ĵ���
	1. ע�� ExceptionListener�� JMS�淶�涨���Զ� JMS connection ע���쳣�������������ͻ��˾Ϳ��Լ��������Ƿ����������������߾Ϳ����ж��Ƿ���Ҫ���´���connection �� Session�� MessageProducer and MessageConsumer��
	2. ��֤��Ϣ�Ƿ��ͳɹ���ȷ���Ѿ�����qpid.sync_publish ���ϵͳ���ԡ������������֮�󣬳����ڷ�����Ϣ�󣬻�ȴ����ͽ��������ŷ��أ�������쳣���֣����򽫻��׳�JMSException�������쳣��ͨ�����������������
		�� Service bus �ܾ����͵���Ϣ���ᷴ��MessageRejectedException�쳣�������Ϣ���ᱻService Bus���ԡ�
		�� ���Service bus �ر���JMS�����ӣ��ᷴ��InvalidDestinationException �쳣����ʱ����Ҫ���´������ӣ������·�����Ϣ��
		
#### Troubleshoot
PKIX��unable to find valid certification path to requested target
��μ���
	- [Java ���� Azure HTTPS API ֤������](http://arui.me/index.php/archives/118/)
	- [��� PKIX��unable to find valid certification path to requested target\](http://www.arui.me/index.php/archives/69/)

�����ĵ�

https://azure.microsoft.com/en-us/documentation/articles/service-bus-java-how-to-use-jms-api-amqp/

