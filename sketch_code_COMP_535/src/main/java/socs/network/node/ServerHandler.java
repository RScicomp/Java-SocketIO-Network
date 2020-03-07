package socs.network.node;

import socs.network.util.Configuration;
import socs.network.message.SOSPFPacket;
import socs.network.message.LSA;
import socs.network.message.LinkDescription;

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
            //If the packet destination is incorrect send a notification back to sender 
            if(!packet.dstIP.equals(router.rd.simulatedIPAddress)){
              ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
              out.writeObject("Wrong IP");
              in.close();
            }
            else{
              //Upon recieving a message. We want to forward the LSA to 
              if(packet.sospfType == 0){
                System.out.println("recieved HELLO from " + packet.srcIP );//once recieved we init
                for(int i = 0; i < 4; i++){
                  //check if already exists
                  //System.out.println("ADDED");
                  if(router.ports[i] == null ){
                    //From recieved message create router description and assign port from packet
                    //System.out.println("PORTS:" + router.ports[i]);
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
                  if(router.ports[i].router2.simulatedIPAddress.equals(packet.srcIP)){
                    portnumber=i;
                  }
                }
                

              }
              //Link State Update Occurring
              if(packet.sospfType==1){
                //LSA newlsa = new LSA();
                //System.out.println("Performing LinkState Update");
                //create link
                //System.out.println("LSAARRAY");

                //System.out.println(packet.lsaArray);
                boolean changed = false;
                //Upon recieving a packet containing the LSAUpdate: we must look through each LSA and cross check with our lsd database to 
                //ensure that each and every LSA is up to date. Otherwise, we replace. If not present, we add. 
                for (LSA recieved: packet.lsaArray){
                  
                  //IF LSA not in already, put it in. 
                  if(router.lsd._store.get(recieved.linkStateID)==null){
                    router.lsd._store.put(recieved.linkStateID,recieved);
                  }
                  else{
                    //System.out.println("We recieved a new LSA");
                    //System.out.println(router.lsd._store.get(recieved.linkStateID));
                    //Replace LSA if old. Else ignore it.
                    if(router.lsd._store.get(recieved.linkStateID).lsaSeqNumber < recieved.lsaSeqNumber){
                      router.lsd._store.put(recieved.linkStateID , recieved);
                      //System.out.println("It has replaced an old LSA");
                      changed = true;
                    }
                  }
                }
                //Check to see if we have approriate LSAs
                //System.out.println(router.lsd._store);
                //forward Update
                //System.out.println("Excluding:" + packet.srcIP);
                if(changed== true){ 
                  //System.out.println("Calling LSAUPDATE");
                  router.lsaUpdate(packet.srcIP);
                }
              }
              
              if(packet.sospfType == 0 ){
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
                  router.lsaUpdate("ALL UPDATE");
                }
                in.close();
                out.close();
              }
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