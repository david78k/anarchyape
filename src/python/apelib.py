# Copyright (c) 2012 Yahoo! Inc. All rights reserved.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License. See accompanying LICENSE file.

import subprocess
import json
import random
from multiprocessing import Process

def ape(remote, hosts, command, args):
	cmd="ape "
	if remote:
		cmd += "-R "
		if(isinstance(hosts, str)):
			cmd += hosts + " "
		else:
			for host in hosts:
				cmd += host + " "
	else:
		cmd += "-L "
	cmd += command + " "
	for arg in args:
		cmd += arg + " "
	print(cmd)
	subprocess.call(cmd, shell=True)

def getSlaves(filename):
	f = open(filename)
	lines = f.readlines()
	return lines

def getMasters(filename):
	f = open(filename)
	lines = f.readlines()
	return lines

def getSlavesFromJSON(jsonFileLocation):
	json_data=open(jsonFileLocation)
	data=json.load(json_data)
	return data["slaves"]

def getMastersFromJSON(jsonFileLocation):
        json_data=open(jsonFileLocation)
        data=json.load(json_data)
        return data["masters"]

def getRandomSlave(filename):
	slaves=getSlaves(filename)
	return random.choice(slaves)

def getNRandomSlaves(n, filename):
	slaves=getSlaves(filename)
	str = ""
	for i in range(n):
		sl=random.choice(slaves)
		slaves.remove(sl)
		str += sl + " "
	return str

def getRandomMaster(filename):
        masters=getMasters(filename)
        return random.choice(masters)

def getNRandomMasters(n, filename):
        masters=getMasters(filename)
        str = ""
        for i in range(n):
                sl=random.choice(masters)
                masters.remove(sl)
                str += sl + " "
        return str

def getCommands(filename):
	f = open(filename)
	lines = f.readlines()
	return lines

def getRandomCommand(filename):
	commands = getCommands(filename)
        return random.choice(commands)

def runCommandsInParallel(commandsArray):
	processArray = []
	# Create a process for each command
	for i in range(len(commandsArray)):
		processArray.append(Process(target=ape, args=(commandsArray[i])))
	# Start each process
	for i in range(len(processArray)):
		processArray[i].start()
	# Wait for each process to complete
	for i in range(len(processArray)):
                processArray[i].join()

def runCommandsInSeries(commandsArray):
	processArray = []
        # Create a process for each command
        for i in range(len(commandsArray)):
                processArray.append(Process(target=ape, args=(commandsArray[i])))
        for i in range(len(processArray)):
        	# Start the process
                processArray[i].start()
		
 	       	# Wait for each process to complete
                processArray[i].join()
	
	
