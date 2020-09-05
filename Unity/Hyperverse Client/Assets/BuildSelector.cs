using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using HVTP;

public class BuildSelector : MonoBehaviour
{
    public Camera playerCamera;

    public GameObject hyperverseRoot;

    private readonly float rayLength = 15.0f;

    private Transform _selection = null;
    private Transform _lastSelection = null;
    public Material highlightedMaterial;
    public Material defaultMaterial;

    private Vector3 before = new Vector3(0f, 0f, 0f);

    void Start()
    {
    }

    void Update()
    {
        // Change the material if a selection is found

        if (_selection != null) 
        {
            var selectionRenderer = _selection.transform.GetComponent<Renderer>();

            if (selectionRenderer != null)
            {
                selectionRenderer.material = defaultMaterial;
            }

            if (Input.GetKeyDown(KeyCode.Delete))
            {
                Destroy(_selection.gameObject);
            }

            if (Input.GetKeyDown(KeyCode.Space))
            {
                Rigidbody body = _selection.gameObject.AddComponent<Rigidbody>();
                body.mass = 0.52f;
            }

            _lastSelection = _selection;
            _selection = null;
        }

        // Check for selections

        RaycastHit hit;

        if (Physics.Raycast(playerCamera.transform.position, playerCamera.transform.forward, out hit, rayLength))
        {
            _selection = hit.transform;
            var selectionRenderer = _selection.transform.GetComponent<Renderer>();

            if (selectionRenderer != null)
            {
                selectionRenderer.material = highlightedMaterial;
            }
        }

        // Do some guesswork...
        
        var pos = playerCamera.transform.position + (playerCamera.transform.forward * rayLength);

        if (Input.GetKeyDown(KeyCode.Insert))
        {
            //var sphere = GameObject.CreatePrimitive(PrimitiveType.Sphere);
            //sphere.transform.position = pos;
            //sphere.transform.parent = hyperverseRoot.transform;
            //sphere.transform.name = System.Guid.NewGuid().ToString();

            //Rigidbody sphereBody = sphere.AddComponent<Rigidbody>();
            //sphereBody.mass = 2;

            // So we can send changes to the server.
            //HVTPClientComponent.IsDirty = true;


            //var cube = GameObject.CreatePrimitive(PrimitiveType.Cube);
            //cube.transform.position = pos;
            //cube.transform.parent = hyperverseRoot.transform;
        }

        if (Input.GetMouseButton(0))    
        {
            var diff = (pos - before) * 1.5f;

            if (_lastSelection != null) 
            {
                _lastSelection.transform.position += (diff * Time.deltaTime);
            }
        }
        else {
            before = pos;
        }
    }
}
