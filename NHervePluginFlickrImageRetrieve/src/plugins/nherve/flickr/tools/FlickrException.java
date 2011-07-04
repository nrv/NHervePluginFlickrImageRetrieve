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
 * along with Color Picker Threshold. If not, see <http://www.gnu.org/licenses/>.
 */

package plugins.nherve.flickr.tools;

/**
 * 
 * @author Nicolas HERVE - n.herve@laposte.net
 */
public class FlickrException extends Exception {
	private static final long serialVersionUID = -1723265347410627757L;

	public FlickrException() {
		super();
	}

	public FlickrException(String message, Throwable cause) {
		super(message, cause);
	}

	public FlickrException(String message) {
		super(message);
	}

	public FlickrException(Throwable cause) {
		super(cause);
	}

}
