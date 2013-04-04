/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */
package com.reades.mapthing;

/**
 * Geometry geom = (Geometry) feature.getDefaultGeometry(); 

// this should always be true for a Geometry object from a shapefile 
// (using the GeoTools Geometries enum 
boolean isMulitPolygon = Geometries.get(geom) == 
Geometries.MULTIPOLYGON; 

// need to allow for the possibility of multiple disjunct polygons 
int numPolys = geom.getNumGeometries(); 
for (int ip = 0; ip < numPolys; ip++) { 
    Polygon p = (Polygon) geom.getGeometryN(ip); 

    // get the outer boundary like this 
    LineString boundary = p.getExteriorRing(); 

    // check for holes 
    boolean hasHoles = p.getNumInteriorRing() > 0; 

    // if there are some holes, get the first one 
    if (hasHoles) { 
        LineString hole1 = p.getInteriorRingN(0); 
    } 
} 
 */

import processing.core.PApplet;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.io.Serializable;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

/**
 * <p>Creates polygons from a Polygon shape file in ArcGIS.</p>
 * 
 * <p>Issues to be tested/implemented:</p>
 * <ul>
 * <li>Merge everything and try to close
 * <li>Multipart to singlepart
 * <li>Features to polygon
 * </ul>
 */
public class Polygons extends Generic implements Serializable {
	
	private static final long serialVersionUID = 8618803832866064725L;
	
	public boolean DEBUG = false;
	
	private boolean closed = true;
	
	ArrayList<Node[]> transformedCoordinates;
	ArrayList<Coordinate[]> rawCoordinates;
	
	/**
	 * Instantiate the GeoLine object with a 
	 * BoundingBox and String. Depending on 
	 * whether the String ends in .csv or .shp
	 * we will employ the appropriate tool to 
	 * open and read the data.
	 * @param b the bounding box of the default view
	 * @param r a String that allows us to determine the type of resource to open
	 */
	public Polygons(BoundingBox b, String r) {
		super(b,r);
	}
	
	/**
	 * Usually used to instantiate a Polygon
	 * object from within the general feature collection
	 * of the shape file so that it can be manipulated 
	 * separately from the other polygons. Note that this
	 * isn't guaranteed to return a viable object (in terms
	 * of it being one that can be displayed on the screen)
	 * and that it could be NULL.
	 * @param b the bounding box of the parent Polygons object
	 * @param f the feature collection used to instantiate the new object
	 */
	public Polygons(BoundingBox b, FeatureCollection<SimpleFeatureType, SimpleFeature> f) {
		super(b, f);
	}
	
	/**
	 * Usually used to instantiate a Polygon
	 * object from within the processing of the shape file 
	 * so that it can be manipulated separately from the 
	 * other polygons.
	 * @param b the bounding box of the parent Polygons object
	 * @param f the Simple Feature  used to instantiate the new object
	 */
	public Polygons(BoundingBox b, SimpleFeature f) {
		super(b, f);
	}
	
	/**
	 * Called before you can display the polygons
	 * in a Processing sketch. It simply passes in 
	 * the height and width of the sketch so that we can 
	 * remap the coordinates of the shape file into the 
	 * plain of the sketch itself.
	 * @param a  the Processing PApplet object
	 */
	public void transformCoordinates(PApplet a) {
		
		if (a.width == this.appletWidth && a.height == this.appletHeight) {

			// Do nothing

		} else {
			
			if (this.appletWidth == 0) {
				this.appletWidth = a.width;
			}
			if (this.appletHeight == 0) {
				this.appletHeight = a.height;
			}

			if (super.featureCollection != null) {
				transformedCoordinates = new ArrayList<Node[]>();
				rawCoordinates         = new ArrayList<Coordinate[]>();

				// This was instantiated in the superclass
				FeatureIterator<SimpleFeature> iterator = super.getFeatures();

				// Now loop through the geometries to
				// set everything up with the correct
				// X/Y coordinates
				try {
					while (iterator.hasNext()) {

						SimpleFeature feature = iterator.next();
						Geometry      theGeom = (Geometry) feature.getDefaultGeometry();
						SimpleFeatureType def = feature.getFeatureType();

						if (this.globalSimplify > 0) {
							theGeom = TopologyPreservingSimplifier.simplify(theGeom, this.globalSimplify);
						}

						for (int i = 0; i < theGeom.getNumGeometries(); i++) {

							//System.out.println("Got geometry " + i);
							Geometry g = theGeom.getGeometryN(i);

							String theName  = "";
							Double theValue = 0d;

							if (this.labelPosition > 0) {
								theName = (String) feature.getAttribute(this.labelPosition);
							} else if (def.indexOf(this.labelName) != -1) {
								theName = (String) feature.getAttribute(this.labelName);
							}

							if (this.valuePosition > 0) {
								theValue = (Double) feature.getAttribute(this.valuePosition);
							} else if (def.indexOf(this.valueName) != -1) {
								theValue = (Double) feature.getAttribute(this.valueName);
							}

							if (g.getGeometryType().equalsIgnoreCase("polygon")) {

								Coordinate[] c = null;

								/*
							    // get the outer boundary like this 
							    LineString boundary = p.getExteriorRing(); 
							    */
								
								Polygon l = (Polygon) g;
								
								if (l.getNumInteriorRing() > 0) { 
									if (this.DEBUG)
										System.out.println("Polygon has " + l.getNumInteriorRing() + " holes. Holes are impossible to display in Processing");
									LineString lb = l.getExteriorRing();
									
									if (this.localSimplify > 0) {
										c = TopologyPreservingSimplifier.simplify(lb, this.localSimplify).getCoordinates();
									} else {
										c = lb.getCoordinates();
									}
								} else if (this.localSimplify > 0) {
									c = TopologyPreservingSimplifier.simplify(l, this.localSimplify).getCoordinates();
								} else {
									c = l.getCoordinates();
								}

								rawCoordinates.add(c);

								try {
									Node[] t = new Node[c.length];
									for (int j = 0; j < c.length; j++) {
										t[j] = new Node(
												c[j].hashCode(),
												this.map((float) c[j].x, box.getWest(), box.getEast(), 0f, a.width),
												this.map((float) c[j].y, box.getNorth(), box.getSouth(), 0f, a.height),
												0d,
												theValue.doubleValue(),
												theName
										);
										//System.out.println(c[j].x + ", " + box.getWest() + ", " + box.getEast() + ", 0f, " + width + " = " + this.map((float) c[j].x, box.getWest(), box.getEast(), 0f, width));
									}
									transformedCoordinates.add(t);
								} catch (NullPointerException e2) {
									System.out.println("NullPointerException " + e2);
								}
							} else {
								System.out.println("Have instantiated a Polygon object but with geometry of type " + g.getGeometryType());
							}
						}
					}
				} finally {
					if (iterator != null) {
						iterator.close();
					}
				}
			}
		}
	}
	
	/** 
	 * Return a HashMap which contains all of the
	 * polygons keyed on the named ID field. This 
	 * would be useful if you wanted selectively 
	 * colour or otherwise adjust the display of 
	 * the polygons based on some external data 
	 * input.
	 * @return HashMap<Integer,Polygons>
	 */
	public HashMap<Integer,Polygons> getPolygonsWithId(String idField) {
		
		FeatureIterator<SimpleFeature> f = super.getFeatures();
		HashMap<Integer,Polygons> h = new HashMap<Integer,Polygons>();
		
		// Now loop through the geometries to
		// set everything up with the correct
		// X/Y coordinates
		try {
			while (f.hasNext()) {
				
				SimpleFeature feature = f.next();
				
				Double d = (Double) feature.getAttribute(idField);	
				int key = (int) Math.round(d);
				
				//System.out.println("Adding key: " + key + " with value " + feature);
				
				Polygons g = new Polygons(this.box, feature);
				h.put(key, g);				
			}
		} finally {
			if (f != null) {
				f.close();
			}
		}
		
		//return new Polygons(this.box, super.getMultipleFeaturesById("PUBLIC_X_A", Integer.toString(id)));
		return h;
	}
	
	/**
	 * Return an ArrayList, each row of which contains
	 * an array of Nodes. The idea is that each 
	 * array represents a separate polygon that can be 
	 * drawn in the sketch. The simplest case is to then
	 * just pass the entire ArrayList straight to the 
	 * drawPolygons() method in GeoPApplet.
	 * @return An ArrayList of Node arrays
	 */
	public ArrayList<Node[]> getCoordinates(PApplet a) {
		this.transformCoordinates(a);
		return this.transformedCoordinates;
	}
	
	/**
	 * Returns the raw SimpleFeature coordinates associated
	 * with the polygons as an ArrayList of coordinate arrays.
	 * You can use these to manipulate the coordinates in 
	 * any way you like as they have not been mapped on to the
	 * space of the sketch.
	 * @return ArrayList<Coordinate[]>
	 */
	public ArrayList<Coordinate[]> getCoordinates() {
		return this.rawCoordinates;
	}
	
	/**
	 * Specify whether polygons should be drawn closed or open. 
	 * If closed, then a line connecting the last point in the 
	 * polygon to the first point will be drawn. If open, then 
	 * no connecting line will be drawn (though the latter could
	 * still <i>look</i> the same if there is a redundant point
	 * at the end of the polygon coordinates that is the same as
	 * the first coordinate).
	 */
	public boolean isClosed(boolean c) {
		this.closed = c;
		return this.closed;
	}
	
	/**
	 * Check whether polygons should be drawn closed or open. 
	 * If closed, then a line connecting the last point in the 
	 * polygon to the first point will be drawn. If open, then 
	 * no connecting line will be drawn (though the latter could
	 * still <i>look</i> the same if there is a redundant point
	 * at the end of the polygon coordinates that is the same as
	 * the first coordinate).
	 */
	public boolean isClosed() {
		return this.closed;
	}
	
	/**
	 * Draws all of the vertices contained in
	 * a Polygon object loaded from a file. 
	 * The simplest way to work with this method is 
	 * to set the color, fill, and stroke in your sketch
	 * and then just call this function by passing it 
	 * the Applet instance (probably by saying: <code>object.project(this)</code>. 
	 * @param a a Processing PApplet object
	 */
	public void project(PApplet a) {
		
		ArrayList<Node[]> b = this.getCoordinates(a);
		//println("Array list returned " + b.size());
		
		for (int i = 0; i < b.size(); i++) {
			Node[] shape = b.get(i);
			a.g.beginShape(processing.core.PConstants.POLYGON);
			for (int j = 0; j < shape.length; j++) {
				a.g.vertex(shape[j].getX(), shape[j].getY());
			}
			if (this.closed) {
				a.g.endShape(processing.core.PConstants.CLOSE);
			} else {
				a.g.endShape();
			}
		}
	}
	
	/**
	 * Apply a color scale to the the polygons 
	 * so that it is possible to represent the values
	 * in a useful way.
	 * @param a   a Processing PApplet object
	 * @param min the minimum value of the value field (I will try to make this automatic in later releases)
	 * @param max the maximum value of the value field (I will try to make this automatic in later releases)
	 */
	public void projectValues(PApplet a, float min, float max) {
		
		ArrayList<Node[]> b = this.getCoordinates(a);
		//println("Array list returned " + b.size());
		
		for (int i = 0; i < b.size(); i++) {
			Node[] shape = b.get(i);
			a.g.fill(this.interpolateColor(a, (float) shape[0].getValue(), min, max));
			a.g.beginShape(processing.core.PConstants.POLYGON);
			for (int j = 0; j < shape.length; j++) {
				a.g.vertex(shape[j].getX(), shape[j].getY());
			}
			if (this.closed) {
				a.g.endShape(processing.core.PConstants.CLOSE);
			} else {
				a.g.endShape();
			}
		}
	}
	
	/**
	 * <p>Draws all of the vertices contained in
	 * a Polygon object loaded from a file. This
	 * one differs from the version without a 
	 * PGraphics object in that it allows you to
	 * use the graphics object as a buffer and won't
	 * write directly to the Applet view.</p>
	 * <p>The simplest way to work with this method is 
	 * to set the color, fill, and stroke in your sketch
	 * and then just call this function by passing it 
	 * the Applet and Graphics instances</p> 
	 * 
	 * Note that if you want the image to appear 
	 * right on top of the coordinates then you will need
	 * to call "imageMode(CENTER)" in your sketch first. 
	 * @param a a Processing PApplet object
	 * @param p a Processing PGraphics object
	 */
	public void project(PApplet a, PGraphics p) {
		
		ArrayList<Node[]> b = this.getCoordinates(a);
		//println("Array list returned " + b.size());
		
		for (int i = 0; i < b.size(); i++) {
			Node[] shape = b.get(i);
			p.beginShape(processing.core.PConstants.POLYGON);
			for (int j = 0; j < shape.length; j++) {
				p.vertex(shape[j].getX(), shape[j].getY());
			}
			if (this.closed) {
				p.endShape(processing.core.PConstants.CLOSE);
			} else {
				p.endShape();
			}
		}
	}
	
	/**
	 * <p>Reverses the order of the points in a polygon.
	 * Note that this will <em>only</em> do something <em>if</em>
	 * you have already called transformCoordinates or project
	 * once (since it only works on the points that have been 
	 * mapped into the Processing sketch.</p>
	 */
	public void reverse() {
		if (this.transformedCoordinates.size() > 0) {
			for (int i=0; i < this.transformedCoordinates.size(); i++) {
				Collections.reverse(Arrays.asList(this.transformedCoordinates.get(i)));
			}
			Collections.reverse(this.transformedCoordinates);
		}
	}
}
