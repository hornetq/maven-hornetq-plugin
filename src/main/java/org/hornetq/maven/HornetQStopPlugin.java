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
            String dirName = hornetqConfigurationDir != null?hornetqConfigurationDir:".";
            final File file = new File(dirName + "/STOP_ME");
            file.createNewFile();
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage());
        }
    }
}
