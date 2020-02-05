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
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;


public class ClientHandler implements Runnable {
    //private RouterDescription cr;
    private RouterDescription cr;
    public ClientHandler(RouterDescription cr) {
      this.cr = cr;
      //System.out.println(cr.simulatedIPAddress);
    }
  
    public void run() {
      //System.out.println("Starting Client to: " + cr.simulatedIPAddress.toString());
      while(true){

        try{
          //System.out.println("HELLO");
          Socket client = new Socket(cr.simulatedIPAddress, cr.processPortNumber);
          // set cr state to INIT or to TWO-WAY
          System.out.println("Just connected to" + client.getRemoteSocketAddress());
          DataOutputStream out = new DataOutputStream(client.getOutputStream());


          out.writeUTF("HELLO from" + client.getLocalSocketAddress());
          InputStream inFromServer = client.getInputStream();
          DataInputStream in = new DataInputStream(inFromServer);
          System.out.println("Server says " + in.readUTF());
          client.close();
          

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