package org.hornetq.maven;

/**
 * @author <a href="mailto:andy.taylor@jboss.com">Andy Taylor</a>
 *         Date: 8/18/11
 *         Time: 2:36 PM
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal runClient
 */
public class HornetQClientPlugin extends AbstractMojo
{

    /**
     * @parameter
     */
    String clientClass;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        try
        {
            Object o = Class.forName(clientClass).newInstance();
            HornetQClient client = (HornetQClient) o;
            ((HornetQClient) o).run();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage());
        }
    }
}
