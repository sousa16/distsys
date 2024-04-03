package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.List;

public class ServerState {

  private List<String> tuples;
  private boolean lock;
  private int clientID;

  public ServerState() {
    this.tuples = new ArrayList<String>();
    this.lock=false;
    this.clientID = -1;
  }

  public synchronized void put(String tuple) {
    synchronized (this) {
      tuples.add(tuple);
      System.out.println("REGISTERED TUPLE: " + tuple);
      notifyAll();
    }
  }

  private String getMatchingTuple(String pattern) {
    for (String s : this.tuples) {
      if (s.matches(pattern)) {
        return s;
      }
    }
    return null;
  }

  public synchronized String read(String pattern) {
    String match;
    while (this.getMatchingTuple(pattern) == null) {
      try {
        wait(); // wait until there is a match
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    match = this.getMatchingTuple(pattern);
    System.out.println("READ PATTERN: " + pattern + " MATCH: " + match);
    return match;
  }

  // Get available (and unlocked) matching tuples
  private List<String> getFreeMatchingTuples(String pattern) {
    List<String> availableTuples = new ArrayList<String>();
    for (String s : this.tuples) {
      if (s.matches(pattern)) {
        availableTuples.add(s);
      }
    }
    return availableTuples;

  }

  public synchronized List<String> takePhase1(String pattern, int clientId) {
    if(this.lock == false || this.clientID == clientId ){
      this.lock = true;
      this.clientID = clientId;
      return getFreeMatchingTuples(pattern);
    }
    return null;
  }

  public synchronized void takePhase1Release(int clientId) {
    if(this.clientID == clientId){
      this.clientID = -1;
      this.lock = false;
    }
  }

  public synchronized void takePhase2(String tuple, int clientId) {
    System.out.println("TAKE TUPLE: " + tuple);
    if(this.clientID == clientId){
      tuples.remove(tuple);
      this.lock = false;
      this.clientID = -1;
    }
  }

  public synchronized List<String> getTupleSpacesState() {
    for (String s : this.tuples) {
      System.out.println("Tuple is in TupleSpace: " + s);
    }
    return this.tuples;
  }
}
