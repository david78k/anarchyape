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
import java.util.UUID;

/**
 * This is the communication class used to connect to a remote host if the R flag is specified.
 * 
 *
 */
public class PDSHCommunication extends CommunicationInterface 
{
	private String [] cmdArgs;
	private String [] hosts;
	private String cmd;
	
	/**
	 * The constructor for this class
	 * @param cmd is the command name
	 * @param cmdArgs is an array of arguments for this command
	 * @param hosts is an array of all hosts
	 */
	public PDSHCommunication(String cmd, String [] cmdArgs, String [] hosts)
	{
		super();
		this.cmd = cmd;
		this.hosts = hosts;
		this.cmdArgs  = cmdArgs;
	}
	
	/**
	 * This method implements the PDSH by PDSHing to the remote host and then executes the command locally
	 * 
	 */
	public boolean execute()
	{
		// If there are no arguments, we want to make an object with 0 Strings, so that cmdArgs.length could return 0 for future usage
		if(cmdArgs==null)
			cmdArgs= new String[0];

		// If there is no host name specified, print out the err message and return false
		if(hosts==null)
		{
			System.err.println("No host name found");
			return false;
		}
		
		// PassIn is an array of Strings passed into the shell to run 
		String[] PassIn = passInString(false);
		
		if(Main.VERBOSE)
		{
			for(int i=0;i<PassIn.length;i++)
			{
				System.out.println(PassIn[i]);
			}
		}
		
		try {
			//Concatenate the Strings together using StringBuffer and pipe them into com.sh, and run com.sh
			StringBuffer cmdFromLocalhost = new StringBuffer("");
			for(int k=0;k<PassIn.length;k++)
			{
				cmdFromLocalhost.append(PassIn[k]+" ");
			}

			String uuid = UUID.randomUUID().toString();
			String cmd = "echo \"" + cmdFromLocalhost.toString() + "\" > " + uuid + ".sh && chmod +x " + uuid + ".sh && ./" + uuid + ".sh && rm " + uuid + ".sh"; 
			if(Main.VERBOSE)
			{
				System.out.println(cmd);
			}

			// Created a process to run the commands
			ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
			pb.redirectErrorStream(false);
			Process sh = pb.start(); 
			
			//Catch the return code of the execution
			if(sh.waitFor()==0)
			{
				writeToSTDOut(sh);
			}
			else 
			{
				writeToSTDError(sh);
				return false;
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Write the Standard Output
	 */
	private boolean writeToSTDOut(Process p)
	{
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        int count;
        CharBuffer cbuf = CharBuffer.allocate(99999);
        try {
			count = stdInput.read(cbuf);//readLine()) != null) 
			if(count!=-1)
				cbuf.array()[count] = '\0';
			else if(cbuf.array()[0]!='\0')
				count  = cbuf.array().length;
			else count = 0;
			for(int i=0; i<count;i++)
				System.out.print(cbuf.get(i));
		} 
        catch (IOException e) {
			System.err.println("Writing Stdout in PDSHCommunication catches an exception, Turn on the VERBOSE flag to see the stack trace");
			
			e.printStackTrace();
			return false;
		}
		
		try {
			stdInput.close();
		} 
		catch (IOException e) {
				System.err.println("Unable to close the IOStream");
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	/**
	 * Write the Standard Error Message
	 */
	private boolean writeToSTDError(Process p)
	{
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        int count;
        CharBuffer cbuf = CharBuffer.allocate(99999);
        try {
			count = stdError.read(cbuf);
			if(count!=-1)
				cbuf.array()[count] = '\0';
			else count  = cbuf.array().length;
			for(int i=0; i<count;i++)
				System.out.print(cbuf.array()[i]);
		} 
        catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		try {
			stdError.close();
		} 
		catch (IOException e) {
			System.err.println("Unable to close the IOStream");
			e.printStackTrace();
			return false;
		}

		
        return true;
	}

	/**
	 *  The part of command that deals with the list of hosts	
	 */
	private String[] constructHostString()
	{
		String [] str = new String [4];
		str[0]="pdsh";
		str[1]="-Rssh";
		str[2]="-w";
		
		StringBuilder tmp = new StringBuilder(hosts[0]);
		for(int i=1;i<hosts.length;i++)
			{
				tmp.append(",");
				tmp.append(hosts[i]);
			}
		str[3] = tmp.toString();
		return str;
	}
	
	/**
	 * The part of command that is run on the host machine
	 */
	private String[] constructCommandString()
	{
		String [] str;
		str = new String[3+cmdArgs.length];
		// TODO: Make this not a hard-coded path, I.E. assume ape is already in the path
		// Alternatively, choose a standardized place to put the ape executable like /usr/bin/ape
		str[0] = "'/usr/local/bin/ape";
		str[1] = "-L";
		str[2] = "-"+cmd;
		for(int i=0;i<cmdArgs.length;i++)
		{
			str[i+3] = cmdArgs[i];
		}
		str[str.length-1]=str[str.length-1]+"'";
		return str;
	}

	private String[] passInString(boolean VERBOSE)
	{
		String []HostSet = constructHostString();
		String []CommandSet;
		if(!VERBOSE)
		{
			CommandSet = constructCommandString();
		}
		else 
		{
			CommandSet = new String[1];
			CommandSet[0] = cmd;
		}
		
		String [] rtv = new String[HostSet.length+CommandSet.length];
		for(int i=0;i<HostSet.length;i++)
		{
			rtv[i] = HostSet[i];
		}
		for(int j=0; j<CommandSet.length;j++)
		{
			rtv[j+HostSet.length] = CommandSet[j];
		}
		return rtv;
	}
	
}
