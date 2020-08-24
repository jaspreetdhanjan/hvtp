using System.IO;
using System.Text;
using System.Net;
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
    }
}