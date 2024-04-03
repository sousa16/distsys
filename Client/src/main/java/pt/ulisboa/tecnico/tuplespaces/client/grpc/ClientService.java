package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;
import java.util.*;

public class ClientService {

  class ServerStub {
    TupleSpacesReplicaGrpc.TupleSpacesReplicaStub stub;
    String qualifier;

    public ServerStub(TupleSpacesReplicaGrpc.TupleSpacesReplicaStub s, String q) {
      this.stub = s;
      this.qualifier = q;
    }

    public void setStub(TupleSpacesReplicaGrpc.TupleSpacesReplicaStub s) {
      this.stub = s;
    }

    public void setQualifier(String q) {
      this.qualifier = q;
    }

  }

  private ServerStub[] stubs = new ServerStub[3];
  private ManagedChannel[] channels = new ManagedChannel[3];
  private OrderedDelayer delayer;
  private int numServers;
  private int clientID;

  /**
   * Set flag to true to print debug messages.
   * The flag can be set using the -Ddebug command line option.
   */
  private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

  /** Helper method to print debug messages. */
  private static void debug(String debugMessage) {
    if (DEBUG_FLAG)
      System.err.println(debugMessage);
  }

  public ClientService(int nServers, List<String> servers, int cID) {

    this.numServers = nServers;

    int serverIndex = 0;

    for (String server : servers) { // for loop to go through server list and create conection to said server

      String[] serverDetails = server.split(";");

      // Channel is the abstraction to connect to a service endpoint.
      // Let us use plaintext communication because we do not have certificates.

      this.channels[serverIndex] = ManagedChannelBuilder.forTarget(serverDetails[0]).usePlaintext().build();

      // It is up to the client to determine whether to block the call.
      // Here we create a non blocking stub
      TupleSpacesReplicaGrpc.TupleSpacesReplicaStub stub = TupleSpacesReplicaGrpc.newStub(channels[serverIndex]);
      this.stubs[serverIndex] = new ServerStub(stub, serverDetails[1]);

      debug("connected to servers: " + server);
      serverIndex++;
    }

    this.clientID = cID;

    this.delayer = new OrderedDelayer(this.numServers);
  }

  public void put(String tuple) {
    ResponseCollector collector = new ResponseCollector(); // creates observer that references collector
    PutObserver<TupleSpacesReplicaXuLiskov.PutResponse> observer = new PutObserver<TupleSpacesReplicaXuLiskov.PutResponse>(
        collector);

    debug("put input: " + tuple);

    for (int i : delayer) {
      this.stubs[i].stub.put(TupleSpacesReplicaXuLiskov.PutRequest.newBuilder().setNewTuple(tuple).build(), observer); // sends the message to servers
    }

    collector.waitUntilAllReceived(this.numServers); // waits for all the servers to respond to continue normal execution
    return;

  }

  public String read(String tuple) {

    ResponseCollector collector = new ResponseCollector(); // creates observer that references collector
    ReadObserver<TupleSpacesReplicaXuLiskov.ReadResponse> observer = new ReadObserver<TupleSpacesReplicaXuLiskov.ReadResponse>(
        collector);

    debug("read input: " + tuple);

    for (int i : delayer) {
      this.stubs[i].stub.read(TupleSpacesReplicaXuLiskov.ReadRequest.newBuilder().setSearchPattern(tuple).build(), observer); // sends the message to servers
    }

    collector.waitUntilAllReceived(1); // waits for atleast 1 message to continue normal execution
    String match = collector.responses.get(0);
   

    return match;

  }

  public String take(String tuple) {
    debug("take input: " + tuple);

    String tupleToRemove = "";
    boolean inPhase1 = true;
    
    while (inPhase1) { //phase1
      TakeCollector collector = new TakeCollector(); // creates observer that references collector

      TakeObserver<TupleSpacesReplicaXuLiskov.TakePhase1Response> observer1 = new TakeObserver<TupleSpacesReplicaXuLiskov.TakePhase1Response>(
        collector);
      
      for (int i : delayer) { // start phase 1
        this.stubs[i].stub.takePhase1(TupleSpacesReplicaXuLiskov.TakePhase1Request.newBuilder().setSearchPattern(tuple)
            .setClientId(this.clientID).build(), observer1);
      }
      collector.waitUntilAllReceived(this.numServers);  //waits for answers from all servers
  
      List<List<String>> reservedTuples = collector.responses;

      if ((reservedTuples.size() / numServers )< 0.5) {   //if only minority of the servers accepted request free servers that accepted

        TakeCollector releaseCollector = new TakeCollector();
        TakeReleaseObserver<TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse> releaseObserver = new TakeReleaseObserver<TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse>(
        releaseCollector);
      
        for (int i : delayer) { // release locks
          this.stubs[i].stub.takePhase1Release(
          TupleSpacesReplicaXuLiskov.TakePhase1ReleaseRequest.newBuilder().setClientId(this.clientID).build(), releaseObserver);
        } 
        releaseCollector.waitUntilAllReceived(this.numServers);
      }
      else if(reservedTuples.size() == this.numServers){    //if all servers accept request

        int emptycounter = 0;
        for(List<String> l: reservedTuples){  //check if there is at least one empty list in server answers 
          if(l.size()==0){                     //if there is atleast one empty list this means that the intesection of all will be empty
            emptycounter++;                    //therefore take request remains in fase1
            break;
          }
        }

        if(emptycounter == 0){//there are no empty lists

          List<String> response1 = reservedTuples.get(0);
          for(int i=1; i<reservedTuples.size(); i++){
            response1.retainAll(reservedTuples.get(i)); //execute intersection
          }

          if(response1.size() != 0){
            tupleToRemove=response1.get(0); // if there is at least 1 match save the first one
            inPhase1=false;     //take request is ready to leave phase1
          }
        }
      }
    }

    TakeCollector phase2Collector = new TakeCollector();
    TakePhase2Observer<TupleSpacesReplicaXuLiskov.TakePhase2Response> phase2Observer = new TakePhase2Observer<TupleSpacesReplicaXuLiskov.TakePhase2Response>(
        phase2Collector);

    for (int i : delayer) { // start phase 2
     this.stubs[i].stub.takePhase2(
          TupleSpacesReplicaXuLiskov.TakePhase2Request.newBuilder().setTuple(tupleToRemove).setClientId(this.clientID).build(),
          phase2Observer);
    }
    phase2Collector.waitUntilAllReceived(this.numServers); //waits for answers from all servers 

    return tupleToRemove;
  }

  public List<String> getTupleSpacesState(String qualifier) {
    List<String> tuples;

    TupleSpaceCollector collector = new TupleSpaceCollector(); // creates observer that references collector
    TupleSpaceObserver<TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse> observer = new TupleSpaceObserver<TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse>(
        collector);

    for (ServerStub s : this.stubs) {
      if (s.qualifier.equals(qualifier)) {
        s.stub.getTupleSpacesState(TupleSpacesReplicaXuLiskov.getTupleSpacesStateRequest.newBuilder().build(),
            observer); // sends the message to servers
        break;
      }
    }

    collector.waitUntilAllReceived(1); // waits for atleast 1 message to continue normal execution
    tuples = collector.responses.get(0);

    return tuples;
  }

  /*
   * Example: How to use the delayer before sending requests to each server
   * Before entering each iteration of this loop, the delayer has already
   * slept for the delay associated with server indexed by 'id'.
   * id is in the range 0..(numServers-1).
   * 
   * for (Integer id : delayer) {
   * //stub[id].some_remote_method(some_arguments);
   * }
   */

  public void setDelay(int qualifier, int delay) {

    debug("delay input: " + qualifier + " " + delay);

    delayer.setDelay(qualifier, delay);

    System.out.println("[Debug only]: After setting the delay, I'll test it");
    for (Integer i : delayer) {
      System.out.println("[Debug only]: Now I can send request to stub[" + i + "]");
    }
    System.out.println("[Debug only]: Done.");

    return;
  }

  public void shutdown() {
    for (int i = 0; i < numServers; i++) {
      this.channels[i].shutdownNow(); // shutsdown the channels of each server
    }
    return;
  }

}
