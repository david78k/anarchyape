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
 * This command forces a kernel panic using the sysrq module available in Linux 
 * systems.  You can check to see if sysrq is enabled by typing the following 
 * into a bash shell on the host in question:
 * 
 * cat /proc/sys/kernel/sysrq
 * 
 * If 1 is returned then the sysrq module is enabled.  If it returns 0 then you 
 * can enable it by typing the following:
 * 
 * echo 1 > /proc/sys/kernel/sysrq
 * 
 * The KernelPanicCommand works by echoing a character to the sysrq trigger which
 * will execute a particular command (see http://en.wikipedia.org/wiki/Magic_SysRq_key)
 * for a full list of what the sysrq trigger can do.
 * 
 *
 */
public class KernelPanicCommand extends ApeCommand
{
	private Option option;
	
	/**
	 * The constructor for this command simply creates its Option object (used by
	 * the CLI parser)
	 */
	public KernelPanicCommand(){
		option = OptionBuilder
		.withValueSeparator()
        .withDescription("Forces a kernel panic and does not restart the system.")
        .withLongOpt("panic")
        .create("P");
	}
	
	public String getName()
	{
		return option.getLongOpt();
	}
	
	public boolean runImpl(String [] args) throws ParseException, IOException 
	{
		String cmd = "echo c > /proc/sysrq-trigger";
		ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
		pb.redirectErrorStream(true);
		Process p =  null;

		try {
			p = pb.start();
		} catch (IOException e) {
			System.err.println("Executing kernel panic catches IOException");
			e.printStackTrace();
	 		return false;
		}
		
		// If the process returns a non-zero value, there is some error executing the command
		try {
			if(p.waitFor()!=0)
			{
				System.err.println("Non-zero return code (" + p.exitValue() + ") when executing: '" + cmd + "'");
    	 		return false;
			}
		} 
		catch (InterruptedException e) 
		{
			System.err.println("Executing '" + cmd + "' was interrupted");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	/**
	 * Get the option of KernelPanicCommand
	 */
	public Option getOption() {
		return option;
	}
}
