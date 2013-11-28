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

import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Collection;

import name.herve.flickrlib.FlickrException;
import name.herve.flickrlib.FlickrImage;
import name.herve.flickrlib.FlickrImageSize;
import name.herve.flickrlib.FlickrLicense;
import plugins.nherve.flickr.FlickrImageRetrieve;
import plugins.nherve.toolbox.genericgrid.GridCell;

/**
 * 
 * @author Nicolas HERVE - n.herve@laposte.net
 */
public class PluginFlickrImage extends GridCell {
	private static final long serialVersionUID = 2168885531941222351L;
	private FlickrImageRetrieve plugin;
	private FlickrImage internal;

	public PluginFlickrImage() {
		super();

		setPlugin(null);
		internal = new FlickrImage();
	}

	public PluginFlickrImage(FlickrImage fi) {
		super();

		setPlugin(null);
		internal = fi;
	}

	public String getBiggestAvailableSize() {
		return internal.getBiggestAvailableSize();
	}

	public String getClosestSize(int prefered) {
		return internal.getClosestSize(prefered);
	}

	public String getFarm() {
		return internal.getFarm();
	}

	public String getId() {
		return internal.getId();
	}

	public URL getImageWebPageURL() throws FlickrException {
		return internal.getImageWebPageURL();
	}

	FlickrImage getInternal() {
		return internal;
	}

	public FlickrLicense getLicense() {
		return internal.getLicense();
	}

	public String getLicenseId() {
		return internal.getLicenseId();
	}

	public String getOwner() {
		return internal.getOwner();
	}

	public String getSecret() {
		return internal.getSecret();
	}

	public String getServer() {
		return internal.getServer();
	}

	public Collection<FlickrImageSize> getSizes() {
		return internal.getSizes();
	}

	public String getTags() {
		return internal.getTags();
	}

	public String getTitle() {
		return internal.getTitle();
	}

	public boolean isSizesDone() {
		return internal.isSizesDone();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			e.consume();
			if (plugin != null) {
				if (!plugin.isRunningHeadless() && plugin.isGrabEnabled()) {
					plugin.display(this);
				}
			}
		}
	}

	public void setPlugin(FlickrImageRetrieve plugin) {
		this.plugin = plugin;
	}

	public void setTags(String tags) {
		internal.setTags(tags);
	}

	void setTitle(String title) {
		internal.setTags(title);
		setName(title);
	}
}
