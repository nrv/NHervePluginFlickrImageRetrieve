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

package plugins.nherve.flickr;

import java.awt.image.BufferedImage;
import java.io.File;

import icy.image.IcyBufferedImage;
import plugins.nherve.flickr.tools.FlickrException;
import plugins.nherve.flickr.tools.FlickrFrontend;
import plugins.nherve.flickr.tools.FlickrImage;
import plugins.nherve.toolbox.genericgrid.ThumbnailException;
import plugins.nherve.toolbox.genericgrid.DefaultThumbnailProvider;

public class FlickrThumbnailProvider extends DefaultThumbnailProvider<FlickrImage> {
	
	private FlickrFrontend front;

	public FlickrThumbnailProvider(FlickrFrontend front) {
		super();
		this.front = front;
		front.setProvider(this);
	}

	@Override
	public IcyBufferedImage getThumbnail(FlickrImage cell) throws ThumbnailException {
		try {
			return front.loadImageThumbnail(cell, null);
		} catch (FlickrException e) {
			throw new ThumbnailException(e);
		}
	}

	@Override
	public boolean isAbleToProvideThumbnailFor(FlickrImage cell) {
		return true;
	}

	@Override
	public boolean isAbleToProvideThumbnailFor(File f) {
		return true;
	}

	@Override
	public BufferedImage getFullSizeImage(FlickrImage cell) throws ThumbnailException {
		try {
			return front.loadImageBiggestAvailableSize(cell, null);
		} catch (FlickrException e) {
			throw new ThumbnailException(e);
		}
	}

}
