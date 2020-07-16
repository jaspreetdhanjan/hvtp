###################################################
#       Hyperverse Transfer Protocol (HVTP)       #
###################################################
#                                                 #
# A HVTP client implementation written in Python. #
#                                                 #
# Written by:                                     #
#   Jaspreet Singh Dhanjan                        #
#                                                 #
# Organisation:                                   #
#   University College London                     #
# #################################################

import socket
import select
import errno
import sys
import threading

from pyrender import Mesh, Scene, Viewer
from io import BytesIO
import numpy as np
import trimesh
import requests

import HVTPConstants

class HVTPClient:
    def __init__(self, username):
        """
        """

        self.username = username

    def create(self, ip, port):
        """
        """

        self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.client_socket.connect((ip, port))
        self.client_socket.setblocking(False)

        # Prepare our init packet TODO: change from username to something more sensible..

        encoded_username = self.username.encode(HVTPConstants.HVTP_ENCODING)
        username_header = f"{len(encoded_username):<{HVTPConstants.HVTP_HEADER_SIZE}}".encode(HVTPConstants.HVTP_ENCODING)
        self.client_socket.send(username_header + encoded_username)

    def destroy(self):
        """
        """

        self.client_socket.shutdown(socket.SHUT_RDWR)
        self.client_socket.close()

    def run(self):
        """
        """

        #threading.Thread(target=self.update).start()

        threading.Thread(target=self.accept_messages).start()

    def update(self):
        """
        We just use this to send messages to the server so that it can distribute them along the clients.
        TODO Probably won't implement this properly until very later...
        """

        while True:
            new_message = input(f'{self.username} > ')

            if new_message:
                new_message = new_message.encode(HVTPConstants.HVTP_ENCODING)
                new_message_header = f"{len(new_message):<{HVTPConstants.HVTP_HEADER_SIZE}}".encode(HVTPConstants.HVTP_ENCODING)
                self.client_socket.send(new_message_header + new_message)
    
    def accept_messages(self):
        """
        Polls our socket for changes that have been sent by the server.
        """

        while True:
            try:
#                username_header = self.client_socket.recv(HVTPConstants.HVTP_HEADER_SIZE)

#                if len(username_header) == 0:
#                    print("Connection closed by server")
#                    sys.exit()

#                username_length = int(username_header.decode(HVTPConstants.HVTP_ENCODING).strip())
#                username = self.client_socket.recv(username_length)

#                message_header = self.client_socket.recv(HVTPConstants.HVTP_HEADER_SIZE)
#                message_length = int(message_header.decode(HVTPConstants.HVTP_ENCODING).strip())
#                message = self.client_socket.recv(message_length).decode(HVTPConstants.HVTP_ENCODING)

#                print(f'{username} > {message}')

                scenegraph_header = self.client_socket.recv(HVTPConstants.HVTP_HEADER_SIZE)

                if len(scenegraph_header) == 0:
                    print("Connection closed by server")
                    sys.exit()
                
                scenegraph_length = int(scenegraph_header.decode(HVTPConstants.HVTP_ENCODING).strip())

                scenegraph = self.client_socket.recv(scenegraph_length)

                print("Recieved a new glTF file!!")

                duck = trimesh.load(BytesIO(scenegraph), file_type='glb')
                duckmesh = Mesh.from_trimesh(list(duck.geometry.values())[0])
                scene = Scene(ambient_light=np.array([1.0, 1.0, 1.0, 1.0]))
                scene.add(duckmesh)
                Viewer(scene)
                
            except IOError as e:
                # This is normal on non blocking connections - when there are no incoming data error is going to be raised
                # Some operating systems will indicate that using AGAIN, and some using WOULDBLOCK error code
                # We are going to check for both - if one of them - that's expected, means no incoming data, continue as normal
                # If we got different error code - something happened
        
                if e.errno != errno.EAGAIN and e.errno != errno.EWOULDBLOCK:
                    print('Reading error: {}'.format(str(e)))
                    sys.exit()

            except Exception as e:
                # Any other exception - something happened, exit
                print('Reading error: {}'.format(str(e)))
                sys.exit()






# Driver:

username = "jaspreet - python"

client = HVTPClient(username)
client.create(ip="127.0.0.1", port=1234)

client.run()
