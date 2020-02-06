package socs.network.node;

import socs.network.util.Configuration;
import socs.network.message.SOSPFPacket;

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

public class Router {

  protected LinkStateDatabase lsd; //

  RouterDescription rd = new RouterDescription();

  //assuming that all routers are with 4 ports
  Link[] ports = new Link[4]; //ports available to current router

  public Router(Configuration config) {
    rd.simulatedIPAddress = config.getString("socs.network.router.ip");
    rd.processIPAddress = "127.0.0.1";
    rd.processPortNumber = config.getShort("socs.network.router.port");
    rd.status = null;

    lsd = new LinkStateDatabase(rd);

    //thread server listening to incoming connections.
    ServerHandler handler = new ServerHandler(rd,this);
    Thread t1 = new Thread(handler);
    //handle incoming connection requests
    t1.start();
  }

  /**
   * output the shortest path to the given destination ip
   * <p/>
   * format: source ip address  -> ip address -> ... -> destination ip
   *
   * @param destinationIP the ip adderss of the destination simulated router
   */
  private void processDetect(String destinationIP) {

  }

  /**
   * disconnect with the router identified by the given destination ip address
   * Notice: this command should trigger the synchronization of database
   *
   * @param portNumber the port number which the link attaches at
   */
  private void processDisconnect(short portNumber) {

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

    //check if already attached
    for(int i=0; i<4; i++){
      if (ports[i] != null && ports[i].router2.simulatedIPAddress.equals(attach.simulatedIPAddress)){
        isAttached = true;
        break;
      }
    }

    if(!isAttached){
      //We need to add this to the ports we connect to.
      for(int i=0;i<4;i++){
        if(ports[i] == null){
          ports[i] = link;
          break;
        }
      }
    }

  }

  /**
   * broadcast Hello to neighbors
   */
  private void processStart() {
    // Router 1 (R1) broadcasts Hello through all ports
    // Run LSAUPDATE
    // LSAs are used to advertise networks
    // to which the advertising router is connected,
    // while other types are used to support additional
    // hierarchys
    
    //RG Code
    //Server Socket initialization
    //ServerSocket serverSocket = new ServerSocket(rd.processPortNumber);

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
          

            //client.close();
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
          e.printStackTrace();
        }
      }
      //use our port number to send
      //Socket client = new Socket("Sender", rd.processPortNumber);



      //set up client and send hello back

      
      //Send message using socket
    }
  }

  /**
   * attach the link to the remote router, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to indentify the process IP and process Port;
   * additionally, weight is the cost to transmitting data through the link
   * <p/>
   * This command does trigger the link database synchronization
   */
  private void processConnect(String processIP, short processPort,
                              String simulatedIP, short weight) {

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
          processStart();
        } else if (command.equals("connect ")) {
          String[] cmdLine = command.split(" ");
          processConnect(cmdLine[1], Short.parseShort(cmdLine[2]),
                  cmdLine[3], Short.parseShort(cmdLine[4]));
        } else if (command.equals("neighbors")) {
          //output neighbors
          processNeighbors();
        } else {
          //invalid command
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
