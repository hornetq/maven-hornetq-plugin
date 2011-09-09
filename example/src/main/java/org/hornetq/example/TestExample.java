package org.hornetq.example;

import org.hornetq.maven.HornetQClient;

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

/**
 * @author <a href="mailto:andy.taylor@jboss.com">Andy Taylor</a>
 *         Date: 8/18/11
 *         Time: 2:46 PM
 */
public class TestExample
{
    public static void main(String[] args)
    {
        Connection connection = null;
        InitialContext initialContext = null;
        try
        {
            System.out.println(System.getProperty("java.naming.factory.initial"));
            // Step 1. Create an initial context to perform the JNDI lookup.
            initialContext = new InitialContext();

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
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            // Step 12. Be sure to close our JMS resources!
            if (initialContext != null)
            {
                try
                {
                    initialContext.close();
                }
                catch (NamingException e)
                {
                    e.printStackTrace();
                }
            }
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (JMSException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
