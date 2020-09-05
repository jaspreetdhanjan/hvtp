using System;
using System.IO;
using System.Text;
using System.Net;
using UnityEngine;
using HVTP;

namespace HVTP
{
    public class PacketSerializer
    {
        private PacketSerializer()
        {
        }

        public static byte[] serialize(Packet packet) {
            MemoryStream stream = new MemoryStream();
            BinaryWriter writer = new BinaryWriter(stream);

            PacketHeader header = packet.GetHeader();

            writer.Write(System.Text.Encoding.ASCII.GetBytes(header.GetMagic()));

            writer.Write(IPAddress.HostToNetworkOrder(header.GetVersion()));

            writer.Write(IPAddress.HostToNetworkOrder(header.GetLength()));

            writer.Write(System.Text.Encoding.ASCII.GetBytes(header.GetPacketType()));

            writer.Write(packet.GetPayload());

            return stream.ToArray();
        }

        public static PacketHeader deserializeHeader(byte[] bytes) {
            BinaryReader reader = new BinaryReader(new MemoryStream(bytes));

            string magic = System.Text.Encoding.ASCII.GetString(reader.ReadBytes(4));

            int version = IPAddress.NetworkToHostOrder(reader.ReadInt32());

            int length = IPAddress.NetworkToHostOrder(reader.ReadInt32());

            string type = System.Text.Encoding.ASCII.GetString(reader.ReadBytes(4));

            return new PacketHeader(magic, version, type, length);
        }

        public static byte[] ToTrnsPayload(Quaternion rotation, Vector3 scale, Vector3 position, string nodeName)
        {
            MemoryStream stream = new MemoryStream();
            BinaryWriter writer = new BinaryWriter(stream);

            Debug.Log("Rotation is: " + rotation);
            Debug.Log("Scale is: " + scale);
            Debug.Log("Position is: " + position);

            int rx = BitConverter.ToInt32(BitConverter.GetBytes(rotation.x), 0);
            int ry = BitConverter.ToInt32(BitConverter.GetBytes(rotation.y), 0);
            int rz = BitConverter.ToInt32(BitConverter.GetBytes(rotation.z), 0);
            int rw = BitConverter.ToInt32(BitConverter.GetBytes(rotation.w), 0);

            int sx = BitConverter.ToInt32(BitConverter.GetBytes(scale.x), 0);
            int sy = BitConverter.ToInt32(BitConverter.GetBytes(scale.y), 0);
            int sz = BitConverter.ToInt32(BitConverter.GetBytes(scale.z), 0);
            int sw = BitConverter.ToInt32(BitConverter.GetBytes(1.0f), 0);

            int px = BitConverter.ToInt32(BitConverter.GetBytes(position.x), 0);
            int py = BitConverter.ToInt32(BitConverter.GetBytes(position.y), 0);
            int pz = BitConverter.ToInt32(BitConverter.GetBytes(position.z), 0);
            int pw = BitConverter.ToInt32(BitConverter.GetBytes(1.0f), 0);

            writer.Write(IPAddress.HostToNetworkOrder(rx));
            writer.Write(IPAddress.HostToNetworkOrder(ry));
            writer.Write(IPAddress.HostToNetworkOrder(rz));
            writer.Write(IPAddress.HostToNetworkOrder(rw));

            writer.Write(IPAddress.HostToNetworkOrder(sx));
            writer.Write(IPAddress.HostToNetworkOrder(sy));
            writer.Write(IPAddress.HostToNetworkOrder(sz));
            writer.Write(IPAddress.HostToNetworkOrder(sw));

            writer.Write(IPAddress.HostToNetworkOrder(px));
            writer.Write(IPAddress.HostToNetworkOrder(py));
            writer.Write(IPAddress.HostToNetworkOrder(pz));
            writer.Write(IPAddress.HostToNetworkOrder(pw));

            writer.Write(nodeName);

            return stream.ToArray();
        }
    }
}