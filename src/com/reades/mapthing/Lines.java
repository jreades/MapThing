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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.io.Serializable;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import processing.core.PApplet;
import processing.core.PGraphics;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.simplify.DouglasPeuckerLineSimplifier;

/**
 * <p>Creates lines from a MultiLine shape file in ArcGIS or (still looking for test cases) x/y pairs in a CSV file.</p>
 * 
 * <p>Issues to be tested/implemented:</p>
 * <ul>
 * <li>Multi-part to single-part conversion
 * <li>Colouring and scaling according to the value field
 * </ul>
 */
public class Lines extends Generic implements Serializable {
	
	private static final long serialVersionUID = 1376311472836304996L;
	
	public boolean DEBUG = false;
	
	ArrayList<Node[]> transformedCoordinates;
	ArrayList<Coordinate[]> rawCoordinates;

	/**
	 * Instantiate the GeoLine object with a 
	 * BoundingBox and String. Depending on 
	 * whether the String ends in .csv or .shp
	 * we will employ the appropriate tool to 
	 * open and read the data.
	 * @param b	the bounding box of the default view
	 * @param r	a String that allows us to determine the type of resource to open
	 */
	public Lines(BoundingBox b, String r) {
		super(b,r);
	}
	
	/**
	 * Instantiate the GeoLine object with a 
	 * BoundingBox and Feature Collection object.
	 * @param b	the bounding box of the default viewport
	 * @param f	a reference to FeatureCollection of SimpleFeature objects
	 */
	public Lines(BoundingBox b, FeatureCollection<SimpleFeatureType,SimpleFeature> f) {
		super(b,f);
	}
	
	/**
	 * You don't normally need to call this directly, 
	 * as it will be done for you when you ask for the 
	 * file to be projected in the Applet. However, I
	 * have left the method public to offer people the
	 * opportunity to improvise or improve on the code.
	 * @param a	a Processing PApplet object
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

				/*
				 * featureCollection was instantiated in the superclass
				 */
				FeatureIterator<SimpleFeature> iterator = super.featureCollection.features();

				/*
				 * Now loop through the geometries to
				 * set everything up with the correct
				 * X/Y coordinates
				 */

				try {
					while (iterator.hasNext()) {

						SimpleFeature feature = iterator.next();
						Geometry      theGeom = (Geometry) feature.getDefaultGeometry();
						SimpleFeatureType def = feature.getFeatureType();

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

							if (g.getGeometryType().equalsIgnoreCase("linestring")) {

								Coordinate[] c = null;

								LineString   l = (LineString) g;
								if (this.localSimplify > 0) {
									c = DouglasPeuckerLineSimplifier.simplify(l.getCoordinates(), this.localSimplify);
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
												theValue.doubleValue(), 
												theName
										);
										if (DEBUG == true) {
											System.out.println("X-axis: " + c[j].x + " is mapped from geographical range (" + box.getWest() + "," + box.getEast() + ") on to display range (0f," + a.width + ") as " + this.map((float) c[j].x, box.getWest(), box.getEast(), 0f, a.width));
											System.out.println("Y-axis: " + c[j].y + " is mapped from geographical range (" + box.getNorth() + "," + box.getSouth() + ") on to display range (0f," + a.height + ") as " + this.map((float) c[j].y, box.getNorth(), box.getSouth(), 0f, a.height));
										}
									}
									transformedCoordinates.add(t);
								} catch (NullPointerException e2) {
									System.out.println("NullPointerException " + e2);
								}
							} else {
								System.out.println("Have instantiated a MultiLine object but with geometry of type " + g.getGeometryType());
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
	 * Return an ArrayList<Node[]>, each row of which contains
	 * an array of Nodes. The idea is that each 
	 * array represents a separate set of lines that can be 
	 * drawn in the sketch. To some extent this is just a 
	 * stub left over from a previous implementation, but it
	 * does provide us with the capacity to insert some logic
	 * in between asking for the coordinates and getting back 
	 * the transformed result.
	 * @return ArrayList<Node[]>
	 */
	public ArrayList<Node[]> getCoordinates(PApplet a) {
		this.transformCoordinates(a);
		return this.transformedCoordinates;
	}
	
	/**
	 * Returns an ArrayList<Node[]> of the raw
	 * coordinates contained in the shape file. You don't
	 * normally need to worry about these, but they could 
	 * be useful if you wanted to completely bypass the 
	 * mapping process for some reason (e.g. you won't to
	 * show things in polar coordinates).
	 * @return ArrayList<Node[]>
	 */
	public ArrayList<Coordinate[]> getCoordinates() {
		return this.rawCoordinates;
	}
	
	/**
	 * Draws all of the lines contained in a
	 * Lines object loaded from a file. 
	 * The simplest way to work with this method is 
	 * to set the color, fill, and stroke in your sketch
	 * and then just call this function by passing it 
	 * the Applet instance (probably by saying: <code>object.project(this)</code>. 
	 * @param a	a Processing PApplet object
	 */
	public void project(PApplet a) {
		
		ArrayList<Node[]> al = this.getCoordinates(a);
		//println("Multiline array list returned " + a.size());

		for (int i = 0; i < al.size(); i++) {
			Node[] line = al.get(i);
			for (int j = 0; j < line.length-1; j++) {
				a.g.line(line[j].getX(), line[j].getY(), line[j+1].getX(), line[j+1].getY());
			}
		}
	}
	
	/**
	 * Apply a color scale to the the polygons 
	 * so that it is possible to represent the values
	 * in a useful way.
	 * @param a		a Processing PApplet object
	 * @param min	the minimum value of the value field (I will try to make this automatic in later releases)
	 * @param max	the maximum value of the value field (I will try to make this automatic in later releases)
	 */
	public void projectValues(PApplet a, float min, float max) {
		
		ArrayList<Node[]> al = this.getCoordinates(a);
		//println("Multiline array list returned " + al.size());
		
		for (int i = 0; i < al.size(); i++) {
			Node[] line = al.get(i);
			a.g.stroke(this.interpolateColor(a, (float) line[0].getValue(), min, max));
			for (int j = 0; j < line.length-1; j++) {
				a.g.line(line[j].getX(), line[j].getY(), line[j+1].getX(), line[j+1].getY());
			}
		}
	}
	
	/**
	 * <p>Draws all of the lines contained in a
	 * Lines object loaded from a file into a
	 * PGraphics object so that you can use it
	 * as a buffer.</p> 
	 * <p.The simplest way to work with this method is 
	 * to set the color, fill, and stroke in your sketch
	 * and then just call this function by passing it 
	 * the Applet instance and a PGraphics object.</p>
	 * @param a	a Processing PApplet object
	 * @param p	a Processing PGraphics
	 */
	public void project(PApplet a, PGraphics p) {
		
		ArrayList<Node[]> al = this.getCoordinates(a);
		//println("Multiline array list returned " + a.size());

		for (int i = 0; i < al.size(); i++) {
			Node[] line = al.get(i);
			for (int j = 0; j < line.length-1; j++) {
				p.line(line[j].getX(), line[j].getY(), line[j+1].getX(), line[j+1].getY());
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
