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

import java.util.ArrayList;
import java.util.Date;

/**
 * Track segment of a GPS track
 * 
 * <p>A segment consists of an arbitrary number of way points. Segments 
 * have a length, can be queried for elevations and for the points in time 
 * when the segment was entered and left during a track recording.</p>
 * 
 * @author Martin Jansen <martin@divbyzero.net>
 * @since 0.1
 */
public class TrackSegment implements Measurable {
	private ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
	
	/**
	 * Adds a new way point to the segment.
	 * 
	 * <p>This method is used for extending a track segment by another
	 * way point. The way point is added at the segment's end.</p>
	 * 
	 * @param waypoint the way point to be added to the segment
	 */
	public void addWaypoint(Waypoint waypoint) {
		waypoints.add(waypoint);
	}

	/**
	 * Returns the way points of which the segment consists.
	 * 
	 * <p>This method returns a list of the way points that make up the
	 * segment. They are returned in the order that they were added,
	 * i.e. the way point at which the segments start is at position 0
	 * while the way point where the segment ends is at the last position
	 * of the list.</p>
	 * 
	 * @return a list of the segment's way points
	 */
	public ArrayList<Waypoint> getWaypoints()
	{
		return waypoints;
	}

	/**
	 * Calculates the length of the track segment
	 * 
	 * @return the segment's length in meters
	 */
	public double length() {
		double length = 0.0;
	
		Waypoint currentWaypoint = null;
		Waypoint previousWaypoint = null;
	
		for (int z = 0; z < waypoints.size(); z++) {
			/* Only attempt to calculate the distance if we are not
			 * on the first way point of the segment.
			 */
			if (z > 0) {
				currentWaypoint = waypoints.get(z);
				previousWaypoint = waypoints.get(z - 1);
				
				length += currentWaypoint.calculateDistanceTo(previousWaypoint);
			}
		}
		
		return length;
	}

	/**
	 * Calculates the total ascent in the segment.
	 * 
	 * <p>The total ascent of the segment is calculated by comparing each
	 * of the segment's way point with their predecessors. If the
	 * elevation of a way point is higher than the elevation of the
	 * predecessor, the total ascent is increased accordingly.</p>
	 * 
	 * @see TrackSegment#cumulativeDescent()
	 * @return the segment's total ascent in meters
	 */
	public double cumulativeAscent()
	{
		double ascent = 0.0;
	
		if (waypoints.size() <= 1) {
			return 0.0;
		}
		
		for (int i = 0; i < waypoints.size(); i++) {
			if (i > 0 && waypoints.get(i - 1).getElevation() < waypoints.get(i).getElevation()) {
				ascent += waypoints.get(i).getElevation() - waypoints.get(i - 1).getElevation();
			}
		}
	
		return ascent;
	}

	/**
	 * Calculates the total descent in the segment.
	 * 
	 * <p>The total descent of the segment is calculated by comparing each
	 * of the segment's way point with their predecessors. If the
	 * elevation of a way point is lower than the elevation of the
	 * predecessor, the total descent is increased accordingly.</p>
	 * 
	 * @return the segment's total descent in meters
	 * 
	 * @see TrackSegment#cumulativeAscent()
	 */
	public double cumulativeDescent()
	{
		double descent = 0.0;
		
		if (waypoints.size() <= 1) {
			return 0.0;
		}

		for (int i = 0; i < waypoints.size(); i++) {
			if (i > 1 && waypoints.get(i).getElevation() < waypoints.get(i - 1).getElevation()) {
				descent += waypoints.get(i - 1).getElevation() - waypoints.get(i).getElevation();
			}
		}

		return descent;
	}
	
	/**
	 * Returns the point in time when the segment was entered
	 * 
	 * <p>Usually this is the time stamp of the way point that was added
	 * first to the segment.</p>
	 * 
	 * @see TrackSegment#endTime
	 * @return the point in time when the segment was entered 
	 */
	public Date startingTime() {
		Date result = null;
		
		for (int i = 0; i < waypoints.size(); i++) {
			Date time = waypoints.get(i).getTime();
			
			if (time != null) {
				if (result == null || time.before(result)) {
					result = time;
				}
			}
		}
		
		return result;
	}

	/**
	 * Returns the point in time when the segment was left
	 * 
	 * <p>Usually this is the time stamp of the way point that was added
	 * last to the segment.</p>
	 *
	 * @see TrackSegment#endTime
	 * @return the point in time when the segment was left
	 */
	public Date endTime() {
		Date result = null;
		
		for (int i = 0; i < waypoints.size(); i++) {
			Date time = waypoints.get(i).getTime();
			
			if (time != null) {
				if (result == null || time.after(result)) {
					result = time;
				}
			}
		}
		
		return result;
	}
}
