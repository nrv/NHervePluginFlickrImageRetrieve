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

import icy.file.Saver;
import icy.image.IcyBufferedImage;
import icy.network.NetworkUtil;
import icy.preferences.ApplicationPreferences;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;

import loci.formats.FormatException;
import plugins.nherve.flickr.tools.FlickrException;
import plugins.nherve.flickr.tools.FlickrFrontend;
import plugins.nherve.flickr.tools.FlickrImage;
import plugins.nherve.flickr.tools.FlickrProgressListener;
import plugins.nherve.flickr.tools.FlickrSearchQuery;
import plugins.nherve.flickr.tools.FlickrSearchResponse;
import plugins.nherve.flickr.tools.FlickrSearchResponse.FlickrSearchResponseIterator;
import plugins.nherve.flickr.tools.filters.MinSizeFilter;
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
		grab.init(APP_KEY, 0, false);

		// long last60days = System.currentTimeMillis() - 60 * 24 * 60 * 60 *
		// 1000;
		// grab.test("license=1,2,5,7&content_type=1&bbox=40,0,50,10&min_date_upload="
		// + last60days + "&sort=interestingness-desc&accuracy=6");
		grab.work("/home/nherve/Travail/Data/Flickr", "license=1,2,5,7&tag_mode=all&sort=interestingness-desc&tags=biology", 10, 400, 1000 * 800);
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
		ApplicationPreferences.load();

		flickr = new FlickrFrontend(key);
		flickr.setDebug(debug);
		setLogEnabled(debug);

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

	private void test(String query) {
		try {
			FlickrSearchQuery q = new FlickrSearchQuery(query, 100);
			q.setPerpage(10);

			FlickrSearchResponse pictures = flickr.search(q);
			for (FlickrImage i : pictures) {
				outWithTime(i.getId());
			}
		} catch (FlickrException e) {
			e.printStackTrace();
		}
	}

	private void work(String parent, String query, int nb, int minDim, int preferedSurface) {
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

			FlickrSearchResponse pictures = flickr.search(q, new MinSizeFilter(minDim));

			for (FlickrImage i : pictures) {
				IcyBufferedImage img = flickr.loadImage(i, i.getClosestSize(preferedSurface), this);
				File outputFile = new File(picdir, i.getId() + ".jpg");
				try {
					Saver.saveImage(img, outputFile, true);
					float sz = outputFile.length();
					String strSz = " o";
					if (sz > 1024) {
						sz /= 1024;
						strSz = " Ko";
						if (sz > 1024) {
							sz /= 1024;
							strSz = " Mo";
						}
					}

					strSz = df.format(sz) + strSz;

					w.write(outputFile.getName() + " | " + img.getWidth() + "x" + img.getHeight() + " | " + strSz + " | " + i.getImageWebPageURL() + " | " + i.getId() + " | " + i.getOwner() + " | " + i.getLicense().getName() + " - " + i.getTitle());
					w.newLine();
					w.flush();
					outWithTime(outputFile.getName() + " - " + strSz + " - " + img.getWidth() + "x" + img.getHeight() + " - " + i.getTitle() + " - " + i.getLicense().getName());
				} catch (IOException e1) {
					err(outputFile.getName() + " - " + e1.getClass().getName() + " : " + e1.getMessage());
				} catch (FormatException e) {
					err(outputFile.getName() + " - " + e.getClass().getName() + " : " + e.getMessage());
				}

				if (gentleSleepSeconds > 0) {
					try {
						Thread.sleep(1l + sleepRandom.nextInt(gentleSleepSeconds * 2000));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (FlickrException e) {
			e.printStackTrace();
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
