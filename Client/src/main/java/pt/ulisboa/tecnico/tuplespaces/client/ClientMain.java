package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import io.grpc.StatusRuntimeException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.nameserver.contract.NameServerGrpc;
import pt.ulisboa.tecnico.nameserver.contract.NameServerOuterClass;
import java.util.*;

public class ClientMain {

    /** Set flag to true to print debug messages. 
	 * The flag can be set using the -Ddebug command line option. */
	private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);
	
    /** Helper method to print debug messages. */
	private static void debug(String debugMessage) {
		if (DEBUG_FLAG)
			System.err.println(debugMessage);
	}

    public static void main(String[] args) {

        System.out.println(ClientMain.class.getSimpleName());

        // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // check arguments
        if (args.length < 1 || args.length > 2) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: mvn exec:java -Dexec.args=<serviceName> <qualifiicador>(opt)");
            return;
        }

        final ManagedChannel channel;
        NameServerGrpc.NameServerBlockingStub stub;

        // channel built with well known nameServer
        channel = ManagedChannelBuilder.forTarget("localhost:5001").usePlaintext().build();

        // nameServer stub is created
        stub = NameServerGrpc.newBlockingStub(channel);

        final String serviceName = args[0];
        debug("serviceName: " + serviceName);

        try { //lookups server in the nameServer without qualifier
            NameServerOuterClass.LookupResponse response=stub.lookup(NameServerOuterClass.LookupRequest.newBuilder().setServiceName(serviceName).setQualifier("NOQUAL").build());
            if(response.getServerListCount()==0){
                System.out.println("No server matching in nameServer");
                channel.shutdownNow();
                return;
            }
            else{
                Random rand = new Random();
                int clientID = rand.nextInt(100); 

                List<String> servers = response.getServerListList();
                debug("servers: " + servers);
                CommandProcessor parser = new CommandProcessor(new ClientService(servers.size(), servers, clientID));  // initialize clientService
                parser.parseInput();
                channel.shutdownNow();
            }
        } catch (StatusRuntimeException e) {  //if name server throws exception catch it
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
            channel.shutdownNow();
            return;
        }


    }
}
