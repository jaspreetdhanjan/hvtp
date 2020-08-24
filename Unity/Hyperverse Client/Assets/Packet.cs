using System;
using HVTP; 

namespace HVTP
{
    public class Packet
    {
        private PacketHeader header;
        private byte[] payload;

        public Packet(PacketHeader header, byte[] payload)
        {
            this.header = header;
            this.payload = payload;
        }

        public PacketHeader GetHeader()
        {
            return header;
        }

        public byte[] GetPayload()
        {
            return payload;
        }
    }

    public class PacketHeader
    {
    	public static readonly int SIZE_IN_BYTES = 16;
 
        private string magic;
        private int version;
        private string type;
        private int length;

        public PacketHeader(string magic, int version, string type, int length)
        {
            this.magic = magic;
            this.version = version;
            this.type = type;
            this.length = length;
        }

        public string GetMagic()
        {
            return magic;
        }

        public int GetVersion()
        {
            return version;
        }

        public string GetPacketType()
        {
            return type;
        }

        public int GetLength()
        {
            return length;
        }
    }
}