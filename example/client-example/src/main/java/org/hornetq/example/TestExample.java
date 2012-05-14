package org.hornetq.example;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * @author <a href="mailto:andy.taylor@jboss.com">Andy Taylor</a>
 *         Date: 8/18/11
 *         Time: 2:46 PM
 *
 * A simple client that uses the JMS API to send and receive a message
 */
public class TestExample
{
   public static void main(String[] args)
   {
      Connection connection = null;
      InitialContext initialContext = null;
      String jndiHost = args[0];
      try
      {
         // Step 1. Create an initial context to perform the JNDI lookup.
         Hashtable<Object, Object> env = new Hashtable<Object, Object>();
         env.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
         env.put("java.naming.factory.url.pkgs","org.jboss.naming:org.jnp.interfaces");
         env.put("java.naming.provider.url", jndiHost);
         initialContext = new InitialContext(env);

         // Step 2. Perfom a lookup on the queue
         Queue queue = (Queue) initialContext.lookup("/queue/exampleQueue");

         // Step 3. Perform a lookup on the Connection Factory
         ConnectionFactory cf = (ConnectionFactory) initialContext.lookup("/ConnectionFactory");

         // Step 4.Create a JMS Connection
         connection = cf.createConnection();

         // Step 5. Create a JMS Session
         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

         // Step 6. Create a JMS Message Producer
         MessageProducer producer = session.createProducer(queue);

         // Step 7. Create a Text Message
         TextMessage message = session.createTextMessage("This is a text message");

         System.out.println("Sent message: " + message.getText());

         // Step 8. Send the Message
         producer.send(message);

         // Step 9. Create a JMS Message Consumer
         MessageConsumer messageConsumer = session.createConsumer(queue);

         // Step 10. Start the Connection
         connection.start();

         // Step 11. Receive the message
         TextMessage messageReceived = (TextMessage) messageConsumer.receive(5000);

         System.out.println("Received message: " + messageReceived.getText());
      } catch (Exception e)
      {
         e.printStackTrace();
      } finally
      {
         // Step 12. Be sure to close our JMS resources!
         if (initialContext != null)
         {
            try
            {
               initialContext.close();
            } catch (NamingException e)
            {
               e.printStackTrace();
            }
         }
         if (connection != null)
         {
            try
            {
               connection.close();
            } catch (JMSException e)
            {
               e.printStackTrace();
            }
         }
      }
   }
}
