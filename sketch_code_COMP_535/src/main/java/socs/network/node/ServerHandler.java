package socs.network.node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerHandler implements Runnable {
    //private RouterDescription cr;
    private RouterDescription sr;
    public ServerHandler(RouterDescription sr) {
      this.sr = sr;
    }
  
    public void run() {
      //System.out.println("Starting: " + sr.simulatedIPAddress.toString());
      ServerSocket serverSocket;

      try {
        serverSocket = new ServerSocket(this.sr.processPortNumber);
      
        while(true){
          try{
            //System.out.println("HELLO"); 
            Socket server = serverSocket.accept();
            DataInputStream in = new DataInputStream(server.getInputStream());
            System.out.println("recieved message " + in.readUTF() + "from ???");
            // set cr state to INIT or to TWO-WAY
            DataOutputStream out = new DataOutputStream(server.getOutputStream());
            out.writeUTF("HELLO");  

            //serverSocket.close();
            server.close();

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
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }
  }