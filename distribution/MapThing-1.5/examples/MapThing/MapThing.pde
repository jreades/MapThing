import com.reades.mapthing.*;
import net.divbyzero.gpx.*;
import net.divbyzero.gpx.parser.*;

/**
 * You will need to define the 'bounding box' or envelope
 * for the sketch. This is used to tell Processing where
 * the top-left and bottom-right corners are *geographically*
 * for the initial view. You can easily implement zoom and pan, 
 * but this will set out the view that users see when you start
 * up. 
 *
 * Right now the projection param is not enormously useful 
 * since you *must* have all of your Shapefiles in the
 * same coordinate system because the library isn't (yet)
 * smart enough to figure out and reproject the data
 * on-the-fly based on the coordinate system. For the sake of
 * future-proofing your application, however, you *should* use 
 * the correct projection for your data.
 *
 * If you want to specify a projection other than lat/long or 
 * the British National Gride, then you will need to do a little
 * work to figure out the number corresponding to the standard 
 * EPSG/OGC WKT form. To do this, go to http://spatialreference.org/
 * and then pick 'OGC WKT' as the format; and then look for the *last*
 * 'AUTHORITY' declaration in the PROJCS command. That number is what 
 * you would pass in to the BoundingBox constructor.
 * 
 * The default (if you don't specify a reference) is
 * WGS84, but because I do a lot of work in the UK I've 
 * made it easy to use OSGB coordinates too. For everything
 * else you're on your own right now.
 * 
 * The order for the parameters is:
 * Projection, North coordinate (float), East coordinate 
 * (float), South coordinate (float), and West coordinate 
 * (float).
 */
BoundingBox envelope = new BoundingBox(4267, 90f, 180f, -90f, -180f);
//BoundingBox envelope = new BoundingBox(BoundingBox.osgb, 207489f, 573062.5f, 146135f, 493279f);
//BoundingBox envelope = new BoundingBox(BoundingBox.wgs, 60.84491f, 18.21533f, -16.39629f, -138.77759f);

/**
 * Example three types of shape files that
 * will be displayed in the sketch.
 */
Lines    canals;
Polygons world;
Polygons worldSimplified;
Points   worldCentroids;
Points   cityMarkers;

PImage   transport;
PFont    labelFont;

/**
 * Implementation of pan and zoom functionality
 */
// - What is the multiplier on key presses? (e.g. zooming and panning)
protected double  keyMultiple = 5d;
// - What is the multiplier on the mouse wheel? (e.g. zooming)
protected double  mouseWheelMultiple = 0.25d;
// - How far out will we allow the user to zoom from the default?
protected double  minScaleFactor = 0.1d;
// - What's the default scale factor we start with
protected double  scaleFactor = 1d;
	
// - Needed to track what the mouse dragging is doing
protected float   mX;
protected float   mY;
protected float   mDifX      = 0f;
protected float   mDifY      = 0f;
protected boolean mLocked    = false;
protected boolean kLocked    = false;

void setup() {

  colorMode(HSB,360,100,100);
  
  int d = 1200;
  size(d,envelope.heightFromWidth(d));
  
  transport = loadImage(dataPath("Airport.png"));
  
  smooth();
  
  // Load the Lines class from a shapefile
  // in a shapes subdirectory of the data folder
  canals  = new Lines(envelope, dataPath("shapes/Canals.shp"));
  // Sets a distance-threshold for simplification
  // of the shape's contents. This can be very useful
  // for speeding up display of the contents as part
  // of a sketch since you're effectively reducing the
  // number of nodes to be joined up.
  canals.setLocalSimplificationThreshold(0.1d);
  
  // Load the Polygons class from a shapefile
  // in a shapes subdirectory of the data folder.
  world  = new Polygons(envelope, dataPath("shapes/world.shp"));
  world.setLabelField("NAME");
  world.setValueField("AREA");
  world.setColourScale(color(83,27,94),color(71,58,42),15);
  
  // Sets a higher distance-threshold for simplifying
  // the contents of the shapefile. This will simplify
  // the display more than a lower number.
  worldSimplified = new Polygons(envelope, dataPath("shapes/world.shp"));
  worldSimplified.setLocalSimplificationThreshold(0.5d);
  
  // Extract the centroids from the simplified polygon
  // and use this as the basis for putting labels on all
  // features with an area value greater than or equal to 
  // the specified double. You could do this in a single
  // step by skipping the assignment, but I want to be a 
  // little clear about what is happening here.
  Polygons tmp = new Polygons(envelope, worldSimplified.getMultipleFeaturesByValue("AREA", 250000d));
  worldCentroids = new Points(envelope, tmp.getCentroids());
  worldCentroids.setLabelField("NAME");
  worldCentroids.setValueField("APPROX");
  worldCentroids.setColourScale(color(100,100,66),color(60,100,100),color(0,100,100),15);
  
  // There is no simplification possible for a
  // points shapefile. However, you may sometimes
  // find it useful to try calling: 
  // gPoint.dedupe(boolean)
  // which will try to check for multiple points at the
  // same geographical location and use either the 
  // first (boolean=true) or last(boolean=false) value
  // found in the shapefile for that location.
  
  // Create a set of points from a tab-delimited file
  // (slightly easier than a CSV to parse). Note that 
  // instantiation is the same so that you don't need
  // to care where the geo-data came from as long as
  // it's well-formatted.
  cityMarkers = new Points(envelope, dataPath("places/Cities.tsv"));
  
  // Here's another useful piece of functionality:
  // you can extract features from the shape file
  // and display them separately. So in the example 
  // below we first load a shape file containing all
  // of London's Tube network, and then pull out the
  // lines individually (allowing us to colour them 
  // individually) using the "LINE" column as our 
  // lookup field, and matching on the line name with
  // a wildcard.
  //  tube        = new Lines(envelope, dataPath("shapes/Tube_Lines.shp"));
  //  jubileeLine = new Lines(envelope, tube.getMultipleFeaturesByPattern("LINE", "Jubilee%"));
  //  dlrLine     = new Lines(envelope, tube.getMultipleFeaturesByPattern("LINE", "Docklands%"));
  
  // Load up the font that we'll use for labelling
  // the cities on the sketch.
  labelFont = loadFont("Serif-16.vlw");
  textFont(labelFont);
  textSize(12);
  
 // Needed to add a mouse wheel listener to the 
 // applet window.
  addMouseWheelListener(new java.awt.event.MouseWheelListener() { 
	public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) { 
		mouseWheel(evt.getWheelRotation());
	}
  });
}

void draw() {
  background(0);
  fill(255);
  
  translate((float) (width - width * this.scaleFactor)/2f, (float) (height - height * this.scaleFactor)/2f);
  /*
  this.lastZoom = this.scaleFactor;
  this.lastMx   = mX;
  this.lastMy   = mY;
  */			
  pushMatrix();
		    
  scale((float) this.scaleFactor);
		    
  translate(mX,mY);
  
  // The project(this) function uses the
  // mapped coordinates of the CSV or
  // shapefile, but if the size of the 
  // sketch is changed then the coordinates
  // will automatically be remapped for you.
  
  // Will colour-code the world according
  // to the AREA column in the shape file
  world.projectValues(this,5000f,17000000f);
  
  // Will show the effect of the simplification
  // process on the number of nodes in each polygon
  noFill();
  strokeWeight(2f);
  stroke(300,80,80);
  worldSimplified.project(this);
  
  // Set the display for the canals
  strokeWeight(2.5f);
  stroke(209,55,87,150);
  canals.project(this);

  noStroke();
  imageMode(CENTER);
  // Here we also specify a size for the 
  // ellipse to be drawn on the map. You can
  // also pass in a PImage here to use icons.
  worldCentroids.projectAreasAndValues(this,50,15000000f,500000000f);
  fill(0,0,100);
  worldCentroids.label(this);
  
  // Here we can both place markers on the 
  // sketch (using the usual project() function)
  // and then attach labels based on the 'name'
  // field in the input file. This functionality
  // could obviously use some improvement (e.g.
  // ability to specify the offset) but is a good
  // working implementation for v1.0. If you want 
  // you can always ask for the ArrayList<Node> set
  // and make your own changes to things like the 
  // size of the marker (e.g. scale it for population
  // size in this example) or write your own label
  // positioning code.
  noStroke();
  cityMarkers.project(this,transport,15,15);
  
  cityMarkers.labelOffset(16,3);
  cityMarkers.label(this);
  
  popMatrix();
}

/**
 * Uses mouse wheel movement (also works with 
 * MacBook-type track pads) to zoom in and out. 
 * @param delta: the movement of the mouse wheel
 */
void mouseWheel(int delta) {
	scaleFactor += delta * mouseWheelMultiple;
	if (scaleFactor <= minScaleFactor) {
		scaleFactor = minScaleFactor;
	}
}

/**
 * Is also needed to implement mouse-dragging in a 
 * sensible way -- we check to see if the mouse 
 * is currently over the user interface (UI) and
 * set this boolean accordingly. The idea here is
 * that mouse dragging while over the UI doesn't 
 * cause the sketch to pan around, so you can still
 * manipulate the UI implemented in ControlP5.
*/
void mousePressed() {
	if (! mLocked) {
		mLocked = true;
	}
	mDifX = mouseX - mX;
	mDifY = mouseY - mY;
}
	
/**
 * Is also needed to implement mouse-dragging in a 
 * sensible way -- we check to see if the mouse 
 * is currently over the user interface (UI) and
 * set this boolean accordingly. The idea here is
 * that mouse dragging while over the UI doesn't 
 * cause the sketch to pan around, so you can still
 * manipulate the UI implemented in ControlP5.
 */
void mouseDragged() {
	if (mLocked) {
		mX = mouseX - mDifX;
		mY = mouseY - mDifY;
	}
}
	
/**
 * Releases the mouse lock that we set when
 * the user activated the mouse while <i>not</i>
 * over part of the UI.
 */
void mouseReleased() {
	mLocked      = false;
}
	
/**
 * Implement simple panning and zoom controls
 * within the sketch using the keyboard:<br/>
 * <ul>
 * <li>z key: zoom in</li>
 * <li>x key: zoom out</li>
 * <li>left arrow key: pan 'west' (not tested on PC)</li>
 * <li>right arrow key: pan 'east' (not tested on PC)</li>
 * <li>up arrow key: pan 'north' (not tested on PC)</li>
 * <li>down arrow key: pan 'south' (not tested on PC)</li>
 * </ul>
 */
void keyPressed() {
	if (key == 'z') {
		scaleFactor = scaleFactor + mouseWheelMultiple;
	} else if (key == 'x') {
		scaleFactor = scaleFactor - mouseWheelMultiple;
	} else if (keyCode == 37) {
		//println("Left");
		mX = (float) (mX + keyMultiple);
	} else if (keyCode == 38) {
		//println("Up");
		mY = (float) (mY + keyMultiple);
	} else if (keyCode == 39) {
		//println("Right");
		mX = (float) (mX - keyMultiple);
	} else if (keyCode == 40) {
		//println("Down");
		mY = (float) (mY - keyMultiple);
	} else if (key == 'q') {
        exit();
    }
}