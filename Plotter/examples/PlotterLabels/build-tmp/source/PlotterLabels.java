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

public class PlotterLabels extends PApplet {

/***************************
Plotter labels

Draws a random label fetched from a list
of labels provided

made by: david sjunnesson
2015
sjunnesson@gmail.com
***************************/




Plotter plotter; // Create a plotter object

//Labels to print
ArrayList labels;

public void setup() {
  background(233, 233, 220);
  size(900, 640, P3D);
  smooth();

  //Select a serial port
  String portName = Serial.list()[3]; //make sure you pick the right one

  //instantiate the plotter object with 
  //the plotter serial port name, a reference to this PApplet and what type of paper that is being used
  // 0=a4 1=a3 2=A 3=B
  plotter = new Plotter(portName, this, 2);

  // create a label and rotate around 360 degrees
  for (int i = 0; i < 10; ++i) {
    plotter.setLabelDirection(map(i, 0, 10, 0, 360));
    plotter.writeLabel("--SMALL->", plotter.xMax / 2 - 3000, plotter.yMax * 0.25f);
  }

  // increase the font size
  plotter.setFontHeight(0.25f);
  for (int i = 0; i < 10; ++i) {
    plotter.setLabelDirection(map(i, 0, 10, 0, 360));
    plotter.writeLabel("--MEDIUM->", plotter.xMax / 2, plotter.yMax * 0.5f);
  }

  // and once again increase the font size
  plotter.setFontHeight(0.35f);
  for (int i = 0; i < 10; ++i) {
    plotter.setLabelDirection(map(i, 0, 10, 0, 360));
    plotter.writeLabel("--LARGE->", plotter.xMax / 2 + 3000, plotter.yMax * 0.75f);
  }

  // put the font back to standard size 
  plotter.setFontHeight(plotter.DEFAULT_FONT_HEIGHT);
  plotter.setLabelDirection(plotter.DEFAULT_LABEL_DIRECTION);
  plotter.writeLabel("LABELS 1", 4800, 1000);
}

public void draw() {
  // an all empty Draw loop since all the drawings is done in the setup
}

public void serialEvent(Serial p) {
  char serialChar = p.readChar();

  if (serialChar == 19) {
    plotter.bufferFull = true;
    println("Buffer full");
  }
  if (serialChar == 17) {
    plotter.bufferFull = false;
    println("Buffer empty");
  }
}
/*************************
HPGL Plotter class

by: david sjunnesson
sjunnesson@gmail.com
2015

inspired by Tobias Toft
http://www.tobiastoft.com/posts/an-intro-to-pen-plotters
*************************/

class Plotter {
  boolean DEBUG = false;
  Serial port;
  boolean PLOTTING_ENABLED = true;
  public float DEFAULT_FONT_HEIGHT=0.15f;
  public float DEFAULT_LABEL_DIRECTION=0;

  float characterWidth = DEFAULT_FONT_HEIGHT;
  float characterHeight = DEFAULT_FONT_HEIGHT;
  float labelAngleHorizontal = 1;
  float labelAngleVertical = 0;
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
      setFontHeight(DEFAULT_FONT_HEIGHT);
      setLabelDirection(DEFAULT_LABEL_DIRECTION);

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
    writeDimensions();
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
      if (hpgl.length() < 50) {
        port.write(hpgl);
      } else {
        println("To long HPGL string");
      }
      delay(20); // seems to need some time to react on full buffer
    }
  }

  public void writeLabel(String _label, int xPos, int yPos) {
    if (PLOTTING_ENABLED) {
      write("PU" + xPos + "," + yPos + ";"); //Position pen
      write("LB" + _label + PApplet.parseChar(3)); //Draw label
    }
  }

// should be 
  public void setFontHeight(float h) {
    characterWidth = h;
    characterHeight = h;
    write("SI" + characterWidth + "," + characterHeight + ";");
  }

  public float getFontHeight(){
    return characterWidth;
  }

  public void setLabelDirection(float angle) {
    String c = nf(cos(radians(angle)), 1, 4); // the precision in our conversion is 4 decimals
    String s = nf(sin(radians(angle)), 1, 4);
    write("DR" + c + "," + s + ";");
  }

  public void writeLabel(String _label, float xPos, float yPos) {
    writeLabel(_label, (int) xPos, (int) yPos);
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
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "PlotterLabels" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
