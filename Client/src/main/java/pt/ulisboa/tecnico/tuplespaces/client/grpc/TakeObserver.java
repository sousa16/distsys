package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.*;
import io.grpc.stub.StreamObserver;
import java.util.*;

public class TakeObserver<TakePhase1Response> implements StreamObserver<TupleSpacesReplicaXuLiskov.TakePhase1Response> { //used for phase 1
    TakeCollector collector;

    public TakeObserver(TakeCollector c){
        this.collector = c;
    }
    
    @Override
    public void onNext(TupleSpacesReplicaXuLiskov.TakePhase1Response r) {
        List<String>response=r.getReservedTuplesList();
        this.collector.add_Response(response);
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
