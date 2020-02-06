package socs.network.node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import socs.network.message.SOSPFPacket;

public class ServerHandler implements Runnable {
    private RouterDescription rd;
    private Router router;
    public ServerHandler(RouterDescription rd, Router router) {
      this.rd = rd;
      this.router = router;
    }
  
    public void run() {
      ServerSocket serverSocket;

      try {
        //System.out.println(rd.processPortNumber);
        serverSocket = new ServerSocket(this.rd.processPortNumber);
      
        while(true){
          try{


            Socket server = serverSocket.accept();
          

            //listen
            InputStream inFromServer = server.getInputStream(); 
            ObjectInputStream in = new ObjectInputStream(inFromServer);
            SOSPFPacket packet = (SOSPFPacket) in.readObject();
            int portnumber = -1;
            //If the packet destination is incorrect send an notification back to sender 
            if(!packet.dstIP.equals(router.rd.simulatedIPAddress)){
              ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
              out.writeObject("Wrong IP");
              in.close();
            }else{
              if(packet.sospfType == 0){
                System.out.println("recieved HELLO from " + packet.srcIP );//once recieved we init
                for(int i = 0; i < 4; i++){
                  //check if already exists?
                  if(router.ports[i] == null){
                    //create link
                    RouterDescription r2 = new RouterDescription();
                    r2.simulatedIPAddress = packet.srcIP;
                    r2.processIPAddress = packet.srcProcessIP;
                    r2.processPortNumber = packet.srcProcessPort;
                    r2.status = RouterStatus.INIT;
                    router.ports[i] = new Link(router.rd,r2);
                    System.out.println("set " + packet.srcIP + " state to INIT");
                    portnumber = i;
                    break;
                  }
                }
              }

            
            
            
            
              //to make sure two way is occurring we send a message back so the initializor can set us as two way
              ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
              SOSPFPacket presponse = new SOSPFPacket();
              presponse.srcProcessIP = rd.processIPAddress;
              presponse.srcProcessPort = rd.processPortNumber;
              presponse.srcIP = rd.simulatedIPAddress;
              presponse.dstIP = packet.srcIP;
              presponse.sospfType = 0;

              out.writeObject(presponse);

              //Upon recieval confirmation set status of sending router state to TWO WAY.
              SOSPFPacket response = (SOSPFPacket) in.readObject();
              if (response.sospfType== 0){
                System.out.println("recieved HELLO from " + response.srcIP);
                router.ports[portnumber].router2.status = RouterStatus.TWO_WAY;
                System.out.println("set "+response.srcIP + " state to TWO_WAY");

              }
              in.close();
              out.close();

            }


          }catch(SocketTimeoutException s){
            System.out.println("Socket timed out!");
            break;
          }catch(IOException e){
            e.printStackTrace();
            break;
          }catch(Exception e){
            e.printStackTrace();
            break;
          }
        }
        serverSocket.close();

      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
  }