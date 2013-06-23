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


package ape_test;

import ape.*;
import java.io.*;
import junit.framework.TestCase;
import org.apache.commons.cli.*;

/**
 * 
 * 
 *
 */
public class CLITest extends TestCase 
{
	/**
	 * This test just checks to see if the start of the help dialog is printed
	 * Since the help dialog will continue to be updated, it's not worth constantly changing this test
	 */
	public void testHelpDialog()
	{
		PrintStream originalOut = System.out;
		OutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		System.setOut(ps);
		
		String[] theArgs = new String[1];
		theArgs[0] = "-h";
		Main.main(theArgs);
		System.setOut(originalOut);
		assertEquals("usage: ape", (os.toString()).substring(0, 10));
	}
	
	public void testVersionPrint()
	{
		PrintStream originalOut = System.out;
		OutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		System.setOut(ps);
		
		String[] theArgs = new String[1];
		theArgs[0] = "-V";
		Main.main(theArgs);
		assertEquals("ChaosMonkey version: " + Main.getVersion() + "\n", os.toString());
		
		System.setOut(originalOut);
	}
	
	public void testCreateOptions()
	{
		Main.createOptions();
		Options opts = Main.getOptions();
		
		assertNotNull(opts);
	}
	
	public void testHelpOption()
	{
		Main.createOptions();
		Options opts = Main.getOptions();
		
		assertEquals(opts.hasOption("-h"), true);
	}
	
	public void testVersionOption()
	{
		Main.createOptions();
		Options opts = Main.getOptions();
		
		assertEquals(opts.hasOption("-v"), true);
	}
	
	public void testCommandLineGet()
	{
		String[] theArgs = new String[1];
		CommandLine cl = null;
		try {
			cl = Main.getCommand(theArgs);
		}
		catch(ParseException e)
		{
			assertNotNull(null);
		}
		
		assertNull(cl);
	}
	
	public void testMainWithEmptyString()
	{
		PrintStream originalOut = System.out;
		OutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		System.setOut(ps);
		
		String[] arg = new String[1];
		arg[0] = "";
		Main.main(arg);
		System.setOut(originalOut);
		assertNotSame("", os.toString());
	}
	
	public void testMainWithNullArg()
	{
		PrintStream originalOut = System.out;
		OutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		System.setOut(ps);
		
		String[] arg = null;
		Main.main(arg);
		System.setOut(originalOut);
		assertNotSame("", os.toString());
	}
	
	public void testMainWithEmptyArray()
	{
		PrintStream originalOut = System.out;
		OutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		System.setOut(ps);
		
		String[] arg = new String[0];
		Main.main(arg);
		System.setOut(originalOut);
		assertNotSame("", os.toString());
	}
	
	public void testHelpWithVerbose()
	{
		PrintStream originalOut = System.out;
		OutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		System.setOut(ps);
		
		String[] arg = new String[2];
		arg[0] = "h";
		arg[1] = "v";
		Main.main(arg);
		System.setOut(originalOut);
		assertNotSame("", os.toString());
	}
	
	public void testParseWithNull()
	{
		String[] theArgs = null;
		CommandLine blah = null;
		try {
			blah = Main.getCommand(theArgs);
		}
		catch(ParseException e)
		{
			assertNotNull(null);
		}
		assertNull(blah);
	}
	
	public void testParseWithEmpty()
	{
		String[] arg = new String[1];
		arg[0] = null;
		CommandLine blah = null;
		try {
			blah = Main.getCommand(arg);
		}
		catch(ParseException e)
		{
			assertNotNull(null);
		}
		assertNull(blah);
	}
	
	public void testParseWithEmptyString()
	{
		String[] arg = new String[1];
		arg[0] = "";
		CommandLine blah = null;
		try {
			blah = Main.getCommand(arg);
		}
		catch(ParseException e)
		{
			assertNotNull(null);
		}
		assertNotNull(blah);
	}
	
	public void testMissingArgument()
	{
		PrintStream originalOut = System.out;
		OutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);
		System.setOut(ps);
		
		String[] arg = new String[1];
		arg[0] = "R";
		Main.main(arg);
		System.setOut(originalOut);
		assertNotSame("", os.toString());
	}
	
	public void testKill()
	{
		String[] arg = new String[1];
		arg[0] = "-k blah 127.0.0.1";
		CommandLine blah = null;
		try {
			blah = Main.getCommand(arg);
		}
		catch(ParseException e)
		{
			assertNotNull(null);
		}
		assertNotNull(blah);
	}
	
}
