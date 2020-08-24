using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class BuildSelector : MonoBehaviour
{
    public Camera playerCamera;

    public GameObject hyperverseRoot;

    private readonly float rayLength = 5.0f;

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
            var cube = GameObject.CreatePrimitive(PrimitiveType.Cube);
            cube.transform.position = pos;
            cube.transform.parent = hyperverseRoot.transform;
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
