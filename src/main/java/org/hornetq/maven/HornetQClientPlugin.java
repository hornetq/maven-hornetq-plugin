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

import java.lang.reflect.Method;
import java.util.Properties;

/**
 * @author <a href="mailto:andy.taylor@jboss.com">Andy Taylor</a>
 *         Date: 8/18/11
 *         Time: 2:36 PM
 *
 *  Allows a Java Client to be run which must hve a static main(String[] args) method
 *
 * @phase verify
 *
 * @goal runClient
 */
public class HornetQClientPlugin extends AbstractMojo
{

  /**
   * @parameter
   */
   String clientClass;

  /**
   * args for test class.
   * @parameter
   */
   String[] args;

   /**
     * My Properties.
     *
     * @parameter
     */
    private Properties systemProperties;

   public void execute() throws MojoExecutionException, MojoFailureException
   {
      try
      {
         if(systemProperties != null && !systemProperties.isEmpty())
         {
            System.getProperties().putAll(systemProperties);
         }
         Class aClass = Class.forName(clientClass);
         Method method = aClass.getDeclaredMethod("main", new Class[]{String[].class});
         method.invoke(null, new Object[]{args});
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new MojoFailureException(e.getMessage());
      }
   }
}
