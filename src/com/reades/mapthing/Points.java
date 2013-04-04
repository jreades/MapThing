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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.io.Serializable;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * <p>Creates points from a Point shape file in ArcGIS.</p>
 */
public class Points extends Generic implements Serializable {
	
	private static final long serialVersionUID = -802163096726427577L;
	
	public boolean DEBUG = false;
	
	ArrayList<Node> transformedCoordinates;
	ArrayList<Coordinate> rawCoordinates;
	
	int labelXOffset = 5;
	int labelYOffset = 5;
	
	/**
	 * Instantiate the GeoLine object with a 
	 * BoundingBox and String. Depending on 
	 * whether the String ends in .csv or .shp
	 * we will employ the appropriate tool to 
	 * open and read the data.
	 * @param b the bounding box of the default view
	 * @param r a String that allows us to determine the type of resource to open
	 */
	public Points(BoundingBox b, String r) {
		super(b,r);
	}
	
	/**
	 * Instantiate the Points object with a 
	 * BoundingBox and Feature Collection object.
	 * @param b the bounding box of the default viewport
	 * @param f a reference to FeatureCollection of SimpleFeature objects
	 */
	public Points(BoundingBox b, FeatureCollection<SimpleFeatureType,SimpleFeature> f) {
		super(b,f);
	}
	
	/**
	 * Use to remove duplicates -- by geographical coordinates
	 * -- from the data you are about to show. You should specify
	 * a de-duping field and a boolean to indicate whether you want
	 * the first or last value encountered. This can be very helpful
	 * when you have a <i>lot</i> of points in your shape file and 
	 * want to simplify the projection (or remove potential sources
	 * of confusion in the mapping process).
	 * @param useFirst use the first point encountered in the index when de-duping points.
	 */
	public void dedupe(boolean useFirst) {
		
		if (super.featureCollection != null) {
			/*
			 * Create a new FeatureCollection to hold
			 * our de-duped data set.
			 */
			FeatureCollection<SimpleFeatureType, SimpleFeature> newCollection = FeatureCollections.newCollection("internal");

			/*
			 * How we'll de-dupe the data set -- since
			 * we care only about matching coordinates we 
			 * just check the key string derived from the
			 * Coordinates object.
			 */
			HashMap<String,SimpleFeature> storage = new HashMap<String,SimpleFeature>();

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

					for (int i = 0; i < theGeom.getNumGeometries(); i++) {

						//System.out.println("Got geometry " + i);
						Geometry g = theGeom.getGeometryN(i);

						if (g.getGeometryType().equalsIgnoreCase("point")) {

							Point      p = (Point) g;
							Coordinate c = p.getCoordinate();

							String location = c.toString();

							if (storage.containsKey(location)) {
								if (useFirst) {
									// Do nothing
									//System.out.println("Skipping because already contains coordinate: " + location);
								} else {
									//System.out.println("Replacing last feature because duplicate coordinate: " + location);
									storage.remove(location);
									storage.put(location, feature);
								}
							} else {
								storage.put(location, feature);
							}
						} else {
							System.out.println("Have instantiated a Point object but with geometry of type " + g.getGeometryType());
						}
					}
				}
			} finally {
				if (iterator != null) {
					iterator.close();
				}
			}

			Set<Entry<String, SimpleFeature>>    set = storage.entrySet();
			Iterator<Entry<String, SimpleFeature>> i = set.iterator();

			while (i.hasNext()) {
				Entry<String, SimpleFeature> e = i.next();
				newCollection.add(e.getValue());
			}

			super.featureCollection = newCollection;
		}
	}
	
	/**
	 * Used to filter on a single criterion in the file. If the 
	 * field matches that criterion, the value is retained, otherwise
	 * it is excluded
	 */
	
	/**
	 * Must be called before you can display the points
	 * in a Processing sketch. You don't normally need to
	 * call this directly since it will be called automatically
	 * when you call <code>points.project(this)</code>. However,
	 * this does allow you to manually perform operations if you
	 * are trying to be very clever, and basically all we're doing
	 * is using the height and width of the sketch so that we can 
	 * remap the coordinates of the shape file into the 
	 * plain of the sketch itself.
	 * @param a a Processing PApplet object
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
			
				transformedCoordinates = new ArrayList<Node>();
				rawCoordinates         = new ArrayList<Coordinate>();

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
							try {
								theValue = (Double) feature.getAttribute(this.valueName);
							} catch(ClassCastException e) {
								if (e.getMessage().equalsIgnoreCase("java.lang.Long cannot be cast to java.lang.Double")) {
									Long l = (Long) feature.getAttribute(this.valueName);
									theValue = l.doubleValue();
								}
							}
						}

						for (int i = 0; i < theGeom.getNumGeometries(); i++) {

							//System.out.println("Got geometry " + i);
							Geometry g = theGeom.getGeometryN(i);

							if (g.getGeometryType().equalsIgnoreCase("point")) {

								Point      p = (Point) g;
								Coordinate c = p.getCoordinate();

								rawCoordinates.add(c);

								try {
									Node n = new Node(
											c.hashCode(),
											this.map((float) c.x, box.getWest(), box.getEast(), 0f, a.width),
											this.map((float) c.y, box.getNorth(), box.getSouth(), 0f, a.height),
											0d,
											theValue.doubleValue(),
											theName
									);
									if (DEBUG == true) {
										System.out.println("X-axis: " + c.x + " is mapped from geographical range (" + box.getWest() + "," + box.getEast() + ") on to display range (0f," + a.width + ") as " + this.map((float) c.x, box.getWest(), box.getEast(), 0f, a.width));
										System.out.println("Y-axis: " + c.y + " is mapped from geographical range (" + box.getNorth() + "," + box.getSouth() + ") on to display range (0f," + a.height + ") as " + this.map((float) c.y, box.getNorth(), box.getSouth(), 0f, a.height));
									}
									transformedCoordinates.add(n);
								} catch (NullPointerException e2) {
									System.out.println("NullPointerException " + e2);
								}
							} else {
								System.out.println("Have instantiated a Point object but with geometry of type " + g.getGeometryType());
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
	 * Return an ArrayList, each row of which contains
	 * a single Node object. The idea is that each 
	 * entry represents a separate point that can be 
	 * drawn in the sketch. The simplest case is to then
	 * just pass the entire ArrayList straight to the 
	 * drawGeoPoint() method in GeoPApplet.
	 * @return An ArrayList of Nodes
	 */
	public ArrayList<Node> getCoordinates(PApplet a) {
		this.transformCoordinates(a);
		return this.transformedCoordinates;
	}
	
	/**
	 * Returns the raw coordinates without any 
	 * transformation to fit into the sketch. In this
	 * case you can manipulate the underlying SimpleFeatures
	 * to perform other operations.
	 * @return ArrayList<Coordinate>
	 */
	public ArrayList<Coordinate> getCoordinates() {
		return this.rawCoordinates;
	}
	
	/**
	 * Draws all of the points contained in
	 * a Points object loaded from a file. 
	 * The simplest way to work with this method is 
	 * to set the color, fill, and stroke in your sketch
	 * and then just call this function by passing it 
	 * the Applet instance (probably by saying: <code>object.project(this, width, height)</code>. 
	 * @param a a Processing PApplet object
	 * @param w a float indicating the width of the ellipse to draw
	 * @param h a float indicating the height of the ellipse to draw
	 */
	public void project(PApplet a, float w, float h) {
		
		ArrayList<Node> c = this.getCoordinates(a);
		//println("Point array list returned " + c.size());

		for (int i = 0; i < c.size(); i++) {
			Node point = c.get(i);
			a.g.ellipse(point.getX(), point.getY(), w, h);
		}
	}
	
	/**
	 * <p>Colours all of the points according to the specified
	 * colour scale. The default values are set as white to 
	 * black, so you'll probably want to override these. If
	 * a value falls below the minimum then it will be assigned 
	 * the colour of the minimum value. If a value exceeds the 
	 * maximum then it will be assigned the colour of the maximum
	 * value.</p> 
	 * <p>In the longer run I'll try to implement a version 
	 * that doesn't require you to specify these values.</p>
	 * @param a   the PApplet (usually just referred to in a sketch as 'this')
	 * @param w   the width of the ellipse you want to draw on top of the point
	 * @param h   the height of the ellipse you want to draw on top of the point
	 * @param min the minimum value to use (against which to scale the point values)
	 * @param max the maximum value to use (against which to scale the point values)
	 */
	public void projectValues(PApplet a, float w, float h, float min, float max) {
		
		ArrayList<Node> c = this.getCoordinates(a);
		//println("Point array list returned " + ac.size());
		
		for (int j = 0; j < c.size(); j++) {
			Node point = c.get(j);
			//System.out.println("Value for " + point.getName() + " is " + point.getValue());
			a.g.fill(this.interpolateColor(a, (float) point.getValue(), min, max));
			a.g.ellipse(point.getX(), point.getY(), w, h);
		}
	}
	
	/**
	 * <p>Scales the size of the point according to the value
	 * of the data associated with the point. You pass in a radius
	 * value that indicates the desired area to associate with the
	 * maximum value specified by the max parameter. In this case, 
	 * values that exceed the maximum will continue to scale up 
	 * to give points whose radius exceeds the specified r value.</p>
	 * <p>In the long run I'll try to implement a version that doesn't
	 * require you to specify a maximum.</p>
	 * @param a   the PApplet (usually just referred to in a sketch as 'this')
	 * @param r   the desired maximum radius
	 * @param max the expected maximum value
	 */
	public void projectAreas(PApplet a, float r, float max) {
		
		ArrayList<Node> c = this.getCoordinates(a);
		//println("Point array list returned " + ac.size());
		
		double maxArea = Math.PI * Math.pow(r,2);
		
		for (int j = 0; j < c.size(); j++) {
			Node point = c.get(j);
			//System.out.println("Value for " + point.getName() + " is " + point.getValue() + " and max is " + max);
			
			double areaTotal = (point.getValue() / max) * maxArea;
			float diameter = Math.round(Math.sqrt(areaTotal / Math.PI ));
			
			a.g.ellipse(point.getX(), point.getY(), diameter, diameter);
		}
	}
	
	/**
	 * <p>Scales the size of the point <i>and</i> the colour according
	 * to the data value associated with that location.</p>
	 * <p>In the long run I'll try to implement a version that doesn't
	 * require you to specify a minimum or maximum.</p>
	 * @see Points#projectAreas(PApplet, float, float) projectAreas
	 * @see Points#projectValues(PApplet, float, float, float, float) projectValues
	 * @param a   the PApplet (usually just referred to in a sketch as 'this')
	 * @param r   the desired maximum radius
	 * @param min the minimum value to use (against which to scale the point values)
	 * @param max the maximum value to use (against which to scale the point values)
	 */
	public void projectAreasAndValues(PApplet a, float r, float min, float max) {
		
		ArrayList<Node> c = this.getCoordinates(a);
		//println("Point array list returned " + ac.size());
		
		double maxArea = Math.PI * Math.pow(r,2);
		
		for (int j = 0; j < c.size(); j++) {
			Node point = c.get(j);
			//System.out.println("Value for " + point.getName() + " is " + point.getValue() + " and max is " + max);
			
			double areaTotal = (point.getValue() / max) * maxArea;
			float diameter = Math.round(Math.sqrt(areaTotal / Math.PI ));
			
			a.g.fill(this.interpolateColor(a, (float) point.getValue(), min, max));
			a.g.ellipse(point.getX(), point.getY(), diameter, diameter);
		}
	}
	
	/**
	 * Draws all of the points contained in
	 * a Points object loaded from a shape file. 
	 * The simplest way to work with this method is 
	 * to set the color, fill, and stroke in your sketch
	 * and then just call this function by passing it 
	 * the Applet instance (probably by saying: <code>object.project(this, image, width, height)</code>. 
	 * 
	 * Note that if you want the image to appear 
	 * right on top of the coordinates then you will need
	 * to call "imageMode(CENTER)" in your sketch first. 
	 * @param a a Processing PApplet object
	 * @param i a PImage object containing the image we want to draw at each point
	 * @param w a float indicating the width of the image to draw
	 * @param h a float indicating the height of the image to draw
	 */
	public void project(PApplet a, PImage i, float w, float h) {
		
		ArrayList<Node> c = this.getCoordinates(a);
		//println("Point array list returned " + c.size());

		for (int j = 0; j < c.size(); j++) {
			Node point = c.get(j);
			a.g.image(i, point.getX(), point.getY(), w, h);
		}
	}
	
	/**
	 * <p>Draws all of the points contained in
	 * a Points object loaded from a file. This
	 * one differs from the version without a 
	 * PGraphics object in that it allows you to
	 * use the graphics object as a buffer and won't
	 * write directly to the Applet view.</p>
	 * <p>The simplest way to work with this method is 
	 * to set the color, fill, and stroke in your sketch
	 * and then just call this function by passing it 
	 * the Applet and Graphics instances.</p> 
	 * @param a a Processing PApplet object
	 * @param p a Processing PGraphics object
	 * @param w a float indicating the width of the ellipse to draw
	 * @param h a float indicating the height of the ellipse to draw
	 */
	public void project(PApplet a, PGraphics p, float w, float h) {
		
		ArrayList<Node> c = this.getCoordinates(a);
		//println("Point array list returned " + c.size());

		for (int i = 0; i < c.size(); i++) {
			Node point = c.get(i);
			p.ellipse(point.getX(), point.getY(), w, h);
		}
	}
	
	/**
	 * <p>Draws all of the points contained in
	 * a Points object loaded from a file. This
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
	 * @param i a PImage object containing the image we want to draw at each point
	 * @param w a float indicating the width of the image to draw
	 * @param h a float indicating the height of the image to draw
	 */
	public void project(PApplet a, PGraphics p, PImage i, float w, float h) {
		
		ArrayList<Node> c = this.getCoordinates(a);
		//println("Point array list returned " + c.size());

		for (int j = 0; j < c.size(); j++) {
			Node point = c.get(j);
			p.image(i, point.getX(), point.getY(), w, h);
		}
	}
	
	public void labelOffset(int x, int y) {
		this.labelXOffset = x;
		this.labelYOffset = y;
	}
	
	public void label(PApplet a) {
		
		ArrayList<Node> c = this.getCoordinates(a);
		//println("Point array list returned " + c.size());

		for (int j = 0; j < c.size(); j++) {
			Node p = c.get(j);
			a.text(p.name, p.getX() + this.labelXOffset, p.getY() + this.labelYOffset);
		}
	}

}
