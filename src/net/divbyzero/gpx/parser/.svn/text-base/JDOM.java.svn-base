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
package net.divbyzero.gpx.parser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.divbyzero.gpx.Coordinate;
import net.divbyzero.gpx.GPX;
import net.divbyzero.gpx.Track;
import net.divbyzero.gpx.TrackSegment;
import net.divbyzero.gpx.Waypoint;

import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 * GPX parser based on the JDOM XML parsing toolkit
 * 
 * @author Martin Jansen <martin@divbyzero.net>
 * @since 0.1
 * @see <a href="http://jdom.org/">JDOM</a>
 */
public class JDOM implements Parser {
	private Namespace ns;
	private SAXBuilder parser = new SAXBuilder();
	
	public GPX parse(File file) throws ParsingException {		
		try {
			Document doc = parser.build(file);
			return parse(doc);
		} catch (IOException e) {
			throw new ParsingException("Unable to open input", e);
		} catch (JDOMException e) {
			throw new ParsingException("Unable to parse input", e);
		}
	}
	
	public GPX parse(URL url) throws ParsingException {
		try {
			Document doc = parser.build(url);
			return parse(doc);
		} catch (IOException e) {
			throw new ParsingException("Unable to open input", e);
		} catch (JDOMException e) {
			throw new ParsingException("Unable to parse input", e);
		}		
	}

	@SuppressWarnings("unchecked")
	private GPX parse(Document doc) {
		GPX gpx = new GPX();
		Element rootNode = doc.getRootElement();
		ns = rootNode.getNamespace();
		
		List<Element> tracks = rootNode.getChildren("trk", ns);
		for (int i = 0; i < tracks.size(); i++) {
			gpx.addTrack(parseTrack(tracks.get(i)));
		}
		
		return gpx;
	}
	
	@SuppressWarnings("unchecked")
	private Track parseTrack(Element trackXML) {
		Track track = new Track();
		
		List<Element> segments = trackXML.getChildren("trkseg", ns);
		for (int i = 0; i < segments.size(); i++) {
			track.addSegment(parseTrackSegment(segments.get(i)));
		}
		
		return track;
	}

	@SuppressWarnings("unchecked")
	private TrackSegment parseTrackSegment(Element segmentXML) {
		TrackSegment segment = new TrackSegment();
		
		List<Element> waypoints = segmentXML.getChildren("trkpt", ns);
		for (int i = 0; i < waypoints.size(); i++) {
			Element pointXML = waypoints.get(i);
			double latitude = 0.0;
			double longitude = 0.0;
			double elevation = 0.0;

			try {
				latitude = pointXML.getAttribute("lat").getDoubleValue();
				longitude = pointXML.getAttribute("lon").getDoubleValue();
			} catch (DataConversionException e) {
				continue;
			}
			
			if (pointXML.getChild("ele", ns) != null) {
				elevation = new Double(pointXML.getChildText("ele", ns));
			}
			
			Waypoint waypoint = new Waypoint();
			
			Coordinate coordinate = new Coordinate();
			coordinate.setLatitude(latitude);
			coordinate.setLongitude(longitude);
			
			waypoint.setCoordinate(coordinate);
			waypoint.setElevation(elevation);

			if (pointXML.getChild("time", ns) != null) {
				try {
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
					Date time = dateFormat.parse(pointXML.getChildText("time", ns));
					waypoint.setTime(time);				
				} catch (ParseException e) {
				}				
			}

			segment.addWaypoint(waypoint);
		}
		
		return segment;
	}
}
