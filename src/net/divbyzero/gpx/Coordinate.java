/*
 * Copyright (c) 2009 Martin Jansen
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.divbyzero.gpx;

/**
 * Coordinate for way points
 * 
 * <p>A coordinate consists of two fields for longitude and latitude.</p>
 * 
 * @author Martin Jansen <martin@divbyzero.net>
 * @since 0.1
 */
public class Coordinate {
	private double longitude = 0.0;
	private double latitude = 0.0;

	/**
	 * Returns the value for the longitude of the coordinate
	 * 
	 * @return the longitude of the coordinate
	 */
	public double getLongitude() {
		return longitude;
	}
	
	/**
	 * Sets the longitude of the coordinate
	 * 
	 * @param longitude the longitude of the coordinate
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	/**
	 * Returns the value for the latitude of the coordinate
	 * 
	 * @return the latitude of the coordinate
	 */
	public double getLatitude() {
		return latitude;
	}
	
	/**
	 * Sets the latitude of the coordinate
	 * 
	 * @param latitude the latitude of the coordinate
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
}
