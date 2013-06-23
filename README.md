AnarchyApe
==========

Fault injection tool for hadoop cluster from yahoo anarchyape


Compilation 
-----------

```
cd src/main/java
download log4j.ar and commons-cli.jar

javac -cp .:log4j-1.4.12.jar:commons-cli-1.2.jar ape/*.java
```

Running 
-------

```
java -cp .:log4j-1.4.12.jar apr/Main

log file: /var/log/ape.log
```

Currently, to create a scenario, the user constructs a shell
script specifying the types of errors to be injected or fail-
ures to be simulated, one after another. A sample line in a
scenario file could be as follows:

```
java -jar ape.jar -remote cluster-ip-list.xml -fb lambda -k lambda
	where the -fb is a “Fork Bomb” injection, the -k is a “Kill
	One Node” command, and the lambda specifies the failure rates.
```
Users can define lambda parameters by computing Mean
Time Between Failures (MTBF) of a system. MTBF is de-
fined to be the average (or expected) lifetime of a system
and is one of the key decision-making criteria for data cen-
ter infrastructure systems [20]. Equipment in data centers
is going to fail, and MTBF helps with predicting which sys-
tems are the likeliest to fail at any given moment. Based on
previous failure statistics, users can develop an estimate of
MTBF for various equipment failures; however, determining
MTBFs for many software failures is challenging.

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
