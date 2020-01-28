package socs.network.node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class serverHandler implements Runnable {
    private RouterDescription cr;
    private RouterDescription sr;

    public serverHandler(Link link) {
      this.cr = link.router1;
      this.sr = link.router2;
    }
  
    public void run() {
      while(true){
        try{
          ServerSocket serverSocket = new ServerSocket(sr.processPortNumber);
          Socket server = serverSocket.accept();
          DataInputStream in = new DataInputStream(server.getInputStream());
          System.out.println("recieved message " + in.readUTF() + "from ???");
          // set cr state to INIT or to TWO-WAY
          DataOutputStream out = new DataOutputStream(server.getOutputStream());
          out.writeUTF("HELLO");  
          serverSocket.close();
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
    }
  }
