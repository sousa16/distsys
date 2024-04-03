package pt.ulisboa.tecnico.tuplespaces.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.nameserver.contract.NameServerGrpc;
import pt.ulisboa.tecnico.nameserver.contract.NameServerOuterClass;

import java.io.IOException;

public class ServerMain {

    public static void main(String[] args) throws IOException, InterruptedException  {
      // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // check arguments
        if (args.length < 2) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: mvn exec:java -Dexec.args=<port> <qualifier>");
            return;
        }

        final BindableService impl = new TupleSpacesServiceImpl(); 

        // get the host and the port

        final int port = Integer.valueOf(args[0]);
        final String qualifier = args[1];
        final String host;
        final String service;
        if(args.length == 2){
            host = "localhost";
            service = "TupleSpaces";
        }
        else{
            host = args[2];
            service = args[3];
        }
    

        // Create a new server to listen on port
		Server server = ServerBuilder.forPort(port).addService(impl).build();
      
        // Start the server
        server.start();

	    // Server threads are running in the background.
		System.out.println("Server started");


        final ManagedChannel channel;
        NameServerGrpc.NameServerBlockingStub stub;

        // channel built with well known nameServer
        channel = ManagedChannelBuilder.forTarget("localhost:5001").usePlaintext().build();

        // nameServer stub is created
        stub = NameServerGrpc.newBlockingStub(channel);

        try { //registers the server in the nameServer
            stub.register(NameServerOuterClass.RegisterRequest.newBuilder().setServiceName(service).setQualifier(qualifier).setTarget(host+":"+port).build());
            System.out.println("Server registered in nameServer");
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
            return;
        }
        
        // Do not exit the main thread. Wait until server is terminated.
        server.awaitTermination();
        
        try {//deletes server from nameServer
            stub.delete(NameServerOuterClass.DeleteRequest.newBuilder().setServiceName(service).setTarget(host+":"+port).build());
            System.out.println("Server deleted from nameServer");
            channel.shutdown();
            return;
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
            return;
    
        }
    }
}

