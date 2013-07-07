AnarchyApe
==========

Fault injection tool for hadoop cluster from yahoo anarchyape


Compilation 
-----------
[Java]
Required version: 1.7.0.21 or later
```
cd src/main/java
download log4j.ar and commons-cli.jar

java -cp . ape/Main.java

rm ape.jar

jar cfm ape.jar META-INF/MANIFEST.MF ape META-INF/services org

(old way)
javac -cp .:log4j-1.4.12.jar:commons-cli-1.2.jar ape/*.java
```

[Perl]
```
   perl Makefile.PL
   cpan -i JSON
   make
   make test
   make install
```

Running 
-------
[Perl]
./ape.pl [remote_ip_list_file]

[Java]
```
java -jar ape.jar [commands]

log file: /var/log/ape.log

(old way)
java -cp .:log4j-1.4.12.jar ape/Main
```

(Local run)
```
java -jar ape.jar -L -S 100 5
```
injects slow network with delay 100 milliseconds for 5 seconds.

(Remote run)
Install pdsh:
```
yum install pdsh
apt-get install pdsh

java -jar ape.jar -R node1,node2,node3 -S 100 5
creates a script to run on the remote hosts:
pdsh -Rssh -w node1,node2,node3 '/usr/local/bin/ape -L -S 100 5'
```

It seems not working as the remote hosts do not have /usr/local/bin/ape file.

Remote nodes can be specified in XML format: cluster-ip-list.xml

Currently, to create a scenario, the user constructs a shell
script specifying the types of errors to be injected or failures to be simulated, one after another. A sample line in a
scenario file could be as follows:

```
java -jar ape.jar -remote cluster-ip-list.xml -F lambda -k lambda
	where the -F is a “Fork Bomb” injection, the -k is a “Kill
	One Node” command, and the lambda specifies the failure rates.
```
Users can define lambda parameters by computing Mean
Time Between Failures (MTBF) of a system. MTBF is defined to be the average (or expected) lifetime of a system
and is one of the key decision-making criteria for data center infrastructure systems [1]. Equipment in data centers
is going to fail, and MTBF helps with predicting which systems are the likeliest to fail at any given moment. Based on
previous failure statistics, users can develop an estimate of
MTBF for various equipment failures; however, determining
MTBFs for many software failures is challenging.

[1] W. Torell and V. Avelar. Performing effective MTBF comparisons for data center infrastructure.
http://www.apcmedia.com/salestools/ASTE-5ZYQF2_R1_EN.pdf.

Available Commands 
------------------
Here are some common failures in Hadoop environments:
```
• Data node is killed
• Application Master (AM) is killed
• Application Master is suspended
• Node Manager (NM) is killed
• Node Manager is suspended
• Data node is suspended
• Tasktracker is suspended
• Node panics and restarts
• Node hangs and does not restart
• Random thread within data node is killed
• Random thread within data node is suspended
• Random thread within tasktracker is killed
• Random thread within tasktracker is suspended
• Network becomes slow
• Network is dropping significant numbers of packets
• Network disconnect (simulate cable pull)
• One disk gets VERY slow
• CPU hog consumes x% of CPU cycles
• Mem hog consumes x% of memory
• Corrupt ext3 data block on disk
• Corrupt ext3 metadata block on disk
```
Command line options:
```
usage: ape [options] ... <failure command>
           options:
 -c,--corrupt-file <file> <size> <offset>        Corrupt the file given
                                                 the address as the first
                                                 argument, size as the 2nd
                                                 arg, and offset as the
                                                 3rd argument
 -C,--corrupt-block <meta/ord> <size> <offset>   Corrupt a random HDFS
                                                 block file with a size in
                                                 bytes as the 2nd arg and
                                                 offset in bytes as the
                                                 3rd argument
 -d,--network-disconnect <time in seconds>       Disconnect the network (eth0 only)
                                                 for a certain period of
                                                 time in seconds specified in the
                                                 argument, and then resumes
 -e,--continue-node <NodeType>                   Continues a tasktracker
                                                 or a datanode at the
                                                 given hostname that has
                                                 already been suspended
 -F,--forkbomb                                   Hangs a host by executing
                                                 a fork bomb
 -h,--help                                       Displays this help menu
 -k,--kill-node <nodetype>                       Kills a datanode,
                                                 tasktracker, jobtracker,
                                                 or namenode.
 -L,--local                                      Run commands locally
 -P,--panic                                      Forces a kernel panic and does not restart the system.
 -p,--network-drop <percentage> <duration>       Drops a specified
                                                 percentage of ALL inbound
                                                 network packets for a
                                                 duration specified in seconds.
 -r,--remount                                    Remounts all filesystems as read-only
 -R,--remote <HostnameList>                      Run commands remotely
 -s,--suspend-node <NodeType>                    Suspends a tasktracker or
                                                 a datanode at the given
                                                 hostname
 -S,--network-slow <delay> <duration>            Delay ALL network packet
                                                 delivery by a specified
                                                 amount of time (in
                                                 milliseconds) for a
                                                 period specified in
                                                 seconds
 -t,--touch                                      Touches a file called
                                                 /tmp/foo.tst
 -u,--udp-flood <hostname> <port> <duration>     Flood the target hostname with a DoS attack.
                                                 For proper effect, use the -R
                                                 flag and designate more
                                                 than one host.
 -v,--verbose                                    Turn on verbose mode
 -V,--version                                    Displays the version number
command:
 -dos <lambda> -k <lambda>	denial of service by launching 4 bombarding threads
 -nic <lambda> -k <lambda>	interface
```
