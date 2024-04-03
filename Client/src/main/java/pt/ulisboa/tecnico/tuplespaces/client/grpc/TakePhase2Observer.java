package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.*;
import io.grpc.stub.StreamObserver;

public class TakePhase2Observer<TakePhase2Response> implements StreamObserver<TupleSpacesReplicaXuLiskov.TakePhase2Response> { //used for phase 2
    TakeCollector collector;

    public TakePhase2Observer(TakeCollector c){
        this.collector = c;
    }
    
    @Override
    public void onNext(TupleSpacesReplicaXuLiskov.TakePhase2Response r) {
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Received error: " + throwable);
    }

    @Override
    public void onCompleted() {
        collector.incrementResponses();
    }
}