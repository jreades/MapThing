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
 * A simple link class that holds references to 
 * two Node objects. You would <i>usually</i>
 * want to extend this class to do something more
 * clever around weighting and such, but the idea
 * here is that this class is easier to work with
 * in Processing and allows us to implement zoom
 * and pan without too much effort.
 * @author jereades
 * @see com.reades.mapthing.Node
 */
public class Link implements Serializable {
	
	private static final long serialVersionUID = -9062368678289290111L;
	
	public boolean DEBUG = false;

	int    id;
	Node   node1;
	Node   node2;
	double value;
	String name;
	
	/**
	 * Constructor takes two SimpleNode (or extensions
	 * of SimpleNode) objects and stores them for later
	 * retrieval.
	 * @param id a unique id for the link
	 * @param n1 the origin node of the link
	 * @param n2 the destination node of the link
	 */
	public Link(int id, Node n1, Node n2) {
		this.id        = id;
		this.node1     = n1;
		this.node2     = n2;
	}
	
	/**
	 * Constructor takes two SimpleNode (or extensions
	 * of SimpleNode) objects and stores them for later
	 * retrieval.
	 * @param id a unique id for the link
	 * @param n1 the origin node of the link
	 * @param n2 the destination node of the link
	 * @param nm a name for the link (if using labels)
	 */
	public Link(int id, Node n1, Node n2, String nm) {
		this.id        = id;
		this.node1     = n1;
		this.node2     = n2;
		this.name      = nm;
	}
	

	/**
	 * Create a Link object
	 * @param id  a unique identifier for the link
	 * @param n1  the origin node for the link
	 * @param n2  the destination node for the link
	 * @param val a link weight/value
	 */
	public Link(int id, Node n1, Node n2, double val) {
		this.id    = id;
		this.node1 = n1;
		this.node2 = n2;
		this.value = val;
	}

	/**
	 * Create a Link object
	 * @param id  a unique identifier for the link
	 * @param n1  the origin node for the link
	 * @param n2  the destination node for the link
	 * @param val a link weight/value
	 * @param nm  a name for the link (if labelling)
	 */
	public Link(int id, Node n1, Node n2, double val, String nm) {
		this.id    = id;
		this.node1 = n1;
		this.node2 = n2;
		this.value = val;
		this.name  = nm;
	}
	
	/**
	 * Get the id of the link
	 * @return An integer identifier
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Get the name of the link
	 * @return An integer identifier
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Get the first node
	 * @return An object of class Node
	 * @see com.reades.mapthing.Node
	 */
	public Node getNode1() {
		return this.node1;
	}
	
	/**
	 * Get the second node
	 * @return An object of class Node
	 * @see com.reades.mapthing.Node
	 */
	public Node getNode2() {
		return this.node2;
	}
}