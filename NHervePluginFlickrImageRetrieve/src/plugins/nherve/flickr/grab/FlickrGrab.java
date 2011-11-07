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

package plugins.nherve.flickr.grab;

import icy.network.NetworkUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;

import plugins.nherve.flickr.tools.FlickrException;
import plugins.nherve.flickr.tools.FlickrFrontend;
import plugins.nherve.flickr.tools.FlickrImage;
import plugins.nherve.flickr.tools.FlickrProgressListener;
import plugins.nherve.flickr.tools.FlickrSearchQuery;
import plugins.nherve.flickr.tools.FlickrSearchResponse;
import plugins.nherve.toolbox.Algorithm;

/**
 * 
 * @author Nicolas HERVE - n.herve@laposte.net
 */
public class FlickrGrab extends Algorithm implements FlickrProgressListener {
	private final static String APP_KEY = "9a96e50181eb0ab5be0ee15b147acaf8";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FlickrGrab grab = new FlickrGrab();
		grab.init(APP_KEY, 5, false);
		grab.work("/home/nherve/Travail/Data/Flickr", "license=1,2,5,7&tag_mode=all&sort=interestingness-desc&tags=biology", 10);
	}

	private FlickrFrontend flickr;
	private DecimalFormat df = new DecimalFormat("0.00");
	private int gentleSleepSeconds;

	private File getDirectoryForGrabSession(String parent) {
		String d = "FlickrGrabSession-" + System.currentTimeMillis();
		if (parent != null) {
			return new File(parent + File.separator + d);
		} else {
			return new File(d);
		}
	}

	private void init(String key, int gentleSleepSeconds, boolean debug) {
		NetworkUtil.enableProxySetting();
		NetworkUtil.enableSystemProxy();

		flickr = new FlickrFrontend(key);
		flickr.setDebug(debug);
		setLogEnabled(true);

		this.gentleSleepSeconds = gentleSleepSeconds;
	}

	@Override
	public void notifyNewProgressionStep(String step) {
		log(step);
	}

	@Override
	public boolean notifyProgress(double position, double length) {
		log(df.format(position / length) + " %");
		return true;
	}

	private void work(String parent, String query, int nb) {
		Random sleepRandom = new Random(System.currentTimeMillis());

		File dir = getDirectoryForGrabSession(parent);
		dir.mkdir();

		File picdir = new File(dir + File.separator + "pictures");
		picdir.mkdir();

		File metadata = new File(dir, "metadata.txt");
		BufferedWriter w = null;
		try {
			w = new BufferedWriter(new FileWriter(metadata));
			w.write("query = " + query);
			w.newLine();
			w.write("nb = " + nb);
			w.newLine();

			FlickrSearchQuery q = new FlickrSearchQuery(query, nb);
			q.setPerpage(10);

			FlickrSearchResponse pictures = flickr.search(q);

			for (FlickrImage i : pictures) {
				log("* " + i.getId() + " - " + i.getLicense().getName());

				if (gentleSleepSeconds > 0) {
					try {
						Thread.sleep((long) (sleepRandom.nextInt(gentleSleepSeconds * 2000)));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		} catch (IOException e) {
			logError(e);
		} catch (FlickrException e) {
			logError(e);
		} finally {
			if (w != null) {
				try {
					w.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

}