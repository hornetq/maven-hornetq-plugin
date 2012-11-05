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
package org.hornetq.maven;

import java.util.HashMap;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class PluginUtil
{
   public static TestClusterManagerMBean getTestClusterManager()
   {
      final String JMX_URL = "service:jmx:rmi:///jndi/rmi://localhost:3000/jmxrmi";
      try
      {
         JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(JMX_URL), new HashMap<String, String>());
         ObjectName name = ObjectName.getInstance("hornetq:module=test,type=TestClusterManager");
         MBeanServerConnection mbsc = connector.getMBeanServerConnection();
         TestClusterManagerMBean clusterControl = MBeanServerInvocationHandler.newProxyInstance(mbsc,
                                                                                         name,
                                                                                         TestClusterManagerMBean.class,
                                                                                         false);
         clusterControl.getNumNodes();//serves as a validation.
         return clusterControl;
      }
      catch (Exception e)
      {
         return null;
      }
   }

}
