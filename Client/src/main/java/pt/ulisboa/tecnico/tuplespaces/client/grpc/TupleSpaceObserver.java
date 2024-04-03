package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.*;

public class TupleSpaceObserver<getTupleSpacesStateResponse> implements StreamObserver<TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse> {
    TupleSpaceCollector collector;

    public TupleSpaceObserver(TupleSpaceCollector c){
        this.collector = c; 
    }
    
    @Override
    public void onNext(TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse r) {
        this.collector.add_Response(r.getTupleList()); //add response to collector
        this.collector.incrementResponses();
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Received error: " + throwable);
    }

    @Override
    public void onCompleted() {
    }
}
