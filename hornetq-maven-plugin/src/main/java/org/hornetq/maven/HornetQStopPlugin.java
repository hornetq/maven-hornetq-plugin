/*
 * Copyright 2009 Red Hat, Inc.
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.hornetq.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:andy.taylor@jboss.com">Andy Taylor</a>
 *         Date: 8/18/11
 *         Time: 12:25 PM
 */

/**
 * @phase verify
 * @goal stop
 */
public class HornetQStopPlugin extends AbstractMojo
{

   /**
    * @parameter
    */
   private String hornetqConfigurationDir;

   public void execute() throws MojoExecutionException, MojoFailureException
   {
      try
      {
         String dirName = hornetqConfigurationDir != null ? hornetqConfigurationDir : ".";
         final File file = new File(dirName + "/" + "/STOP_ME");
         file.createNewFile();
         long time = System.currentTimeMillis();
         while(System.currentTimeMillis() < time + 60000)
         {
            if(!file.exists())
            {
               break;
            }
            try
            {
               Thread.sleep(200);
            }
            catch (InterruptedException e)
            {
               //ignore
            }
         }
         if(file.exists())
         {
            throw new MojoExecutionException("looks like the server hasn't been stopped");
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
         throw new MojoExecutionException(e.getMessage());
      }
   }
}
