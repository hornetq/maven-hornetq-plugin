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
         while(time + 5000 > System.currentTimeMillis())
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
