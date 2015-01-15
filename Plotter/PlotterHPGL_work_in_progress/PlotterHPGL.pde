import processing.serial.*;

Serial myPort; // Create object from Serial class
Plotter plotter; // Create a plotter object

//Label
String label = "IT'S ALIVE!";

boolean DEBUG = false;


void setup() {
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

void draw() {
  // plotter.writeLabel(label+frameCount, (int)random(xMin, xMax), (int)random(yMin, yMax));
}