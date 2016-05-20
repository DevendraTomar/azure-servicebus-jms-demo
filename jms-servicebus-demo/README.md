##Using JMS with Azure Service Bus and AMQP 1.0 

>Microsoft Azure Service Bus 是微软提供的消息服务总线云服务，支持消息队列， 主题订阅，点对点消息， Event Hubs功能。类似于JAVA的ActiveMQ的功能， Service Bus是支持AMQP 1.0协议的，所以基于AMQP 1.0协议实现的JMS客户端均都可以连接 Service Bus发送接受消息。

本文将采用Apache Qpid 来连接Service Bus 的消息队列，简单实现一个发送接收消息队列的功能。
Azure 官网提供的示例和文档，均是使用较旧的Qpid客户端，本文将使用最新的 Qpid JMS client 去连接Azure 的Service Bus.
可以从Qpid 官网中，看出最新的Qpid JMS client 是支持AMQP1.0协议的。
![image](http://oss.arui.me/typecho/2016/05/18/857703302848628.png)
#### using AMQP with Service Bus Queue
首先，引入Qpid JMS client lib，使用Maven配置依赖
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
或者官网下载Qpid的包 https://qpid.apache.org/releases/qpid-jms-0.9.0/
其次， 创建jndi.properties 配置文件：
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
配置文件连接后可以加参数：
amqp.idleTimeout  : 表示空闲连接timeout时间, 默认连接的时间是30000，Azure  Service Bus 要求的最低时间为60000。
更多配置请参考 https://qpid.apache.org/releases/qpid-jms-0.2.0/docs/index.html

最新版本的Qpid的使用和之前类似，以下连接Queue的示例代码：
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
 JMS API with AMQP 1.0 是不支持Service bus 的topic subscrptions
以下是微软官方文档中列出的 受限或者不支持特性
![image](http://oss.arui.me/typecho/2016/05/18/857711232994156.png)

#### 如何构建稳健的程序
JMS规范定义了如何编写捕获JMS异常的方法，这里有几点需要注意，示例代码中只是简单实现，并未实现对异常的处理。
	1. 注册 ExceptionListener， JMS规范规定可以对 JMS connection 注册异常监听器，这样客户端就可以监听连接是否正常，这样开发者就可以判断是否需要重新创建connection ， Session， MessageProducer and MessageConsumer。
	2. 验证消息是否发送成功。确保已经配置qpid.sync_publish 这个系统属性。设置这个属性之后，程序在发送消息后，会等待发送结果反馈后才返回，如果有异常出现，程序将会抛出JMSException。触发异常的通常是以下两种情况：
		○ Service bus 拒绝发送的消息，会反馈MessageRejectedException异常。这个消息将会被Service Bus忽略。
		○ 如果Service bus 关闭了JMS的连接，会反馈InvalidDestinationException 异常。这时就需要重新创建连接，并重新发送消息。
		
#### Troubleshoot
PKIX：unable to find valid certification path to requested target
请参见：
	- [Java 调用 Azure HTTPS API 证书问题](http://arui.me/index.php/archives/118/)
	- [解决 PKIX：unable to find valid certification path to requested target\](http://www.arui.me/index.php/archives/69/)

更多文档

https://azure.microsoft.com/en-us/documentation/articles/service-bus-java-how-to-use-jms-api-amqp/

