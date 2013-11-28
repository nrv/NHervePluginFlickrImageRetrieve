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

package plugins.nherve.flickr.tools;

import icy.image.IcyBufferedImage;

import java.util.List;

import name.herve.flickrlib.FlickrException;
import name.herve.flickrlib.FlickrFrontend;
import name.herve.flickrlib.FlickrImage;
import name.herve.flickrlib.FlickrProgressListener;
import plugins.nherve.flickr.FlickrThumbnailProvider;
import plugins.nherve.toolbox.genericgrid.GridCellCollection;

/**
 * 
 * @author Nicolas HERVE - n.herve@laposte.net
 */
public class PluginFlickrFrontend extends FlickrFrontend {
	private FlickrThumbnailProvider provider;

	public PluginFlickrFrontend(String key) {
		super(key);
	}

	private PluginFlickrImage transform(FlickrImage im) {
		return new PluginFlickrImage(im);
	}
	
	private GridCellCollection<PluginFlickrImage> transform(List<FlickrImage> l) {
		GridCellCollection<PluginFlickrImage> g = new GridCellCollection<PluginFlickrImage>(provider);
		
		for (FlickrImage fi : l) {
			g.add(transform(fi));
		}
		
		return g;
	}
	
	public GridCellCollection<PluginFlickrImage> getIcySearchByExpertQuery(String query, FlickrProgressListener l) throws FlickrException {
		return transform(getSearchByExpertQuery(query, l));
	}

	public PluginFlickrImage getRandomInterestingIcyImage(FlickrProgressListener l) throws FlickrException {
		return transform(getRandomInterestingImage(l));
	}
	
	public GridCellCollection<PluginFlickrImage> getRandomInterestingIcyImage(int max, FlickrProgressListener l) throws FlickrException {
		return transform(getRandomInterestingImage(max, l));
	}

	public PluginFlickrImage getRandomRecentIcyImage(FlickrProgressListener l) throws FlickrException {
		return transform(getRandomRecentImage(l));
	}

	public GridCellCollection<PluginFlickrImage> getRandomRecentIcyImage(int max, FlickrProgressListener l) throws FlickrException {
		return transform(getRandomRecentImage(max, l));
	}

	public PluginFlickrImage getRandomSearchByTagIcyImage(String tags, FlickrProgressListener l) throws FlickrException {
		return transform(getRandomSearchByTagImage(tags, l));
	}

	public GridCellCollection<PluginFlickrImage> getRandomSearchByTagIcyImage(String tags, int max, FlickrProgressListener l) throws FlickrException {
		return transform(getRandomSearchByTagImage(tags, max, l));
	}

	public IcyBufferedImage loadIcyImageBiggestAvailableSize(PluginFlickrImage fi, FlickrProgressListener l) throws FlickrException {
		return IcyBufferedImage.createFrom(loadImageBiggestAvailableSize(fi.getInternal(), l));
	}

	public IcyBufferedImage loadIcyImageThumbnail(PluginFlickrImage fi, FlickrProgressListener l) throws FlickrException {
		return IcyBufferedImage.createFrom(loadImageThumbnail(fi.getInternal(), l));
	}

	public void setProvider(FlickrThumbnailProvider provider) {
		this.provider = provider;
	}
}
