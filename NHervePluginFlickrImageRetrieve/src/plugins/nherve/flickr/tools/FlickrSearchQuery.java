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
public class FlickrSearchQuery {
	private String query;
	private int page;
	private int perpage;
	private int max;

	public FlickrSearchQuery(String query, int max) {
		super();
		
		this.query = query;
		this.max = max;
		this.page = 1;
		this.perpage = 100;
	}
	
	public FlickrSearchQuery nextPageQuery() {
		FlickrSearchQuery next = new FlickrSearchQuery(getInitialQuery(), getMax());
		
		next.setPerpage(getPerpage());
		next.setPage(getPage() + 1);
		
		return next;
	}

	public String getInitialQuery() {
		return query;
	}
	
	public String getEffectiveQuery() {
		return query + "&extras=license&per_page=" + perpage + "&page=" + page;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPerpage() {
		return perpage;
	}

	public void setPerpage(int perpage) {
		this.perpage = perpage;
	}

	public int getMax() {
		return max;
	}
}
