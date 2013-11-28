/*
 * Copyright 2011-2013 Nicolas Hervé.
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

import icy.image.IcyBufferedImage;

import java.awt.image.BufferedImage;
import java.io.File;

import name.herve.flickrlib.FlickrException;
import plugins.nherve.flickr.tools.PluginFlickrFrontend;
import plugins.nherve.flickr.tools.PluginFlickrImage;
import plugins.nherve.toolbox.genericgrid.DefaultThumbnailProvider;
import plugins.nherve.toolbox.genericgrid.ThumbnailException;

public class FlickrThumbnailProvider extends DefaultThumbnailProvider<PluginFlickrImage> {
	
	private PluginFlickrFrontend front;

	public FlickrThumbnailProvider(PluginFlickrFrontend front) {
		super();
		this.front = front;
		front.setProvider(this);
	}

	@Override
	public IcyBufferedImage getThumbnail(PluginFlickrImage cell) throws ThumbnailException {
		try {
			return front.loadIcyImageThumbnail(cell, null);
		} catch (FlickrException e) {
			throw new ThumbnailException(e);
		}
	}

	@Override
	public boolean isAbleToProvideThumbnailFor(PluginFlickrImage cell) {
		return true;
	}

	@Override
	public boolean isAbleToProvideThumbnailFor(File f) {
		return true;
	}

	@Override
	public BufferedImage getFullSizeImage(PluginFlickrImage cell) throws ThumbnailException {
		try {
			return front.loadIcyImageBiggestAvailableSize(cell, null);
		} catch (FlickrException e) {
			throw new ThumbnailException(e);
		}
	}

}
