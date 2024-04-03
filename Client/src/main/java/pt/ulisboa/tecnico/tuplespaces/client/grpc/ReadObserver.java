package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.*;

public class ReadObserver<ReadResponse> implements StreamObserver<TupleSpacesReplicaXuLiskov.ReadResponse> {
    ResponseCollector collector;

    public ReadObserver(ResponseCollector c){
        this.collector = c; 
    }
    
    @Override
    public void onNext(TupleSpacesReplicaXuLiskov.ReadResponse r) {
        this.collector.add_Response(r.getResult()); //add response to collector
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Received error: " + throwable);
    }

    @Override
    public void onCompleted() {
    }
}
