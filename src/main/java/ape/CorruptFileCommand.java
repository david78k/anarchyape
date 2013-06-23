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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
	
/**
 * This command will corrupt a given file with a given corruption size
 * (in bytes) and a given offset.  It will overwrite the given section 
 * with 00000011 (base 2) or 3 (base 10).
 * 
 *
 */
public class CorruptFileCommand extends ApeCommand
{
	public int offset;
	public int size;
	private Option option;
	
	/**
	 * The constructor for this command simply creates its Option object (used by
	 * the CLI parser)
	 */
	public CorruptFileCommand()
	{
		option = OptionBuilder
		.withArgName("file> <size> <offset")
		.hasArgs(3)
		.withValueSeparator()
		.withDescription("Corrupt the file given the address as the first argument, size as the 2nd arg, and offset as the 3rd argument")
		.withLongOpt("corrupt-file")
		.create("c");
	}

	public String getName()
	{
		return option.getLongOpt();
	}
	
	public Option getOption() 
	{
	    return option;
	}
	
	/**
	 * This method is the actual method used to corrupt data/file
	 */
	public boolean corrupt(String corruptAddress) throws IOException  
	{
	    FileInputStream fin;
	    byte[] buf;
	    int count;
	    
		try {
				
			RandomAccessFile tmp = new RandomAccessFile(corruptAddress, "rw");
			tmp.seek(offset);
			 
		    if(size<=0)
		    {
		    	System.out.println("ERROR: The size parameter must be positive");
		    	Main.logger.info("ERROR: The size parameter must be positive");
		    	return false;
		    }
		     
		    buf = new byte[size];

		    count = 0;
        	if((count=tmp.read(buf, 0, size))==-1)
			{
        		System.out.println("The file chosen is smaller than the corruption size (" + size + " bytes)");
				Main.logger.info("The file chosen is smaller than the corruption size (" + size + " bytes)");
				return false;
			}
	       
	        for(int i=0; i<count;i++)
	        {
	        	buf[i]=0x3;
	        }
	         
	        tmp.seek(0);
	        tmp.close();
		} 
		catch (FileNotFoundException e1) 
		{
			System.out.println("Cannot open the file on the path given");
			Main.logger.info("Cannot open the file on the path given");
			e1.printStackTrace();
			Main.logger.info(e1);
	 		return false;
		}
		catch (IOException e) 
	    {
	        e.printStackTrace();
			return false;
	    }
        
        RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(corruptAddress, "rw");
	        try {
	        	raf.seek(offset);
	        	 
	        	raf.write(buf, 0, count);
	        	raf.seek(0);
	        	raf.close();
			} 
	        catch (IOException e) 
	        {
	        	System.out.println("Corrupting file failed");
				Main.logger.info("Corrupting file failed");
				e.printStackTrace();
				Main.logger.info(e);
				return false;
			}
			
			return true;
		} 
		catch (FileNotFoundException e1) 
		{
			System.out.println("Cannot open the file on the path: " + corruptAddress);
			Main.logger.info("Cannot open the file on the path: " + corruptAddress);
			e1.printStackTrace();
			Main.logger.info(e1);
	 		return false;
		}
	}

	@Override
	protected boolean runImpl(String[] args) throws ParseException, IOException 
	{
		String corruptAddress = null;
		corruptAddress = args[0];
		try {
			size = Integer.parseInt(args[1]);
			offset = Integer.parseInt(args[2]);
		}
		catch(NumberFormatException t)
		{
			System.out.println("Unable to parse the size or offset given as an integer.");
			Main.logger.info("Unable to parse the size or offset given as an integer.");
			t.printStackTrace();
			Main.logger.info(t);
			return false;
		}
		if(Main.VERBOSE)
		{
			System.out.println("address is " + args[0]);
			System.out.println("SIZE is " + size);
			System.out.println("OFFSET is " + offset);
		}
		Main.logger.info("File location is: " + args[0]);
		Main.logger.info("Corruption size is: " + size);
		Main.logger.info("Corruption offset is: " + offset);
		
		if(!corrupt(corruptAddress))
		{
			System.err.println("Error: Corrupting failed.");
			Main.logger.info("Error: Corrupting failed.");
			return false;
		}
		
		return true;
	}
}

