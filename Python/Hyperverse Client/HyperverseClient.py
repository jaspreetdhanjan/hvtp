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

import collections
import socket
import select
import errno
import sys
import threading
import io
import struct
import os

from pyrender import Mesh, Scene, Viewer, PointLight, SpotLight, DirectionalLight, Node
import numpy as np

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
        self.reference_dictionary = {}

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

                # Perform the relevant action on the payload

                if hvtp_type == "INIT":
                    self.loaded_payload = trimesh.load(BytesIO(payload), file_type='glb')

                    # Clear anything we had before

                    self.viewer.render_lock.acquire()

                    for key in self.reference_dictionary.keys():
                        self.scene.remove_node(self.reference_dictionary[key])

                    self.viewer.render_lock.release()

                    # Re add

                    self.reference_dictionary = self.dump_to_dictionary(self.loaded_payload)

                    # mesh = Mesh.from_trimesh(list(self.reference_dictionary.values()))

                    # self.add_to_scene(list(reference_dictionary.values()))

                elif hvtp_type == "TRNS":
                    i = 0
                    offs = 4

                    # Rotation quaternion
                    rx = struct.unpack('>f', payload[(i*offs):(offs + i*offs)])[0]
                    i += 1
                    ry = struct.unpack('>f', payload[(i*offs):(offs + i*offs)])[0]
                    i += 1
                    rz = struct.unpack('>f', payload[(i*offs):(offs + i*offs)])[0]
                    i += 1
                    rw = struct.unpack('>f', payload[(i*offs):(offs + i*offs)])[0]
                    i += 1

                    # Scale vector
                    sx = struct.unpack('>f', payload[(i*offs):(offs + i*offs)])[0]
                    i += 1
                    sy = struct.unpack('>f', payload[(i*offs):(offs + i*offs)])[0]
                    i += 1
                    sz = struct.unpack('>f', payload[(i*offs):(offs + i*offs)])[0]
                    i += 1
                    sw = struct.unpack('>f', payload[(i*offs):(offs + i*offs)])[0]
                    i += 1

                    # Position vector
                    px = struct.unpack('>f', payload[(i*offs):(offs + i*offs)])[0]
                    i += 1
                    py = struct.unpack('>f', payload[(i*offs):(offs + i*offs)])[0]
                    i += 1
                    pz = struct.unpack('>f', payload[(i*offs):(offs + i*offs)])[0]
                    i += 1
                    pw = struct.unpack('>f', payload[(i*offs):(offs + i*offs)])[0]
                    i += 1

                    # UUID
                    uuid = payload[(i*offs):len(payload)].decode(HVTPConstants.HVTP_ENCODING)
                    uuid = uuid[1:]

                    if uuid == "RootScene":
                        return

                    print("UUID: " + uuid)

                    quaternion = np.array([rx, ry, rz, rw])
                    scale_vector = np.array([sx, sy, sz])
                    translation_vector = np.array([px, py, pz])

                    print(quaternion)
                    print(scale_vector)
                    print(translation_vector)
                    # print(pw)

                    self.reference_dictionary[uuid].rotation = quaternion
                    self.reference_dictionary[uuid].scale = scale_vector
                    self.reference_dictionary[uuid].translation = translation_vector
                
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


    def dump_to_dictionary(self, scene):
        dic = {}

        self.viewer.render_lock.acquire()

        for node_name in scene.graph.nodes_geometry:
            transform, geometry_name = scene.graph[node_name]

            current = scene.geometry[geometry_name].copy()
            current.apply_transform(transform)

            dic[node_name] = self.scene.add(Mesh.from_trimesh(current))

        self.viewer.render_lock.release()

        return dic


# Driver:

client = HVTPClient()
client.create(ip="localhost", port=8088)

client.run()
