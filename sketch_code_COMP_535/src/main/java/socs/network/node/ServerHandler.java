package socs.network.node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerHandler implements Runnable {
    private RouterDescription rd;
    public ServerHandler(RouterDescription rd) {
      this.rd = rd;
    }
  
    public void run() {
      ServerSocket serverSocket;

      try {
        //System.out.println(rd.processPortNumber);
        serverSocket = new ServerSocket(this.rd.processPortNumber);
      
        while(true){
          try{
            // System.out.println("HELLO");
            // System.out.println(serverSocket.getSocketAddress());

            Socket server = serverSocket.accept();
            System.out.println("HELLO");
            DataInputStream in = new DataInputStream(server.getInputStream());
            System.out.println("recieved " + in.readUTF());
            // set cr state to INIT or to TWO-WAY
            DataOutputStream out = new DataOutputStream(server.getOutputStream());
            out.writeUTF("HELLO from " + server.getLocalSocketAddress());  

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