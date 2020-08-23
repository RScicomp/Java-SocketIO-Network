# Networks
This project simulates a network using java's socketio. We set up a link database, and we forward packets using Djikstras Shortest Path algorithm.
<img src="Packet Forwarding Example.png"  width="1000" height="400"/>
<img src="Shortest Path.png"  width="500" height="400"/>

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
