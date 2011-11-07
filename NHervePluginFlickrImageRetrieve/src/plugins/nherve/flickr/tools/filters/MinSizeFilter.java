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

package plugins.nherve.flickr.tools.filters;

import plugins.nherve.flickr.tools.FlickrImage;
import plugins.nherve.flickr.tools.FlickrImageSize;

/**
 * 
 * @author Nicolas HERVE - n.herve@laposte.net
 */
public class MinSizeFilter implements FlickrSearchResponseFilter {
	public MinSizeFilter(int min) {
		super();
		this.min = min;
	}

	private int min;

	@Override
	public boolean match(FlickrImage img) {
		if (img.isSizesDone()) {
			for (FlickrImageSize sz : img.getSizes()) {
				if ((sz.getWidth() >= min) && (sz.getHeight() >= min)) {
					return true;
				}
			}
		}
		return false;
	}

}