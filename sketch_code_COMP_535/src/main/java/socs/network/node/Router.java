package socs.network.node;

import socs.network.util.Configuration;
import socs.network.message.SOSPFPacket;
import socs.network.message.LSA;
import socs.network.message.LinkDescription;

import java.util.Vector;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.HashMap;

public class Router {

  protected LinkStateDatabase lsd; //

  RouterDescription rd = new RouterDescription();
  Thread listener = null;
  //assuming that all routers are with 4 ports
  Link[] ports = new Link[4]; //ports available to current router

  public Router(Configuration config) {
    rd.simulatedIPAddress = config.getString("socs.network.router.ip");
    rd.processIPAddress = "127.0.0.1";
    rd.processPortNumber = config.getShort("socs.network.router.port");
    rd.status = null;

    lsd = new LinkStateDatabase(rd);
    System.out.println(lsd);
    //thread server listening to incoming connections.
    
    ServerHandler handler = new ServerHandler(rd,this);
    Thread t1 = new Thread(handler);
    //handle incoming connection requests
    listener = t1;
  }

  /**
   * output the shortest path to the given destination ip
   * <p/>
   * format: source ip address  -> ip address -> ... -> destination ip
   *
   * @param destinationIP the ip adderss of the destination simulated router
   */
  private void processDetect(String destinationIP) {
    //System.out.println(this.lsd.toString());
    System.out.println("Detection LSD: " + lsd.toString());
    for(int i = 0; i < 4; i++){
      if(this.ports[i] != null){
        System.out.println("LINK " + i);
        System.out.println(ports[i].router1.simulatedIPAddress);
        System.out.println(ports[i].router2.simulatedIPAddress);
      }
    }
    //System.out.println(this.ports[1].router1.simulatedIPAddress);
    System.out.println(lsd.getShortestPath(destinationIP));

  }
  public void resetLSD(){
    LinkStateDatabase newlsd = new LinkStateDatabase(rd);
    HashMap<String,LSA> newstore = new HashMap<String,LSA>();
    for (String key : lsd._store.keySet()){
      newstore.put(key,lsd._store.get(key));
    }
    newlsd._store=newstore;
    this.lsd=newlsd;
  }

  /**
   * disconnect with the router identified by the given destination ip address
   * Notice: this command should trigger the synchronization of database
   *
   * @param portNumber the port number which the link attaches at
   */
  private void processDisconnect(short portNumber) {
    if(portNumber >= 0 && portNumber < 4){
      RouterDescription router1old= ports[portNumber].router1;
      RouterDescription router2old= ports[portNumber].router2;
      Link oldremove = new Link(router1old,router2old);
      Link linktoremove= oldremove;

      Link[] oldports = new Link[4];
      //System.out.println("Here");

      //Remember old ports
      for (int i = 0; i < 4; i++){
        if(ports[i] != null){
          RouterDescription router1= ports[i].router1;
          RouterDescription router2= ports[i].router2;
          Link old = new Link(router1,router2);
          oldports[i] = old;
        }
      }
      //System.out.println("Link to remove: " + linktoremove.router2.simulatedIPAddress);

      if(linktoremove.router2 != null){
        //System.out.println("Here");
        ports[portNumber]=null;
        LSA lsa = lsd._store.get(rd.simulatedIPAddress);
        //System.out.println("LSD1: " + lsd.toString());

        for (LinkDescription ld : lsa.links) {
          //System.out.println("Checking: " +ld.toString());
          if (ld.linkID.equals(linktoremove.router2.simulatedIPAddress)) {
            lsa.links.remove(ld);
            lsa.lsaSeqNumber += 1;
            lsd._store.put(lsa.linkStateID,lsa);
            //System.out.println("LSD2: " + lsd.toString());
            break;
          }
        }
        LSA lsa2 = lsd._store.get(linktoremove.router2.simulatedIPAddress);
        //System.out.println("LSA3: " + lsa2.toString());

        for (LinkDescription ld : lsa2.links) {
          //System.out.println("Checking: " +ld.toString());
          if (ld.linkID.equals(linktoremove.router1.simulatedIPAddress)) {
            lsa2.links.remove(ld);
            lsa2.lsaSeqNumber += 1;
            lsd._store.put(lsa.linkStateID,lsa);
            //System.out.println("LSD4: " + lsd.toString());
            break;
          }
          
        }


      }
      //resetLSD();
      
      lsaUpdateDisconnect("All Update",oldports);
    } 
  }

  /**
   * attach the link to the remote router, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to indentify the process IP and process Port;
   * additionally, weight is the cost to transmitting data through the link
   * <p/>
   * NOTE: this command should not trigger link database synchronization
   */
  private void processAttach(String processIP, short processPort,
                             String simulatedIP, short weight) {
    //Create a router description
    RouterDescription attach = new RouterDescription();
    attach.processIPAddress = processIP;
    attach.processPortNumber = processPort;
    attach.simulatedIPAddress = simulatedIP;
    attach.status = null; //Unsure?


    //Link to current router:
    Link link = new Link(this.rd, attach);
            
    //We need to add this to the ports we connect to.
    boolean isAttached = false;
    int attachedport = -1;

    //check if already attached
    for(int i=0; i<4; i++){
      if (ports[i] != null && ports[i].router2.simulatedIPAddress.equals(attach.simulatedIPAddress)){
        System.out.println("Already Attached!");
        isAttached = true;
        attachedport = i;
        break;
        //isAttached = true;
        //break;
      }
    }
    if(!isAttached){
      //We need to add this to the ports we connect to.
      for(int i=0;i<4;i++){
        if(ports[i] == null){
          //System.out.println("Attaching!");
          //Create LSA to be inserted into LINKSTATE DATABASE Store. This LSA describes the connection from current router to others.
          LSA lsa = new LSA();
          lsa.linkStateID = rd.simulatedIPAddress;

          //If this LSA already exists, update it. Incremenet to sure that we know the version. copy all past links into the newest LSA.
          //To ensure that when we send out a packet with lsaArray, it contains the most up to date LSAs. 
          if(lsd._store.containsKey(rd.simulatedIPAddress)){
            LSA previousLSA = (LSA)lsd._store.get(rd.simulatedIPAddress);
            //System.out.println("Updating Sequence number to: " + previousLSA.lsaSeqNumber+1);
            lsa.lsaSeqNumber = previousLSA.lsaSeqNumber+1;
            //System.out.println("Updating");
            lsa.links = lsd._store.get(rd.simulatedIPAddress).links;
          }

          //Create a Link description. (What we're linking to, the port and the weight)
          LinkDescription ld = new LinkDescription();
          ld.linkID = attach.simulatedIPAddress;
          ld.portNum = i;
          //System.out.println("PortNum:");
          //System.out.println(ld.portNum);
          ld.tosMetrics = weight;
          lsa.links.add(ld);

          //Add the new lsa into the LSD hashmap. at the specified address.
          lsd._store.put(lsa.linkStateID,lsa);

          //System.out.println(lsd);
          ports[i] = link;

          break;
        }
      }
    }
    //System.out.println(ports);
    //allowing for multiple attaches?
    /*
    else{
      LinkedList<LinkDescription> old = lsd._store.get(rd.simulatedIP).links;
      for (LinkDescription link : old){
        if(link.linkID == attach.simulatedIP){
          old.tosMetrics
        }
      }
    }
    */
  }

  /**
   * broadcast Hello to neighbors.... RG here: When sending hello we send the weight and so the router 2 now can update their lsd database...
   */
  private void processStart() {
    // Router 1 (R1) broadcasts Hello through all ports
    // Run LSAUPDATE
    // LSAs are used to advertise networks
    // to which the advertising router is connected,
    // while other types are used to support additional
    // hierarchys
    try{
      listener.start();
    }
    catch(Exception e){
      //System.out.println("Error Starting");
    }

    for (int i = 0; i<4; i++ ){

      Link link = ports[i];
      
      if(link != null){
        try{

          //Send HELLO
          Socket client = new Socket(link.router2.processIPAddress, link.router2.processPortNumber);
          OutputStream outToServer = client.getOutputStream();
          ObjectOutputStream out = new ObjectOutputStream(outToServer);
          SOSPFPacket packet = new SOSPFPacket();
          packet.srcProcessIP = rd.processIPAddress;
          //print(packet.srcProcessIP);
          packet.srcProcessPort = rd.processPortNumber;
          packet.srcIP = rd.simulatedIPAddress;
          packet.dstIP = link.router2.simulatedIPAddress;
          packet.sospfType = 0;

          out.writeObject(packet);
         
          //Recieve
          InputStream inFromServer = client.getInputStream();
          ObjectInputStream in = new ObjectInputStream(inFromServer);
          Object message2 = in.readObject();
          
          //If we recieve a message telling us wrong IP, print error message
          if(message2.toString().equals("Wrong IP")){
            System.out.println("Wrong IP: "+ link.router2.simulatedIPAddress);
          }else{
            SOSPFPacket message = (SOSPFPacket) message2; 

            //Confirmed that reciever is a two way
            if(packet.sospfType == 0){
              System.out.println("recieved HELLO from " + message.srcIP );
              ports[i].router2.status = RouterStatus.TWO_WAY;//After recieving HELLO
              System.out.println("set "+message.srcIP + " state to TWO_WAY");
            }
            //SEND again, HELLO
            packet = new SOSPFPacket();
            packet.srcProcessIP = rd.processIPAddress;
            packet.srcProcessPort = rd.processPortNumber;
            packet.srcIP = rd.simulatedIPAddress;
            packet.dstIP = link.router2.simulatedIPAddress;
            packet.sospfType = 0;
            out.writeObject(packet);
            //System.out.println("WE ARE HERE");

            in.close();
            out.close();
            client.close();
          }


        }catch(Exception e){
          System.out.println("Error. Not started yet.");
          //e.printStackTrace();
        }


      }
      
      
    }
    //Perform LSAUpdate once we confirm all are TWO WAY.
    lsaUpdate("All Update");
  }

  //Send packets
  public void sendPacket(SOSPFPacket packet, RouterDescription receiver){
    try{
      Socket client = new Socket(receiver.processIPAddress, receiver.processPortNumber);
      OutputStream outToServer = client.getOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(outToServer);
      out.writeObject(packet);
      out.close(); 
      client.close();
    }catch(Exception e){
      System.out.println("Error Sending Packet");
    }

  }

  //Perform lsaUpdate
  public void lsaUpdate(String exclude){
    //Send packets containing the current lsd to all neighbors
    
    for (Link link : this.ports){
      if(link!= null){
        //Confirm the links are two way.
        if(link.router2.status == RouterStatus.TWO_WAY){ 
          //Do not update to src/initiator of LSAUpdate
          if(!exclude.equals(link.router2.simulatedIPAddress)){
            SOSPFPacket update = new SOSPFPacket();
            update.srcProcessIP = rd.simulatedIPAddress;
            update.srcProcessPort = rd.processPortNumber;
            update.srcIP = rd.simulatedIPAddress;
            update.dstIP = link.router2.simulatedIPAddress;
            update.sospfType = 1;
            update.lsaArray = new Vector<LSA>(lsd._store.values());
            /*
            for(int i = 0; i < update.lsaArray.size();i++){
              System.out.println("LSAs:" + update.lsaArray.get(i).toString());
            }*/
            sendPacket(update,link.router2);
          }
        }
      }
    }
  }
  public void lsaUpdateDisconnect(String exclude, Link[] oldports){
    //Send packets containing the current lsd to all neighbors
    //System.out.println("SENDING"); 
    for (Link link : oldports){
      if(link!= null){
        //Confirm the links are two way.
        //System.out.println("SENDING");
        if(link.router2.status == RouterStatus.TWO_WAY){
          //System.out.println("SENDING"); 
          //Do not update to src/initiator of LSAUpdate
          if(!exclude.equals(link.router2.simulatedIPAddress)){
            //System.out.println("SENDING");
            SOSPFPacket update = new SOSPFPacket();
            update.srcProcessIP = rd.simulatedIPAddress;
            update.srcProcessPort = rd.processPortNumber;
            update.srcIP = rd.simulatedIPAddress;
            update.dstIP = link.router2.simulatedIPAddress;
            update.sospfType = 2;
            update.lsaArray = new Vector<LSA>(lsd._store.values());
            sendPacket(update,link.router2);
          }
        }
      }
    }
  }
  


  /**
   * attach the link to the remote router, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to indentify the process IP and process Port;
   * additionally, weight is the cost to transmitting data through the link
   * <p/>
   * This command does trigger the link database synchronization
   */
  private void processConnect(String processIP, short processPort, String simulatedIP, short weight) {
    processAttach(processIP, processPort, simulatedIP, weight);
    processStart();
  }

  /**
   * output the neighbors of the routers
   */
  private void processNeighbors() {
    for(int i=0;i<4;i++){
      if(ports[i] == null){

        break;
      }
      //Ensure links are two way only
      if(ports[i].router2.status == RouterStatus.TWO_WAY){
        System.out.println(ports[i].router2.simulatedIPAddress + " is neighbor" + (i+1));
      }
    }
  }

  /**
   * disconnect with all neighbors and quit the program
   */
  private void processQuit() {
    for(short s = 0; s<4; s++){
      int i = s;
      if(ports[i] != null){
        processDisconnect(s);
      }
    }

    System.exit(1);

  }

  public void terminal() {
    try {
      InputStreamReader isReader = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(isReader);
      
      System.out.print(">> ");
      String command = br.readLine();
      while (true) {  

        if (command.startsWith("detect ")) {
          String[] cmdLine = command.split(" ");
          processDetect(cmdLine[1]);
        } else if (command.startsWith("disconnect ")) {
          String[] cmdLine = command.split(" ");
          processDisconnect(Short.parseShort(cmdLine[1]));
        } else if (command.startsWith("quit")) {
          processQuit();
        } else if (command.startsWith("attach ")) {
          String[] cmdLine = command.split(" ");
          processAttach(cmdLine[1], Short.parseShort(cmdLine[2]),
                  cmdLine[3], Short.parseShort(cmdLine[4]));
        } else if (command.equals("start")) {
          //System.out.println("CONNECTING");
          processStart();
        } else if (command.startsWith("connect ")) {
          //System.out.println("CONNECTING");
          String[] cmdLine = command.split(" ");
          processConnect(cmdLine[1], Short.parseShort(cmdLine[2]),
                  cmdLine[3], Short.parseShort(cmdLine[4]));
        } else if (command.equals("neighbors")) {
          //output neighbors
          processNeighbors();
        } else {
          //invalid command
          System.out.println("INVALID COMMAND");
          break;
        }
        System.out.print(">> ");
        command = br.readLine();
      }
      
      isReader.close();
      br.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
