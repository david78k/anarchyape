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
import java.util.Iterator;
import java.util.ServiceLoader;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

/**
 * This is the abstract class that every Ape command must extend.
 * 
 *
 */
public abstract class ApeCommand 
{
	private static ServiceLoader<ApeCommand> loader = ServiceLoader.load(ApeCommand.class);
	public abstract String getName();
	protected abstract boolean runImpl(String [] args) throws ParseException, IOException;
	public abstract Option getOption();
	
	public boolean exec(String [] args) throws ParseException, IOException 
	{
	    return runImpl(args);
	}
	
	/**
	 * This method uses the iterator inside the ServiceLoader class 
	 * to get all the options that represents each implementation of ApeCommand
	 */
	public static ApeCommand getCommand(String cmdname) 
	{
	    String name = cmdname.toLowerCase();
	    
	    Iterator<ApeCommand> iter = loader.iterator();
	    while(iter.hasNext()) 
	    {
	    	ApeCommand ac = iter.next();
	    	if(name.equals(ac.getName().toLowerCase())) 
	    	{
	    		return ac;
	    	}
	    }
	    return null;
	}
}
