# Networks_Assignments
Note, our detect will not work if we have -1 as the weight for links.
To Run: 
java -jar target/COMP535-1.0-SNAPSHOT-jar-with-dependencies.jar conf/router1.conf

To Attach:
attach 127.0.0.1 5020 192.168.1.100 5
See config files for proper Simulated IPs and Ports for each Router!

To Start:
start

To Check neighbors:
neighbors

Test scenario to run detect:

Connect 1 to 2 and 3:
attach 127.0.0.1 5020 192.168.1.100 3
attach 127.0.0.1 5030 192.168.2.1 1

Start 1

Connect 2 to 3 and 4 and 5:
attach 127.0.0.1 5030 192.168.2.1 1
attach 127.0.0.1 5035 192.168.3.1 100
attach 127.0.0.1 5050 192.168.4.1 1

Start 2 

connect 4 to 5:
attach 127.0.0.1 5050 192.168.4.1 1

Start 4

RUN DETECT on router 1: detect 192.168.3.1
Ecpected output:

192.168.1.1 ->(1) 192.168.2.1 ->(1) 192.168.1.100 ->(1) 192.168.4.1 ->(1) 192.168.3.1









In order to set JAVA_HOME :

open -e .bash_profile and fill in the following with your path

#Maven
export M2_HOME=/Users/{User}/apache-maven-3.6.3 
export PATH=$PATH:$M2_HOME/bin

export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home/jre"


In order to run: 
java -jar target/COMP535-1.0-SNAPSHOT-jar-with-dependencies.jar conf/router1.conf
