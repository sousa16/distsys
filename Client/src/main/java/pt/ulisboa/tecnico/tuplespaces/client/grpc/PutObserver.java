package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.*;

public class PutObserver<PutResponse> implements StreamObserver<TupleSpacesReplicaXuLiskov.PutResponse> {
    ResponseCollector collector;

    public PutObserver(ResponseCollector c){
        this.collector = c; 
    }
    
    @Override
    public void onNext(TupleSpacesReplicaXuLiskov.PutResponse r) {
        this.collector.add_Response(""); //add response to collector
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Received error: " + throwable);
    }

    @Override
    public void onCompleted() {
    }
}
