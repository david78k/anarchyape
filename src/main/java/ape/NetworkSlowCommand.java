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

import java.io.IOException;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;

/**
 * This command will delay all network traffic by a time specified in milliseconds for
 * a duration specified in seconds.  This is acheived using the netem (network emulation)
 * module.
 * 
 *
 */
public class NetworkSlowCommand extends ApeCommand{

	
	private Option option;
	
	/**
	 * The constructor for this command simply creates its Option object (used by
	 * the CLI parser)
	 */
	public NetworkSlowCommand()
	{
		option = OptionBuilder
		.withArgName("delay> <duration")
		.hasArgs(2)
		.withValueSeparator()
        .withDescription("Delay all network packet delivery by a specified amount of time (in milliseconds) for a period specified in seconds")
        .withLongOpt("network-slow")
        .create("S");
	}

	public String getName()
	{
		return option.getLongOpt();
	}
	
	public Option getOption() 
	{
		return option;
	}
	
	public boolean runImpl(String [] args) throws ParseException, IOException 
	{
		String arg1,arg2 = null;
		arg1 = args[0];
		arg2 = args[1];
		
		double time = Double.parseDouble(arg1);
		double period = Double.parseDouble(arg2);
		
		if(time<=0||period <=0)
		{
			System.err.println("Argument Not Positive");
			return false;
		}

		if(!executecommand(time, period))
		{
			System.err.println("Simulating Network Delay unsuccessful, turn on VERBOSE flag to check");
			return false;
		}
		
		return true;
	}
	
	/**
	 * This method implements the event
	 * @param time The amount of time to delay all network traffic in milliseconds
	 * @param period How long the delay should last in seconds
	 * @return True if successful execution, false if an error occurred
	 * @throws IOException
	 */
	private boolean executecommand(double time, double period) throws IOException
	{
		String cmd = "tc qdisc add dev eth0 root netem delay " + time + "ms && sleep "+period+" && tc qdisc del dev eth0 root netem";
		ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
		pb.redirectErrorStream(true);
		Process p =  null;

		try {
			p = pb.start();
		} 
		catch (IOException e) 
		{
			System.err.println("Executing network connection simulation catches IOException, enter VERBOSE mode to see the Stack Trace");
			e.printStackTrace();
   	 		return false;
		}
		
		try {
			int retVal = p.waitFor();
			System.out.println("The return value for '" + cmd + "' was " + retVal);
			if(retVal != 0)
			{
				System.err.println("Non-zero return code (" + p.exitValue() + ") when executing: '" + cmd + "'");

				ProcessBuilder tmp2 = new ProcessBuilder("bash", "-c", "tc qdisc del dev eth0 root netem");
				Process ptmp = tmp2.start();
				try {
					if(ptmp.waitFor()==0)
						System.out.println("Connection Resumed");
					else 
					{
						System.out.println("Connection Resumed Failed");
						return false;
					}
				} 
				catch (InterruptedException e1) 
				{
					e1.printStackTrace();
					System.err.println("Catches an exception when trying to recover the network");
					return false;
				}			
				
				return false;
			
			}
		} 
		catch (InterruptedException e) 
		{
			System.err.println("Executing Command catches an Interrupt, resume connection");
			ProcessBuilder tmp2 = new ProcessBuilder("bash", "-c", "tc qdisc del dev eth0 root netem");
			Process ptmp = tmp2.start();
			try {
				if(ptmp.waitFor()==0)
					System.out.println("Connection Resumed");
				else 
				{
					System.out.println("Connection Resumed Failed");
					return false;
				}
			} 
			catch (InterruptedException e1) 
			{
				e1.printStackTrace();
				System.err.println("Catches an exception when trying to recover the network");
					e.printStackTrace();
				return false;
			}
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
}
