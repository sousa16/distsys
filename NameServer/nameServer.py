class ServerEntry:
    def __init__(self, host_port, qualifier):
        self.host_port = host_port
        self.qualifier = qualifier

    def __repr__(self):
        return f"ServerEntry({self.host_port}, {self.qualifier})"


class ServiceEntry:
    def __init__(self, service_name):
        self.service_name = service_name
        self.servers = []

    def add_server(self, host_port,qualifier):
        self.servers.append(ServerEntry(host_port,qualifier))

    def __repr__(self):
        return f"ServiceEntry({self.service_name}, {self.servers})"


class NamingServer:
    def __init__(self):
        self.service_map = {}

    def add_service_entry(self, service_name):
        self.service_map[service_name] = ServiceEntry(service_name)

    def get_service_entry(self, service_name):
        return self.service_map.get(service_name)

    def __repr__(self):
        return f"NamingServer({self.service_map})"