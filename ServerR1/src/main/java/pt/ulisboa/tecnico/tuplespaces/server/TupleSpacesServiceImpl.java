package pt.ulisboa.tecnico.tuplespaces.server;

import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov;
import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.NOT_FOUND;
import java.util.List;

public class TupleSpacesServiceImpl extends TupleSpacesReplicaGrpc.TupleSpacesReplicaImplBase {

	/** Tuple Spaces implementation. */
	private static final String BGN_TUPLE = "<";
	private static final String END_TUPLE = ">";
	private ServerState serverState = new ServerState();

	private boolean inputIsValid(String[] input) {
		if (input.length < 1
				||
				!input[0].substring(0, 1).equals(BGN_TUPLE)
				||												
				!input[0].endsWith(END_TUPLE)
				||
				input.length > 1) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void put(TupleSpacesReplicaXuLiskov.PutRequest request,
			StreamObserver<TupleSpacesReplicaXuLiskov.PutResponse> responseObserver) {
		// StreamObserver is used to represent the gRPC stream between the server and
		// client in order to send the appropriate responses (or errors, if any occur).

		String tuple = request.getNewTuple();

		String[] validation = new String[1];
		validation[0] = tuple;

		if (inputIsValid(validation)) {
			serverState.put(tuple);

			TupleSpacesReplicaXuLiskov.PutResponse response = TupleSpacesReplicaXuLiskov.PutResponse.newBuilder()
					.build();

			// Send a single response through the stream.
			responseObserver.onNext(response);

			// Notify the client that the operation has been completed.
			responseObserver.onCompleted();

		} else {
			responseObserver
					.onError(INVALID_ARGUMENT.withDescription("Input has to be a valid tuple").asRuntimeException());
		}
	}

	@Override
	public void read(TupleSpacesReplicaXuLiskov.ReadRequest request,
			StreamObserver<TupleSpacesReplicaXuLiskov.ReadResponse> responseObserver) {
		// StreamObserver is used to represent the gRPC stream between the server and
		// client in order to send the appropriate responses (or errors, if any occur).

		String tuple = request.getSearchPattern();

		String[] validation = new String[1];
		validation[0] = tuple;

		if (inputIsValid(validation)) {
			TupleSpacesReplicaXuLiskov.ReadResponse response = TupleSpacesReplicaXuLiskov.ReadResponse.newBuilder()
					.setResult(serverState.read(tuple)).build();

			// Send a single response through the stream.
			responseObserver.onNext(response);
			// Notify the client that the operation has been completed.
			responseObserver.onCompleted();
		} else {
			responseObserver
					.onError(INVALID_ARGUMENT.withDescription("Input has to be a valid tuple").asRuntimeException());
		}

	}

	@Override
	public void takePhase1(TupleSpacesReplicaXuLiskov.TakePhase1Request request,
			StreamObserver<TupleSpacesReplicaXuLiskov.TakePhase1Response> responseObserver) {
		// StreamObserver is used to represent the gRPC stream between the server and
		// client in order to send the appropriate responses (or errors, if any occur).

		String tuple = request.getSearchPattern();
		int clientId = request.getClientId();

		String[] validation = new String[1];
		validation[0] = tuple;

		if (inputIsValid(validation)) {

			List<String>tuples = serverState.takePhase1(tuple, clientId);
			TupleSpacesReplicaXuLiskov.TakePhase1Response.Builder response= TupleSpacesReplicaXuLiskov.TakePhase1Response
			.newBuilder();

			if(tuples != null){	//if server accepts request
				response.addAllReservedTuples(tuples);
			}

			// Send a single response through the stream.
			responseObserver.onNext(response.build());

			// Notify the client that the operation has been completed.
			responseObserver.onCompleted();
		} else {
			responseObserver
					.onError(INVALID_ARGUMENT.withDescription("Input has to be a valid tuple").asRuntimeException());
		}
	}

	@Override
	public void takePhase1Release(TupleSpacesReplicaXuLiskov.TakePhase1ReleaseRequest request,
			StreamObserver<TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse> responseObserver) {
		// StreamObserver is used to represent the gRPC stream between the server and
		// client in order to send the appropriate responses (or errors, if any occur).

		int clientId = request.getClientId();

		serverState.takePhase1Release(clientId);
		TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse response = TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse
				.newBuilder().build();
		// Send a single response through the stream.
		responseObserver.onNext(response);
		// Notify the client that the operation has been completed.
		responseObserver.onCompleted();
	

	}

	@Override
	public void takePhase2(TupleSpacesReplicaXuLiskov.TakePhase2Request request,
			StreamObserver<TupleSpacesReplicaXuLiskov.TakePhase2Response> responseObserver) {
		// StreamObserver is used to represent the gRPC stream between the server and
		// client in order to send the appropriate responses (or errors, if any occur).

		String tuple = request.getTuple();
		int clientId = request.getClientId();

		String[] validation = new String[1];
		validation[0] = tuple;

		if (inputIsValid(validation)) {
			serverState.takePhase2(tuple, clientId);
			// Notify the client that the operation has been completed.
			// Send a single response through the stream.

			TupleSpacesReplicaXuLiskov.TakePhase2Response response = TupleSpacesReplicaXuLiskov.TakePhase2Response
					.newBuilder().build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} else {
			responseObserver
					.onError(INVALID_ARGUMENT.withDescription("Input has to be a valid tuple").asRuntimeException());
		}
	}

	@Override
	public void getTupleSpacesState(TupleSpacesReplicaXuLiskov.getTupleSpacesStateRequest request,
			StreamObserver<TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse> responseObserver) {
		// StreamObserver is used to represent the gRPC stream between the server and
		// client in order to send the appropriate responses (or errors, if any occur).

		List<String> state = serverState.getTupleSpacesState();

		if (state.size() != 0) {
			TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse response = TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse
					.newBuilder().addAllTuple(state).build();

			// Send a single response through the stream.
			responseObserver.onNext(response);
			// Notify the client that the operation has been completed.
			responseObserver.onCompleted();

		} else {
			responseObserver.onError(NOT_FOUND.withDescription("Tuple Space is empty.").asRuntimeException());
		}
	}
}
