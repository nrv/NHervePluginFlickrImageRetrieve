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

import java.util.List;

/**
 * 
 * @author Nicolas HERVE - n.herve@laposte.net
 */
public class FlickrSearchResponseData {
	private int page;
	private int pages;
	private int perpage;
	private List<FlickrImage> pictures;
	private int total;
	
	public boolean isLastPage() {
		return page == pages;
	}

	public int getPage() {
		return page;
	}

	public int getPages() {
		return pages;
	}

	public int getPerpage() {
		return perpage;
	}

	public List<FlickrImage> getPictures() {
		return pictures;
	}

	public int getTotal() {
		return total;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public void setPages(int pages) {
		this.pages = pages;
	}

	public void setPerpage(int perpage) {
		this.perpage = perpage;
	}

	public void setPictures(List<FlickrImage> pictures) {
		this.pictures = pictures;
	}

	public void setTotal(int total) {
		this.total = total;
	}

}
