/*
 * Copyright 2011 Nicolas Herv�.
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import plugins.nherve.flickr.FlickrImageRetrieve;
import plugins.nherve.toolbox.genericgrid.GridCell;

/**
 * 
 * @author Nicolas HERVE - n.herve@laposte.net
 */
public class FlickrImage extends GridCell {
	private static final long serialVersionUID = 2168885531941222351L;
	private String farm;
	private String id;
	private FlickrLicense license;
	private String licenseId;
	private String owner;
	private FlickrImageRetrieve plugin;
	private String secret;
	private String server;
	private Map<String, FlickrImageSize> sizes;
	private boolean sizesDone;
	private String tags;
	private String title;

	public FlickrImage() {
		super();

		sizes = new HashMap<String, FlickrImageSize>();
		setSizesDone(false);
		setPlugin(null);
	}

	void addAvailableSize(FlickrImageSize s) {
		sizes.put(s.getLabel(), s);
	}

	public String getBiggestAvailableSize() {
		int maxSurf = 0;
		String maxSize = null;

		for (FlickrImageSize sz : sizes.values()) {
			int surf = sz.getWidth() * sz.getHeight();
			if (surf > maxSurf) {
				maxSurf = surf;
				maxSize = sz.getLabel();
			}
		}

		return maxSize;
	}
	
	public String getClosestSize(int prefered) {
		int prefSurfDif = Integer.MAX_VALUE;
		String prefSize = null;

		for (FlickrImageSize sz : sizes.values()) {
			int surfDif = Math.abs(prefered - sz.getWidth() * sz.getHeight());
			if (surfDif < prefSurfDif) {
				prefSurfDif = surfDif;
				prefSize = sz.getLabel();
			}
		}

		return prefSize;
	}

	public String getFarm() {
		return farm;
	}

	public String getId() {
		return id;
	}

	URL getImageURL(String size) throws FlickrException {
		FlickrImageSize sz = sizes.get(size);
		String url = sz.getSource();

		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new FlickrException(e);
		}
	}

	public URL getImageWebPageURL() throws FlickrException {
		String url = "http://www.flickr.com/photos/" + owner + "/" + id + "";

		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new FlickrException(e);
		}
	}

	public FlickrLicense getLicense() {
		return license;
	}

	public String getLicenseId() {
		return licenseId;
	}

	public String getOwner() {
		return owner;
	}

	public String getSecret() {
		return secret;
	}

	public String getServer() {
		return server;
	}

	public Collection<FlickrImageSize> getSizes() {
		if (isSizesDone()) {
			return sizes.values();
		}

		return null;
	}

	public String getTags() {
		return tags;
	}

	public String getTitle() {
		return title;
	}

	public boolean isSizesDone() {
		return sizesDone;
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

	void setFarm(String farm) {
		this.farm = farm;
	}

	void setId(String id) {
		this.id = id;
	}

	void setLicense(FlickrLicense license) {
		this.license = license;
	}

	void setLicenseId(String licenseId) {
		this.licenseId = licenseId;
	}

	void setOwner(String owner) {
		this.owner = owner;
	}

	public void setPlugin(FlickrImageRetrieve plugin) {
		this.plugin = plugin;
	}

	void setSecret(String secret) {
		this.secret = secret;
	}

	void setServer(String server) {
		this.server = server;
	}

	public void setSizesDone(boolean sizesDone) {
		this.sizesDone = sizesDone;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	void setTitle(String title) {
		this.title = title;
		setName(title);
	}
}
