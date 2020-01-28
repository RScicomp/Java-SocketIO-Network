package socs.network.node;

import socs.network.util.Configuration;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Router {

  protected LinkStateDatabase lsd; //

  RouterDescription rd = new RouterDescription();

  // assuming that all routers are with 4 ports
  Link[] ports = new Link[4]; // ports available to curreent router

  public Router(Configuration config) {
    rd.simulatedIPAddress = config.getString("socs.network.router.ip");
    lsd = new LinkStateDatabase(rd);
  }

  /**
   * output the shortest path to the given destination ip
   * <p/>
   * format: source ip address -> ip address -> ... -> destination ip
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
   * attach the link to the remote router, which is identified by the given
   * simulated ip; to establish the connection via socket, you need to indentify
   * the process IP and process Port; additionally, weight is the cost to
   * transmitting data through the link
   * <p/>
   * NOTE: this command should not trigger link database synchronization
   */
  private void processAttach(String processIP, short processPort, String simulatedIP, short weight) {
    // Create a router description
    RouterDescription attach = new RouterDescription();
    attach.processIPAddress = processIP;
    attach.processPortNumber = processPort;
    attach.simulatedIPAddress = simulatedIP;
    //attach.status = RouterStatus.INIT; // Unsure?
    // Link to current router:
    Link link = new Link(rd, attach);

    // We need to add this to the ports we connect to.
    for (int i = 0; i < 4; i++) {
      if (ports[i] == null) {
        ports[i] = link;
      }
    }
  }

  /**
   * broadcast Hello to neighbors
   * 
   * @throws IOException
   * @throws UnknownHostException
   */
  private void processStart() throws UnknownHostException, IOException {
    // Router 1 (R1) broadcasts Hello through all ports
    // Run LSAUPDATE
    // LSAs are used to advertise networks
    // to which the advertising router is connected,
    // while other types are used to support additional
    // hierarchy

    for (int i = 0; i<4; i++ ){
      Link link = ports[i];
      System.out.println(link);
      try{
        Runnable server = new serverHandler(link);
        Thread serverThread = new Thread(server);
        serverThread.start();
      }catch(Exception e){
        e.printStackTrace();
      }
      

      //Client
      System.out.println("Connecting to Router");
      Socket client = new Socket(link.router2.processIPAddress, link.router2.processPortNumber);
      System.out.println("Just connected to " + client.getRemoteSocketAddress());

      OutputStream outToServer = client.getOutputStream();
      DataOutputStream out = new DataOutputStream(outToServer);
      out.writeUTF("HELLO");

      InputStream inFromServer = client.getInputStream();
      DataInputStream in = new DataInputStream(inFromServer);

      System.out.println("recieved " + in.readUTF() + "from " + link.router2.simulatedIPAddress);
      client.close();
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