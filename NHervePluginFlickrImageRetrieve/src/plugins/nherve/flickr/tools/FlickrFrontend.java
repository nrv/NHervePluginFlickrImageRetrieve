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

import icy.image.IcyBufferedImage;
import icy.image.ImageUtil;
import icy.network.NetworkUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import plugins.nherve.flickr.FlickrThumbnailProvider;
import plugins.nherve.toolbox.genericgrid.GridCellCollection;

/**
 * 
 * @author Nicolas HERVE - n.herve@laposte.net
 */
public class FlickrFrontend {
	private final static String API_URL = "http://api.flickr.com/services/rest/";

	private boolean debug;
	private Random rand;
	private String applicationKey;
	private String endpoint;
	private FlickrThumbnailProvider provider;

	public FlickrFrontend(String key) {
		super();
		setDebug(false);
		rand = new Random(System.currentTimeMillis());

		applicationKey = key;
		endpoint = API_URL + "?api_key=" + applicationKey;
	}

	public void checkConnection() throws FlickrException {
		send("flickr.test.echo", null);
	}

	private GridCellCollection<FlickrImage> getRandomFromXml(String fullXml, int max, FlickrProgressListener l) throws FlickrException {
		l.notifyNewProgressionStep("Parsing images response");
		List<String> imagesXml = FlickrXmlParser.splitImagesXml(fullXml);
		if (imagesXml.size() == 0) {
			throw new FlickrException("No image found");
		}

		GridCellCollection<FlickrImage> result = new GridCellCollection<FlickrImage>(provider);

		do {
			int choosen = rand.nextInt(imagesXml.size());
			String xmlChoosen = imagesXml.get(choosen);
			FlickrImage img = FlickrXmlParser.parseImage(xmlChoosen);
			imagesXml.remove(choosen);
			result.add(img);
		} while (!imagesXml.isEmpty() && (result.size() < max));

		return result;
	}

	void populateAvailableSizes(FlickrImage img, FlickrProgressListener l) throws FlickrException {
		if (!img.isSizesDone()) {
			if (l != null) {
				l.notifyNewProgressionStep("Getting available sizes");
			}
			String fullXml = send("flickr.photos.getSizes&photo_id=" + img.getId(), l);
			List<String> sizesXml = FlickrXmlParser.splitSizesXml(fullXml);
			for (String sz : sizesXml) {
				img.addAvailableSize(FlickrXmlParser.parseSize(sz));
			}
			img.setSizesDone(true);
		}
	}

	public GridCellCollection<FlickrImage> getRandomInterestingImage(int max, FlickrProgressListener l) throws FlickrException {
		String fullXml = send("flickr.interestingness.getList", l);
		return getRandomFromXml(fullXml, max, l);
	}

	public GridCellCollection<FlickrImage> getRandomRecentImage(int max, FlickrProgressListener l) throws FlickrException {
		String fullXml = send("flickr.photos.getRecent", l);
		return getRandomFromXml(fullXml, max, l);
	}

	public GridCellCollection<FlickrImage> getRandomSearchByTagImage(String tags, int max, FlickrProgressListener l) throws FlickrException {
		return getRandomFromXml(searchByTags(tags, l), max, l);
	}

	public FlickrImage getRandomInterestingImage(FlickrProgressListener l) throws FlickrException {
		return getRandomInterestingImage(1, l).get(0);
	}

	public FlickrImage getRandomRecentImage(FlickrProgressListener l) throws FlickrException {
		return getRandomRecentImage(1, l).get(0);
	}

	public FlickrImage getRandomSearchByTagImage(String tags, FlickrProgressListener l) throws FlickrException {
		return getRandomSearchByTagImage(tags, 1, l).get(0);
	}

	public boolean isDebug() {
		return debug;
	}

	private IcyBufferedImage loadImage(FlickrImage fi, String size, FlickrProgressListener l) throws FlickrException {
		URL url = fi.getImageURL(size);
		log("Loading " + fi.getId() + " - " + url);
		return loadImage(url, l);
	}

	private IcyBufferedImage loadImage(URL url, FlickrProgressListener l) throws FlickrException {
		try {
			if (l != null) {
				l.notifyNewProgressionStep("Downloading image");
			}

			byte[] rawData = NetworkUtil.download(url, l, true);
			ByteArrayInputStream is = new ByteArrayInputStream(rawData);

			IcyBufferedImage img = IcyBufferedImage.createFrom(ImageUtil.loadImage(is));
			is.close();

			return img;
		} catch (Throwable e) {
			throw new FlickrException(e);
		}
	}

	public IcyBufferedImage loadImageBiggestAvailableSize(FlickrImage fi, FlickrProgressListener l) throws FlickrException {
		populateAvailableSizes(fi, l);
		return loadImage(fi, fi.getBiggestAvailableSize(), l);
	}

	public IcyBufferedImage loadImageThumbnail(FlickrImage fi, FlickrProgressListener l) throws FlickrException {
		populateAvailableSizes(fi, l);
		return loadImage(fi, "Thumbnail", l);
	}

	private String searchByTags(String tags, FlickrProgressListener l) throws FlickrException {
		if (tags == null) {
			throw new FlickrException("Invalid tags");
		}

		StringTokenizer stk = new StringTokenizer(tags, " ");

		if (stk.countTokens() == 0) {
			throw new FlickrException("Invalid tags");
		}

		String newTags = "";
		while (stk.hasMoreTokens()) {
			if (newTags.length() > 0) {
				newTags += ",";
			}
			newTags += stk.nextToken();
		}

		return send("flickr.photos.search&tag_mode=all&sort=interestingness-desc&tags=" + newTags, l);
	}

	private String send(String method, FlickrProgressListener l) throws FlickrException {
		if (l != null) {
			l.notifyNewProgressionStep("Sending a query");
		}
		try {
			URL url = new URL(endpoint + "&method=" + method);
			log("Sending " + url.toString());
			URLConnection uc = url.openConnection();
			uc.setDefaultUseCaches(false);
			uc.setUseCaches(false);
			uc.setRequestProperty("Cache-Control", "no-cache");
			uc.setRequestProperty("Pragma", "no-cache");

			final BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));

			if (l != null) {
				l.notifyNewProgressionStep("Getting a response");
			}

			String response = "";
			String temp;
			while ((temp = in.readLine()) != null)
				response += temp + "\n";

			in.close();

			log("Receiving " + response);

			if (!response.contains("<rsp stat=\"ok\">")) {
				throw new FlickrException("Call failed : " + response);
			}

			in.close();

			return response;

		} catch (MalformedURLException e) {
			throw new FlickrException(e);
		} catch (IOException e) {
			throw new FlickrException(e);
		}
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	private void log(String message) {
		if (isDebug()) {
			System.out.println("[Flickr] " + message);
		}
	}

	public void setProvider(FlickrThumbnailProvider provider) {
		this.provider = provider;
	}

}
