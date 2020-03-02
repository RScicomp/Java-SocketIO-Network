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
            }else{
              if(packet.sospfType == 0){
                System.out.println("recieved HELLO from " + packet.srcIP );//once recieved we init
                for(int i = 0; i < 4; i++){
                  //check if already exists?
                  if(router.ports[i] == null){

                  

                    RouterDescription r2 = new RouterDescription();
                    r2.simulatedIPAddress = packet.srcIP;
                    r2.processIPAddress = packet.srcProcessIP;
                    r2.processPortNumber = packet.srcProcessPort;
                    r2.status = RouterStatus.INIT;
                    router.ports[i] = new Link(router.rd,r2);
                    System.out.println("set " + packet.srcIP + " state to INIT");
                    portnumber = i;
                    LSA newlsa = new LSA();

                    //create link
                    System.out.println("LSAARRAY");
                    System.out.println(packet.lsaArray);

                    //Update LSD 
                    LSA lsa = new LSA();
                    lsa.linkStateID = rd.simulatedIPAddress;

                    //If this LSA already exists, update it. Incremenet to sure that we know the version. copy all past links into the newest LSA.
                    if(router.lsd._store.containsKey(rd.simulatedIPAddress)){
                      for ( LSA lsaold1 : packet.lsaArray){
                        //If sender ip is in the LSAarray
                        if(lsaold1.linkstateID.equals(r2.simulatedIPAddress)){
                          for ( LSA lsastore: router.lsd_.store){

                            //If present in our LSD
                            if(lsastore.linkstateID.equals(lsaold1.linkstateID)){
                              //UPDATE IF sequence number lower
                              if (lsastore.lsaSeqNumber < lsaold1.lsaSeqNumber){
                                //Loop through the links 
                              }
                              
                            }

                          }
                        }
                      }

                      LSA previousLSA = (LSA)router.lsd._store.get(rd.simulatedIPAddress);

                      lsa.lsaSeqNumber = previousLSA.lsaSeqNumber+1;
                      System.out.println("Updating");
                      lsa.links = router.lsd._store.get(rd.simulatedIPAddress).links;
                    }
                    else{
                      //Upon HELLO we must create a LSA. Because this is a HELLO we can send link weight through packet
                      //Create a Link description. (What we're linking to, the port and the weight)
                      LinkDescription ld = new LinkDescription();
                      ld.linkID = r2.simulatedIPAddress;
                      int weight = 0;
                      for ( LSA lsaold : packet.lsaArray){
  
                        for (LinkDescription linkd: lsaold.links){
                          //if router 2 in the links of the source router.
                          if(linkd.linkID.equals(rd.simulatedIPAddress)){
                            weight = lsaold.links.get(sourceI)tosMetrics;
                            break;
                          }
                        }
                      }
                      ld.portNum = portnumber;
                      ld.tosMetrics = weight;
                      //lsa.links.add(ld);

                      //Synchronize Link State Database, retrieving LSD Information.
                      for (LSA lsaold : packet.lsaArray){
                        for (LinkDescription linkd: lsaold.links){
                          LinkDescription ld = new LinkDescription();
                          //If you encouter yourself
                          if(linkd.linkID.equals(rd.simulatedIPAddress)){
                            ld.linkID = router.simulatedIPAddress;
                            ld.portNum =portnumber;
                            ld.tosMetrics=weight;
                            lsa.links.add(ld);
                          }else{
                            ld.linkID = r2.simulatedIPAddress;
                            ld.portNum = portnumber;
                            ld.tosMetrics = linkd.tosMetrics + weight;
                            lsa.links.add(ld);
                          }
                        }
                      }


                      System.out.println("Recieved LSA");
                      System.out.println(ld);
                      //Add the new lsa into the LSD hashmap. at the specified address.
                      rd.lsd._store.put(lsa.linkStateID,lsa);


                      //System.out.println(lsd);
                    }  
                    




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