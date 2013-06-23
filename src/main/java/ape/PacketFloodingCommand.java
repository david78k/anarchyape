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
 * This command will flood a host with packets on the specified port for a specified duration.
 * 
 * This is done using the DenialOfServiceRunner class.
 * 
 *
 */
public class PacketFloodingCommand extends ApeCommand
{
	private Option option;
	
	/**
	 * The constructor for this command simply creates its Option object (used by
	 * the CLI parser)
	 */
	public PacketFloodingCommand(){
		option = OptionBuilder
		.withArgName("hostname> <port> <duration")
		.hasArgs(3)
		.withValueSeparator()
        .withDescription("Flood the target hostname with a DoS attack.  For proper effect, use the -R flag and designate more than one host.")
        .withLongOpt("udp-flood")
        .create("u");
	}

	public String getName()
	{
		return option.getLongOpt();
	}
	
	
	public Option getOption() 
	{
		return option;
	}

	public boolean runImpl(String[] args) throws ParseException, IOException 
	{
		// Fork off 4 threads of this DoS
		//TODO: Make this a configuration setting
		for(int i = 0; i < 4; i++)
		{
			new Thread(new DenialOfServiceRunner(args[0], args[1], args[2])).start();
		}
		return true;
	}
}
