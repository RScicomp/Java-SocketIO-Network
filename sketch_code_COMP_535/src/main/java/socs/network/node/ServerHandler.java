package socs.network.node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerHandler implements Runnable {
    private Router router;
    
    public ServerHandler(Router rd) {
      this.router = rd;
    }
  
    public void run() {
      ServerSocket serverSocket;

      try {
        serverSocket = new ServerSocket(this.router.rd.processPortNumber);
      
        while(true){
          try{
            Socket server = serverSocket.accept();
            DataInputStream in = new DataInputStream(server.getInputStream());
            
            //Recives and prints Hello
            System.out.println("recieved " + in.readUTF());
            // set cr state to INIT or to TWO-WAY
            DataOutputStream out = new DataOutputStream(server.getOutputStream());
            
            //Send Hello
            out.writeUTF("HELLO from " + server.getLocalSocketAddress());  
            out.flush();

      
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