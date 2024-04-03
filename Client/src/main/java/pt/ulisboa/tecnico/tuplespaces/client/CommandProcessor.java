package pt.ulisboa.tecnico.tuplespaces.client;

import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import io.grpc.StatusRuntimeException;
import java.util.*;

public class CommandProcessor {

    private static final String SPACE = " ";
    private static final String BGN_TUPLE = "<";
    private static final String END_TUPLE = ">";
    private static final String PUT = "put";
    private static final String READ = "read";
    private static final String TAKE = "take";
    private static final String SLEEP = "sleep";
    private static final String SET_DELAY = "setdelay";
    private static final String EXIT = "exit";
    private static final String GET_TUPLE_SPACES_STATE = "getTupleSpacesState";

    private final ClientService clientService;

    /**
     * Set flag to true to print debug messages.
     * The flag can be set using the -Ddebug command line option.
     */
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    /** Helper method to print debug messages. */
    private static void debug(String debugMessage) {
        if (DEBUG_FLAG)
            System.err.println(debugMessage);
    }

    private int indexOfServerQualifier(String qualifier) {
        switch (qualifier) {
            case "A":
                return 0;
            case "B":
                return 1;
            case "C":
                return 2;
            default:
                return -1;
        }
    }

    public CommandProcessor(ClientService clientService) {
        this.clientService = clientService;
    }

    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String[] split = line.split(SPACE);
            debug("command: " + split[0]);
            switch (split[0]) {
                case PUT:
                    this.put(split);
                    break;

                case READ:
                    this.read(split);
                    break;

                case TAKE:
                    this.take(split);
                    break;

                case GET_TUPLE_SPACES_STATE:
                    this.getTupleSpacesState(split);
                    break;

                case SLEEP:
                    this.sleep(split);
                    break;

                case SET_DELAY:
                    this.setdelay(split);
                    break;

                case EXIT:
                    exit = true;
                    break;

                default:
                    this.printUsage();
                    break;
            }
        }
        clientService.shutdown();
        scanner.close();
    }

    private void put(String[] split) {

        // check if input is valid
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }

        // get the tuple
        String tuple = split[1];
        debug("put: " + tuple);

        // put the tuple
        try {
            clientService.put(tuple);
            System.out.println("OK\n");
            return;
        } catch (StatusRuntimeException e) {    // if put throws exception catch it
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
            return;
        }

    }

    private void read(String[] split) {

        String match;
        // check if input is valid
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }

        // get the tuple
        String tuple = split[1];
        debug("read: " + tuple);

        // read the tuple
        try {
            match = clientService.read(tuple);
            System.out.println("OK");
            System.out.println(match + "\n");
            return;
        } catch (StatusRuntimeException e) {    // if read throws exception catch it
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
            return;
        }
    }

    private void take(String[] split) {

        String match;
        // check if input is valid
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }

        // get the tuple
        String tuple = split[1];
        debug("take: " + tuple);

        // take the tuple
        try {
            match = clientService.take(tuple);

            System.out.println("OK");
            System.out.println(match + "\n");
            return;
        } catch (StatusRuntimeException e) {    //if take throws exception catch it
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
            return;
        }
    }

    private void getTupleSpacesState(String[] split) {

        List<String> tuples;

        if (split.length != 2) {    // if command is not in right format return
            this.printUsage();
            return;
        }

        String qualifier = split[1];
        debug("getTupleSpaceState: " + qualifier);

        List<String> acceptedServers = new ArrayList<>();
        acceptedServers.add("A");
        acceptedServers.add("B");
        acceptedServers.add("C");

        if (acceptedServers.contains(qualifier)) {// check if input is one of the accepted servers
            // get tuples
            try {
                tuples = clientService.getTupleSpacesState(qualifier);
                System.out.println("OK");
                System.out.println(tuples+ "\n");
                return;
            } catch (StatusRuntimeException e) { //catch exception thrown by getTupleSpace operation
                System.out.println("Caught exception with description: " + e.getStatus().getDescription());
                return;
            }
        } else {  // inform user that server must belong to (A,B,C)
            System.out.println("Invalid server qualifier, must be A, B or C");
            return;
        }
    }

    private void sleep(String[] split) {
        if (split.length != 2) {
            this.printUsage();
            return;
        }
        Integer time;

        // checks if input String can be parsed as an Integer
        try {
            time = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) { //if number is not integer throw exception
            this.printUsage();
            return;
        }

        debug("sleep: " + time);
        System.out.println("OK");

        try {
            Thread.sleep(time * 1000);  //sleep for this time
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void setdelay(String[] split) {
        if (split.length != 3) {
            this.printUsage();
            return;
        }

        int qualifier = indexOfServerQualifier(split[1]);
        if (qualifier == -1)
            System.out.println("Invalid server qualifier");

        Integer time;

        // checks if input String can be parsed as an Integer
        try {
            time = Integer.parseInt(split[2]);
        } catch (NumberFormatException e) { //if number is not integer throw exception
            this.printUsage();
            return;
        }

        debug("setDelay: " + time);
        System.out.println("OK");

        // register delay <time> for when calling server <qualifier>
        this.clientService.setDelay(qualifier, time);
        return;
    }

    private void printUsage() {
        System.out.println("Usage:\n" +
                "- put <element[,more_elements]>\n" +
                "- read <element[,more_elements]>\n" +
                "- take <element[,more_elements]>\n" +
                "- getTupleSpacesState <server>\n" +
                "- sleep <integer>\n" +
                "- setdelay <server> <integer>\n" +
                "- exit\n");
    }

    private boolean inputIsValid(String[] input) {
        if (input.length < 2
                ||
                !input[1].substring(0, 1).equals(BGN_TUPLE)
                ||
                !input[1].endsWith(END_TUPLE)
                ||
                input.length > 2) {
            this.printUsage();
            return false;
        } else {
            return true;
        }
    }
}
