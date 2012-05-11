package org.hornetq.maven;

import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.config.impl.FileConfiguration;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.JournalType;
import org.hornetq.core.server.NodeManager;
import org.hornetq.core.server.impl.HornetQServerImpl;
import org.hornetq.server.InVMNodeManager;
import org.hornetq.jms.server.JMSServerManager;
import org.hornetq.jms.server.impl.JMSServerManagerImpl;
import org.hornetq.spi.core.security.HornetQSecurityManagerImpl;
import org.jnp.server.Main;
import org.jnp.server.NamingBeanImpl;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.*;


/**
 * @author <a href="mailto:andy.taylor@jboss.com">Andy Taylor</a>
 *         Date: 8/18/11
 *         Time: 11:21 AM
 *
 * @phase verify
 *
 * @goal start
 */
public class HornetQStartPlugin extends AbstractMojo

{
   /**
    * The plugin descriptor
    *
    * @parameter default-value="${descriptor}"
    */
   private PluginDescriptor descriptor;


   /**
    * @parameter default-value=false
    */
   private Boolean waitOnStart;

   /**
    * @parameter
    */
   private String hornetqConfigurationDir;

   /**
    * @parameter default-value=true
    */
   private Boolean useJndi;

  /**
   * @parameter
   */
   private String nodeId;

  /**
   * @parameter default-value=localhost
   */
   private String jndiHost;

   /**
   * @parameter default-value=1099
   */
   private int jndiPort;

   /**
   * @parameter default-value=1098
   */
   private int jndiRmiPort;

   private static Map<String, NodeManager> managerMap = new HashMap<String, NodeManager>();


   public void execute() throws MojoExecutionException, MojoFailureException
   {
      try
      {
         final Main main = useJndi?new Main() : null;
         if (useJndi)
         {
            System.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
            System.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
            NamingBeanImpl namingBean = new NamingBeanImpl();
            namingBean.start();
            main.setNamingInfo(namingBean);
            main.setBindAddress(jndiHost);
            main.setPort(jndiPort);
            main.setRmiBindAddress(jndiHost);
            main.setRmiPort(jndiRmiPort);
            main.start();
         }

         Configuration configuration;
         if (hornetqConfigurationDir != null)
         {
            extendPluginClasspath(hornetqConfigurationDir);
            configuration = new FileConfiguration();
            File file = new File(hornetqConfigurationDir + "/" + "hornetq-configuration.xml");
            ((FileConfiguration) configuration).setConfigurationUrl(file.toURI().toURL().toExternalForm());
            ((FileConfiguration) configuration).start();
         }
         else
         {
            configuration = new ConfigurationImpl();
            configuration.setJournalType(JournalType.NIO);
         }

         HornetQServer server;

         if(nodeId != null)
         {
             InVMNodeManager nodeManager = (InVMNodeManager) managerMap.get(nodeId);
             if(nodeManager == null)
             {
                 nodeManager = new InVMNodeManager();
                 managerMap.put(nodeId, nodeManager);
             }
             server = new InVMNodeManagerServer(configuration, ManagementFactory.getPlatformMBeanServer(), new HornetQSecurityManagerImpl(), nodeManager);
         }
         else
         {
            server = new HornetQServerImpl(configuration, ManagementFactory.getPlatformMBeanServer(), new HornetQSecurityManagerImpl());
         }

         final JMSServerManager manager = new JMSServerManagerImpl(server);
         manager.start();

         if (waitOnStart)
         {
            String dirName = System.getProperty("hornetq.config.dir", ".");
            final File file = new File(dirName + "/STOP_ME");
            if (file.exists())
            {
               file.delete();
            }

            while (!file.exists())
            {
               Thread.sleep(500);
            }

            manager.stop();
            if(main != null)
            {
               main.stop();
            }
            file.delete();
         }
         else
         {
            String dirName = hornetqConfigurationDir != null?hornetqConfigurationDir:".";
            final File file = new File(dirName + "/STOP_ME");
            if (file.exists())
            {
               file.delete();
            }
            final Timer timer = new Timer("HornetQ Server Shutdown Timer", true);
            timer.scheduleAtFixedRate(new TimerTask()
            {
               @Override
               public void run()
               {
                  if (file.exists())
                  {
                     try
                     {
                        timer.cancel();
                     } finally
                     {
                        try
                        {
                           manager.stop();
                           if (main != null)
                           {
                              main.stop();
                           }
                           file.delete();
                        } catch (Exception e)
                        {
                           e.printStackTrace();
                        }
                     }
                  }
               }
            }, 500, 500);
         }
      } catch (Exception e)
      {
         e.printStackTrace();
         throw new MojoExecutionException(e.getMessage());
      }
   }


   public void extendPluginClasspath(String element) throws MojoExecutionException
   {
      ClassWorld world = new ClassWorld();
      ClassRealm realm;
      try
      {
         realm = world.newRealm(
               "maven.plugin." + getClass().getSimpleName() + ((nodeId == null)?"":nodeId),
               Thread.currentThread().getContextClassLoader()
         );
            File elementFile = new File(element);
            getLog().debug("Adding element to plugin classpath" + elementFile.getPath());
            realm.addConstituent(elementFile.toURI().toURL());
      }
      catch (Exception ex)
      {
         throw new MojoExecutionException(ex.toString(), ex);
      }
      System.out.println(realm.getConstituents());
      Thread.currentThread().setContextClassLoader(realm.getClassLoader());
   }


}
