package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import java.util.ArrayList;
import java.util.List;

public class TupleSpaceCollector {
    List<List<String>> responses=new ArrayList<List<String>>();
    int numResponses=0;

  public synchronized void add_Response(List<String> res){
    this.responses.add(res);
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

  public synchronized void incrementResponses(){
    synchronized (this){
      this.numResponses++;  //increment number of responses and notify wainting processes
      notifyAll();
    }
  }

  public List<List<String>> getResponses(){
    return this.responses;
  }
}
