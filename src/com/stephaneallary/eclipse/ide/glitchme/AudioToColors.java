package com.stephaneallary.eclipse.ide.glitchme;

import processing.core.*;
import ddf.minim.AudioInput;
import ddf.minim.Minim;
import ddf.minim.analysis.*;

public class AudioToColors {

	private PApplet parent;
	  
	  private Minim minimIn;  
	  private AudioInput in;	 
	  
	  private int bufferSizeFFT;
	  private int sampleRateFFT;
	  private int countMarkDetect;
	  private int pixelCount;
	  private int pixelsPos;
	  
	  private  float lastFreqMax;

	  public int tempPix = 0;
	  public int pix =0;
	  public int[] pixels; 
	  
	  public float[] ampMax;
	  
	  public FFT fft;

	  public AudioToColors(PApplet applet, int bufferSize, int sampleRate){
	    this.parent = applet;
	    this.bufferSizeFFT = bufferSize;
	    this.sampleRateFFT = sampleRate;
	    this.minimIn = new Minim(this.parent);
	    this.in = this.minimIn.getLineIn(Minim.MONO, this.bufferSizeFFT, this.sampleRateFFT);
	    this.fft = new FFT(in.bufferSize(), in.sampleRate());
	    this.fft.window(FourierTransform.HAMMING);
	    this.fft.noAverages();
	    this.ampMax = new float[this.fft.specSize()];
	    this.countMarkDetect = 0;
	    this.lastFreqMax = 0.0f;
	    this.pix = 0;
	  }

	  public void listen(int pixelsLength){
	     this.pixels = new int[pixelsLength];
	     this.fft = new FFT(in.bufferSize(), in.sampleRate());
	     this.fft.window(FourierTransform.HAMMING);
	     this.fft.noAverages();
	     this.pixelsPos = 0;
	     this.pixelCount = 0;
	     this.countMarkDetect = 0;
	     this.tempPix = 0;
	  }
	  
	  public void detectCalibrate(){
	    this.fft.forward(in.mix);
	    int numPoints = this.fft.specSize();
	    for(int i = 0; i < numPoints; i++){
	      if(this.ampMax[i] < this.fft.getBand(i)){
	          this.ampMax[i] = this.fft.getBand(i);
	      }
	    }
	 } 

	  public void detect(){
	    this.fft.forward(this.in.mix);
	    float freqMax = 0.0f;
	    float deltaFreqMax = 0.0f;
	    for (int i = 8;i < 281; i++) {
	      if(this.fft.getBand(i)-this.ampMax[i] > deltaFreqMax){ 
	        deltaFreqMax = this.fft.getBand(i)-this.ampMax[i];
	        freqMax = this.fft.indexToFreq(i);
	      }
	    }
	    if(freqMax == this.fft.indexToFreq(15) && this.lastFreqMax == freqMax){
	      this.countMarkDetect ++;
	     }
	     else if(this.countMarkDetect > 0 && freqMax > this.fft.indexToFreq(15) && this.lastFreqMax == freqMax){
	       this.countMarkDetect = 0;
	       this.setPixel(freqMax);
	     }
	    this.lastFreqMax = freqMax;
	  }  

	  private void setPixel(float freq){
	    this.pix = 280-this.fft.freqToIndex(freq);
	    this.tempPix = this.pix << ((8*this.pixelCount)) | this.tempPix;
	    this.pixelCount++;
	    if(this.pixelCount > 3){
	      this.pixels[this.pixelsPos] = this.tempPix;
	      if(this.pixelsPos < this.pixels.length-1){
	        this.pixelsPos++;
	      }
	      this.pixelCount = 0;
	      this.tempPix = 0;
	    }
	  }
}
