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
import java.io.InputStream;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;

/**
 * This command will execute a forkbomb on a host. The forkbomb itself is forked 
 * to the background so the command is not running while the forkbomb propagates.
 * 
 * Essentially, the following is executed in a bash shell on the host:
 * :(){ :|: & };:& 
 * 
 * This consumes all of the process IDs on the host and if no ulimit is set, it 
 * will continue forever which will slow the system to a crawl.  You can check
 * to see if ulimits are set by typing "ulimit -a" in a bash shell on the host.
 * 
 *
 */
public class ForkBombCommand extends ApeCommand
{
	private Option option;

	/**
	 * The constructor for this command simply creates its Option object (used by
	 * the CLI parser)
	 */
	public ForkBombCommand()
	{
		option = OptionBuilder
		.withValueSeparator()
		.withDescription("Hangs a host by executing a fork bomb")
		.withLongOpt("forkbomb")
		.create("F");
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
		// The explicit bash command that is executed
		String cmd = ":(){ :|: & };:&";
		
		ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
		pb.redirectErrorStream(true);
		Process sh = pb.start();
		InputStream shIn = sh.getInputStream();
		try {
			if(sh.waitFor()!=0)
			{
				System.err.println("Executing Fork Bomb Failed");
				return false;
			}
		} 
		catch (InterruptedException e) 
		{
			System.out.println("The fork command received an Interrupt.");
			e.printStackTrace();
			return false;
		}
		int c;
		while((c = shIn.read()) != -1)
		{
			System.out.write(c);
		}
		try {
			shIn.close();
		}
		catch (IOException e)
		{
			System.out.println("Could not close InputStream from the forkbomb process.");
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
