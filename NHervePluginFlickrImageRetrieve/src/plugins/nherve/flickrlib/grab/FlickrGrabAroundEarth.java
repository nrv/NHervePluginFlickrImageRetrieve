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

package plugins.nherve.flickrlib.grab;

import icy.file.Saver;
import icy.image.IcyBufferedImage;
import icy.network.NetworkUtil;
import icy.preferences.ApplicationPreferences;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;

import loci.formats.FormatException;
import name.herve.flickrlib.FlickrException;
import name.herve.flickrlib.FlickrImage;
import name.herve.flickrlib.FlickrProgressListener;
import name.herve.flickrlib.FlickrSearchQuery;
import name.herve.flickrlib.FlickrSearchResponse;
import name.herve.flickrlib.FlickrSearchResponse.FlickrSearchResponseIterator;
import name.herve.flickrlib.filters.ChainedFilters;
import name.herve.flickrlib.filters.HasTagsFilter;
import name.herve.flickrlib.filters.MinSizeFilter;
import name.herve.flickrlib.filters.NoDuplicateAuthorFilter;
import plugins.nherve.flickr.tools.PluginFlickrFrontend;
import plugins.nherve.toolbox.Algorithm;

/**
 * 
 * @author Nicolas HERVE - n.herve@laposte.net
 */
public class FlickrGrabAroundEarth extends Algorithm implements FlickrProgressListener {
	private final static String APP_KEY = "9a96e50181eb0ab5be0ee15b147acaf8";

	private final static int MIN_LONGITUDE = -180;
	private final static int MAX_LONGITUDE = 180;
	private final static int MIN_LATITUDE = -90;
	private final static int MAX_LATITUDE = 90;
	private final static int LONG_LENGTH = MAX_LONGITUDE - MIN_LONGITUDE;
	private final static int LAT_LENGTH = MAX_LATITUDE - MIN_LATITUDE;
	private final static int FULL_SURFACE = LONG_LENGTH * LAT_LENGTH;

	private final static String FIELD_SEP = " | ";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean displayHelp = false;

		String dir = null;
		int slp = -1;
		int nbs = -1;
		int pps = -1;
		int dim = -1;
		int srf = -1;
		int day = -1;
		int slg = MIN_LONGITUDE;
		int slt = MIN_LATITUDE;

		if (args.length >= 7) {
			int p = 0;
			try {
				dir = args[p++];
				slp = Integer.parseInt(args[p++]);
				nbs = Integer.parseInt(args[p++]);
				pps = Integer.parseInt(args[p++]);
				dim = Integer.parseInt(args[p++]);
				srf = Integer.parseInt(args[p++]);
				day = Integer.parseInt(args[p++]);
				if (args.length >= 8) {
					slg = Integer.parseInt(args[p++]);
					if (args.length >= 9) {
						slt = Integer.parseInt(args[p++]);
					}
				}
			} catch (NumberFormatException e) {
				err(e);
				displayHelp = true;
			}
		} else {
			displayHelp = true;
		}

		if (displayHelp) {
			err("Usage : FlickrGrabAroundEarth [grab directory] [sleep sec] [nb. squares] [nb. pics per square] [min dim] [prefered surf.] [max uploaded days] [start longitude (optional)] [start latitude (optional)]");
			err("e.g.  : ./grabEarth.sh ./data 0 5000 25 400 800000 120");
			err("e.g.  : ./grabEarth.sh ./data 0 5000 25 400 800000 120 -171 54");
			err("");
			String allArgs = "";
			for (String a : args) {
				allArgs += "(" + a + ") ";
			}
			err(allArgs);
			System.exit(1);
		}

		FlickrGrabAroundEarth grab = new FlickrGrabAroundEarth();
		grab.init(APP_KEY, slp, false);

		grab.grabEarthGrid(dir, nbs, pps, dim, srf, day, slg, slt);
	}

	private PluginFlickrFrontend flickr;
	private DecimalFormat df = new DecimalFormat("0.00");
	private int gentleSleepSeconds;

	private File getDirectoryForGrabSession(String parent) {
		String d = "FlickrGrabAroundEarth-" + System.currentTimeMillis();
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

		flickr = new PluginFlickrFrontend(key);
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

	private void grabEarthGrid(String parent, int nbSquare, int nbPicPerSquare, int minDim, int preferedSurface, int maxUploadedDays, int slg, int slt) {
		int unitLength = (int) Math.floor(Math.sqrt(FULL_SURFACE / (double) nbSquare));

		File parentDir = new File(parent);
		if (!parentDir.exists()) {
			parentDir.mkdirs();
		}

		File dir = getDirectoryForGrabSession(parent);
		dir.mkdir();

		File picdir = new File(dir + File.separator + "pictures");
		picdir.mkdir();

		File metadata = new File(dir, "metadata.txt");
		BufferedWriter w = null;

		long lastDays = System.currentTimeMillis() - maxUploadedDays * 24 * 60 * 60 * 1000;
		Random sleepRandom = new Random(System.currentTimeMillis());

		try {
			w = new BufferedWriter(new FileWriter(metadata));

			int longitude = slg;
			int latitude = slt;
			while (longitude < (MAX_LONGITUDE - unitLength)) {
				while (latitude < (MAX_LATITUDE - unitLength)) {
					int[] bbox = new int[4];
					bbox[0] = longitude;
					bbox[1] = latitude;
					bbox[2] = longitude + unitLength;
					bbox[3] = latitude + unitLength;

					String query = "license=1,2,5,7";
					query += "&content_type=1";
					query += "&min_date_upload=" + lastDays;
					query += "&sort=interestingness-desc";
					query += "&bbox=" + bbox[0] + "," + bbox[1] + "," + bbox[2] + "," + bbox[3];
					query += "&accuracy=6";

					ChainedFilters filter = new ChainedFilters();
					filter.add(new MinSizeFilter(minDim));
					filter.add(new HasTagsFilter());
					filter.add(new NoDuplicateAuthorFilter());

					FlickrSearchQuery q = new FlickrSearchQuery(query, nbPicPerSquare);
					q.setPerpage(nbPicPerSquare * 5);

					FlickrSearchResponse pictures = flickr.search(q, filter);

					FlickrSearchResponseIterator it = (FlickrSearchResponseIterator) pictures.iterator();

					if (it != null) {
						String bboxstr = "";
						for (int b = 0; b < 4; b++) {
							if (bbox[b] < 0) {
								bboxstr += "n";
							} else {
								bboxstr += "p";
							}
							bboxstr += Math.abs(bbox[b]);
						}

						outWithTime("bbox = " + bboxstr + " - " + it.getTotal() + " images in the last " + maxUploadedDays + " days");

						FlickrImage i = null;

						while (it.hasNext()) {
							i = it.next();
							File outputFile = null;
							try {
								BufferedImage img = flickr.loadImage(i, i.getClosestSize(preferedSurface), this);
								outputFile = new File(picdir, bboxstr + "_" + i.getId() + ".jpg");

								Saver.saveImage(IcyBufferedImage.createFrom(img), outputFile, true);
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

								w.write(outputFile.getName());
								w.write(FIELD_SEP);
								w.write(img.getWidth() + "x" + img.getHeight());
								w.write(FIELD_SEP);
								w.write(strSz);
								w.write(FIELD_SEP);
								w.write(i.getImageWebPageURL().toString());
								w.write(FIELD_SEP);
								w.write(i.getId());
								w.write(FIELD_SEP);
								w.write(i.getOwner());
								w.write(FIELD_SEP);
								w.write(i.getLicense().getName());
								w.write(FIELD_SEP);
								w.write(i.getTags());
								w.write(FIELD_SEP);
								w.write(i.getTitle());
								w.newLine();
								w.flush();

								outWithTime("bbox = " + bboxstr + " - " + outputFile.getName() + " - " + strSz + " - " + img.getWidth() + "x" + img.getHeight() + " - " + i.getTitle() + " - " + i.getLicense().getName());
							} catch (IOException e1) {
								err(outputFile.getName() + " - " + e1.getClass().getName() + " : " + e1.getMessage());
							} catch (FormatException e) {
								err(outputFile.getName() + " - " + e.getClass().getName() + " : " + e.getMessage());
							} catch (FlickrException e) {
								err(e);
							}

							if (gentleSleepSeconds > 0) {
								try {
									Thread.sleep(1l + sleepRandom.nextInt(gentleSleepSeconds * 2000));
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}
					latitude += unitLength;
				}

				longitude += unitLength;
				latitude = MIN_LATITUDE;
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
