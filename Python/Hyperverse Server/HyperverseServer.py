###################################################
#       Hyperverse Transfer Protocol (HVTP)       #
###################################################
#                                                 #
# A HVTP server implementation written in Python. #
#                                                 #
# Written by:                                     #
#   Jaspreet Singh Dhanjan                        #
#                                                 #
# Organisation:                                   #
#   University College London                     #
# #################################################

import socket
import select

import HVTPConstants

# OUTDATED SERVER

class HVTPServer:
    def __init__(self, scenegraph):
        """
        The Hyperverse server requires the GLB filename in the required glTF format so
        that it can be passed to any new clients.
        """

        self.scenegraph = scenegraph

    def create(self, ip, port):
        """
        This method creates the Hyperverse server at the specified IP address and port number.
        """

        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.server_socket.bind((ip, port))

        self.server_socket.listen()

        # Keep track of the connected sockets

        self.socket_list = [self.server_socket]

        # Keep track of connected clients.
        # This is a dictionary, where key = [socket] and value = [header, name]

        self.clients = {}

        print(f"Listening for connections on {ip}:{port}...")

    def destory(self):
        """
        """

        self.server_socket.shutdown(socket.SHUT_RDWR)
        self.server_socket.close()

    def _receive_message(self, client_socket):
        """
        """

        try:
            message_header = client_socket.recv(HVTPConstants.HVTP_HEADER_SIZE)

            if len(message_header) == 0:
                return False

            message_header_decoded = message_header.decode(HVTPConstants.HVTP_ENCODING).strip()
            message_length = int(message_header_decoded)

            message = client_socket.recv(message_length)
            #message_decoded = message.decode(HVTPConstants.HVTP_ENCODING).strip()

            return {"header": message_header, "data": message}

        except:
            return False

    def update(self):
        """
        """

        # This is a blocking call that returns three sockets: read, write and exception.

        read_sockets, write_sockets, exception_sockets = select.select(self.socket_list, [], self.socket_list)

        for notified_socket in read_sockets:

            if notified_socket == self.server_socket:

                # If our notified socket in the read list is this server, then we have a new incoming connection!
                # Let's accept this request and track the client socket and client address.

                client_socket, client_address = self.server_socket.accept()

                # Client should send some init packet straight away... (USERNAME info) TODO: change this later

                user = self._receive_message(client_socket)

                # If the client disconnects right away...

                if user is False:
                    continue

                # Add accepted socket

                self.socket_list.append(client_socket)

                # Save username and username header

                self.clients[client_socket] = user

                print('Accepted new connection from {}:{}, username: {}'.format(*client_address, user["data"].decode('utf-8')))

                ###############################################################
                # Send the scene-graph

                print('Sending the glTF to -> {}:{}, username: {}'.format(*client_address, user["data"].decode('utf-8')))

                #scenegraph_encoded = self.scenegraph.encode(HVTPConstants.HVTP_ENCODING)

                scenegraph_file = open(self.scenegraph, "rb")
                scenegraph_encoded = scenegraph_file.read()
                scenegraph_file.close()

                scenegraph_header = f"{len(scenegraph_encoded):<{HVTPConstants.HVTP_HEADER_SIZE}}".encode(HVTPConstants.HVTP_ENCODING)

                client_socket.send(scenegraph_header + scenegraph_encoded)

            else:

                # If the notified socket is already in our list of sockets:

                message = self._receive_message(notified_socket)

                if message is False:
                    print('Closed connection from: {}'.format(self.clients[notified_socket]["data"].decode('utf-8')))

                    self.socket_list.remove(notified_socket)

                    del self.clients[notified_socket]

                    continue

                # Fetch username using the socket!

                user = self.clients[notified_socket]

                print(f'Received message from {user["data"].decode("utf-8")}: {message["data"].decode("utf-8")}')

                # Iterate of connected clients and broadcast message, except the sender!

                for client_socket in self.clients:
                    if client_socket != notified_socket:
                        client_socket.send(user["header"] + user["data"] + message["header"] + message["data"])


# Driver:

#scenegraph_file = open("BoxAnimated.gltf", "r")
#scenegraph_file = open("Duck.glb", "rb")
#scenegraph = scenegraph_file.read()

scenegraph = "Duck.glb"

server = HVTPServer(scenegraph)
server.create(ip="127.0.0.1", port=1234)

while True:
    server.update()

