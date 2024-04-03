# TupleSpaces

Distributed Systems Project 2024

**Group A71**

_(choose one of the following levels and erase the other one)_  
**Difficulty level: Bring 'em on!**

### Code Identification

In all source files (namely in the *groupId*s of the POMs), replace **GXX** with your group identifier. The group
identifier consists of either A or T followed by the group number - always two digits. This change is important for
code dependency management, to ensure your code runs using the correct components and not someone else's.

### Team Members

| Number | Name        | User                            | Email                                  |
| ------ | ----------- | ------------------------------- | -------------------------------------- |
| 99286  | Miguel Mano | <https://github.com/miguelmano> | <miguelccmano@tecnico.ulisboa.pt>      |
| 99991  | Joao Sousa  | <https://github.com/sousa16>    | <joao.p.c.sousa@tecnico.ulisboa.pt>    |
| 95908  | Rita Costa  | <https://github.com/ritamcosta> | <ritamendesdacosta@tecnico.ulisboa.pt> |

## Getting Started

The overall system is made up of several modules. The different types of servers are located in _ServerX_ (where X denotes stage 1, 2 or 3).
The clients is in _Client_.
The definition of messages and services is in _Contract_. The future naming server
is in _NamingServer_.

See the [Project Statement](https://github.com/tecnico-distsys/TupleSpaces) for a complete domain and system description.

### Prerequisites

The Project is configured with Java 17 (which is only compatible with Maven >= 3.8), but if you want to use Java 11 you
can too -- just downgrade the version in the POMs.

To confirm that you have them installed and which versions they are, run in the terminal:

```s
javac -version
mvn -version
```

### Installation

To compile and install all modules:

```s
mvn clean install
```

### Configuration and Start

To start the NameServer, create and start a Virtual Environment using:

```s
python -m venv .venv
source .venv/bin/activate
```

Install the necessary packages using:

```s
python -m pip install grpcio
python -m pip install grpcio-tools
```

In the Contract directory, execute the following commands:

```s
mvn install
mvn exec:exec
```

In the NameServer directory, start the NameServer:

```s
python server.py
```

To start 3 Servers, execute the following commands in the ServerR1 directory:

```s
mvn exec:java -Dexec.args="2001 A"
mvn exec:java -Dexec.args="2002 B"
mvn exec:java -Dexec.args="2003 C"
```

For each Client you want to start, execute the following command in the Client directory:

```s
mvn exec:java
```

## Built With

- [Maven](https://maven.apache.org/) - Build and dependency management tool;
- [gRPC](https://grpc.io/) - RPC framework.
