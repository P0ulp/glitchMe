package com.stephaneallary.eclipse.ide.glitchme;

import processing.core.*;
import ddf.minim.*;
import ddf.minim.ugens.Oscil;
import ddf.minim.ugens.Waves;

public class ColorsToAudio {

	 private PApplet parent;
	 
	 private Minim minim;
	 private AudioOutput out;
	 private Oscil sine;
	 
	 private int bufferSizeFFT;
	 private int sampleRateFFT;
	 private int transVal;
	 private int pixelPos;
	 
	 private float[] freq;
	 
	 private boolean mark;
	 
	 
	 public boolean endPixel;
	 
	 public int[] pixels;
	  
	 public ColorsToAudio(PApplet applet, int bufferSize, int sampleRate){
	   this.parent = applet;
	   this.bufferSizeFFT = bufferSize;
	   this.sampleRateFFT = sampleRate;
	   this.minim = new Minim(this.parent);
	   this.out = this.minim.getLineOut(Minim.STEREO);
	   this.freq = new float[this.bufferSizeFFT/2 + 1];
	   for(int i = 0; i < this.freq.length; i++){
	     this.freq[i] =  ((float)i/(this.bufferSizeFFT))*this.sampleRateFFT;
	   }
	   this.transVal = 0;
	   this.pixelPos = 0;
	   this.mark = true;
	   this.endPixel = false;
	 }
	 
	 public void calibrate(boolean init){
	  if(init){
	    this.sine = new Oscil(0, 0.5f,Waves.SINE);
	    this.sine.patch(this.out);
	  }
	  else{
	     if(this.pixelPos == this.freq.length){
	     this.endPixel = true;
	    }
	    else{
	      this.sine.setFrequency(this.freq[this.pixelPos]);
	      this.pixelPos ++;
	    }
	  }
	 }
	 
	 public void transmit(int[] pixels){
	   this.pixels = pixels;
	   this.sine = new Oscil(200, 0.7f,Waves.SINE);
	   this.sine.patch(this.out);
	 }
	 
	 public void removeSignal(){
	   this.sine.unpatch(out);
	   this.transVal = 0;
	   this.pixelPos = 0;
	   this.mark = true;
	   this.endPixel = false;
	 }
	 
	 
	 public void goNext(){
	   int hex;
	   if(this.pixelPos >= this.pixels.length){
	     this.endPixel = true;
	   }
	   else if(this.mark){
		 this.sine.setFrequency(freq[15]);
	     this.mark=false;
	   }
	   else{
	     hex = (this.pixels[this.pixelPos] >> this.transVal*8) & 0xFF;
	     this.transVal++;
	     if(this.transVal > 3){
	      this.transVal = 0; 
	      this.pixelPos ++;
	     }
	     this.sine.setFrequency(freq[280-hex]);
	     this.mark = true;
	   }
	 }
	 
}
