package com.stephaneallary.eclipse.ide.glitchme;

import processing.core.*;
import processing.video.*;

import org.apache.commons.net.ftp.*;

import gab.opencv.*;

import java.io.*;
import java.awt.Rectangle;

public class GlitchMe extends PApplet {

	// Variables //
	Capture cam;
	OpenCV opencv;
	Rectangle[] faces;
	PImage capture;
	PImage glitchedImg;
	PoulpCodec glitch;

	float scaleNormalisation;
	float xPosCam;
	float widthGlitchedImage;
	float coefCalib;

	int bufferSizeFFT;
	int sampleRateFFT;
	int state;
	int nbTx;
	int timeTrans;
	int nbHexTx;
	int posTx;
	int lastPos;
	int timeTx;
	int tx;
	int timer;
	int flashTrans;
	int widthDetectImage;
	int heightDetectImage;

	PFont font;
	PGraphics rulerA;
	PGraphics rulerB;
	PGraphics grid;

	AudioToColors audioReceiver;
	ColorsToAudio audioEmetter;

	boolean transmitting;
	boolean switx;
	boolean faceDetect;
	boolean fla;
	boolean captured;

	FTPClient ftp;

	String host = "ftp.perso.ovh.net";
	String username = "maximefr";
	String password = "lov3z1on";
	
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		PApplet.main( new String[] {"com.stephaneallary.eclipse.ide.glitchme.GlitchMe"});

	}
	
	// Application //
	public void setup() {
	  size(420, 600,P2D); //facteur 0.71
	  String[] cameras = Capture.list();
	  frameRate(60);
	  this.bufferSizeFFT = 1024;
	  this.sampleRateFFT = 44100;
	  this.cam = new Capture(this, cameras[1]);
	  this.state = 0;
	  this.font = loadFont("STHeitiSC-Light-150.vlw");
	  this.textFont(this.font);
	  this.textSize(0.165f*height);
	  this.audioReceiver = new AudioToColors(this, this.bufferSizeFFT, this.sampleRateFFT);
	  this.audioEmetter = new ColorsToAudio(this, this.bufferSizeFFT, this.sampleRateFFT);
	  this.timeTx = 70;
	  this.tx = 10;
	  this.nbTx = 0;
	  this.lastPos = 6; //Les 5 premier sont pour l'en-tête de l'image
	  this.posTx = 0;
	  this.transmitting = false;
	  this.switx = false;
	  this.faces = new Rectangle[0];
	  this.createRuler();
	  this.createGrid();
	  this.faceDetect = false;
	  this.fla = false;
	  this.captured = false;
	  this.heightDetectImage = 150;
	  
	}


	public void draw() { 
	  switch(this.state) {
	  case 0 :
	    this.calib();
	    break;
	  case 1: 
	    this.mirror();
	    break;
	  case 2: 
	    this.takePicture();
	    break;
	  case 3:
	    this.waitGlitch(); 
	    break;
	  case 4:
	    this.showGlitchedImage(); 
	    break;
	  }
	  frame.setTitle((int)frameRate + " fps");
	}


	private void calib() {
	  if (this.transmitting && !this.audioEmetter.endPixel) {
	    if (millis() - this.timeTrans > this.timeTx ) {
	      this.audioEmetter.calibrate(false);
	      this.timeTrans = millis();
	    }
	    this.audioReceiver.detectCalibrate();
	  }
	  else if (!this.audioEmetter.endPixel) {
	    this.audioEmetter.calibrate(true);
	    this.transmitting = true;
	    this.timeTrans = millis();
	  }
	  else {
	    float max = 0;
	    this.audioEmetter.removeSignal();
	    for (int i = 0; i < this.audioReceiver.ampMax.length; i+=this.audioReceiver.ampMax.length/20) {
	      if (this.audioReceiver.ampMax[i] > max) {
	        max = this.audioReceiver.ampMax[i];
	      }
	    }
	    this.coefCalib = (this.width/4)/max;
	    this.transmitting = false;
	    this.state = 1;
	  }
	}


	private void mirror() {
	  if (this.cam.available()) {
	    this.cam.read();
	    if (this.xPosCam == 0) {
	      this.scaleNormalisation = norm(height, 0, this.cam.height);
	      this.xPosCam = ((width-this.cam.width*this.scaleNormalisation)/2)/this.scaleNormalisation;
	      this.widthGlitchedImage = (float)(this.width)/this.height*this.cam.height;
	      this.widthDetectImage = (int)((((float)this.heightDetectImage)/this.cam.height)*this.widthGlitchedImage);
		  this.opencv = new OpenCV(this,this.widthDetectImage,this.heightDetectImage);
		  this.opencv.loadCascade(OpenCV.CASCADE_FRONTALFACE);  
	    }
	    else {
	      this.cam.loadPixels();
	      PImage temp  = this.cam.get((int)((this.cam.width-this.widthGlitchedImage)/2), 0, (int)this.widthGlitchedImage, this.cam.height);
	      temp.resize(this.widthDetectImage, this.heightDetectImage);
	      this.opencv.loadImage(temp);
	      this.faces = this.opencv.detect();
	    }
	    this.pushMatrix();
	    this.scale(this.scaleNormalisation, this.scaleNormalisation);
	    this.image(this.cam,this.xPosCam, 0);
	    this.popMatrix();
	    noFill();

	    for( int i=0; i<this.faces.length; i++ ) {
	      float wA = map(faces[i].width,0,this.opencv.width,0,this.width);
	      float wB = map(faces[i].width,0,this.opencv.width,0,this.width)*0.8f;
	      float hA = map(faces[i].height,0,this.opencv.height,0,this.height)*0.65f;
	      float hB = map(faces[i].height,0,this.opencv.height,0,this.height)*0.55f;
	      stroke(255,170);
	      rect(map(faces[i].x,0,this.opencv.width,0,this.width), map(faces[i].y,0,this.opencv.height,0,this.height), wA, hA);
	      stroke(255,90);
	      rect(map(faces[i].x,0,this.opencv.width,0,this.width)+((wA-wB)/2), map(faces[i].y,0,this.opencv.height,0,this.height)+((hA-hB)/2), wB, hB);
	    }
	    
	    if(this.faces.length > 0){
	      if(this.faceDetect && millis()-this.timer > 1500){
	        this.state = 2; 
	        this.faceDetect = false;
	        this.faces = new Rectangle[0];
	        this.timer = 0;
	       }
	       else if(!this.faceDetect){
	         this.timer = millis(); 
	         this.faceDetect = true; 
	       }
	       else{
	         this.faceDetect = true; 
	       }
	    }
	    else{
	      this.faceDetect = false;
	    }
	  }
	  else {
	    this.cam.start();
	  }
	}


	private void takePicture() {
	    this.pushMatrix();
	    this.scale(this.scaleNormalisation, this.scaleNormalisation);
	    this.image(this.cam, this.xPosCam, 0);
	    this.popMatrix();
	    
	    if(!this.fla && this.captured){
	      this.flash();
	    }
	    else if(this.fla && this.captured){
	      this.state = 3;
	      this.captured = false;
	      this.flash();
	    }
	    else{
	      this.cam.loadPixels();
	      this.capture = this.cam.get((int)((this.cam.width-this.widthGlitchedImage)/2), 0, (int)(this.widthGlitchedImage), this.cam.height);
	      this.bright(this.capture,2);
	      this.capture.loadPixels();
	      this.glitch = new PoulpCodec(this, this.capture.pixels, this.capture.width, this.capture.height);
	      this.cam.stop();
	      this.captured = true;
	    }
	}


	private void waitGlitch() {
	  if (!this.transmitting && this.nbTx < this.tx) {
	    this.nbHexTx = (int)(random(2, 3));
	    this.audioReceiver.listen(nbHexTx);
	    this.posTx = (int)(random(this.lastPos, this.glitch.pixelsPoulp.length*1/2*((this.nbTx+1)/this.tx)));

	    
	    if (this.nbHexTx > this.glitch.pixelsPoulp.length-this.posTx) {
	      this.nbHexTx = this.glitch.pixelsPoulp.length-this.posTx;
	    }

	    this.audioEmetter.transmit(subset(this.glitch.pixelsPoulp, this.posTx, this.nbHexTx));
	    this.transmitting = true;
	    this.lastPos = this.posTx+this.nbHexTx;
	    this.timeTrans = millis();
	  }
	  else if (this.audioEmetter.endPixel) {
	    if (millis() - this.timeTrans > this.timeTx*this.nbHexTx*2) {
	      this.audioEmetter.removeSignal();
	      this.transmitting = false;
	      for (int j=this.posTx; j<(this.posTx+this.nbHexTx); j++) {
	        println(hex(this.glitch.pixelsPoulp[j])+" = "+hex(this.audioReceiver.pixels[j-this.posTx]));
	        this.glitch.pixelsPoulp[j] = this.audioReceiver.pixels[j-this.posTx];
	      }
	      println("================="+this.nbTx+"========================");
	      this.lastPos = this.posTx+this.nbHexTx;
	      this.nbTx++;
	    }
	    else {
	      this.audioReceiver.detect();
	      this.drawDetect();
	    }
	  }
	  else if (this.transmitting) {
	    if (millis() - this.timeTrans > this.timeTx) {
	      this.audioEmetter.goNext();
	      this.timeTrans = millis();
	    }
	    this.audioReceiver.detect();
	    this.drawDetect();
	  }
	  else {
	    this.glitchedImg = this.glitch.getPImage();
	    this.state = 4;
	    this.nbTx = 0;
	    this.lastPos = 6; //Les 5 premier sont pour l'en-tête de l'image
	    this.posTx = 0;
	    this.timer = 0;
	    this.fla = false;
	  }
	}

	private void drawDetect() {
	  this.pushMatrix();
	  this.scale(this.scaleNormalisation, this.scaleNormalisation);
	  this.image(this.cam, this.xPosCam, 0);
	  this.popMatrix();
	  
	  image(this.grid,0,0);
	  
	  float re = 0.0f;

	  beginShape();
	  noStroke();
	  if (this.switx) {
	    fill(255, 170);
	    this.switx = false;
	  }
	  else {
	    fill(255, 220);
	    this.switx = true;
	  }
	  vertex(0, 0);
	  for (int i =0; i< this.audioReceiver.ampMax.length;i+=2) {
	    if (this.audioReceiver.fft.getBand(i-2) > 5 || this.audioReceiver.fft.getBand(i+2) > 5|| this.audioReceiver.fft.getBand(i-4) > 5 || this.audioReceiver.fft.getBand(i+4) > 5) {
	      re = (this.audioReceiver.fft.getBand(i)*this.coefCalib*1.5f)+3;
	      curveVertex((width/2)-re, map(i, 0, this.audioReceiver.ampMax.length, 0, height));
	    }
	    else if (this.audioReceiver.fft.getBand(i) > 5) {
	      re = (this.audioReceiver.fft.getBand(i)*this.coefCalib*2)+3;
	      curveVertex((width/2)-re, map(i, 0, this.audioReceiver.ampMax.length, 0, height));
	    }
	    else {
	      re = (this.audioReceiver.fft.getBand(i)*this.coefCalib)+3;
	      vertex((width/2)-re, map(i, 0, this.audioReceiver.ampMax.length, 0, height));
	    }
	  }
	  vertex(0, height);
	  endShape(CLOSE);

	  beginShape();
	  vertex(width, 0);
	  for (int i =0; i< this.audioReceiver.ampMax.length;i+=2) {
	    if (this.audioReceiver.fft.getBand(i-2) > 5 || this.audioReceiver.fft.getBand(i+2) > 5|| this.audioReceiver.fft.getBand(i-4) > 5 || this.audioReceiver.fft.getBand(i+4) > 5) {
	      re = (this.audioReceiver.fft.getBand(i)*this.coefCalib*1.5f)+3;
	      curveVertex((width/2)+re, map(i, 0, this.audioReceiver.ampMax.length, 0, height));
	    }
	    else if (this.audioReceiver.fft.getBand(i) > 5) {
	      re = (this.audioReceiver.fft.getBand(i)*this.coefCalib*2)+3;
	      curveVertex((width/2)+re, map(i, 0, this.audioReceiver.ampMax.length, 0, height));
	    }
	    else {
	      re = (this.audioReceiver.fft.getBand(i)*this.coefCalib)+3;
	      vertex((width/2)+re, map(i, 0, this.audioReceiver.ampMax.length, 0, height));
	    }
	  }
	  vertex(width, height);
	  endShape(CLOSE);



	  beginShape();
	  noStroke();
	  fill(0, 110);
	  vertex((width/2), 0);
	  for (int i =0; i< this.audioReceiver.ampMax.length;i+=this.audioReceiver.ampMax.length/20) {
	    curveVertex((width/2)-this.audioReceiver.ampMax[i]*this.coefCalib, map(i, 0, this.audioReceiver.ampMax.length, 0, height));
	  }
	  vertex((width/2), height);
	  endShape(CLOSE);
	  beginShape();
	  noStroke();
	  fill(0, 110);
	  vertex((width/2), 0);
	  for (int i =0; i< this.audioReceiver.ampMax.length;i+=this.audioReceiver.ampMax.length/20) {
	    curveVertex((width/2)+this.audioReceiver.ampMax[i]*this.coefCalib, map(i, 0, this.audioReceiver.ampMax.length, 0, height));
	  }
	  vertex((width/2), height);
	  endShape(CLOSE);
	  
	  image(this.rulerA, 0, 0);
	  image(this.rulerB, 0, 0);
	  
	  String sHex = "0x"+hex(this.audioReceiver.tempPix);

	  fill(0, 115);
	  this.pushMatrix();
	  this.rotate(HALF_PI);
	  text(sHex, 14, -25);
	  this.popMatrix();
	  this.pushMatrix();
	  this.rotate(-HALF_PI);
	  this.scale(-1, 1);
	  text(sHex, 14, width - 25);
	  this.popMatrix();
	  this.flash();
	}


	private void showGlitchedImage(){
	  if(this.timer == 0){
	    this.timer = millis(); 
	    this.pushMatrix();
	    this.scale(this.scaleNormalisation, this.scaleNormalisation);
	    this.image(this.glitchedImg, 0, 0);
	    this.popMatrix();
	    this.cam.start();
	    //this.glitch.savep0("image/",year()+nf(month(),2)+nf(day(),2)+"-"+nf(hour(),2)+nf(minute(),2)+nf(second(),2));
	    String imgPath = "gallery-images/"+year()+nf(month(),2)+nf(day(),2)+"-"+nf(hour(),2)+nf(minute(),2)+nf(second(),2)+".jpg";
	    this.glitchedImg.save(imgPath);
	    this.toFtp(imgPath);
	  }
	  else if((millis()-this.timer) > 10000){
	    this.timer = 0;
	    this.state = 1; 
	  }
	}


	private void flash(){
	  if(!this.fla){
	    if(this.timer == 0){
	      this.flashTrans = 0;
	      this.timer = millis();
	    }
	    else if(this.flashTrans < 150 ){
	      if((millis()-this.timer) > 5){
	        this.flashTrans += 45;
	        this.timer = millis();
	      }
	    }
	    else {
	      this.timer = 0;
	      this.fla = true;
	    }
	    fill(0,this.flashTrans);
	    rect(0,0,width,height);
	  }
	  else { 
	    if(this.timer == 0){
	      this.flashTrans = 255;
	      this.timer = millis();
	    }
	    else if(this.flashTrans > 0 ){
	      if((millis()-this.timer) > 20){
	        this.flashTrans -= 5;
	        this.timer = millis();
	      }
	    }
	    fill(255,this.flashTrans);
	    rect(0,0,width,height);
	  }
	}

	private void bright(PImage img, float br){
	  img.loadPixels(); 
	  for (int x = 0; x < img.width; x++ ) {
	    for (int y = 0; y <img.height; y++ ) {
	      int loc = x + y*img.width;
	      float r = red (img.pixels[loc]);
	      float g = green (img.pixels[loc]);
	      float b = blue (img.pixels[loc]);
	      float adjustBrightness = br; 
	      r *= adjustBrightness;
	      g *= adjustBrightness;
	      b *= adjustBrightness;
	      r = constrain(r,0,255); 
	      g = constrain(g,0,255);
	      b = constrain(b,0,255);
	      int c = color(r,g,b);
	      img.pixels[loc] = c;
	    }
	  }
	  img.updatePixels();  
	}

	private void toFtp(String path){
	  FileInputStream file;
	  try {
		file = new FileInputStream(path);
		try {
		    this.ftp = new FTPClient();
		    this.ftp.connect(this.host);
		    this.ftp.login(this.username, this.password);
		    this.ftp.setFileType(FTP.BINARY_FILE_TYPE);
		    this.ftp.storeFile("/www/who/"+path, file);
		    this.ftp.disconnect();
		 }
		 catch (Exception e) {
		   println ("FTP failed");
		 }
		
	  } catch (FileNotFoundException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	  }

	}

	private void createRuler(){
	  this.rulerA = createGraphics(width,height);
	  this.rulerB = createGraphics(width,height);
	  this.rulerA.beginDraw();
	  this.rulerB.beginDraw();
	  this.rulerA.strokeWeight(1); 
	  this.rulerA.stroke(0,75);
	  this.rulerB.strokeWeight(1); 
	  this.rulerB.stroke(0,75);

	  for(int i =0; i< this.audioReceiver.ampMax.length;i++){
	    if(i%20 == 0){
	      this.rulerA.line(0,(map(i,0,this.audioReceiver.ampMax.length,0,height)),10,(map(i,0,this.audioReceiver.ampMax.length,0,height))); 
	      this.rulerA.line(width-10,(map(i,0,this.audioReceiver.ampMax.length,0,height)),width,(map(i,0,this.audioReceiver.ampMax.length,0,height))); 
	    }
	    else if(i%10 == 0){
	      this.rulerB.line(0,(map(i,0,this.audioReceiver.ampMax.length,0,height)),5,(map(i,0,this.audioReceiver.ampMax.length,0,height))); 
	      this.rulerB.line(width-5,(map(i,0,this.audioReceiver.ampMax.length,0,height)),width,(map(i,0,this.audioReceiver.ampMax.length,0,height))); 
	    }
	  }
	 this.rulerA.endDraw();
	 this.rulerB.endDraw();
	}

	private void createGrid(){
	  this.grid = createGraphics(width,height);
	  this.grid.beginDraw();
	  this.grid.strokeWeight(1); 
	  this.grid.stroke(255, 70);
	  this.grid.noFill(); 
	  this.grid.hint(ENABLE_STROKE_PURE);
	  for (int i = 0; i <= height; i+=(height/10)) {   
	    for (int j = 0; j <= width; j+=(width/10)) {
	      this.grid.triangle(j, i,j, i+(height/10),j+(width/10), i);
	    }
	  }
	  this.grid.endDraw();
	}

}
