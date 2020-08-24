using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class RandomCubes : MonoBehaviour
{
    private List<Transform> transforms = new List<Transform>();

    public float rotationSpeed = 15f;
    public uint numberOfCubes = 10;


    void Start()
    {        
        for (int i = 0; i < numberOfCubes; i++) {
            var cube = GameObject.CreatePrimitive(PrimitiveType.Cube);

            transforms.Add(cube.transform);

            cube.transform.parent = transform;
            cube.transform.position = new Vector3(Random.Range(-3, 13), Random.Range(2, 8), Random.Range(-3, 13));
            cube.transform.rotation = Random.rotation;
            cube.tag = "Interactive";

//            cube.RemoveComponent<BoxCollider>();
            Destroy(cube.GetComponent<BoxCollider>());
            cube.AddComponent<MeshCollider>();
        }
    }

    void Update()
    {
        foreach (var trans in transforms) {
            trans.Rotate(Vector3.up * (rotationSpeed * Time.deltaTime));
            trans.Rotate(Vector3.forward * (rotationSpeed * Time.deltaTime));
            trans.Rotate(Vector3.right * (rotationSpeed * Time.deltaTime));
        }
    }
}
