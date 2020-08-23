# Networks
This project simulates a network using java's socketio. In the terminal we can create links between routers and assign weights to the links. We set up a link database, and we forward packets using Djikstras Shortest Path algorithm.

## Features
Router 192.168.1.2 upon establishing link to Router 192.168.1.3:
<img src="Packet Fowarding Router 2.png" />

Router 192.168.1.3 upon established Link:
<img src="Packet Fowarding .png" />

Getting Shortest Path Between Routers 192.168.1.2 to 192.168.1.6
<img src="Shortest Path.png" />

## Set up

An application to communicate between simulated Routers using socketio.
In order to set JAVA_HOME :

open -e .bash_profile and fill in the following with your path

#Maven
export M2_HOME=/Users/{User}/apache-maven-3.6.3 
export PATH=$PATH:$M2_HOME/bin

export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre"


In order to run: 
java -jar target/COMP535-1.0-SNAPSHOT-jar-with-dependencies.jar conf/router1.conf
