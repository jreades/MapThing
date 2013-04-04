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

import java.io.Serializable;
import processing.core.PApplet;

// Eventually we'll want to support different
// types of coordinate systems, but for now
// let's make it easy and ignore them entirely.
//import org.gicentre.utils.spatial.*;

/**
 * An implementation of a bounding box for Processing 
 * sketches. We use this to specify the default viewable
 * area of the sketch in terms of its geographical coordinates
 * (North, East, South, and West) so that we can easily 
 * remap geographical coordinates on to the plain of the 
 * sketch itself.
 * 
 * We also need to handle projections -- right now we'll do
 * it a fairly simplistic way just to get this thing out 
 * the door, but in time it might be nice to get a little
 * smarter about enabling people to do things in more
 * sophisticated ways.
 */
public class BoundingBox implements Serializable {
	
	private static final long serialVersionUID = -6187345095375316773L;
	
	public static final int osgb = 27700;
	public static final int wgs  = 4326;
	
	public boolean DEBUG = false;
	
	private int    projection;
	private float  east;
	private float  north;
	private float  west;
	private float  south;
	
	private BoundingBox context;
	private float width;
	private float height;
	
	private float screenX;
	private float screenY;
	private float screenWidth;
	private float screenHeight;
	
	/**
	 * The default constructor creates a bounding box
	 * that assumes standard lat/long coordinates. So
	 * using this constructor with OSGB coordinates would
	 * give... interesting... results. If you want to use
	 * anything else then you will have to use the form of
	 * the constructor that takes a projection value. To
	 * make things easy for those of you in the UK you can 
	 * just use the public osgb integer in this class.
	 * @param n the northern edge of the intended map
	 * @param e the eastern edge of the intended map
	 * @param s the southern edge of the intended map
	 * @param w the western edge of the intended map
	 */
	public BoundingBox(float n, float e, float s, float w) {
		this.projection = wgs;
		this.east  = e;
		this.north = n;
		this.west  = w;
		this.south = s;
	}
	
	/**
	 * Right now there's not much 'smarts' behind the use 
	 * of the projection, but in future releases I plan to
	 * make this much more relevant by allowing you to use
	 * multiple projections and have the code be clever
	 * enough to reproject everything on the fly so that 
	 * it 'just works' without your having to do a lot of
	 * faffing about on your own. For now, however, you 
	 * *must* use the same projection for everything and we
	 * assume a system that maps pretty cleanly on to x and
	 * y coordinates.
	 * 
	 * So consider this method a fairly useless extension
	 * *for the time being* since we only store and don't 
	 * really do much with the projection string.
	 * But if you want to specify a particular projection then
	 * you will need to tell us what one you're using in 
	 * the standard EPSG/OGC WKT form (go to http://spatialreference.org/
	 * and then pick 'OGC WKT' as the format and look for the *last*
	 * 'AUTHORITY' declaration in the PROJCS command).
	 * The default (if you don't specify a reference) is
	 * WGS84, but because I do a lot of work in the UK I've 
	 * made it easy to use OSGB coordinates too. For everything
	 * else you're on your own right now.
	 * @param p the projection integer taken from OGC WKT authority declaration
	 * @param n the northern edge of the intended map
	 * @param e the eastern edge of the intended map
	 * @param s the southern edge of the intended map
	 * @param w the western edge of the intended map
	 */
	public BoundingBox(int p, float n, float e, float s, float w) {
		this.projection = p;
		this.east  = e;
		this.north = n;
		this.west  = w;
		this.south = s;
	}
	
	public void setContext(BoundingBox c, int w, int h) {
		this.context = c;
		this.width   = w;
		this.height  = h;
		
		if (this.west == this.context.getWest()) {
			this.screenX = 0f;
		} else {
			this.screenX = PApplet.map(this.west, this.context.getWest(), this.context.getEast(), 0f, this.width);
		}
		if (this.north == this.context.getNorth()) {
			this.screenY = 0f;
		} else {
			this.screenY = PApplet.map(this.north, this.context.getNorth(), this.context.getSouth(), 0f, this.height);
		}
		
		// Need to calculate the screenX1 and screenY1 using map,
		// then subtract screenX and screenY from those values
		this.screenHeight = PApplet.map(this.south, this.context.getNorth(), this.context.getSouth(), 0f, this.height) - this.screenY;
		this.screenWidth  = PApplet.map(this.east, this.context.getWest(), this.context.getEast(), 0f, this.width) - this.screenX;
		
		//System.out.println("Mapped " + this.name + " with extent " + this.north + ", " + this.east + ", " + this.south + ", " + this.west + " on to " + this.mapToScreenX() + ", " + this.mapToScreenY() + ", " + this.mapToScreenHeight() + ", " + this.mapToScreenWidth());
	}
	
	public float mapToScreenX() {
		return this.screenX;
	}
	
	public float mapToScreenY() {
		return this.screenY;
	}
	
	public float mapToScreenWidth() {
		return this.screenWidth;
	}
	
	public float mapToScreenHeight() {
		return this.screenHeight;
	}
	
	public float getEast() {
		return this.east;
	}
	
	public float getWest() {
		return this.west;
	}
	
	public float getNorth() {
		return this.north;
	}
	
	public float getSouth() {
		return this.south;
	}
	
	public int getProjection() {
		return this.projection;
	}
	
	public boolean isEqual(BoundingBox b) {
		if (this.south > b.south && this.west == b.west && this.north == b.north && this.east == b.east) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isEqual(BoundingBox b, float margin) {
		
		if(compare(this.south, b.south, margin) == false) {
			return false;
		}
		
		if(compare(this.west, b.west, margin) == false) {
			return false;
		}
		
		if(compare(this.north, b.north, margin) == false) {
			return false;
		}
		
		if(compare(this.east, b.east, margin) == false) {
			return false;
		}
		
		return true;
	}
	
	private boolean compare(float f1, float f2, float margin) {
		boolean rv;
		
		if (f1 > f2) {
			rv = (f1 - f2 > margin ? false : true);
		} else {
			rv = (f2 - f1 > margin ? false : true);
		}
		
		return rv;
	}
	
	/**
	 * Use this if you need to manually re-set
	 * the projection for some reason. You might
	 * find this useful if you have no idea what
	 * projection your files are in and want to 
	 * load a shape file first and then pull the
	 * projection metadata from there.
	 * @param p An OGC WKT format integer (4326 is WGS86 lat/long, 27700 is OSGB 1936, etc.)
	 */
	public void setProjection(int p) {
		this.projection = p;
	}
	
	public int widthFromHeight(int w) {
		float height = this.getNorth() - this.getSouth();
		float width  = this.getWest()  - this.getEast();
		float target = w * (width / height);
		return (int) Math.abs(target);
	}
	
	public int heightFromWidth(int h) {
		float height = this.getNorth() - this.getSouth();
		float width  = this.getWest()  - this.getEast();
		float target = h * (height / width);
		return (int) Math.abs(target);
	}
}
