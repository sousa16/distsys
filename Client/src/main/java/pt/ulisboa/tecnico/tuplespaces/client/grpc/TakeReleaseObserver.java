package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.*;
import io.grpc.stub.StreamObserver;

// used for take phase 1 lock releases
public class TakeReleaseObserver<TakePhase1ReleaseResponse> implements StreamObserver<TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse> { //used for phase 1 release
    TakeCollector collector;

    public TakeReleaseObserver(TakeCollector c){
        this.collector = c;
    }
    
    @Override
    public void onNext(TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse r) {
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