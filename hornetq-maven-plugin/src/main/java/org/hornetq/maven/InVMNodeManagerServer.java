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

import org.hornetq.core.config.Configuration;
import org.hornetq.core.server.NodeManager;
import org.hornetq.core.server.impl.HornetQServerImpl;
import org.hornetq.spi.core.security.HornetQSecurityManager;

import javax.management.MBeanServer;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 */
public class InVMNodeManagerServer extends HornetQServerImpl
{
  final NodeManager nodeManager;
  public InVMNodeManagerServer(NodeManager nodeManager)
  {
     super();
     this.nodeManager = nodeManager;
  }

  public InVMNodeManagerServer(Configuration configuration, NodeManager nodeManager)
  {
     super(configuration);
     this.nodeManager = nodeManager;
  }

  public InVMNodeManagerServer(Configuration configuration, MBeanServer mbeanServer, NodeManager nodeManager)
  {
     super(configuration, mbeanServer);
     this.nodeManager = nodeManager;
  }

  public InVMNodeManagerServer(Configuration configuration, HornetQSecurityManager securityManager, NodeManager nodeManager)
  {
     super(configuration, securityManager);
     this.nodeManager = nodeManager;
  }

  public InVMNodeManagerServer(Configuration configuration, MBeanServer mbeanServer, HornetQSecurityManager securityManager, NodeManager nodeManager)
  {
     super(configuration, mbeanServer, securityManager);
     this.nodeManager = nodeManager;
  }

  @Override
   protected NodeManager createNodeManager(String directory, String nodeGroupName, boolean replicatingBackup)
  {
     return nodeManager;
  }
}