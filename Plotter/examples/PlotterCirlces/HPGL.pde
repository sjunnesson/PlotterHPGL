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
  float characterWidth = 0.2;
  float characterHeight = 0.2;
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
      port.write(hpgl);
      delay(20); // seems to need some time to react on full buffer
    }
  }

  void writeLabel(String _label, int xPos, int yPos) {
    if (PLOTTING_ENABLED) {
      write("PU" + xPos + "," + yPos + ";"); //Position pen
      write("SI" + characterWidth + "," + characterHeight + ";DI" + labelAngleHorizontal + "," + labelAngleVertical + ";LB" + _label + char(3)); //Draw label
    }
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