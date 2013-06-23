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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**
 * This command touches a file on a given host.  It is used to test if the communication
 * protocol is working properly.  For instance, you can run:
 * 
 * ape -R hostname -t
 * 
 * The above command would touch a file called /tmp/foo.tst.  If this command fails then there is
 * some issue with the PDSHCommunication class.
 * 
 *
 */
public class TouchCommand extends ApeCommand 
{
	private Option option;

	@Override
	public String getName()
	{
		return option.getLongOpt();
	}
	
	/**
	 * The constructor for this command simply creates its Option object (used by
	 * the CLI parser)
	 */
	public TouchCommand(){
		option = OptionBuilder
		.withValueSeparator()
		.withDescription("Touches a file called /tmp/foo.tst")
		.withLongOpt("touch")
		.create("t");
	}
	
	public Option getOption() 
	{
		return option;
	}
	
	public boolean runImpl(String [] args)  
	{
		System.out.println("Going to touch /tmp/foo.tst");

		Runtime rt = Runtime.getRuntime();
		Process p;
		try {
			p = rt.exec("touch /tmp/foo.tst");
			p.waitFor();
			p = rt.exec("ls /tmp");	
			p.waitFor();
			return writeSTDOut(p);
		} 
		catch (IOException e) 
		{
			System.out.println("IOException caught in executing command.");
			e.printStackTrace();
	 		return false;
		}
		catch (InterruptedException e) {
			System.out.println("The process for the 'ls /tmp' command was interrupted.");
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean writeSTDOut(Process p)
	{
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String count;
        try {
			while ((count = stdInput.readLine()) != null) 
			{
			    System.out.println(count);
			}
			stdInput.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
