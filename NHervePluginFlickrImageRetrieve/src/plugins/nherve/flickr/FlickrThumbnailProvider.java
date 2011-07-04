package plugins.nherve.flickr;

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

}
