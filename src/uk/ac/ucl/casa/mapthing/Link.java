package uk.ac.ucl.casa.mapthing;

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
 * @see uk.ac.ucl.casa.mapthing.Node
 */
public class Link implements Serializable {
	
	private static final long serialVersionUID = -9062368678289290111L;
	
	public static boolean DEBUG = false;

	int    id;
	Node   node1;
	Node   node2;
	double value;
	String name;
	
	/**
	 * Constructor takes two SimpleNode (or extensions
	 * of SimpleNode) objects and stores them for later
	 * retrieval.
	 * @param id
	 * @param n1
	 * @param n2
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
	 * @param id
	 * @param n1
	 * @param n2
	 */
	public Link(int id, Node n1, Node n2, String nm) {
		this.id        = id;
		this.node1     = n1;
		this.node2     = n2;
		this.name      = nm;
	}
	

	/**
	 * Create a Link object
	 * @param id: a (preferably) unique identifier for the node
	 * @param nx: the x-position of the node (usually in pixels)
	 * @param ny: the y-position of the node (usually in pixels)
	 * @param nz: the z-position of the node (usually in pixels)
	 * @param nm: the name of the node (useful for having labels)
	 */
	public Link(int id, Node n1, Node n2, double val) {
		this.id    = id;
		this.node1 = n1;
		this.node2 = n2;
		this.value = val;
	}

	/**
	 * Create a Link object
	 * @param id: a (preferably) unique identifier for the node
	 * @param nx: the x-position of the node (usually in pixels)
	 * @param ny: the y-position of the node (usually in pixels)
	 * @param nz: the z-position of the node (usually in pixels)
	 * @param val: the value of the node (usually some kind of double)
	 * @param nm: the name of the node (useful for having labels)
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
	 * @return An object of class SimpleNode
	 * @see uk.ac.ucl.casa.geography.nodes.SimpleNode
	 */
	public Node getNode1() {
		return this.node1;
	}
	
	/**
	 * Get the second node
	 * @return An object of class SimpleNode
	 * @see uk.ac.ucl.casa.geography.nodes.SimpleNode
	 */
	public Node getNode2() {
		return this.node2;
	}
}