package socs.network.node;

import socs.network.util.Configuration;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.IOException;
import java.net.ServerSocket;

public class Router {

  protected LinkStateDatabase lsd; //

  RouterDescription rd = new RouterDescription();

  //assuming that all routers are with 4 ports
  Link[] ports = new Link[4]; //ports available to current router

  public Router(Configuration config) {
    rd.simulatedIPAddress = config.getString("socs.network.router.ip");
    lsd = new LinkStateDatabase(rd);
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
    Link link = new Link(rd,attach);

    //We need to add this to the ports we connect to.
    for(int i=0;i<4;i++){
      if(ports[i] != null){
        ports[i] = link;
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
    // hierarchy
    
    //RG Code
    //Server Socket initialization
    //ServerSocket serverSocket = new ServerSocket(rd.processPortNumber);

    for (int i = 0; i<4; i++ ){
      Link link = ports[i];
      System.out.println("SocketServer Example");
      ServerSocket server = null;
      try {
        server = new ServerSocket(link.router1.processPortNumber);

        while (true) {
          /**
           * create a new {@link SocketServer} object for each connection
           * this will allow multiple client connections
           */
          
          Socket server = new SocketServer(server.accept());
          System.out.println("Just connected to " + server.getRemoteSocketAddress());
          DataInputStream in = new DataInputStream(server.getInputStream());
          System.out.println(in.readUTF());
          if (in.readUTF().equals("HELLO")){
            break;
          }
        }
      } catch (IOException ex) {
        System.out.println("Unable to start server.");
      } finally {
        try {
          if (server != null)
            server.close();
        } catch (IOException ex) {
          ex.printStackTrace();
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
