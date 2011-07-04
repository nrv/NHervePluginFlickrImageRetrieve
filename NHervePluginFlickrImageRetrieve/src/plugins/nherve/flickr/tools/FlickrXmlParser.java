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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Nicolas HERVE - n.herve@laposte.net
 */
public class FlickrXmlParser {
	public static String getXmlValue(String xml, String parameter) throws FlickrException {
		parameter += "=\"";
		int idx = xml.indexOf(parameter);
		if (idx < 0) {
			throw new FlickrException("Unable to find parameter " + parameter);
		}
		idx += parameter.length();
		return xml.substring(idx, xml.indexOf("\"", idx));
	}
	
	public static FlickrImage parseImage(String xml) throws FlickrException {
		FlickrImage image = new FlickrImage();
		
		image.setFarm(getXmlValue(xml, "farm"));
		image.setServer(getXmlValue(xml, "server"));
		image.setId(getXmlValue(xml, "id"));
		image.setSecret(getXmlValue(xml, "secret"));
		image.setOwner(getXmlValue(xml, "owner"));
		image.setTitle(getXmlValue(xml, "title"));
		
		return image;
	}
	
	public static FlickrImageSize parseSize(String xml) throws FlickrException {
		FlickrImageSize size = new FlickrImageSize();
		
		size.setLabel(getXmlValue(xml, "label"));
		size.setSource(getXmlValue(xml, "source"));
		size.setUrl(getXmlValue(xml, "url"));
		try {
			size.setWidth(Integer.parseInt(getXmlValue(xml, "width")));
			size.setHeight(Integer.parseInt(getXmlValue(xml, "height")));
		} catch (NumberFormatException e) {
			throw new FlickrException(e);
		}
		
		return size;
	}
	
	public static List<String> splitImagesXml(String xml) throws FlickrException {
		return splitXml(xml, "<photo id=\"", "/>");
	}
	
	public static List<String> splitSizesXml(String xml) throws FlickrException {
		return splitXml(xml, "<size label=\"", "/>");
	}
	
	public static List<String> splitXml(String xml, String start, String end) throws FlickrException {
		if (xml == null) {
			throw new FlickrException("No XML to parse in splitXml("+start+")");
		}
		List<String> data = new ArrayList<String>();

		int s = 0;
		int e = 0;

		do {
			s = xml.indexOf(start, e);
			if (s > 0) {
				e = xml.indexOf(end, s);
				if (e > 0) {
					data.add(xml.substring(s, e + 2));
				}
			}
		} while ((s > 0) && (e > 0));

		return data;
	}
}
