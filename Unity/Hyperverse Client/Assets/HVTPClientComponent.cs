using System;
using System.IO;
using System.Collections;
using System.Threading;
using System.Collections.Generic;
using System.Net;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEditor;
using UnityGLTF;
using HVTP;
using GLTF;
using GLTF.Extensions;
using GLTF.Schema;
using GLTF.Utilities;

/*
 The Hyperverse Transfer Protocol (HVTP)

 A HVTP client implementation written for the Unity development environment.

 Please attach the script to the scene.

 If there are no attached HVTPClientPlugin, then this client will connect to the server and retrieve
 the active scene-graph (if there is one).

 Written by:
    Jaspreet Singh Dhanjan
 
 Organisation:
    University College London
*/

public class HVTPClientComponent : MonoBehaviour
{
    private HyperverseClient client = new HyperverseClient();

    public GameObject hyperverseRoot;

    public static bool IsDirty = false;


    private string RetrieveTexturePath(UnityEngine.Texture texture)
    {
        return AssetDatabase.GetAssetPath(texture);
    }
    
    public void OnPacketReceived(Packet packet)
    {
        PacketHeader header = packet.GetHeader();

        string type = header.GetPacketType();

        if (type == "INIT")
        {
            Debug.Log("Received INIT packet. Reloading the scene...");
            PrintHeader(header);


            // We have to use the main thread to schedule this work. 
            // Ideally, this entire callback should be running on the main thread.
            // This will do for now.

            // https://wiki.appodeal.com/en/unity/run-callbacks-in-main-unity-thread

            UnityMainThreadDispatcher.Instance().Enqueue(LoadGLB(packet.GetPayload()));
        }
        else if (type == "UPDT")
        {
            Debug.Log("Update packet received");
        }
        else if (type == "TRNS")
        {
            Debug.Log("Trans packet received");
        }
        else
        {
            Debug.Log("Problem: This packet type is not supported! Something has gone wrong.");
            PrintHeader(header);
        }
    }

    private void PrintHeader(PacketHeader header)
    {
        Debug.Log(header.GetMagic());
        Debug.Log(header.GetVersion());
        Debug.Log(header. GetLength());
        Debug.Log(header.GetPacketType());
    }

    private IEnumerator LoadGLB(byte[] bytes) {
        // Define the publiclyVisible=true, otherwise will get an error.
        // https://stackoverflow.com/questions/1646193/why-does-memorystream-getbuffer-always-throw

        Stream stream = new MemoryStream(bytes, 0, bytes.Length, false, true);

		GLTFRoot gLTFRoot;
        GLTFParser.ParseJson(stream, out gLTFRoot);

        Debug.Log("Successfully parsed INIT packet!");

        GLTFSceneImporter loader = new GLTFSceneImporter(gLTFRoot, null, null, stream);
        loader.Collider = GLTFSceneImporter.ColliderType.Box;
		loader.IsMultithreaded = false;

        loader.LoadSceneAsync().Wait();

        GameObject obj = loader.LastLoadedScene;
        obj.transform.parent = hyperverseRoot.transform;
        
        Debug.Log("Loaded scene from INIT packet!");

        // TEST:
        ResetSceneGraph();

        yield return null;
    }

    private byte[] SaveGLB() {
        Transform[] transforms = GetAllChildren(hyperverseRoot.transform);

        GLTFSceneExporter exporter = new GLTFSceneExporter(transforms, RetrieveTexturePath);

        return exporter.SaveGLB("hyperverse_loadout");
    }

    private Transform[] GetAllChildren(Transform transform)
    {
        List<Transform> transformations = new List<Transform>();

        foreach (Transform t in transform)
        {
            transformations.Add(t);

            if (t.childCount > 0)
            {
                transformations.AddRange(GetAllChildren(t));
            }
        }

        return transformations.ToArray();
    }

    // Start is called before the first frame update
    void Start()
    {
        client.Create("localhost", 8088, OnPacketReceived);

        // Check for changes in the scene-graph every 0.25 seconds, 5 seconds after start-up.
        InvokeRepeating("CheckChanges", 5.0f, 0.45f);
    }

    private void ResetSceneGraph()
    {
        if (hyperverseRoot != null)
        {
            Transform[] transforms = GetAllChildren(hyperverseRoot.transform);

            foreach (Transform t in transforms)
            {
                t.hasChanged = false;
            }
        }
    }

    private void CheckChanges()
    {
        if (IsDirty)
        {
            Debug.Log("Scene is dirty. Sending new init packet");
            Packet p = NewInitPacket(SaveGLB());
            client.SendPacket(p);
            IsDirty = false;
        }

        if (hyperverseRoot != null)
        {
            Transform[] transforms = GetAllChildren(hyperverseRoot.transform);

            foreach (Transform t in transforms)
            {
                if (t.hasChanged)
                {
                    t.hasChanged = false;

                    if (t.name != "RootScene" || t.parent.name != "RootScene")
                    { 
                        OnChangeInHyperverse(t);
                    }

                    break;
                }
            }
        }
    }
    
    private void OnChangeInHyperverse(Transform transform)
    {
        Debug.Log("Something has changed!! Sending TRNS packet...");

        //byte[] bytes = SaveGLB();

        //Packet packet = NewInitPacket(bytes);

        // For some reason, the GLTFSceneImporter creates the glTF Node and puts the geometry inside a child to the Node as 'Primitive'.
        // To avoid this, use the parent name to apply the transform.

        Packet packet = NewTrnsPacket(
            PacketSerializer.ToTrnsPayload(transform.rotation, transform.localScale, transform.localPosition, transform.parent.name)
        );

        client.SendPacket(packet);

        ResetSceneGraph();
    }

    private Packet NewInitPacket(byte[] payload)
    {
        PacketHeader header = new PacketHeader("HVTP", 1, "INIT", payload.Length);
        return new Packet(header, payload);
    }

    private Packet NewTrnsPacket(byte[] payload)
    {
        PacketHeader header = new PacketHeader("HVTP", 1, "TRNS", payload.Length);
        return new Packet(header, payload);
    }

    // Update is called once per frame
    void Update()
    {
    }

    void OnDestroy()
    {
        client.Destroy();
    }
}
