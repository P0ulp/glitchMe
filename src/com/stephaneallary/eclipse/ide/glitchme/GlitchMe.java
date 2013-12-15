package com.stephaneallary.eclipse.ide.glitchme;

import processing.core.*;
import processing.video.*;
import gab.opencv.*;

import java.awt.Rectangle;

public class GlitchMe extends PApplet {

	// Variables //
	Capture cam;
	OpenCV opencv;
	Rectangle[] faces;
	PImage capture;
	
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		PApplet.main( new String[] {"com.stephaneallary.eclipse.ide.glitchme.GlitchMe"});

	}
	
	public void setup() {
		  size(420, 600); //facteur 0.71
	}

}
