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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.maven.artifact.DefaultArtifact;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 * @author <a href="mailto:csuconic@redhat.com">Clebert Suconic</a>
 * @version <tt>$Revision$</tt>
 */
public class SpawnedVMSupport
{
   // Constants -----------------------------------------------------


   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------


   public static Process spawnVM(List<DefaultArtifact> arts,
                                 final String logName,
                                 final String className,
                                 final Properties properties,
                                 final boolean logOutput,
                                 final String success,
                                 final String failure,
                                 final String workDir,
                                 final String configDir,
                                 boolean debug,
                                 final String... args) throws Exception
   {
      StringBuffer sb = new StringBuffer();

      sb.append("java").append(' ');
      StringBuffer props = new StringBuffer();
      if(properties != null)
      {
         for (Map.Entry<Object, Object> entry : properties.entrySet())
         {
            props.append("-D").append(entry.getKey()).append("=").append(entry.getValue()).append(" ");
         }
      }
      String vmarg = props.toString();
      String osName = System.getProperty("os.name");
      osName = (osName != null) ? osName.toLowerCase() : "";
      boolean isWindows = osName.contains("win");
      if (isWindows)
      {
         vmarg = vmarg.replaceAll("/", "\\\\");
      }
      sb.append(vmarg).append(" ");
      String pathSeparater = System.getProperty("path.separator");
      StringBuilder classpath = new StringBuilder();
      for (DefaultArtifact artifact : arts)
      {
         classpath.append(artifact.getFile().getAbsolutePath()).append(pathSeparater);
      }
      classpath.append(configDir).append(pathSeparater);

      if (isWindows)
      {
         sb.append("-cp").append(" \"").append(classpath.toString()).append("\" ");
      }
      else
      {
         sb.append("-cp").append(" ").append(classpath.toString()).append(" ");
      }

      // FIXME - not good to assume path separator
      String libPath = "-Djava.library.path=" + System.getProperty("java.library.path", "./native/bin");
      if (isWindows)
      {
         libPath = libPath.replaceAll("/", "\\\\");
         libPath = "\"" + libPath + "\"";
      }
      sb.append("-Djava.library.path=").append(libPath).append(" ");
      if(debug)
      {
         sb.append("-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 ");
      }

      sb.append(className).append(' ');

      for (String arg : args)
      {
         sb.append(arg).append(' ');
      }

      String commandLine = sb.toString();

      //SpawnedVMSupport.log.trace("command line: " + commandLine);

      Process process = Runtime.getRuntime().exec(commandLine, null, new File(workDir));

      //SpawnedVMSupport.log.trace("process: " + process);

      CountDownLatch latch = new CountDownLatch(1);

      ProcessLogger outputLogger = new ProcessLogger(logOutput,
                                                     process.getInputStream(),
                                                     logName,
                                                     false,
                                                     success,
                                                     failure,
                                                     latch);
      outputLogger.start();

      // Adding a reader to System.err, so the VM won't hang on a System.err.println as identified on this forum thread:
      // http://www.jboss.org/index.html?module=bb&op=viewtopic&t=151815
      ProcessLogger errorLogger = new ProcessLogger(true,
                                                    process.getErrorStream(),
                                                    logName,
                                                    true,
                                                    success,
                                                    failure,
                                                    latch);
      errorLogger.start();

      if (!latch.await(60, TimeUnit.SECONDS))
      {
         process.destroy();
         throw new RuntimeException("Timed out waiting for server to start");
      }

      if (outputLogger.failed || errorLogger.failed)
      {
         try
         {
            process.destroy();
         }
         catch (Throwable e)
         {
         }
         throw new RuntimeException("server failed to start");
      }
      return process;
   }

   /**
    * Redirect the input stream to a logger (as debug logs)
    */
   static class ProcessLogger extends Thread
   {
      private final InputStream is;

      private final String logName;

      private final boolean print;

      private final boolean sendToErr;

      private final String success;

      private final String failure;

      private final CountDownLatch latch;

      boolean failed = false;

      ProcessLogger(final boolean print,
                    final InputStream is,
                    final String logName,
                    final boolean sendToErr,
                    final String success,
                    final String failure,
                    final CountDownLatch latch) throws ClassNotFoundException
      {
         this.is = is;
         this.print = print;
         this.logName = logName;
         this.sendToErr = sendToErr;
         this.success = success;
         this.failure = failure;
         this.latch = latch;
         setDaemon(false);
      }

      @Override
      public void run()
      {
         try
         {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null)
            {
               if (line.startsWith(success))
               {
                  failed = false;
                  latch.countDown();
               }
               else if (line.startsWith(failure))
               {
                  failed = true;
                  latch.countDown();
               }
               if (print)
               {
                  if (sendToErr)
                  {
                     System.err.println(logName + " err:" + line);
                  }
                  else
                  {
                     System.out.println(logName + " out:" + line);
                  }
               }
            }
         }
         catch (IOException e)
         {
            // ok, stream closed
         }

      }
   }

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}