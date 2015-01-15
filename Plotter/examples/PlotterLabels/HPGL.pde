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
  public float DEFAULT_FONT_HEIGHT=0.15;
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

  void setDimensions(int paperType) {
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

  void setCustomDimension(int _xMin, int _yMin, int _xMax, int _yMax) {
    xMin = _xMin;
    yMin = _yMin;
    xMax = _xMax;
    yMax = _yMax;
    writeDimensions();
  }

  void writeDimensions() {
    write("IW" + xMin + "," + yMin + "," + xMax + "," + yMax + ";");
  }

  void write(String hpgl) {
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

  void writeLabel(String _label, int xPos, int yPos) {
    if (PLOTTING_ENABLED) {
      write("PU" + xPos + "," + yPos + ";"); //Position pen
      write("LB" + _label + char(3)); //Draw label
    }
  }

// should be 
  void setFontHeight(float h) {
    characterWidth = h;
    characterHeight = h;
    write("SI" + characterWidth + "," + characterHeight + ";");
  }

  float getFontHeight(){
    return characterWidth;
  }

  void setLabelDirection(float angle) {
    String c = nf(cos(radians(angle)), 1, 4); // the precision in our conversion is 4 decimals
    String s = nf(sin(radians(angle)), 1, 4);
    write("DR" + c + "," + s + ";");
  }

  void writeLabel(String _label, float xPos, float yPos) {
    writeLabel(_label, (int) xPos, (int) yPos);
  }

  void moveTo(int xPos, int yPos) {
    if (PLOTTING_ENABLED) {
      write("PU" + yPos + "," + xPos + ";"); //Go to specified position
    }
  }

  void line(float x1, float y1, float x2, float y2) {
    _this.line(x1 / 10, y1 / 10, x2 / 10, y2 / 10);
    write("PU" + (int) x1 + "," + (int) y1 + ";");
    write("PD" + (int) x2 + "," + (int) y2 + ";");
  }

  void circle(int x, int y, int r) {
    circle(x, y, r, 0.5);
  }

  void circle(int x, int y, int r, float precision) {
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