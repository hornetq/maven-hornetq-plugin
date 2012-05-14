package org.hornetq.maven;

/**
 * @author <a href="mailto:andy.taylor@jboss.com">Andy Taylor</a>
 *         Date: 8/18/11
 *         Time: 2:36 PM
 *
 *  Allows a Java Client to be run which must hve a static main(String[] args) method
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.lang.reflect.Method;

/**
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

   public void execute() throws MojoExecutionException, MojoFailureException
   {
      try
      {
         Class aClass = Class.forName(clientClass);
         Method method = aClass.getDeclaredMethod("main", new Class[]{String[].class});
         method.invoke(null, new Object[]{args});
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new MojoExecutionException(e.getMessage());
      }
   }
}
