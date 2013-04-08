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

import java.io.File;
import java.io.IOException;

public class TestNode
{
   String nodeId;
   String workingDir;
   String configDir;
   int jndiPort;
   int jndiRmiPort;

   public TestNode(String nodeId, String workingDir,
         String configDir, int jndiPort, int jndiRmiPort)
   {
      this.nodeId = nodeId;
      this.workingDir = workingDir;
      this.configDir = configDir;
      this.jndiPort = jndiPort;
      this.jndiRmiPort = jndiRmiPort;
   }

   public void kill() throws IOException
   {
      File file = new File(configDir, "KILL_ME");
      file.createNewFile();
      try
      {
         Thread.sleep(3000);
      }
      catch (InterruptedException e)
      {
      }
   }

}
