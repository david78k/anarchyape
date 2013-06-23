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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;

/**
 * This command will disconnect the host's network interface for a duration specified.
 * 
 * The ifdown command is invoked by this class.
 * 
 *
 */
public class NetworkDisconnectCommand extends ApeCommand
{
	private Option option;
	
	/**
	 * The constructor for this command simply creates its Option object (used by
	 * the CLI parser)
	 */
	public NetworkDisconnectCommand()
	{
		option = OptionBuilder.withArgName("time").hasArgs(1)
		.withValueSeparator()
        .withDescription("Disconnect the network for a certain period of time specified in the argument, and then resumes")
        .withLongOpt("network-disconnect")
        .create("d");
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
		String argument = null;
		argument = args[0];
		
		double time = Double.parseDouble(argument);
		
		if(time<=0)
		{
			System.out.println("ERROR: The duration of the disconnect must be positive, got " + time);
			Main.logger.info("ERROR: The duration of the disconnect must be positive, got " + time);
			return false;
		}

		if(!executecommand(time))
		{
			System.out.println("ERROR: Simulating network failure unsuccessful.");
			Main.logger.info("ERROR: Simulating network failure unsuccessful.");
			return false;
		}
		
		return true;
	}

	/**
	 * This method actually executes the command that would disconnect the network
	 */
	private boolean executecommand(double time) throws IOException
	{
		String cmd = "ifdown eth0 && sleep " + time + " && /etc/init.d/network restart";
		ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
		pb.redirectErrorStream(true);
		Process p =  null;

		try {
			p = pb.start();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
    	 	return false;
		}
		
		try {
			if(p.waitFor()!=0)
			{
				System.out.println("ERROR: Non-zero return code (" + p.exitValue() + ") when executing: '" + cmd + "'");
				Main.logger.info("ERROR: Non-zero return code (" + p.exitValue() + ") when executing: '" + cmd + "'");

				ProcessBuilder tmp2 = new ProcessBuilder("bash", "-c", "/etc/init.d/network restart");
				Process ptmp = tmp2.start();
				try {
					if(ptmp.waitFor()==0)
						System.out.println("Connection resumed");
					else 
					{
						System.out.println("Connection resume failed");
						return false;
					}
				} catch (InterruptedException e1) {
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
			ProcessBuilder tmp2 = new ProcessBuilder("bash", "-c", "/etc/init.d/network restart");
			Process ptmp = tmp2.start();
			try {
				if(ptmp.waitFor()==0)
					System.out.println("Connection Resumed");
				else 
				{
					System.out.println("Connection Resumed Failed");
					return false;
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				System.err.println("Catches an exception when trying to recover the network");
				return false;

			}
			e.printStackTrace();
			return false;
		}
		
		return true;
	}


	/**
	 * This method writes to the Stand output
	 */
	private boolean writeSTDOut(Process p)
	{
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        int count;
        CharBuffer cbuf = CharBuffer.allocate(99999);
        try {
			count = stdInput.read(cbuf);
			if(count!=-1)
				cbuf.array()[count] = '\0';
			else if(cbuf.array()[0]!='\0')
				count  = cbuf.array().length;
			else count = 0;
			for(int i=0; i<count;i++)
				System.out.print(cbuf.get(i));
		} catch (IOException e) {
			System.err.println("Writing Stdout in NetworkDisconnectCommand catches an exception, Turn on the VERBOSE flag to see the stack trace");
			e.printStackTrace();
			return false;
		}
		
		try {
			stdInput.close();
		} catch (IOException e) {
			System.err.println("Unable to close the IOStream");
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
