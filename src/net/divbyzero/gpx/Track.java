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
 * A GPS track
 * 
 * <p>A track consists of an arbitrary number of segments. Tracks
 * have a length, can be queried for elevations and for the points in time 
 * when they were entered and left during a recording.</p>
 * 
 * @author Martin Jansen <martin@divbyzero.net>
 * @since 0.1
 */
public class Track implements Measurable {

	private ArrayList<TrackSegment> segments = new ArrayList<TrackSegment>();
	
	/**
	 * Adds a new segment to the track.
	 * 
	 * <p>This method is used for extending a track by another segment.
	 * The segment is added at the tracks's end.</p>
	 * 
	 * @param segment the segment to be added to the track
	 */
	public void addSegment(TrackSegment segment) {
		segments.add(segment);
	}
	
	/**
	 * Returns the segments of which the track consists.
	 * 
	 * <p>This method returns a list of the segments that make up the
	 * track. They are returned in the order that they were added,
	 * i.e. the segment at which the track start is at position 0
	 * while the segment where the track ends is at the last position
	 * of the list.</p>
	 * 
	 * @return a list of the track's segments
	 */
	public ArrayList<TrackSegment> getSegments() {
		return segments;
	}

	/**
	 * Calculates the length of the track
	 * 
	 * @return the tracks's length in meters
	 */
	public double length()
	{
		double length = 0.0;

		for (int i = 0; i < segments.size(); i++) {			
			length += segments.get(i).length();
		}
		
		return length;
	}

	/**
	 * Calculates the total ascent in the track.
	 * 
	 * <p>The total ascent of the track is calculated by comparing each
	 * of the track's segments  with their predecessors. If the
	 * elevation of a segments is higher than the elevation of the
	 * predecessor, the total ascent is increased accordingly.</p>
	 * 
	 * @see Track#cumulativeDescent()
	 * @return the tracks's total ascent in meters
	 */
	public double cumulativeAscent() {
		double ascent = 0.0;
		
		for (int i = 0; i < segments.size(); i++) {
			ascent += segments.get(i).cumulativeAscent();
		}
		
		return ascent;
	}

	/**
	 * Calculates the total descent in the track.
	 * 
	 * <p>The total descent of the track is calculated by comparing each
	 * of the track's segments with their predecessors. If the
	 * elevation of a segment is lower than the elevation of the
	 * predecessor, the total descent is increased accordingly.</p>
	 * 
	 * @see Track#cumulativeAscent()
	 * @return the tracks's total descent in meters
	 */
	public double cumulativeDescent() {
		double descent = 0.0;
		
		for (int i = 0; i < segments.size(); i++) {
			descent += segments.get(i).cumulativeDescent();
		}
		
		return descent;
	}

	/**
	 * Returns the point in time when the track was entered
	 * 
	 * <p>Usually this is the time stamp of the segment that was added
	 * first to the track.</p>
	 * 
	 * @see Track#endTime()
	 * @return the point in time when the track was entered 
	 */
	public Date startingTime() {
		Date result = null;
		
		for (int i = 0; i < segments.size(); i++) {
			TrackSegment segment = segments.get(i);
			Date startingTime = segment.startingTime();
			
			if (startingTime != null) {
				if (result == null || startingTime.before(result)) {
					result = startingTime;
				}
			}
		}
		
		return result;
	}

	/**
	 * Returns the point in time when the track was left
	 * 
	 * <p>Usually this is the time stamp of the segment that was added
	 * last to the track.</p>
	 * 
	 * @see Track#startingTime
	 * @return the point in time when the track was left
	 */
	public Date endTime() {
		Date result = null;
		
		for (int i = 0; i < segments.size(); i++) {
			TrackSegment segment = segments.get(i);
			Date endTime = segment.endTime();
			
			if (endTime != null) {
				if (result == null || endTime.after(result)) {
					result = endTime;
				}
			}
		}
		
		return result;
	}
}
