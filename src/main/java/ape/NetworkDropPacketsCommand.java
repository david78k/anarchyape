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
 * This command will drop a specified percentage of packets for a specified duration.
 * All inbound and outbound packets are affected by this.  This is achieved using
 * the netem (network emulation) module.
 * 
 *
 */
public class NetworkDropPacketsCommand extends ApeCommand
{
	private Option option;
	
	/**
	 * The constructor for this command simply creates its Option object (used by
	 * the CLI parser)
	 */
	public NetworkDropPacketsCommand()
	{
		option = OptionBuilder
		.withArgName("percentage> <duration")
		.hasArgs(2)
		.withValueSeparator()
        .withDescription("Drops a specified percentage of all inbound network packets for a duration specified in seconds.")
        .withLongOpt("network-drop")
        .create("p");
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
		
		double percent = Double.parseDouble(arg1);
		double period = Double.parseDouble(arg2);

		
		if(percent<=0||period <=0)
		{
			System.err.println("Argument Not Positive");
			return false;
		}

		if(!executecommand(percent, period))
		{
			System.err.println("Simulating Network Dropping Packets unsuccessful, turn on VERBOSE flag to check");
			return false;
		}
		
		return true;
	}

	/**
	 * This method implement the event by using the netem module
	 * @param percent The percentage of packets that are to be dropped
	 * @param period The duration that the packet loss should last
	 * @return True if the execution was successful, false if an error occurred 
	 * @throws IOException
	 */
	private boolean executecommand(double percent, double period) throws IOException
	{
		String cmd = "tc qdisc add dev eth0 root netem loss " + percent + "% && sleep " + period + " && tc qdisc del dev eth0 root netem";
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
			if(p.waitFor()!=0)
			{
				System.err.println("Non-zero return code (" + p.exitValue() + ") when executing: '" + cmd + "'");

				ProcessBuilder tmp2 = new ProcessBuilder("bash", "-c", "tc qdisc del dev eth0 root netem");
				Process ptmp = tmp2.start();
				if(ptmp.waitFor()==0)
					System.out.println("Connection resumed");
				else 
				{
					System.out.println("Connection resumed failed");
					return false;
				}
				
				return false;
			}
		} 
		catch (InterruptedException e1) 
		{
			e1.printStackTrace();
			return false;
		}
		
		return true;
	}
}
