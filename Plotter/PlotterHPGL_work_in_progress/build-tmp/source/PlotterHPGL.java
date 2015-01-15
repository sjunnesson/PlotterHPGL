import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.serial.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class PlotterHPGL extends PApplet {



Serial myPort; // Create object from Serial class
Plotter plotter; // Create a plotter object

//Label
String label = "IT'S ALIVE!";

boolean DEBUG = false;


public void setup() {
  background(233, 233, 220);
  size(900, 640, P3D);
  smooth();

  //Select a serial port
  String portName = Serial.list()[3]; //make sure you pick the right one
  //Associate with a plotter object
  plotter = new Plotter(portName, this, 2);
  // plotter.circle(3000,2000,500);
  // plotter.writeLabel(label, 2000, 3000);
  // noLoop();
  for (int i = 0; i < 10; ++i) {
    // plotter.line((int) random(plotter.xMin, plotter.xMax), (int) random(plotter.yMin, plotter.yMax), (int) random(plotter.xMin, plotter.xMax), (int) random(plotter.yMin, plotter.yMax));
    plotter.circle(3000 + i * 50, 5000 + i * 50, 500,15);
    plotter.circle(5000 + i * 50, 5000 + i * 50, 500,30);
    plotter.circle(7000 + i * 50, 5000 + i * 50, 500,45);
  }

}

public void draw() {
  // plotter.writeLabel(label+frameCount, (int)random(xMin, xMax), (int)random(yMin, yMax));
}
/*************************
HPGL Plotter class

by: david sjunnesson
sjunnesson@gmail.com
2015
*************************/

class Plotter {
  Serial port;
  boolean PLOTTING_ENABLED = true;
  float characterWidth = 0.2f;
  float characterHeight = 0.2f;
  float labelAngleHorizontal = 0;
  float labelAngleVertical = 1;
  //Plotter dimensions
  public int xMin = 0;
  public int yMin = 0;
  public int xMax = 9000;
  public int yMax = 6402;

  int paperA4 = 0;
  int paperA3 = 1;
  int paperA = 2;
  int paperB = 3;

  boolean bufferFull = false;

  PApplet _this;

  Plotter(String portName, PApplet _t, int paperType) {
    _this = _t;
    if (PLOTTING_ENABLED) {
      port = new Serial(_this, portName, 9600);
      port.clear();
      println("Plotting to port: " + portName);
      // port.bufferUntil(lf);
      //Initialize plotter
      write("IN;SP1;");
      println("Plotter Initialized");
      setDimensions(paperType);

    } else {
      println("Plotting DISABLED");
    }
  }

  public void setDimensions(int paperType) {
    switch (paperType) {
      case 0: // a4
        xMin = 430;
        yMin = 200;
        xMax = 10430;
        yMax = 7400;
        break;
      case 1: //a3
        xMin = 380;
        yMin = 430;
        xMax = 15580;
        yMax = 10430;
        break;
      case 2: // size A
        xMin = 80;
        yMin = 320;
        xMax = 10080;
        yMax = 7520;
        break;
      case 3: // size B
        xMin = 620;
        yMin = 80;
        xMax = 15820;
        yMax = 10080;
        break;
      default: // a4
        println("Unknow paper type, setting default size A4");
        xMin = 430;
        yMin = 200;
        xMax = 10430;
        yMax = 7400;
        break;
    }
    // writeDimensions();
  }

  public void setCustomDimension(int _xMin, int _yMin, int _xMax, int _yMax) {
    xMin = _xMin;
    yMin = _yMin;
    xMax = _xMax;
    yMax = _yMax;
    writeDimensions();
  }

  public void writeDimensions() {
    write("IW" + xMin + "," + yMin + "," + xMax + "," + yMax + ";");
  }

  public void write(String hpgl) {
    if (PLOTTING_ENABLED) {
      while (bufferFull == true) {
        println("Waiting for buffer to clear");
        delay(200);
      }
      if (DEBUG) {
        println(hpgl);
      }
      port.write(hpgl);
      delay(20); // seems to need some time to react on full buffer
    }
  }

  public void writeLabel(String _label, int xPos, int yPos) {
    if (PLOTTING_ENABLED) {
      write("PU" + yPos + "," + xPos + ";"); //Position pen
      write("SI" + characterWidth + "," + characterHeight + ";DI" + labelAngleHorizontal + "," + labelAngleVertical + ";LB" + _label + PApplet.parseChar(3)); //Draw label
    }
  }
  public void moveTo(int xPos, int yPos) {
    if (PLOTTING_ENABLED) {
      write("PU" + yPos + "," + xPos + ";"); //Go to specified position
    }
  }
  public void line(float x1, float y1, float x2, float y2) {
    _this.line(x1 / 10, y1 / 10, x2 / 10, y2 / 10);
    write("PU" + (int) x1 + "," + (int) y1 + ";");
    write("PD" + (int) x2 + "," + (int) y2 + ";");
  }
  public void circle(int x, int y, int r) {
    circle(x, y, r, 0.5f);
  }
  public void circle(int x, int y, int r, float precision) {
    int circleMode = CENTER;
    switch (circleMode) {
      case CENTER:
        write("PU" + (int) x + "," + (int) y + ";");
        break;
      case CORNER:
        write("PU" + (int) x + "," + (int) y + ";");
        break;
      default: // CENTER
        write("PU" + (int) x + "," + (int) y + ";");
        break;
    }
    write("CI" + r + "," + precision + ";");
  }
}

public void serialEvent(Serial p) {
  char serialChar = p.readChar();
  if (DEBUG) {
    println(serialChar);
  }
  if (serialChar == 19) {
    plotter.bufferFull = true;
    println("Buffer full");
  }
  if (serialChar == 17) {
    plotter.bufferFull = false;
    println("Buffer empty");
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "PlotterHPGL" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
