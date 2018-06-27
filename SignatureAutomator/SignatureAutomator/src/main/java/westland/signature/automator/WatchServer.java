package westland.signature.automator;

import java.io.*;
import java.net.*;
import java.util.*;

public class WatchServer
{
  private final int PORT_NUMBER = 2000;
  private final int BUFFER_SIZE = 1000;

  public void run()
  {
    ServerSocket MyService;
    Socket serviceSocket = null;
    
    try{
      MyService = new ServerSocket(PORT_NUMBER,BUFFER_SIZE);
      System.out.println(MyService.getInetAddress().toString());
      while(true){
        try{
          serviceSocket = MyService.accept();
          InputStream in = serviceSocket.getInputStream();
          ArrayList<Character> message = new ArrayList<>();
          int byt;
          while((byt=in.read()) != -1){
            message.add((char)byt);
            System.out.println((char)byt);
          }
          System.out.println("----------------------");
          System.out.println("----------------------");
          System.out.println("----------------------");
        }catch(IOException e){
          System.out.println(e);
        }
      }
    }catch(IOException e){
      System.out.println(e);
    }



  }

}
