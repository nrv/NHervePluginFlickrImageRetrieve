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

/**
 * 
 * @author Nicolas HERVE - n.herve@laposte.net
 */
public class FlickrImageSize {
	private String label;
	private int width;
	private int height;
	private String source;
	private String url;
	
	public FlickrImageSize() {
		super();
	}

	public String getLabel() {
		return label;
	}
	
	void setLabel(String label) {
		this.label = label;
	}
	
	public int getWidth() {
		return width;
	}
	
	void setWidth(int width) {
		this.width = width;
	}
	
	public int getHeight() {
		return height;
	}
	
	void setHeight(int height) {
		this.height = height;
	}
	
	public String getSource() {
		return source;
	}
	
	void setSource(String source) {
		this.source = source;
	}
	
	public String getUrl() {
		return url;
	}
	
	void setUrl(String url) {
		this.url = url;
	}
}
