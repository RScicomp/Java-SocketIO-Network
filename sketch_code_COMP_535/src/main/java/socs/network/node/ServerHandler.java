package socs.network.node;

import socs.network.message.SOSPFPacket;
import socs.network.message.LSA;
import socs.network.message.LinkDescription;

import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;

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
                  if(router.ports[i] == null ){
                    //From recieved message create router description and assign port from packet
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
              if(packet.sospfType==2){
                System.out.println("We're Looking at LSD1: " + router.lsd.toString());
                boolean changed = false;
                //Upon recieving a packet containing the LSAUpdate: we must look through each LSA and cross check with our lsd database to 
                //ensure that each and every LSA is up to date. Otherwise, we replace. If not present, we add. 
                LSA changedLSA = null;
                System.out.println("HEY");
                  Link[] oldports = new Link[4];
                  for (int i = 0; i < 4; i++){
                    if(router.ports[i]!= null){
                      RouterDescription router1= router.ports[i].router1;
                      RouterDescription router2= router.ports[i].router2;
                      Link old = new Link(router1,router2);
                      oldports[i] = old;
                    }
                  }
                for (LSA recieved: packet.lsaArray){
                  //System.out.println("Recieved: "+ recieved.toString());
                  //System.out.println("Seq numbers recieved: " + recieved.lsaSeqNumber + "our numbers: " + router.lsd._store.get(recieved.linkStateID).lsaSeqNumber);
                  //IF LSA not in already, put it in. 
                  if(router.lsd._store.get(recieved.linkStateID)==null){
                    router.lsd._store.put(recieved.linkStateID,recieved);
                  }
                  else{
                    //Check sender's LSA and our version of senders LSAs first. Update
                    
                    
                    //Replace our LSAs from sender if old. Else ignore it.
                    if(router.lsd._store.get(recieved.linkStateID).lsaSeqNumber <= recieved.lsaSeqNumber){
                      System.out.println("Updated: " +router.lsd._store.get(recieved.linkStateID) + " With: ");
                      router.lsd._store.put(recieved.linkStateID , recieved);
                      System.out.println(router.lsd._store.get(recieved.linkStateID));
                      changedLSA=recieved;
                      changed = true;
                      //Update ports
                      for (int i = 0; i < this.router.ports.length;i++){
                        if(this.router.ports[i]!= null){
                          if(this.router.ports[i].router2.simulatedIPAddress.equals(changedLSA.linkStateID)){
                            System.out.println("REMOVED!");
                            this.router.ports[i]=null;
                          }
                        }
                      }

                    }
                  }
                   
                }

                //Check to see if we have approriate LSAs
                //forward Update
                //if(changed == true){ 
                  //router.resetLSD();
                  //System.out.println("New LSD: " + router.lsd);
                 router.lsaUpdateDisconnect(packet.srcIP,oldports);
                //} 
              }
              //Link State Update Occurring
              if(packet.sospfType==1){
                boolean changed = false;
                //Upon recieving a packet containing the LSAUpdate: we must look through each LSA and cross check with our lsd database to 
                //ensure that each and every LSA is up to date. Otherwise, we replace. If not present, we add. 
                for (LSA recieved: packet.lsaArray){
                  
                  //IF LSA not in already, put it in. 
                  if(router.lsd._store.get(recieved.linkStateID)==null){
                    router.lsd._store.put(recieved.linkStateID,recieved);
                  }
                  else{
                    //Replace LSA if old. Else ignore it.
                    if(router.lsd._store.get(recieved.linkStateID).lsaSeqNumber < recieved.lsaSeqNumber){
                      System.out.println("UPDATING");
                      router.lsd._store.put(recieved.linkStateID , recieved);
                      changed = true;
                    }
                  }
                }
                //Check to see if we have approriate LSAs
                //forward Update
                //if(changed== true){ 
                router.lsaUpdate(packet.srcIP);
                //}
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