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

import plugins.nherve.flickr.tools.filters.FlickrSearchResponseFilter;
import plugins.nherve.flickr.tools.filters.NoFilter;

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

		private FlickrImage nextImage;

		public FlickrSearchResponseIterator() throws FlickrException {
			super();

			currentQuery = originalQuery;
			currentData = null;
			currentIterator = null;
			nextImage = null;
			count = 0;
		}

		public void init() throws FlickrException {
			nextPage();
			doNext();
		}

		@Override
		public boolean hasNext() {
			return (count < currentQuery.getMax()) && (nextImage != null);
		}

		private void doNext() {
			nextImage = null;
			if ((currentData != null) && (currentIterator != null)) {
				if (!currentIterator.hasNext() && (!currentData.isLastPage())) {
					try {
						nextPage();
					} catch (FlickrException e) {
						return;
					}
				}

				if (currentIterator.hasNext()) {
					nextImage = currentIterator.next();
				}
			}
		}

		@Override
		public FlickrImage next() {
			FlickrImage result = null;

			do {
				result = nextImage;
				doNext();
			} while (!filter.match(result) && hasNext());

			count++;
			return result;
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

		public int getTotal() {
			return currentData.getTotal();
		}
	}

	private FlickrFrontend flickr;
	private FlickrSearchQuery originalQuery;
	private FlickrSearchResponseFilter filter;

	public FlickrSearchResponse(FlickrFrontend flickr, FlickrSearchQuery query, FlickrSearchResponseFilter filter) {
		super();
		this.flickr = flickr;
		this.originalQuery = query;
		this.filter = filter;
	}

	public FlickrSearchResponse(FlickrFrontend flickr, FlickrSearchQuery query) {
		this(flickr, query, new NoFilter());
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
