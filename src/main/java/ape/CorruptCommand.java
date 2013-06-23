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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;

/**
 * This command will corrupt a random HDFS block file with a given corruption size
 * (in bytes) and a given offset.  It can corrupt metadata or regular data.  
 * It will overwrite the given section with 00000011 or "3".
 * 
 * Note: This class relies upon the HADOOP_HOME environment variable being set.
 * 
 *
 */
public class CorruptCommand extends ApeCommand
{
	public int size;
	public int offset; 
	private Option option;
	private String datatype = null;
	
	/**
	 * The constructor for this command simply creates its Option object (used by
	 * the CLI parser)
	 */
	public CorruptCommand()
	{
		option = OptionBuilder
		.withArgName("meta/ord> <size> <offset")
		.hasArgs(3)
		.withValueSeparator()
		.withDescription("Corrupt a random HDFS block file with a size in bytes as the 2nd arg and offset in bytes as the 3rd argument")
		.withLongOpt("corrupt-block")
		.create("C");
	}

	/**
	 * @return This instance method returns the name of the event this class supports
	 */
	public String getName()
	{
		return option.getLongOpt();
	}

	/**
	 * @return This method returns the Option object that this class represents
	 */
	public Option getOption() 
	{
	    return option;
	}

	/**
	 * This method overrides the abstract function declared in the ApeCommand class 
	 * which actual implements the event
	 */
	@Override
	protected boolean runImpl(String[] args) throws ParseException, IOException
	{
		datatype = args[0];
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
			System.out.println("VERBOSE: Data type is " + args[0]);
			System.out.println("VERBOSE: Size of corruption is " + size);
			System.out.println("VERBOSE: Offset is " + offset);
		}
		
		if(!corrupt(null))
		{
			System.out.println("Corrupting block file unsuccessful");
			return false;
		}
		
		return true;
	}
	
	/**
	 * This method is the implementation of the corrupt function.
	 * Given an address, it corrupts the file in the given address
	 */
	public boolean corrupt(String corruptAddress) throws IOException  
	{
		// Trying to get a random HDFS block file
		if(Main.VERBOSE)
		{
			System.out.println("Trying to get a random HDFS block file");
		}
		if(corruptAddress==null)
		{
			corruptAddress = getCorruptAddress();
		}
		
		// If the above statement failed to set corruptAddress then there was a failure
		if(corruptAddress==null)
		{
			System.out.println("Could not get a random HDFS block file");
			Main.logger.info("Could not get a random HDFS block file");
			return false;
		}
		
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
        	System.out.println("Corrupting file failed");
        	Main.logger.info("Corrupting file failed");
        	e.printStackTrace();
        	Main.logger.info(e);
        	return false;
		}
		
        RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(corruptAddress, "rw");
			raf.seek(offset);
        	raf.write(buf, 0, count);
        	raf.seek(0);
        	raf.close();
	        
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
		catch (IOException e) 
		{
			System.out.println("Corrupting file failed");
			Main.logger.info("Corrupting file failed");
			e.printStackTrace();
			Main.logger.info(e);
			return false;
		}
	}


	
	/**
	 * This method is used to fetch the hdfs config file
	 * and then fetch the address of where hdfs blk files
	 * are stored from the config file
	 * finally, it returns that a random hdfs blk in that address
	 */
	private String getCorruptAddress() throws IOException 
	{
		String cmd = "cat $HADOOP_HOME/conf/hdfs-site.xml | grep -A1 'dfs.data.dir' | grep 'value'";
		System.out.println(cmd);
		ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
		pb.redirectErrorStream(true);
		Process sh =  null;
		try {
			
			sh = pb.start();
			if(sh.waitFor()!=0)
			{
				System.out.println("Executing '" + cmd + "' returned a nonzero exit code.");
				System.out.println("Unable to find HDFS block files");
				Main.logger.info("Executing '" + cmd + "' returned a nonzero exit code.");
				Main.logger.info("Unable to find HDFS block files");
				return null;
			}
		} 
		catch (IOException e) {
			System.out.println("Failed to acquire block address");
			Main.logger.info("Failed to acquire block address");
			e.printStackTrace();
			Main.logger.info(e);
			return null;
			
		}
		catch (InterruptedException e) {
			System.out.println("Caught an Interrupt while runnning");
			Main.logger.info("Caught an Interrupt while runnning");
			e.printStackTrace();
			Main.logger.info(e);
			return null;
		}
		
		InputStream shIn = sh.getInputStream();
		InputStreamReader isr = new InputStreamReader(shIn);
		BufferedReader br = new BufferedReader(isr);
		String line = null;
	    try {
			line= br.readLine();
		}
	    catch (IOException e) 
	    {
			e.printStackTrace();
			return null;
		}
	    
		br.close();
		isr.close();
		line = line.trim();
		
		int end = line.indexOf("</value>");
		
		//parse the String and randomly select an address
		String address = line.substring(7, end);

		ArrayList<String> addresses = new ArrayList<String>();

		int idx;
		
		while( (idx=address.indexOf(',')) != -1)
		{
			addresses.add(address.substring(0,idx));
			address = address.substring(idx + 1);
		}
		addresses.add(address);
		
		int index = new Random().nextInt(addresses.size());
		
		address = addresses.get(index).concat("/current");
		
		if(Main.VERBOSE)
		{
			System.out.println("The address of the HDFS data folder is: " + address);
		}
		Main.logger.info("The address of the HDFS data folder is: " + address);
				
		if(datatype.equalsIgnoreCase("meta"))
		{
			cmd = "ls "+address+" | grep -i 'blk' |grep -i 'meta' ";
		}
		else { 
			cmd = "ls "+address+" | grep -i 'blk' | grep -v 'meta' ";
		}

		pb = new ProcessBuilder("bash", "-c", cmd);
		pb.redirectErrorStream(true);

		sh=pb.start();

		try {
			if(sh.waitFor()!=0)
			{
				System.out.println("Getting address of the list of files failed");
				return null;
			}
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
			return null;
		}
		
		shIn = sh.getInputStream();

		isr = new InputStreamReader(shIn);
		br = new BufferedReader(isr);
		
		ArrayList<String> data = new ArrayList<String>(); 
		
		while((line = br.readLine())!=null)
		{
			data.add(line);
		}
		
		int length = data.size();
		Random rdm = new Random();
		
		int random = rdm.nextInt(length);
		
		address = address.concat("/"+data.get(random));
		
		if(Main.VERBOSE)
		{
			System.out.println("The location of the data corrupted is " + address);
		}
		
		// Log the corrupted block
		Main.logger.info("The location of the data corrupted is " + address);
		
		br.close();
		isr.close();
		shIn.close();
		
		return address;
	}
}
