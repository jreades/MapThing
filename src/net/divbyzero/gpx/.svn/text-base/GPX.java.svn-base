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

/**
 * GPX track collection
 * 
 * <p>A track collection is used to encapsulate the arbitrary number of
 * tracks that can be contained in a single GPX file.</p>
 * 
 * @author Martin Jansen <martin@divbyzero.net>
 * @since 0.1
 */
public class GPX {

	protected ArrayList<Track> tracks = new ArrayList<Track>();
	
	/**
	 * Adds a track to the collection.
	 * 
	 * <p>The track is added to the end of the collection. If it already
	 * existed in the collection before, it is added again.</p>
	 * 
	 * @param track the track to be added
	 */
	public void addTrack(Track track) {
		tracks.add(track);
	}
	
	/**
	 * Returns the list of track that make up the collection.
	 * 
	 * @return a list of tracks
	 */
	public ArrayList<Track> getTracks() {
		return tracks;
	}
}
