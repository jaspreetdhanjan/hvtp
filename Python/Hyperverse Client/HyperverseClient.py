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
import io
import struct
import os

from pyrender import Mesh, Scene, Viewer, PointLight, SpotLight, DirectionalLight, Node

from io import BytesIO
import numpy as np
import trimesh

import HVTPConstants

class HVTPClient:
    def __init__(self):
        """
        """

        self.scene = Scene(ambient_light=np.array([0.02, 0.02, 0.02, 1.0]))
        self.scene.add(PointLight(color=[0.5, 0.2, 0.3], intensity=2.0))
        self.scene.add(SpotLight(color=[0.1, 0.6, 0.3], intensity=2.0, innerConeAngle=0.05, outerConeAngle=0.5))
        self.scene.add(DirectionalLight(color=[0.33, 0.33, 0.33], intensity=2.0))

        self.root_node = None
        # self.scene.add_node(self.root_node)

    def create(self, ip, port):
        """
        """

        self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.client_socket.connect((ip, port))
        self.client_socket.setblocking(True)

    def destroy(self):
        """
        """

        self.client_socket.shutdown(socket.SHUT_RDWR)
        self.client_socket.close()

    def run(self):
        """
        """
        self.viewer = Viewer(self.scene, use_raymond_lighting=False, cull_faces=False, run_in_thread=True)

        threading.Thread(target=self.accept_messages).start()

    def accept_messages(self):
        """
        Polls our socket for changes that have been sent by the server.
        """

        print("Starting message acceptor")

        while True:
            try:
                packet_header = self.client_socket.recv(HVTPConstants.HVTP_HEADER_SIZE_IN_BYTES)

                print("Received!")

                if len(packet_header) == 0:
                    print("Connection closed by server")
                    sys.exit()
                
                # Read the header

                i = 0
                offs = 4

                hvtp_magic = packet_header[(i*offs):(offs + i*offs)].decode(HVTPConstants.HVTP_ENCODING)
                i += 1
                hvtp_version = int.from_bytes(packet_header[(i*offs):(offs + i*offs)], byteorder='big', signed=False)
                i += 1
                hvtp_length = int.from_bytes(packet_header[(i*offs):(offs + i*offs)], byteorder='big', signed=False)
                i += 1
                hvtp_type = packet_header[(i*offs):(offs + i*offs)].decode(HVTPConstants.HVTP_ENCODING)

                print(hvtp_magic)
                print(hvtp_version)
                print(hvtp_length)
                print(hvtp_type)

                # Read the rest of the packet

                payload = self.client_socket.recv(hvtp_length)

                loaded_payload = trimesh.load(BytesIO(payload), file_type='glb')
               
                mesh = Mesh.from_trimesh(list(loaded_payload.dump()))
                
                # Add to the scene

                self.add_to_scene(mesh)
                
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

    def add_to_scene(self, mesh):
        """
        """

        # Grab the viewer mutex

        self.viewer.render_lock.acquire()

        # Remove anything before and insert the mesh into the scene-graph

        if self.root_node != None:
            self.scene.remove_node(self.root_node)

        self.root_node = self.scene.add(mesh)

        # Release the viewer mutex

        self.viewer.render_lock.release()



# Driver:

client = HVTPClient()
client.create(ip="localhost", port=8088)

client.run()
