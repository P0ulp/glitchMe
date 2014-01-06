package com.stephaneallary.eclipse.ide.glitchme;

/* Attention, le code n'est pas sécurisé sur le chargement des images au format p0 et la sauvegarde de l'image écrase le fichier s'il existe déjà*/

import java.util.ArrayList;

import processing.core.*;

public class PoulpCodec {

	 private int[] pixelsSource;
	 private int width;
	 private int height;
	 private PApplet parent;
	 
	 public int[] pixelsPoulp;
	  
	 public PoulpCodec(PApplet parent, int[] pixels, int width, int height){
		 this.parent = parent;
		 this.setPImage(pixels, width, height);
	 }
	 
	 public PoulpCodec(String pathFile){
	  this.loadp0(pathFile);
	 }
	 
	 public void setPImage(int[] pixels, int width, int height){
	   this.pixelsSource = pixels;
	   this.width = width;
	   this.height = height;
	      
	   ArrayList<Integer> rArray = new ArrayList<Integer>();
	   ArrayList<Integer> gArray = new ArrayList<Integer>();
	   ArrayList<Integer> bArray = new ArrayList<Integer>();
	   
	   int compTx = 10;
	   
	   int rMulti = 0;   
	   int gMulti = 0;  
	   int bMulti = 0;  
	   
	   int colTemp = this.pixelsSource[0];
	   
	   rArray.add(colTemp & 0x00FF0000);
	   gArray.add(colTemp & 0x0000FF00);
	   bArray.add(colTemp & 0x000000FF);
	    
	   int rTemp = colTemp & 0x00FF0000;
	   int gTemp = colTemp & 0x0000FF00;
	   int bTemp = colTemp & 0x000000FF;
	   
	   int rcolTemp;
	   int gcolTemp;
	   int bcolTemp;
	   int compr = 0x00010000*compTx;
	   int compg = 0x00000100*compTx;
	   int compb = 0x00000001*compTx;
	        
	   for(int i=1; i < this.pixelsSource.length; i++){
	    
	     rcolTemp = this.pixelsSource[i] & 0x00FF0000;
	     gcolTemp = this.pixelsSource[i] & 0x0000FF00;
	     bcolTemp = this.pixelsSource[i] & 0x000000FF;
	     
	     if((rTemp - rcolTemp) > compr || (rTemp - rcolTemp) < -compr){
	       if(rMulti != 0) {
	         rArray.add(rMulti | 0xFF000000);
	         rMulti = 0;
	       }    
	       rArray.add(rcolTemp);
	       rTemp = rcolTemp;
	     }
	     else {
	       rMulti ++;
	     }
	      
	     if((gTemp - gcolTemp) > compg || (gTemp - gcolTemp) < -compg){
	       if(gMulti != 0) {
	         gArray.add(gMulti | 0xFF000000);
	         gMulti = 0;
	       }    
	       gArray.add(gcolTemp);
	       gTemp = gcolTemp;
	     }
	     else {
	       gMulti ++;
	     }
	        
	     if((bTemp - bcolTemp) > compb || (bTemp - bcolTemp) < -compb){
	       if(bMulti != 0) {
	         bArray.add(bMulti | 0xFF000000);
	         bMulti = 0;
	       }    
	       bArray.add(bcolTemp);
	       bTemp = bcolTemp;
	     }
	     else {
	      bMulti ++;
	     }
	   }

	   if(rMulti!= 0) {
	     rArray.add(rMulti | 0xFF000000);
	   }  

	   if(gMulti!= 0) {
	     gArray.add(gMulti | 0xFF000000);
	   }  
	      
	   if(bMulti!= 0) {
	     bArray.add(bMulti | 0xFF000000);
	   }  

	   this.pixelsPoulp = new int[rArray.size()+gArray.size()+bArray.size()+6];
	   this.pixelsPoulp[0] = 0x7030;
	   this.pixelsPoulp[1] = this.width;
	   this.pixelsPoulp[2] = this.height;
	   this.pixelsPoulp[3] = rArray.size(); //longueur des données de la couche R
	   this.pixelsPoulp[4] = gArray.size(); //longueur des données de la couche G
	   this.pixelsPoulp[5] = bArray.size(); //longueur des données de la couche B   
	      
	   ArrayList<Integer> colorList = new ArrayList<Integer>();
	   colorList.addAll(rArray);
	   colorList.addAll(gArray);
	   colorList.addAll(bArray);  
	   
	   for(int i=6; i < this.pixelsPoulp.length; i++) {
	     this.pixelsPoulp[i] = colorList.get(i-6);
	   }     
	 }


	 public PImage getPImage(){
	   PImage img = this.parent.createImage(this.pixelsPoulp[1], this.pixelsPoulp[2],PConstants.RGB);
	   int lengthR = this.pixelsPoulp[3];
	   int lengthG = this.pixelsPoulp[4];
	   int lengthB = this.pixelsPoulp[5];
	   
	   if(lengthR > (this.pixelsPoulp.length-6)){
	     lengthR = this.pixelsPoulp.length-6;
	     lengthG = 0;
	     lengthB = 0;
	   }
	   else{
	     if(lengthG > (this.pixelsPoulp.length-6-lengthR)){
	       lengthG = this.pixelsPoulp.length-6-lengthR;
	       lengthB = 0;
	     }
	     else{
	       if(lengthB > (this.pixelsPoulp.length-6-lengthR-lengthG)){
	         lengthB = this.pixelsPoulp.length-6-lengthR-lengthG;
	       }
	     }
	   }
	      
	   int[] rArrayC = PApplet.subset(this.pixelsPoulp, 6,lengthR);
	   int[] gArrayC = PApplet.subset(this.pixelsPoulp, 6+lengthR,lengthG);
	   int[] bArrayC = PApplet.subset(this.pixelsPoulp, 6+lengthR+lengthG,lengthB);

	   ArrayList<Integer> rArray = new ArrayList<Integer>();
	   ArrayList<Integer> gArray = new ArrayList<Integer>();
	   ArrayList<Integer> bArray = new ArrayList<Integer>();
	    
	   int multi = 0;
	   int testMulti = 0;
	   int byt = 0;
	   
	   if(rArrayC.length > 0){
	     byt = rArrayC[0];
	   }
	         
	   for(int j = 1; j <lengthR; j++){
	     multi = rArrayC[j];
	     testMulti = multi & 0xFF000000;
	        
	     if(testMulti == 0xFF000000){
	       if((multi & 0x00FFFFFF) > this.width*this.height){
	         multi = this.width*this.height;
	       }
	       for(int i = 0; i <= (multi & 0x00FFFFFF); i++){
	         rArray.add(byt);
	       }
	          
	       if(j+1 < lengthR) {
	         byt = rArrayC[j+1];
	         j++;
	       }
	       else {
	         byt = -1;
	       }
	     }
	     else {
	       rArray.add(byt);   
	       byt = multi;
	     }
	   }
	   
	   if(byt != -1) {
	     rArray.add(byt); 
	   }
	      
	   multi = 0;
	   testMulti = 0;
	   if(gArrayC.length > 0){
	     byt = gArrayC[0];
	   }
	   
	   for(int j = 1; j < lengthG; j++){
	     multi = gArrayC[j];
	     testMulti = multi & 0xFF000000;
	     if(testMulti == 0xFF000000){
	       if((multi & 0x00FFFFFF) > this.width*this.height){
	         multi = this.width*this.height;
	       }
	       for(int i = 0; i <= (multi & 0x00FFFFFF); i++) {
	         gArray.add(byt);  
	       }
	       
	       if(j+1 < lengthG){
	         byt = gArrayC[j+1];
	         j++;
	       }
	       else{
	         byt = -1;
	       }
	     }
	     else{
	       gArray.add(byt);   
	       byt = multi;
	     }
	   }
	      
	   if(byt != -1){
	     gArray.add(byt); 
	   }
	   
	   multi = 0;
	   testMulti = 0;
	   if(bArrayC.length > 0){
	     byt = bArrayC[0];
	   }
	   
	   for(int j = 1; j < lengthB; j++){
	     multi = bArrayC[j];
	     testMulti = multi & 0xFF000000;
	     
	     if(testMulti == 0xFF000000){
	       if((multi & 0x00FFFFFF) > this.width*this.height){
	         multi = this.width*this.height;
	       }
	       for(int i = 0; i <= (multi & 0x00FFFFFF); i++){
	         bArray.add(byt);  
	       }
	          
	       if(j+1 < lengthB){
	         byt = bArrayC[j+1];
	         j++;
	       }
	       else{
	         byt = -1;
	       }
	     }
	     else{
	       bArray.add(byt);   
	       byt = multi;
	     }
	   }
	   
	   if(byt != -1){
	     bArray.add(byt); 
	   } 
	   
	   int[] pixelsResult = new int[this.width*this.height];

	   for(int i=0; i < rArray.size() && i < gArray.size() && i < bArray.size() && i < pixelsResult.length; i++){
	     pixelsResult[i] = (Integer)rArray.get(i) | (Integer)gArray.get(i) | (Integer)bArray.get(i)| 0xFF000000 ;
	   }
	      
	   img.pixels = pixelsResult;
	   return img;
	 }
	 
	  public void savep0(String path, String name){
	   byte[] pixelsPoulpBytes = new byte[(4*this.pixelsPoulp.length)];
	   int j = 0;
	   for(int i=0; i < this.pixelsPoulp.length; i++){
	     pixelsPoulpBytes[j] = (byte) (this.pixelsPoulp[i] >> 24 & 0xFF);
	     pixelsPoulpBytes[j+1] = (byte) (this.pixelsPoulp[i] >> 16 & 0xFF);
	     pixelsPoulpBytes[j+2] = (byte) (this.pixelsPoulp[i] >> 8 & 0xFF); 
	     pixelsPoulpBytes[j+3] = (byte) (this.pixelsPoulp[i] & 0xFF);
	     j+=4;
	   }
	  parent.saveBytes(path+name+".p0", pixelsPoulpBytes);
	 }
	 
	 private void loadp0(String filePath){
	   byte b[] = parent.loadBytes(filePath);
	   this.pixelsPoulp = new int[b.length/4];
	   int j = 0;
	   for(int i=0; i < (b.length/4); i++){
	     this.pixelsPoulp[i] = ((0xFF & b[j]) << 24) | ((0xFF & b[j+1]) << 16) |((0xFF & b[j+2]) << 8) | (0xFF & b[j+3]);
	     j+=4;
	   }
	   this.width = this.pixelsPoulp[1];
	   this.height = this.pixelsPoulp[2];
	 }
	
}
