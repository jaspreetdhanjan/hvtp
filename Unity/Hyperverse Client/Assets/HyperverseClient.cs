using System.Net.Sockets;
using System.Threading;
using UnityEngine;
using HVTP;

namespace HVTP
{
    public class HyperverseClient
    {
        private volatile bool running = false;

        private Thread thread;
        private Socket socket;
        private OnPacketReceived callbackDelegate;

        public delegate void OnPacketReceived(Packet packet);

        // public delegate void OnSceneGraphReceived(byte[] bytes);

        public void Create(string ip, int port, OnPacketReceived callbackDelegate)
        {
            if (running)
            {
                return;
            }

            socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            socket.Connect(ip, port);

            if (!socket.Connected)
            {
                Debug.Log("Connection failed!");
                return;
            }

            running = true;

            this.callbackDelegate = callbackDelegate;

            // Upon connection, the Hyperverse server will pass us the entire scene-graph under the INIT packet.
            // The client update function will run on a separate thread. 

            thread = new Thread(Run);
            thread.Start();
        }

        public void SetCallbackDelegate(OnPacketReceived callbackDelegate) {
            this.callbackDelegate = callbackDelegate;
        }

        private Packet ReceivePacket()
        {
            byte[] bytes = new byte[PacketHeader.SIZE_IN_BYTES];

            socket.Receive(bytes, 0, bytes.Length, SocketFlags.None);

            PacketHeader header = PacketSerializer.deserializeHeader(bytes);

            byte[] payload = new byte[header.GetLength()];

            socket.Receive(payload, 0, payload.Length, SocketFlags.None);

            return new Packet(header, payload);
        }

        public void SendPacket(Packet packet)
        {
            byte[] bytes = PacketSerializer.serialize(packet);
            
            socket.Send(bytes, bytes.Length, SocketFlags.None);   
        }

        public void Destroy()
        {
            if (!running)
            {
                return;
            }

            running = false;
            //thread.Join();
            thread.Abort();
            socket.Dispose();
        }

        public void Run()
        {
            while (running)
            {
                // ReceivePacket will block here until another message is passed.

                Packet packet = ReceivePacket();
                callbackDelegate(packet);
            }
        }
    }
}