/*
* JBoss, Home of Professional Open Source.
* Copyright 2010, Red Hat, Inc., and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.hornetq.server;

import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.config.impl.FileConfiguration;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.JournalType;
import org.hornetq.core.server.NodeManager;
import org.hornetq.core.server.impl.HornetQServerImpl;
import org.hornetq.jms.server.JMSServerManager;
import org.hornetq.jms.server.impl.JMSServerManagerImpl;
import org.hornetq.maven.InVMNodeManagerServer;
import org.hornetq.spi.core.security.HornetQSecurityManager;
import org.hornetq.spi.core.security.HornetQSecurityManagerImpl;
import org.jnp.server.Main;
import org.jnp.server.NamingBeanImpl;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This will bootstrap the HornetQ Server and also the naming server if required
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 */
public class HornetQBootstrap
{
   private final Boolean useJndi;

   private final String jndiHost;

   private final int jndiPort;

   private final int jndiRmiPort;

   private final String hornetqConfigurationDir;

   private final Boolean waitOnStart;

   private final String nodeId;

   private static Map<String, NodeManager> managerMap = new HashMap<String, NodeManager>();

   private boolean spawned = false;

   private Main main;

   private NamingBeanImpl namingBean;

   private HornetQServer server;

   private Configuration configuration;

   private JMSServerManager manager;

   private HornetQSecurityManager securityManager;


   public HornetQBootstrap(Boolean useJndi, String jndiHost, int jndiPort, int jndiRmiPort, String hornetqConfigurationDir,
                           Boolean waitOnStart, String nodeId, HornetQSecurityManager securityManager)
   {
      this.useJndi = useJndi;
      this.jndiHost = jndiHost;
      this.jndiPort = jndiPort;
      this.jndiRmiPort = jndiRmiPort;
      this.hornetqConfigurationDir = hornetqConfigurationDir;
      this.waitOnStart = waitOnStart;
      this.nodeId = nodeId;
      this.securityManager = securityManager;
   }

   public HornetQBootstrap(String[] args)
   {
      this.useJndi = Boolean.valueOf(args[0]);
      this.jndiHost = args[1];
      this.jndiPort = Integer.valueOf(args[2]);
      this.jndiRmiPort = Integer.valueOf(args[3]);
      this.hornetqConfigurationDir = args[4];
      this.waitOnStart = Boolean.valueOf(args[5]);;
      this.nodeId = args[6];
      spawned = true;
   }

   public void execute() throws Exception
   {
      try
      {
         System.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
         System.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");

         if (useJndi)
         {
            createNamingServer();
         }

         if (hornetqConfigurationDir != null)
         {
            //extendPluginClasspath(hornetqConfigurationDir);
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

         createServer(configuration);

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
            final File stopFile = new File(dirName + "/STOP_ME");
            if (stopFile.exists())
            {
               stopFile.delete();
            }
            final File killFile = new File(dirName + "/KILL_ME");
            if (killFile.exists())
            {
               killFile.delete();
            }
            final File restartFile = new File(dirName + "/RESTART_ME");
            if (restartFile.exists())
            {
               restartFile.delete();
            }
            final Timer timer = new Timer("HornetQ Server Shutdown Timer", true);
            timer.scheduleAtFixedRate(new ServerStopTimerTask(stopFile, killFile, restartFile, timer), 500, 500);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new Exception(e.getMessage());
      }
   }

   private void createServer(Configuration configuration) throws Exception
   {
      if(nodeId != null && !nodeId.equals("") && !nodeId.equals("null"))
      {
          InVMNodeManager nodeManager = (InVMNodeManager) managerMap.get(nodeId);
          if(nodeManager == null)
          {
            boolean replicatedBackup = configuration.isBackup() && !configuration.isSharedStore();
            nodeManager = new InVMNodeManager(replicatedBackup, configuration.getJournalDirectory());
              managerMap.put(nodeId, nodeManager);
          }
          server = new InVMNodeManagerServer(configuration, ManagementFactory.getPlatformMBeanServer(),
                securityManager!=null?securityManager:new HornetQSecurityManagerImpl(), nodeManager);
      }
      else
      {
         server = new HornetQServerImpl(configuration, ManagementFactory.getPlatformMBeanServer(),
               securityManager!=null?securityManager:new HornetQSecurityManagerImpl());
      }

      manager = new JMSServerManagerImpl(server);
      manager.start();
   }

   private void createNamingServer() throws Exception
   {
      main = new Main();
      namingBean = new NamingBeanImpl();
      namingBean.start();
      main.setNamingInfo(namingBean);
      main.setBindAddress(jndiHost);
      main.setPort(jndiPort);
      main.setRmiBindAddress(jndiHost);
      main.setRmiPort(jndiRmiPort);
      main.start();
   }


   private class ServerStopTimerTask extends TimerTask
   {
      private final File stopFile;
      private final Timer timer;
      private final File killFile;
      private final File restartFile;

      public ServerStopTimerTask(File stopFile, File killFile, File restartFile, Timer timer)
      {
         this.stopFile = stopFile;
         this.killFile = killFile;
         this.restartFile = restartFile;
         this.timer = timer;
      }

      @Override
      public void run()
      {
         if (stopFile.exists())
         {
            try
            {
               timer.cancel();
            }
            finally
            {
               try
               {
                  if (manager != null)
                  {
                     manager.stop();
                     manager = null;
                  }
                  server = null;
                  if (main != null)
                  {
                     main.stop();
                     main = null;
                  }
                  stopFile.delete();
               }
               catch (Exception e)
               {
                  e.printStackTrace();
               }
            }
            if(spawned)
            {
               Runtime.getRuntime().halt(666);
            }
         }
         else if(killFile.exists())
         {
            try
            {
               manager.getHornetQServer().stop(true);
               manager.stop();
               manager = null;
               server = null;
               main.stop();
               main = null;
               namingBean.stop();
               namingBean = null;
               killFile.delete();
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
         }
         else if(restartFile.exists())
         {
            try
            {
               if(useJndi)
               {
                  createNamingServer();
               }
               createServer(configuration);
               restartFile.delete();
            }
            catch (Exception e)
            {
               e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
         }
      }
   }
}
