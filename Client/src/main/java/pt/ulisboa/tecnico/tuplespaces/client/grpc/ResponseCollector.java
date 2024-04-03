package pt.ulisboa.tecnico.tuplespaces.client.grpc;
import java.util.*;

public class ResponseCollector {
  List<String> responses=new ArrayList<String>();
  int numResponses=0;

  public synchronized void add_Response(String res){
    synchronized (this){
      responses.add(res);
      numResponses++;
      notifyAll();      //every time someone adds response this notifies waiting function
    }

  }

  public synchronized void waitUntilAllReceived(int expectMessages){
    while(this.numResponses < expectMessages){
      try {
        wait(); // wait until messages have been received
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public List<String> getResponses(){
    return this.responses;
  }


  
}