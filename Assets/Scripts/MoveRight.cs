using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class MoveRight : MonoBehaviour {

	void Start () {
		
	}
	
	void Update ()
    {
        Vector2 move = Vector2.zero;
        move.x = 10 * Time.deltaTime;
        transform.Translate(move);
		
	}
}
