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

/**
 * The abstract class for whichever communication protocol is used to connect to a remote host
 * 
 * 
 */
public abstract class CommunicationInterface extends Thread
{
	protected String hostname;
	protected String []command;

	/**
	 * @param hostnamePI The IP address of the client AnarchyApe want to connect to
	 * @param theCommand The list of commands that AnarchyApe class want to run 
	 */
	public CommunicationInterface(String hostnamePI, String [] theCommand)
	{
		hostname = hostnamePI;
		command = theCommand;
	}
	
	public CommunicationInterface()
	{
		// Null parameter constructor
	}
	
	public abstract boolean execute() throws IOException;
}
