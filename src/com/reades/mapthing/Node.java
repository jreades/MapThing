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

/**
 * <p>A simple node class that holds references to a
 * position and an identifier. You would <i>usually</i>
 * want to extend this class to do something more
 * clever, but this is also used internally to hold
 * the transformed coordinates within a Polygons/Lines/Points object.</p>
 *  
 * <p>The idea here is that we take our complex
 * geographic coordinates from the data source and turn
 * them into simple objects that are easy to draw in 
 * Processing as this will be faster and it will also
 * be simpler to move them around and do things like 
 * zoom and pan.</p>
 * @author jereades
 * @see com.reades.mapthing.Link
 */
public class Node implements Serializable {

	private static final long serialVersionUID = -7750398192543108321L;
	
	public boolean DEBUG = false;
	
	String name; 
	int    id;
	double val;
	double X;
	double Y;
	double Z;
	
	/**
	 * Create a Node object
	 * @param id a (preferably) unique identifier for the node
	 * @param nx the x-position of the node (usually in pixels)
	 * @param ny the y-position of the node (usually in pixels)
	 */
	public Node(int id, double nx, double ny) {
		this.id    = id;
		this.X     = nx;
		this.Y     = ny;
	}
	
	/**
	 * Create a Node object
	 * @param id a (preferably) unique identifier for the node
	 * @param nx the x-position of the node (usually in pixels)
	 * @param ny the y-position of the node (usually in pixels)
	 * @param nm the name of the node (useful for having labels)
	 */
	public Node(int id, double nx, double ny, String nm) {
		this.id    = id;
		this.X     = nx;
		this.Y     = ny;
		this.name  = nm;
	}
	
	/**
	 * Create a Node object
	 * @param id a (preferably) unique identifier for the node
	 * @param nx the x-position of the node (usually in pixels)
	 * @param ny the y-position of the node (usually in pixels)
	 * @param nz the z-position of the node (usually in pixels)
	 * @param nm the name of the node (useful for having labels)
	 */
	public Node(int id, double nx, double ny, double nz, String nm) {
		this.id    = id;
		this.X     = nx;
		this.Y     = ny;
		this.Z     = nz;
		this.name  = nm;
	}
	
	/**
	 * Create a Node object
	 * @param id  a (preferably) unique identifier for the node
	 * @param nx  the x-position of the node (usually in pixels)
	 * @param ny  the y-position of the node (usually in pixels)
	 * @param nz  the z-position of the node (usually in pixels)
	 * @param val the value of the node (usually some kind of double)
	 */
	public Node(int id, double nx, double ny, double nz, double val) {
		this.id    = id;
		this.X     = nx;
		this.Y     = ny;
		this.Z     = nz;
		this.val   = val;
	}
	
	/**
	 * Create a Node object
	 * @param id  a (preferably) unique identifier for the node
	 * @param nx  the x-position of the node (usually in pixels)
	 * @param ny  the y-position of the node (usually in pixels)
	 * @param nz  the z-position of the node (usually in pixels)
	 * @param val the value of the node (usually some kind of double)
	 * @param nm  the name of the node (useful for having labels)
	 */
	public Node(int id, double nx, double ny, double nz, double val, String nm) {
		this.id    = id;
		this.X     = nx;
		this.Y     = ny;
		this.Z     = nz;
		this.val   = val;
		this.name  = nm;
	}
	
	/**
	 * Get the (preferably) unique identifier
	 * @return A (preferably) unique identifying integer
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Get the (preferably) unique name
	 * @return A (preferably) unique identifying name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * The x-position of the node
	 * @return A float representing the node's x-position
	 */
	public float getX() {
		return (float) this.X;
	}
	
	/**
	 * The y-position of the node
	 * @return A float representing the node's y-position
	 */
	public float getY() {
		return (float) this.Y;
	}
	
	/**
	 * The z-position of the node -- you can 
	 * overload this as the value of the node
	 * if you like since it doesn't have to be
	 * a geographical z-axis. 
	 * @return A float representing the node's z-position or value
	 */
	public float getZ() {
		return (float) this.Z;
	}
	
	/**
	 * A better way to get a value that is
	 * distinct from some z-value such as an
	 * altitude.
	 * @return A double representing the node's 'value' on an arbitrary scale
	 */
	public double getValue() {
		return this.val;
	}
}