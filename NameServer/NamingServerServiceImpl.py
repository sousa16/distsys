import sys
sys.path.insert(1, '../contract/target/generated-sources/protobuf/python')
import grpc
import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc
from nameServer import NamingServer, ServiceEntry, ServerEntry

def inputIsValid(target):
        hostNport=target.split(":")
        if(int(hostNport[1])>65336 or int(hostNport[1])<0):
                return False
        return True

class NamingServerServiceImpl(pb2_grpc.NameServerServicer):
    nameServer=NamingServer()

    def __init__(self, *args, **kwargs):
        pass

    def register(self, request, context):
        # get service name
        serviceName = request.serviceName

        # get server qualifier
        qualifier = request.qualifier
        
        # get host and port
        target= request.target

        # print input
        print("register: "+serviceName+ " " +qualifier+ " " +target)
        
        response = pb2.RegisterResponse()

        if(inputIsValid(target)):
            if( serviceName in self.nameServer.service_map.keys()):           #if service is registered in nameServer adds a new server to that service
                self.nameServer.service_map[serviceName].add_server(target,qualifier)
            
            else:                                                              # service isnt registered in nameServer so we register it and add a server to that service
                self.nameServer.add_service_entry(serviceName)
                self.nameServer.service_map[serviceName].add_server(target,qualifier)
            # return response
            return response
        else:                                                       #if input is invalid an exception is thrown 
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details("Not possible to register the server!")
            
            # return response
            return response
        
    def lookup(self, request, context):

        # get service name
        serviceName = request.serviceName

        response = pb2.LookupResponse()

        if( serviceName  in self.nameServer.service_map.keys()):

            sList = self.nameServer.service_map[serviceName].servers
            if(request.qualifier == "NOQUAL"):                       # if message has no qualifier and service exists this returns said service servers
                print("lookup: "+serviceName)
                for server in sList:
                    response.serverList.append(server.host_port+";"+server.qualifier)
                # return response
                return response
            else:
                qualifier= request.qualifier
                # print input
                print("lookup: "+serviceName+ " " +qualifier)
                if (any(server.qualifier==qualifier for server in sList)):    #if qualifier exists answer servers that have that qualifier
                    for server in sList:
                        if (server.qualifier==qualifier):
                            response.serverList.append(server.host_port)
                    # return response
                    return response
                else:                                   #if qualifier doesnt exist answer empty list
                    # return response
                    return response          
        else:                                       #if service doesnt exist answer empty list    
            # return response
            return response


def delete(self, request, context):

        # get service name
        serviceName = request.serviceName
        
        # get host and port
        target= request.target

        # print input
        print("delete: "+serviceName+ " " +target)

        hostNport=target.split(":")
        if(int(hostNport[1])<=65336 and int(hostNport[1])>=0):
            if( serviceName in self.nameServer.service_map.keys()):           #service is registered in nameServer
                
                removeCounter=0
                for server in self.nameServer.service_map[serviceName].servers:
                    if (server.host_port==target):
                         self.nameServer.service_map[serviceName].servers.remove(server)
                         removeCounter+=1
                
                if (removeCounter>0):                                       #if there is any server with matching target that got removed answers empty list
                    response = pb2.DeleteResponse()     
                    # return response
                    return response
                else:                                                      #if there isnt any server with matching target an exception is thrown
                    context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
                    context.set_details("Not possible to remove the server!")
            
                    # return response
                    return response
                             
            else:                                                              # service isnt registered in nameServer so there are no servers associated with it, an exception is thrown
                context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
                context.set_details("Not possible to remove the server!")
            
                # return response
                return response
        else:                                                       #if input is invalid an exception is thrown 
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details("Not possible to remove the server!")
            
            # return response
            return response
        
        
            