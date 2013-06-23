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
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceLoader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * This is the main class that is executed when the Ape jar is run.
 * Parameters are passed in over the command line and parsed by the Apache
 * CLI parser.  The commands are loaded into an Options object using
 * a service loader (see src/main/resources/META-INF/services/ape.ApeCommand for
 * a complete list of commands loaded by the service loader).  See this 
 * class' createOptions method to see other Options that are added or 
 * simply run this program with the -h flag to see the help dialog. 
 * 
 * The version number is also stored in this class.  If it is changed,
 * it must also be changed in the pom.xml.
 * 
 * The log4j configuration is also located in this class.
 * 
 *
 */
public class Main 
{
	public static boolean VERBOSE=true;
	private static Options opts;
	private static CommandLine line;
	// If the version is modified here, it must also be modified in pom.xml
	private static double VERSION = 0.2;
	private static int cmdN = -1;
	private static int modeN = -1;
	private static ServiceLoader<ApeCommand> loader = ServiceLoader.load(ApeCommand.class);
	private static int MAX_OPTION_LENGTH;

	public static final Logger logger = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) 
	{
		// Creating the Properties object for log4j
		Properties ppt = new Properties();
		ppt.setProperty("log4j.rootLogger", "INFO, appender1");
		ppt.setProperty("log4j.appender.appender1", "org.apache.log4j.DailyRollingFileAppender");
		ppt.setProperty("log4j.appender.appender1.File", "/var/log/ape.log");
		ppt.setProperty("log4j.appender.appender1.DatePattern", ".yyyy-MM-dd");
		ppt.setProperty("log4j.appender.appender1.layout","org.apache.log4j.PatternLayout");

		// Configuring log4j to use the Properties object created above
		PropertyConfigurator.configure(ppt);
		
		// Log the current date and time
		logger.info("\n---------------------------------\nStarting time:");
		logTime();

		// Initialize all of the Option objects for each command (these are used by the CLI parser)
		createOptions();
		
		// There should be an array of strings passed in as an argument (even if it's empty)
		// If we get null, we exit
		if(args == null)
		{
			System.err.println("Invalid arguments.  main(String[] args) method expected array of strings, got null.");
			logger.info("Invalid arguments.  main(String[] args) method expected array of strings, got null");
			printHelp();
			return;
		}
		
		// If an empty array is passed in, print the help dialog and exit
		if(args.length == 0)
		{
			printHelp();
			return;
		}
		
		// Use the CLI parser to attempt to parse the command into a series of Option objects
		try {
			line = getCommand(args);
		}
		catch(MissingArgumentException e)
		{
			System.out.println("Missing an argument.  Check your syntax.");
			logger.info("Missing an argument.");
			logger.info("Dumping args array:");
			for(int i = 0; i < args.length; i++)
			{
				logger.info(i + ": " + args[i]);
			}
			printHelp();
			return;
		}
		catch(ParseException e)
		{
			System.out.println("Parsing error, see help dialog:");
			logger.info("Parsing error, see help dialog.");
			printHelp();
			return;
		}
		
		// Get the array of options that were parsed from the command line 
		Option[] options = line.getOptions();
		
		if(line.hasOption("v"))
		{
			MAX_OPTION_LENGTH = 3;
		}
		else {
			MAX_OPTION_LENGTH = 2;
		}
		
		if(options==null||options.length> MAX_OPTION_LENGTH||options.length<1)
		{
			System.out.println("Too many options");
			logger.info("Too many options");
			printHelp();
			return;
		}
		
		if(line.hasOption("v"))
		{
			VERBOSE = true;
			logger.info("Executing Ape verbosely.");
			System.out.println("Executing Ape verbosely");
		}
	
		//Find which option is cmd, which is -local/-remote, order might be disturbed
		for(int k=0;k<options.length;k++)
		{
			if(!options[k].getOpt().equals("v"))
			{
				if(options[k].getOpt()=="L"||options[k].getOpt()=="R")
					modeN = k;
				else cmdN = k;
			}
		}
		
		// If the version flag was in the command, print the version and exit
		if(line.hasOption("V"))
		{
			logger.info("Printing out current version: " + VERSION);
			System.out.println("ChaosMonkey version: " + VERSION);
			return;
		}
		
		if(line.hasOption('h') || options.length < 1 || modeN==cmdN || modeN==-1|| cmdN==-1)
		{
			printHelp();
			return;
		}
		
		if(VERBOSE)
		{
			System.out.println("Mode is " + options[modeN].getLongOpt());
		
			if(options[modeN].getOpt()=="R")
			{
				System.out.println("List of Hosts:");
				for(int j=0;j<line.getOptionValues("R").length;j++)
				{
					System.out.println(line.getOptionValues("R")[j]);
				}
			}
				
			System.out.println("Command is " + options[cmdN].getLongOpt());
		
			if(line.getOptionValues(options[cmdN].getOpt())!=null)
			{
				for(int l=0; l < line.getOptionValues(options[cmdN].getOpt()).length; l++)
				System.out.println("Command Argument: " + line.getOptionValues(options[cmdN].getOpt())[l]);
			}
		}
		
		logger.info("Type of Event " + options[cmdN].getLongOpt());

		// Remote command execution
		if(line.hasOption("R"))
		{
			//go to remote
			String []passIn = line.getOptionValues("R");
			logger.info("Executing a command remotely");
			logger.info("hosts: ");
			
			for(int k=0; k< passIn.length;k++)
			{
				logger.info(passIn[k]);
			}
			
			CommunicationInterface r = new PDSHCommunication(options[cmdN].getOpt(), line.getOptionValues(options[cmdN].getOpt()) ,passIn);
			try {
				// If the command executed successfully
				if(r.execute())
				{
					logger.info("End time");
					
					logTime();
					
					System.out.println("Running Remote Command Succeeded");
				}
				// If the command exited with an error
				else 
					{
						System.out.println("Running remote command failed");
						logger.info("Running remote command failed");
					}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				return;
			}
			return;
			
		}
		// Local command execution
		else if(line.hasOption("L"))
		{
			logger.info("Running Locally");

			ApeCommand ac = ApeCommand.getCommand(options[cmdN].getLongOpt());
			if(ac == null) 
			{
				System.out.println(options[cmdN].getLongOpt()+" is not a valid command.");
				System.out.println("This can occur if a new command class is added but an entry is not added in the ApeCommand file.");
				System.out.println("See src/main/resources/META-INF/services/ape.ApeCommand and ensure that the command's class is there.");
				logger.info(options[cmdN].getLongOpt()+" is not a valid command.");
				
				return;
			}
			
			try {
				String [] cmdArgs = line.getOptionValues(options[cmdN].getOpt());
				if(ac.exec(cmdArgs))
				{
					System.out.println("Running local command succeeded");
					logger.info("End time");
					
					logTime();

				}
				else 	
				{
					System.out.println("Running local command failed");
					logger.info("Running local command failed");
				}
				return ;
			}
			catch(ParseException e) 
			{
				if(Main.VERBOSE)
				{
					System.out.println("VERBOSE: A parse exception was thrown.  ");
					System.out.println("VERBOSE: Interpreting this as an invalid number of arguments for a particular flag and printing the help dialog.");
					System.out.println("VERBOSE: Stack trace:");
					e.printStackTrace();
					logger.info("VERBOSE: A parse exception was thrown.  ");
					logger.info("VERBOSE: Interpreting this as an invalid number of arguments for a particular flag and printing the help dialog.");
					logger.info("VERBOSE: Stack trace:");
					logger.info(e);
				}
				System.out.println("Invalid number of arguments.");
				logger.info("Invalid number of arguments");
				printHelp();
			}
			catch(IOException e)
			{
				System.out.println("Running local command failed");
				logger.info("Running local command failed");
				e.printStackTrace();
			}
		}
		// If the local or remote flags were not used then print the help dialog
		else {
			printHelp();
		}
	}
	
	
	/**
	 * This method would parse the array that store all arguments read from StdIn 
	 * and return a CommandLine object
	 */
	public static CommandLine getCommand(String [] args) throws ParseException
	{
		if(args == null || args.length < 1 || args[0] == null)
		{
			printHelp();
			return null;
		}

		CommandLineParser parser = new PosixParser();

		return parser.parse(opts, args);
	}
	
	/**
	 * This method generates all the options, and stores them in opts
	 */
	public static void createOptions() 	
	{
		Options options = new Options();
		options.addOption("h", "help", false, "Displays this help menu");
		options.addOption("V", "version", false, "Displays the version number");
		options.addOption(OptionBuilder.withValueSeparator().withDescription("Turn on verbose mode").withLongOpt("verbose").create("v"));
		
		// Adds all of the commands in the service loader to an OptionGroup so that they are all mutually exclusive
		OptionGroup apeCommands = new OptionGroup();
	    Iterator<ApeCommand> iter = loader.iterator();
	    while(iter.hasNext() ) 
	    {
	    	ApeCommand ac = iter.next();
	    	apeCommands.addOption(ac.getOption());
	    }
	    options.addOptionGroup(apeCommands);
			
	    // Makes the local and remote commands mutually exclusive
		OptionGroup remoteOrLocal = new OptionGroup();
	    remoteOrLocal.addOption(OptionBuilder.withArgName("HostnameList").hasArgs().withValueSeparator().withDescription("Run commands remotely").withLongOpt("remote").create("R"));
	    remoteOrLocal.addOption(OptionBuilder.withArgName("Command").withValueSeparator().withDescription("Run commands locally").withLongOpt("local").create("L"));
	    options.addOptionGroup(remoteOrLocal);
	    
	    opts=options;
	}
	
	
	/**
	 * This method logs the current time into the ape.log file
	 */
	private static void logTime()
	{
		// Get a Date object with the current date and time
		Date d = new Date();
		
		// Log it in a format like so: Aug 11, 2011 11:51:21 AM
		logger.info(DateFormat.getDateTimeInstance().format(d));		
	}

	/**
	 * This method prints out all the options
	 */
	public static void printHelp()
	{
		// Add a logging message
		logger.info("Printing the help dialog.");
		
		// Use the Apache CLI library's build in help dialog printer
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("ape", opts);
	}

	/**
	 * @return The current version number
	 */
	public static double getVersion()
	{
		return VERSION;
	}
	
	/**
	 * @return The Options object which contains all of the possible options
	 */
	public static Options getOptions()
	{
		return opts;
	}
}
