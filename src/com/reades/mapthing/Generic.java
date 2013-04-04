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

import processing.core.PApplet;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.divbyzero.gpx.parser.JDOM;
import net.divbyzero.gpx.parser.Parser;
import net.divbyzero.gpx.parser.ParsingException;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.SchemaException;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * <p>This serves as a Generic template and super-class
 * for implementations of various entity-drawing classes.</p>
 * 
 * <p>You wouldn't call this directly, but it is used by
 * the sub-classes to do things like open shape files (and save a 
 * reference to the FeatureCollection object, and to 
 * map the geographic coordinates on to the plane of the
 * Processing sketch window).</p>
 * @author jon@reades.com
 *
 * Features to implement:
 * <ul>
 * <li>Output extracted data (e.g. from CSV or Shape File) to a new format (e.g. to CSV or Shape File)
 * <li>Build in ColorBrewer for chloropleth maps and test lerpcolor() calls are working as expected
 * <li>Binning of data in arbitrary number of groups
 * <li>Using Strings to create categories from a value field instead of only numeric data
 * <li>Automatically find min and max values in the value field
 * <li>Ability to specify and access more than one value field (so you could cycle between several value columns without needing to copy the entire shape file)
 * <li>Ability to use different binning approaches (e.g. logarithmic, standard deviations, etc.) for numeric data
 * <li>Ability to automagically convert from the source srid to the envelope's srid
 * </ul>
 * 
 * <p>Package designed to hold objects used 
 * to translate between Shape and CSV files, and 
 * Processing's PShape, PImage and other 
 * sketch drawing classes.</p>
 * 
 * <p>I am deeply indebted for the CSV code to 
 * the GeoTools example available here:
 * http://docs.geotools.org/stable/userguide/examples/csv2shp.html</p>
 * 
 * <p>I should also definitely acknowledge the
 * input of Camilo Vargas, without which this
 * would never have happened!</p>
 */
public class Generic implements Serializable {
	
	private static final long serialVersionUID = -898229787710388349L;
	
	public boolean DEBUG = false;
	
	protected String type;
	protected String source;
	protected BoundingBox box;
	
	protected int srid;
	
	protected double localSimplify;
	protected double globalSimplify;
	
	protected int appletWidth;
	protected int appletHeight;
	
	int scaleStartColor = 255;
	int scaleEndColor   = 0;
	int scaleMidColor;
	int scaleSteps;
	
	// These are not serializable and I'm not
	// about to figure out how to make them so
	transient FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;
	transient FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection;
	
	/**
	 * Default places to look for likely name and
	 * value fields in the source file. We allow the
	 * user to specify a column position or a column
	 * name as they like.
	 */
	int labelPosition = 0;
	String labelName  = "name";
	int valuePosition = 0;
	String valueName  = "value";
	
	/**
	 * <p>Called by the implementing sub-classes. Takes a 
	 * reference to a BoundingBox (usually the 'view port'
	 * of the sketch) and a String that is (loosely) a path
	 * to a file.</p>
	 * <p>Right now, the String must end in one of: .shp, .csv, 
	 * and .tsv for us to be able to do anything with it. However,
	 * in the longer run I hope to implement things like using a
	 * dsn to specify an ArcSDE server or PostgreSQL database...
	 * How exactly that will work I have yet to determine but this
	 * seems like the best way to 'future-proof' the spec.</p>
	 * <p>In the even that you are passing it a CSV or TSV
	 * file, the method will attempt to parse the file based
	 * on whether it was called by the Lines or Points class.
	 * If called by the Points class then it will assume that
	 * there is only one x/y coordinate pair. If called by the
	 * Lines class then it will assume that there are two x/y
	 * coordinate pairs.</p>
	 * <p> The x-coordinate will be based on any
	 * column matching 'x','x1','x2','lon','long','longitude','easting'; 
	 * and the y-coordinate will be based on any column matching 'y',
	 * 'y1','y2','lat','latitude','northing'. I'm reluctant to add other
	 * patterns, but this could be done for good reason.</p>
	 * @param b the BoundingBox object defining the default view
	 * @param r a String that would allow us to find the Shape file, CSV, dsn, etc.
	 * @param t a String that specifies the type of object (used internally)
	 */
	protected Generic(BoundingBox b, String r) {

		this.source = r;
		this.box    = b;
		this.type   = new Throwable().getStackTrace()[1].getClassName();

		//System.out.println("Type is: "+ type);
		//System.out.println("Opening resource: " + r);

		try {
			if (r.endsWith(".shp")) {
				
				this.instantiateShapeFile(r);

			} else if (r.endsWith(".gpx")) {

				this.instantiateGpxTracks(r);

			} else if (r.endsWith(".csv") || r.endsWith(".tsv")) {

				this.instantiateDelimittedFile(r);
				
			}
		} catch (IOException e) {
			System.out.println("Problems with reading/loading resource: " + r + "!");
		}
		
		if (this.box.getProjection() != this.getProjection()) {
			System.out.println("Projection mismatch: envelope has srid of " + this.box.getProjection() + " but this source (" + this.source + ") has srid of " + this.getProjection() + ". Will show the map but you may have alignment issues.");
		}
		
	}
	
	/**
	 * <p>A default implementation. Often used
	 * to instantiate polygons and lines from 
	 * within the shape file as separate, draw-able
	 * entities within the sketch.</p>
	 * @param b the BoundingBox object defining the default view
	 * @param f the FeatureCollection used to instantiate the object
	 */
	public Generic(BoundingBox b, FeatureCollection<SimpleFeatureType,SimpleFeature> f) {
		this.box = b;
		this.featureCollection = f;
	}
	
	/**
	 * <p>A default implementation. Often used
	 * to instantiate polygons and lines from 
	 * within the shape file as separate, draw-able
	 * entities within the sketch. Note that we 
	 * create a FeatureCollection even though there's
	 * only one feature contained within it. This is 
	 * to ensure consistency in handling objects across
	 * the pipeline.</p>
	 * @param b the BoundingBox object defining the default view
	 * @param f the SimpleFeature retrieved from the shape file
	 */
	public Generic(BoundingBox b, SimpleFeature f) {
		this.box = b;
		
		FeatureCollection<SimpleFeatureType, SimpleFeature> collection = FeatureCollections.newCollection();
		collection.add(f);
		
		this.featureCollection = collection;
	}
	
	/**
	 * <p>Used by the Lines and Polygons objects to 
	 * implement various types of simplification
	 * appropriate to the object being displayed. 
	 * This works in the units of the shape file 
	 * itself, so in the case of OSGB-mapped shapes
	 * it would be metres.</p>
	 * <p>Note that we have two types of simplifcation
	 * here: this one works on each object individually,
	 * while the other method works on them all 
	 * simultaneously.</p>
	 * @param d the distance over which to simplify
	 * @see Generic#setGlobalSimplificationThreshold(double) setGlobalSimplificationThreshold
	 */
	public void setLocalSimplificationThreshold(double d) {
		this.localSimplify = d;
	}
	
	/**
	 * <p>Used by the Lines and Polygons objects to 
	 * implement various types of simplification
	 * appropriate to the object being displayed. 
	 * This works in the units of the shape file 
	 * itself, so in the case of OSGB-mapped shapes
	 * it would be metres.</p>
	 * <p>Note that we have two types of simplifcation
	 * here: this one works on them all at once,
	 * while the other method works on each one
	 * individually.</p>
	 * @param d the distance over which to simplify
	 * @see Generic#setLocalSimplificationThreshold(double) setLocalSimplificationThreshold
	 */
	public void setGlobalSimplificationThreshold(double d) {
		this.globalSimplify = d;
	}
	
	/**
	 * Returns the centroids of an object in a Processing sketch. 
	 * This essentially clones the Points, Polygons, or Lines object 
	 * so that you can use both in your sketch with the same 
	 * reference features.
	 */
	public FeatureCollection<SimpleFeatureType, SimpleFeature> getCentroids() {
		
		/*
		 * We create a FeatureCollection into which we will put each Feature created from a record
		 * in the input csv data file
		 * FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection;
		 */
		SimpleFeatureCollection collection = FeatureCollections.newCollection();

		/*
		 * Retrieve the features associated with this object, 
		 * regardless of type.
		 */
		FeatureIterator<SimpleFeature> i = this.getFeatures();
		
		try {
			
			while (i.hasNext()) {
				
				SimpleFeature f = i.next();
				Geometry      g = (Geometry) f.getDefaultGeometry();
				Point         p = g.getCentroid();

				//DataUtilities.reType(arg0, arg1, arg2);
				String originalSpec = DataUtilities.spec(f.getFeatureType());
				StringBuffer newSpec = new StringBuffer();
				
				/*
				 * This sets up the two default elements of the typeSpec
				 * -- the type of feature and its projection, next we need
				 * to find out what else is in the the existing feature so
				 * that we can clone it appropriately
				 */
				newSpec.append("location:Point:");
				newSpec.append("srid=" + this.box.getProjection() + ",");
								
				String[] elements = originalSpec.split(",");		
				for (int k=0; k < elements.length; k++) {
					if (elements[k].startsWith("the_geom")) {
						// Do nothing
					} else if (elements[k].startsWith("srid")) {
						// Do nothing
					} else {
						newSpec.append(elements[k]);
						if (k < elements.length - 1) {
							newSpec.append(",");
						}
					}
				}
				
				/*
				 * And initialise the new SimpleFeatureType using
				 * the cloned specification, but with the type changed
				 * to a Point.
				 */
				SimpleFeatureType st = DataUtilities.createType(
						"Location",                   // <- the name for our feature type
						newSpec.toString()
				);
				
				/*
				System.out.println("Original spec is " + originalSpec);
				System.out.println("New spec is " + newSpec.toString());
				System.out.println("Features have " + f.getAttributeCount() + " attributes");
				
				List<Object> attrs = f.getAttributes();
				System.out.println("All Attributes: " + attrs.toString());
				*/
				SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(st);

				featureBuilder.add(p);
				for (int k=1; k < f.getAttributeCount(); k++) {
					featureBuilder.add(f.getAttribute(k));
				}

				SimpleFeature feature = featureBuilder.buildFeature(null);
				collection.add(feature);
			}

		} catch (SchemaException s) {
			System.out.println("Problem creating the feature type from the source type: " + s.toString());	
		} finally {
			if (i != null) {
				i.close();
			}
		}
		
		return collection;
	}
	
	/**
	 * <p>Used by all classes to map their contents on 
	 * to the plane of the sketch. So we translate 
	 * between the range of the shape file and range
	 * of the viewport we want to work with. Note that
	 * this can produce remapped values that are 
	 * negative, below xRemapMin, or exceed xRemapMax.</p>
	 * @param x1        the input position along the x- or y-axis
	 * @param xMin      the expected minimum value of the range from which x1 is drawn
	 * @param xMax      the expected maximum value of the range from which x1 is drawn
	 * @param xRemapMin what we are mapping xMin on to
	 * @param xRemapMax what we are mapping xMax on to
	 * @return A float indicating the new value of x1
	 */
	public float map(float x1, float xMin, float xMax, float xRemapMin, float xRemapMax) {
		return PApplet.map(x1, xMin, xMax, xRemapMin, xRemapMax);
	}
	
	/**
	 * <p>Retrieves the set of features identified by a particular
	 * 'id' in a given field of a shape file and then 
	 * returns it as a FeatureCollection. Note that it
	 * makes no assumptions about the type of feature 
	 * returned, you must take care of this yourself.</p>
	 * 
	 * <p>The main thing that this offers is the ability to 
	 * retrieve results based on a 'LIKE' query. So if you
	 * pass in '%Greater%' as the 'id', and CITY_NAME as 
	 * the field, then you might get back 'Greater London',
	 * 'Greater Manchester', etc.</p>
	 * @return FeatureCollection<SimpleFeatureType, SimpleFeature>
	 */
	public FeatureCollection<SimpleFeatureType, SimpleFeature> getMultipleFeaturesByPattern(String field, String id) {
		
		try {
			
			Filter c = CQL.toFilter(field + " LIKE '" + id + "'");

			FeatureCollection<SimpleFeatureType,SimpleFeature> fc = featureSource.getFeatures(c);
			return fc;
			/*
			FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( null );
			Filter f = ff.like(ff.property(field), id);
			
			FeatureIterator<SimpleFeature> it = fc.features();
			
			while (it.hasNext()) {
				SimpleFeature sff = it.next();
				List<Object> l = sff.getAttributes();
				
				System.out.println("Attribute count: " + l.size());
				for (int i=0; i < l.size(); i++) {
					System.out.println("Name: " + l.get(i));
				}
			}
			return featureSource.getFeatures( ff.like( ff.property(field), id )); //, false ));
			*/

		} catch (IOException e) {
			System.out.println("IOException " + e);
			e.printStackTrace();
		} catch (CQLException e) {
			System.out.println("Error creating CQL Filter: " + e);
		}
		return null;
	}
	
	/**
	 * Deprecated. I've kept this for backwards-compatibility
	 * with anyone who tried out version 1.0 but I intend to remove
	 * this down the line since the method name is less clear.
	 * @param field
	 * @param id
	 * @return FeatureCollection<SimpleFeatureType, SimpleFeature>
	 */
	public FeatureCollection<SimpleFeatureType, SimpleFeature> getMultipleFeaturesById(String field, String id) {
		return this.getMultipleFeaturesByPattern(field, id);
	}
	
	/**
	 * <p>Retrieves all features identified by a particular
	 * id in a given field of the shape file and then 
	 * returns them as a FeatureCollection. Note that it
	 * makes no assumptions about the type of feature 
	 * returned, you must take care of this yourself.</p>
	 * 
	 * <p>The principal difference between this and the version
	 * that takes a Set<String> is that the return values will
	 * ordered by the input array.</p>
	 * @return FeatureCollection<SimpleFeatureType, SimpleFeature>
	 */
	public FeatureCollection<SimpleFeatureType, SimpleFeature> getMultipleFeaturesByPattern(String field, String[] ids) {
		try {
			FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( null );
			
			List<Filter> match = new ArrayList<Filter>();
			for (int i=0; i < ids.length; i++) {
				match.add( ff.equals( ff.property(field), ff.literal(ids[i])) );
			}
			Filter filter = ff.or( match );
			return featureSource.getFeatures( filter );
		} catch (IOException e) {
			System.out.println("IOException " + e);
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Deprecated. I've kept this for backwards-compatibility
	 * with anyone who tried out version 1.0 but I intend to remove
	 * this down the line since the method name is less clear.
	 * @param field
	 * @param ids
	 * @return FeatureCollection<SimpleFeatureType, SimpleFeature>
	 */
	public FeatureCollection<SimpleFeatureType, SimpleFeature> getMultipleFeaturesById(String field, String[] ids) {
		return this.getMultipleFeaturesByPattern(field, ids);
	}
	
	/**
	 * <p>Retrieves all features identified by a particular
	 * id in a given field of the shape file and then 
	 * returns them as a FeatureCollection. Note that it
	 * makes no assumptions about the type of feature 
	 * returned, you must take care of this yourself.</p>
	 * 
	 * <p>The principal difference between this and the version
	 * that takes a String[] is that the return values will
	 * not necessarily be ordered in any particular way. So 
	 * if the order of the rows in the Shape file are meaningful 
	 * in some way then you should use the other method instead.</p>
	 * @return FeatureCollection<SimpleFeatureType, SimpleFeature>
	 */
	public FeatureCollection<SimpleFeatureType, SimpleFeature> getMultipleFeaturesByPattern(String field, Set<String> ids) {
		try {
			FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( null );
			
			List<Filter> match = new ArrayList<Filter>();
			for ( String id : ids) {
				match.add( ff.equals( ff.property(field), ff.literal(id)) );
			}
			return featureSource.getFeatures( ff.or( match ) );
		} catch (IOException e) {
			System.out.println("IOException " + e);
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Deprecated. I've kept this for backwards-compatibility
	 * with anyone who tried out version 1.0 but I intend to remove
	 * this down the line since the method name is less clear.
	 * @param field
	 * @param ids
	 * @return FeatureCollection<SimpleFeatureType, SimpleFeature>
	 */
	public FeatureCollection<SimpleFeatureType, SimpleFeature> getMultipleFeaturesById(String field, Set<String> ids) {
		return this.getMultipleFeaturesByPattern(field, ids);
	}
	
	/**
	 * Select a set of attributes using a double-precision
	 * value instead of a SQL-like string. This works for, 
	 * say, selecting countries whose area is greater than 'min'
	 * so that you only label the largest ones.
	 * @param field the name of the column in which the values can be found
	 * @param min   the minimum value which must be exceeded for the feature to be selected
	 * @return FeatureCollection<SimpleFeatureType, SimpleFeature>
	 */
	public FeatureCollection<SimpleFeatureType, SimpleFeature> getMultipleFeaturesByValue(String field, double min) {
		try {
			FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( null );
			
			List<Filter> match = new ArrayList<Filter>();
			match.add( ff.greaterOrEqual( ff.property(field), ff.literal(min) ));
			return featureSource.getFeatures( ff.or( match ) );
		} catch (IOException e) {
			System.out.println("IOException " + e);
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Retrieves all features contained in the
	 * data source and returns them as a FeatureCollection. 
	 * This is the best way to get at the underlying GeoTools
	 * data if you want to do something more sophisticated that
	 * isn't yet implemented in the main codebase.
	 * @return FeatureIterator<SimpleFeature>
	 */
	public FeatureIterator<SimpleFeature> getFeatures() {
		return this.featureCollection.features();
	}
	
	/**
	 * Get the boundary envelope of the shape file itself
	 * @return A ReferenceEnvelop object
	 */
	public ReferencedEnvelope getBounds() {
		return featureCollection.getBounds();
	}
	
	/**
	 * Specify the name of the column that contains
	 * the values (i.e. place names or country names)
	 * that you want to use as a label in your
	 * Processing sketch.
	 * @param fieldName
	 */
	public void setLabelField(String fieldName) {
		this.labelName = fieldName;
	}
	
	/**
	 * Specify the column position that contains
	 * the values that you want to use as a label
	 * in your Processing sketch. This will automatically
	 * skip over the features themselves, but you'll need
	 * to use trial-and-error to figure out which field
	 * contains the values you want (although they should
	 * come in the order in which they're specified in the
	 * shape file or CSV file).
	 * @param fieldId
	 */
	public void setLabelField(int fieldId) {
		this.labelPosition = fieldId;
	}
	
	/**
	 * Specify the name of the column that contains
	 * the values (i.e. numbers or categories) that 
	 * you want to use in your Processing sketch.
	 * @param fieldName
	 */
	public void setValueField(String fieldName) {
		this.valueName = fieldName;
	}
	
	/**
	 * Specify the column position that contains
	 * the values that you want to use in your
	 * Processing sketch. This will automatically
	 * skip over the features themselves, but you'll need
	 * to use trial-and-error to figure out which field
	 * contains the values you want (although they should
	 * come in the order in which they're specified in the
	 * shape file or CSV file).
	 * @param fieldId
	 */
	public void setValueField(int fieldId) {
		this.valuePosition = fieldId;
	}
	
	/**
	 * Apply a scale that will be used for values or categories
	 * within this object. The idea here is that you can set a
	 * single color scale and use it on different values while
	 * the code takes care of coloring it for you. 
	 * @param lowColor
	 * @param highColor
	 * @param numberOfSteps
	 */
	public void setColorScale(int lowColor, int highColor, int numberOfSteps) {
		this.scaleStartColor = lowColor;
		this.scaleEndColor   = highColor;
		this.scaleSteps      = numberOfSteps;
	}
	
	/**
	 * Apply a scale that will be used for values or categories
	 * within this object. The idea here is that you can set a
	 * single colour scale and use it on different values while
	 * the code takes care of colouring it for you. 
	 * @param lowColour
	 * @param highColour
	 * @param numberOfSteps
	 */
	public void setColourScale(int lowColour, int highColour, int numberOfSteps) {
		this.setColorScale(lowColour, highColour, numberOfSteps);
	}
	
	/**
	 * Apply a scale that will be used for values or categories
	 * within this object. The idea here is that you can set a
	 * single color scale and use it on different values while
	 * the code takes care of coloring it for you. 
	 * @param lowColor
	 * @param midColor
	 * @param highColor
	 * @param numberOfSteps
	 */
	public void setColorScale(int lowColor, int midColor, int highColor, int numberOfSteps) {
		this.scaleStartColor = lowColor;
		this.scaleEndColor   = highColor;
		this.scaleMidColor   = midColor;
		this.scaleSteps      = numberOfSteps;
	}
	
	/**
	 * Apply a scale that will be used for values or categories
	 * within this object. The idea here is that you can set a
	 * single colour scale and use it on different values while
	 * the code takes care of colouring it for you. 
	 * @param lowColour    a Processing color object representing the low-value colour
	 * @param midColour    a Processing color object representing the mid-value colour
	 * @param highColour   a Processing color object representing the high-value colour
	 * @param numberOfSteps
	 */
	public void setColourScale(int lowColour, int midColour, int highColour, int numberOfSteps) {
		this.setColorScale(lowColour, midColour, highColour, numberOfSteps);
	}
	
	public int interpolateColor(PApplet a, float value, float min, float max) {
		
		int low;
		int high;
		
		if (value > max) {
			return this.scaleEndColor;
		} else if (value < min) {
			return this.scaleStartColor;
		} else if (this.scaleMidColor > 0 && value < (max-min)/2) {
			low  = this.scaleStartColor;
			high = this.scaleMidColor;
		} else if (this.scaleMidColor > 0 && value > (max-min)/2) {
			low  = this.scaleMidColor;
			high = this.scaleEndColor;
		} else {
			low  = this.scaleStartColor;
			high = this.scaleEndColor;
		}
		return a.g.lerpColor(low, high, PApplet.map(value, min, max, 0, 1));
	}
	
	public int getProjection() {
		
		if (this.srid > 0) {
			return this.srid;
		} else {
			String proj = DataUtilities.spec(this.featureCollection.getSchema());
			System.out.println("Projection string: " + proj);
			if (proj.indexOf("srid") == -1) {
				System.out.println("Can't check srid on Multi-Line objects");
				this.srid = this.box.getProjection();
			} else {
				try {
					this.srid   = Integer.parseInt(proj.substring(proj.indexOf("srid=",0)+5, proj.indexOf(",",proj.indexOf("srid=",0)+5)));
				} catch (StringIndexOutOfBoundsException e) {
					this.srid   = Integer.parseInt(proj.substring(proj.indexOf("srid=",0)+5, proj.length()));
				}
			}
			System.out.println("Parsed srid " + this.srid + " from " + proj);
		}
		return this.srid;
	}
	
	public int getProjection(FeatureCollection<SimpleFeatureType, SimpleFeature> f) {
		String proj = DataUtilities.spec(f.getSchema());
		int srid    = Integer.parseInt(proj.substring(proj.indexOf("srid=",0)+5, proj.indexOf(",",proj.indexOf("srid=",0)+5)));
		return srid;
	}
	
	/**
	 * Incomplete implementation -- this is supposed
	 * to allow you to select features falling within
	 * a particular distance of a polygon, but as yet I've
	 * not quite figured how to do it properly.
	 */
	public void appendNearestFeatures(Generic gs, double maxDistance) {
		
		/*
		 * This class' feature iterator
		 */
		FeatureIterator<SimpleFeature> fi  = this.featureCollection.features();
		
		try {
			while (fi.hasNext()) {
				SimpleFeature  s = fi.next();
				Geometry theGeom = (Geometry) s.getDefaultGeometry();

				for (int i = 0; i < theGeom.getNumGeometries(); i++) {

					//System.out.println("Got geometry " + i);
					Geometry g = theGeom.getGeometryN(i);
					/*
					FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
				    Filter filter = ff.dwithin(ff.property("POLYGON"), ff.literal(point), maxDistance, uom.toString());
				    featureSource.getFeatures(filter);
				    */

					if (g.getGeometryType().equalsIgnoreCase("point")) {
						// Point      p = (Point) g;
						//Coordinate c = p.getCoordinate();
					} else if (g.getGeometryType().equalsIgnoreCase("polyline")) {
						
					}
				}
			}
		} finally {
			if (fi != null) {
				fi.close();
			}
		}
	}
	
	private void instantiateShapeFile(String r) throws IOException {
		File f = new File(r);
		System.out.println("Opening shape file " + f.getPath());

		FileDataStore store = FileDataStoreFinder.getDataStore(f);
		featureSource       = store.getFeatureSource();
		featureCollection   = featureSource.getFeatures();
		
		if (DEBUG == true)
			System.out.println("FeatureType: " + DataUtilities.spec(store.getSchema()));
		
	}
	
	private void instantiateDelimittedFile(String r) throws FileNotFoundException, IOException {

		/*
		 * Read the first two rows of the file in order to get the
		 * column names and some representative data types
		 */
		BufferedReader reader = new BufferedReader(new FileReader(r));

		String tokeniser;
		if (r.endsWith(".tsv")) {
			tokeniser = "\\t";
		} else {
			tokeniser = "\\,";
		}

		/* First line of the data file is assumed to be the header */
		String[] cols = reader.readLine().split(tokeniser);
		String[] vals = reader.readLine().split(tokeniser);

		System.out.print("Header row: ");
		for (int i=0; i < cols.length; i++) {
			System.out.print(cols[i]);
			if (i < cols.length - 1) {
				System.out.print(", ");
			}
		}
		System.out.println(" ");

		if (DEBUG == true) {
			System.out.print("Value row: ");

			for (int i=0; i < vals.length; i++) {
				System.out.print(vals[i]);
				if (i < vals.length-1) {
					System.out.print(", ");
				}
			}
			System.out.println(" ");
		}

		StringBuffer typeSpec = new StringBuffer();

		// Change the type based on what was instantiated
		if (type.contains("com.reades.mapthing.Points")) {
			typeSpec.append("location:Point:");
		} else {
			typeSpec.append("location:Line:");
		}

		// Set the projection based on the envelope's SRID
		typeSpec.append("srid=" + this.box.getProjection() + ",");

		// Now try to figure out what all of the content is --
		// right now we take it simply and assume that there
		// are only doubles and strings... this could be improved
		for (int i=0; i < cols.length; i++) {
			if (DEBUG == true)
				System.out.println("Column: " + cols[i] + " --> " + vals[i]);
			try {
				Integer.parseInt(vals[i]);
				typeSpec.append(cols[i] + ":Double");
				if (DEBUG == true)
					System.out.print(" is integer");
			} catch (NumberFormatException e) {
				try {
					Double.parseDouble(vals[i]);
					typeSpec.append(cols[i] + ":Double");
					if (DEBUG == true)
						System.out.print(" is double");
				} catch (NumberFormatException e1) {
					typeSpec.append(cols[i] + ":String");
					if (DEBUG == true)
						System.out.print(" is string");
				}
			}
			if (i < cols.length-1) {
				typeSpec.append(",");
			}

			if (DEBUG == true)
				System.out.println(" ");
		}

		reader.close();

		if (DEBUG == true)
			System.out.println("Done creating type specification for delimitted file.");

		/*
		 * We create a FeatureCollection into which we will put each Feature 
		 * created from a record in the input TSV or CSV data file
		 * FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection;
		 */
		SimpleFeatureCollection collection = FeatureCollections.newCollection();

		/*
		 * GeometryFactory will be used to create the geometry attribute of each feature (a Point
		 * object for the location)
		 */
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

		// Re-open the file so that we can pick up the
		// first row of data that we already had to read
		// (there may be another way to do this, but this
		// works for now)
		reader = new BufferedReader(new FileReader(r));

		// Discard the header
		reader.readLine().split(tokeniser);
		
		try {
			if (DEBUG == true)
				System.out.println("Creating Type: " + typeSpec.toString());
			
			SimpleFeatureType st = DataUtilities.createType(
					"Location",                   // <- the name for our feature type
					typeSpec.toString()
			);

			SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(st);

			SimpleFeatureTypeBuilder stb = new SimpleFeatureTypeBuilder();
			stb.setCRS(st.getCoordinateReferenceSystem());

			try {

				if (type.contains("com.reades.mapthing.Points")) {

					HashMap<String,Integer> headers = new HashMap<String,Integer>();

					Pattern pX = Pattern.compile("^(?:x|x1|easting|long?)$");
					Pattern pY = Pattern.compile("^(?:y|y1|northing|lat)$");

					for (int i=0; i < cols.length; i++) {
						String testCase = cols[i].toLowerCase();

						if (DEBUG == true)
							System.out.println("Checking column[" + i + "] name: " + testCase);

						Matcher mX = pX.matcher(testCase);
						Matcher mY = pY.matcher(testCase);

						if (DEBUG == true && mX.matches()) {
							System.out.println("Got mX match on column " + i);
						}
						if (DEBUG == true && mY.matches()) {
							System.out.println("Got mY match on column " + i);
						}

						if (! headers.containsKey("x") && mX.matches()) {
							headers.put("x",i);
						} else if (! headers.containsKey("y") && mY.matches()) {
							headers.put("y",i);
						}
					}

					String line;

					for (line = reader.readLine(); line != null; line = reader.readLine()) {

						if (DEBUG == true)
							System.out.println("Read line: " + line);

						if (line.trim().length() > 0) { // skip blank lines

							try {
								double x = 0d;
								double y = 0d;

								String tokens[] = line.split(tokeniser);

								if (DEBUG == true)
									System.out.println("Tokens[] has length: " + tokens.length);

								try {
									x = Double.parseDouble(tokens[headers.get("x")]);
									y = Double.parseDouble(tokens[headers.get("y")]);
								} catch (NullPointerException e1) {
									System.out.println("Unable to parse x and y coordinates from positions " + headers.get("x"));
								}

								Coordinate[] c = new Coordinate[1];
								c[0] = new Coordinate(x,y);

								/* Longitude (= x coord) first ! */
								Point point = geometryFactory.createPoint(c[0]);

								featureBuilder.add(point);

								for (int j=0; j < cols.length; j++) {
									featureBuilder.add(tokens[j]);
									if (DEBUG == true)
										System.out.println("Adding feature " + j + " " + tokens[j]);
								}

								SimpleFeature feature = featureBuilder.buildFeature(null);
								collection.add(feature);

							} catch (NullPointerException e) {
								System.out.println("A null pointer error occurred reading line: " + line + "; " + e.getMessage());
								e.printStackTrace();
							}
						}
					}

				} else if (type.contains("com.reades.mapthing.Lines")) {

					HashMap<String,Integer> headers = new HashMap<String,Integer>();

					Pattern pX1 = Pattern.compile("^\bx1??|easting1??|long??1??");
					Pattern pY1 = Pattern.compile("^\by1??|northing1??|lat1??");
					Pattern pX2 = Pattern.compile("^\bx2??|easting2??|long??2??");
					Pattern pY2 = Pattern.compile("^\by2??|northing2??|lat2??");

					for (int i=0; i < cols.length; i++) {

						String testCase = cols[i].toLowerCase();
						Matcher mX1 = pX1.matcher(testCase);
						Matcher mY1 = pY1.matcher(testCase);
						Matcher mX2 = pX2.matcher(testCase);
						Matcher mY2 = pY2.matcher(testCase);

						if (! headers.containsKey("x1") && mX1.matches()) {
							headers.put("x1",i);
						} else if (! headers.containsKey("y1") && mY1.matches()) {
							headers.put("y1",i);
						} else if (! headers.containsKey("x2") && mX2.matches()) {
							headers.put("x2",i);
						} else if (! headers.containsKey("y2") && mY2.matches()) {
							headers.put("y2",i);
						}
					}

					String line;

					for (line = reader.readLine(); line != null; line = reader.readLine()) {
						if (line.trim().length() > 0) { // skip blank lines

							double x1 = 0d;
							double y1 = 0d;
							double x2 = 0d;
							double y2 = 0d;

							String tokens[] = line.split(tokeniser);

							x1 = Double.parseDouble(tokens[headers.get("x1")]);
							y1 = Double.parseDouble(tokens[headers.get("y1")]);

							x2 = Double.parseDouble(tokens[headers.get("x2")]);
							y2 = Double.parseDouble(tokens[headers.get("y2")]);

							/* Longitude (= x coord) first ! */
							Coordinate[] c = new Coordinate[2];
							c[0] = new Coordinate(x1, y1);
							c[1] = new Coordinate(x2, y2);

							LineString l = geometryFactory.createLineString( c );

							featureBuilder.add(l);

							for (int j=0; j < cols.length; j++) {
								featureBuilder.add(tokens[j]);
							}

							SimpleFeature feature = featureBuilder.buildFeature(null);
							collection.add(feature);
						}
					}

				} else {
					throw new IOException("Don't know what to do with classes of type " + type + " when loading from text file");
				}

				this.featureCollection = collection;
			} finally {
				reader.close();
			}
		} catch (SchemaException e2) {
			System.out.println("SchemaException building features!");
			e2.printStackTrace();
		}
	}
	
	private void instantiateGpxTracks(String r) {
		File f = new File(r);
		System.out.println("Opening GPX track file " + f.getPath());

		/*
		 * First we need to set up a JDOM parser and 
		 * extract the GPS waypoints from the GPX file
		 */
		Parser parser = new JDOM();
		net.divbyzero.gpx.GPX gpx = null;
		try {
			gpx = parser.parse(f);
			
			System.out.println("GPX file with " + gpx.getTracks().size() + " tracks parsed.");
			
		} catch (ParsingException e) {
			System.out.println("Unable to parse GPX track file!");
			e.printStackTrace();
		}
		
		StringBuffer typeSpec = new StringBuffer();

		// Change the type based on what was instantiated
		if (type.contains("com.reades.mapthing.Points")) {
			typeSpec.append("location:Point:");
		} else {
			//typeSpec.append("location:Line:");
			typeSpec.append("the_geom:MultiLineString:");
		}

		// Set the projection based on the envelope's SRID
		typeSpec.append("srid=" + this.box.getProjection()); // + ",");
		
		/*
		 * We create a FeatureCollection into which we will put each Feature created from a record
		 * in the input csv data file
		 * FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection;
		 */
		SimpleFeatureCollection collection = FeatureCollections.newCollection();
		
		/*
		 * GeometryFactory will be used to create the geometry attribute of each feature (a Point
		 * object for the location)
		 */
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
		
		try {
			
			if (DEBUG == true)
				System.out.println("Type spec: Location " + typeSpec.toString());
			
			SimpleFeatureType st = DataUtilities.createType(
					"Location",                   // <- the name for our feature type
					typeSpec.toString()
			);

			SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(st);

			SimpleFeatureTypeBuilder stb = new SimpleFeatureTypeBuilder();
			stb.setCRS(st.getCoordinateReferenceSystem());

			if (DEBUG == true)
				System.out.println("Creating Type: " + typeSpec.toString());
			
			if (type.contains("com.reades.mapthing.Points")) {
				
				if (gpx != null) {
					for (int i=0; i < gpx.getTracks().size(); i++) {
						
						ArrayList<net.divbyzero.gpx.TrackSegment> tsa = gpx.getTracks().get(i).getSegments();
						
						for (int j=0; j < tsa.size(); j++) {
							
							ArrayList<net.divbyzero.gpx.Waypoint> waa = tsa.get(j).getWaypoints();
							
							for (int k=0; k < waa.size(); k++) {
								
								net.divbyzero.gpx.Coordinate c = waa.get(k).getCoordinate();
								
								double y = c.getLatitude();
								double x = c.getLongitude();
								
								if (DEBUG == true)
									System.out.println("Creating waypoint with lat/lon: " + x + "/" + y);
								
								/* Longitude (= x coord) first ! */
								Point point = geometryFactory.createPoint(new Coordinate(x, y));

								featureBuilder.add(point);

								SimpleFeature feature = featureBuilder.buildFeature(null);
								collection.add(feature);
							}
						}
					}
				}
						
			} else if (type.contains("com.reades.mapthing.Lines")) {
				
				if (gpx != null) {
					for (int i=0; i < gpx.getTracks().size(); i++) {
						
						ArrayList<net.divbyzero.gpx.TrackSegment> tsa = gpx.getTracks().get(i).getSegments();
						
						for (int j=0; j < tsa.size(); j++) {
							
							ArrayList<net.divbyzero.gpx.Waypoint> waa = tsa.get(j).getWaypoints();
							
							for (int k=1; k < waa.size(); k++) {
								
								net.divbyzero.gpx.Coordinate c1 = waa.get(k-1).getCoordinate();
								net.divbyzero.gpx.Coordinate c2 = waa.get(k).getCoordinate();
								
								double y1 = c1.getLatitude();
								double x1 = c1.getLongitude();
								double y2 = c2.getLatitude();
								double x2 = c2.getLongitude();
								
								Coordinate[] c = new Coordinate[2];
								c[0] = new Coordinate(x1, y1);
								c[1] = new Coordinate(x2, y2);
								LineString l = geometryFactory.createLineString( c );
								
								if (DEBUG == true)
									System.out.println("Creating line from waypoints with lat/lon: " + x1 + "/" + y1 + " to " + x2 + "/" + y2);

								featureBuilder.add(l);

								SimpleFeature feature = featureBuilder.buildFeature(null);
								collection.add(feature);
							}
						}
					}
				}

			} else {
				throw new IOException("Don't know what to do with classes of type " + type + " when loading from text file");
			}

		} catch (SchemaException s) {
			System.out.println("Problem creating the feature type from GPX file");
			s.printStackTrace();	
		} catch (IOException e) {
			System.out.println("IOException with GPX Track");
			e.printStackTrace();
		}
		
		this.featureCollection = collection;
	}
}
