# Networks_Assignments

To Run: 
java -jar target/COMP535-1.0-SNAPSHOT-jar-with-dependencies.jar conf/router1.conf

To Attach:
attach 127.0.0.1 5020 192.168.1.100 5
See config files for proper Simulated IPs and Ports for each Router!

To Start:
start

To Check neighbors:
neighbors








In order to set JAVA_HOME :

open -e .bash_profile and fill in the following with your path

#Maven
export M2_HOME=/Users/{User}/apache-maven-3.6.3 
export PATH=$PATH:$M2_HOME/bin

export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre"


In order to run: 
java -jar target/COMP535-1.0-SNAPSHOT-jar-with-dependencies.jar conf/router1.conf
