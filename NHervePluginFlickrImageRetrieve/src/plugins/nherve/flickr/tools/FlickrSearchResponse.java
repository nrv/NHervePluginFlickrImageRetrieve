/*
 * Copyright 2011 Nicolas Hervé.
 * 
 * This file is part of FlickrImageRetrieve, which is an ICY plugin.
 * 
 * FlickrImageRetrieve is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FlickrImageRetrieve is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with FlickrImageRetrieve. If not, see <http://www.gnu.org/licenses/>.
 */

package plugins.nherve.flickr.tools;

import java.util.Iterator;

/**
 * 
 * @author Nicolas HERVE - n.herve@laposte.net
 */
public class FlickrSearchResponse implements Iterable<FlickrImage> {
	public class FlickrSearchResponseIterator implements Iterator<FlickrImage> {
		private FlickrSearchResponseData currentData;
		private Iterator<FlickrImage> currentIterator;
		private FlickrSearchQuery currentQuery;
		private int count;

		public FlickrSearchResponseIterator() throws FlickrException {
			super();
			
			currentQuery = originalQuery;
			currentData = null;
			currentIterator = null;
			count = 0;
		}

		public void init() throws FlickrException {
			nextPage();
		}

		@Override
		public boolean hasNext() {
			return (count < currentQuery.getMax()) && (currentData != null) && (currentIterator != null) && (currentIterator.hasNext() || (!currentData.isLastPage()));
		}

		@Override
		public FlickrImage next() {
			if (!currentIterator.hasNext()) {
				try {
					nextPage();
				} catch (FlickrException e) {
					return null;
				}
			}
			
			if (currentIterator.hasNext()) {
				count++;
				return currentIterator.next();
			}

			return null;
		}

		@Override
		public void remove() {
			// ignore
		}

		private void nextPage() throws FlickrException {
			if (currentData != null) {
				currentQuery = currentQuery.nextPageQuery();
			}

			currentData = flickr.searchAsData(currentQuery);
			currentIterator = currentData.getPictures().iterator();
		}
	}

	private FlickrFrontend flickr;
	private FlickrSearchQuery originalQuery;

	public FlickrSearchResponse(FlickrFrontend flickr, FlickrSearchQuery query) {
		super();
		this.flickr = flickr;
		this.originalQuery = query;
	}

	@Override
	public Iterator<FlickrImage> iterator() {
		try {
			FlickrSearchResponseIterator it = new FlickrSearchResponseIterator();
			it.init();
			return it;
		} catch (FlickrException e) {
			return null;
		}
	}

}
