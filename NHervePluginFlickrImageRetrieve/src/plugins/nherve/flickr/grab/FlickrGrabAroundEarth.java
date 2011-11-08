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
import plugins.nherve.flickr.tools.filters.ChainedFilters;
import plugins.nherve.flickr.tools.filters.FlickrSearchResponseFilter;
import plugins.nherve.flickr.tools.filters.HasTagsFilter;
import plugins.nherve.flickr.tools.filters.MinSizeFilter;
import plugins.nherve.flickr.tools.filters.NoDuplicateAuthorFilter;
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
		
		if (args.length == 7) {
			int p = 0;
			try {
				dir = args[p++];
				slp = Integer.parseInt(args[p++]);
				nbs = Integer.parseInt(args[p++]);
				pps = Integer.parseInt(args[p++]);
				dim = Integer.parseInt(args[p++]);
				srf = Integer.parseInt(args[p++]);
				day = Integer.parseInt(args[p++]);
			} catch (NumberFormatException e) {
				err(e);
				displayHelp = true;
			}
		} else {
			displayHelp = true;
		}
		
		if (displayHelp) {
			err("Usage : FlickrGrabAroundEarth [grab directory] [sleep sec] [nb. squares] [nb. pics per square] [min dim] [prefered surf.] [max uploaded days]");
			err("e.g.  : ./grabEarth.sh ./data 0 5000 25 400 800000 120");
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

		grab.grabEarthGrid(dir, nbs, pps, dim, srf, day);
	}

	private FlickrFrontend flickr;
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

	private void grabEarthGrid(String parent, int nbSquare, int nbPicPerSquare, int minDim, int preferedSurface, int maxUploadedDays) {
		int unitLength = (int) Math.floor(Math.sqrt(FULL_SURFACE / (double) nbSquare));

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

			for (int longitude = MIN_LONGITUDE; longitude < (MAX_LONGITUDE - unitLength); longitude += unitLength) {
				for (int latitude = MIN_LATITUDE; latitude < (MAX_LATITUDE - unitLength); latitude += unitLength) {
					int bbox1 = longitude;
					int bbox2 = latitude;
					int bbox3 = longitude + unitLength;
					int bbox4 = latitude + unitLength;

					String query = "license=1,2,5,7";
					query += "&content_type=1";
					query += "&min_date_upload=" + lastDays;
					query += "&sort=interestingness-desc";
					query += "&bbox=" + bbox1 + "," + bbox2 + "," + bbox3 + "," + bbox4;
					query += "&accuracy=6";

					ChainedFilters filter = new ChainedFilters();
					filter.add(new MinSizeFilter(minDim));
					filter.add(new HasTagsFilter());
					filter.add(new NoDuplicateAuthorFilter());

					FlickrSearchQuery q = new FlickrSearchQuery(query, nbPicPerSquare);
					q.setPerpage(nbPicPerSquare * 5);

					FlickrSearchResponse pictures = flickr.search(q, filter);

					FlickrSearchResponseIterator it = (FlickrSearchResponseIterator) pictures.iterator();

					outWithTime("bbox = " + bbox1 + ", " + bbox2 + ", " + bbox3 + ", " + bbox4 + " - " + it.getTotal() + " images in the last " + maxUploadedDays + " days");

					FlickrImage i = null;

					while (it.hasNext()) {
						i = it.next();
						File outputFile = null;
						try {
							IcyBufferedImage img = flickr.loadImage(i, i.getClosestSize(preferedSurface), this);
							outputFile = new File(picdir, i.getId() + ".jpg");

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

							outWithTime("bbox = " + bbox1 + ", " + bbox2 + ", " + bbox3 + ", " + bbox4 + " - " + outputFile.getName() + " - " + strSz + " - " + img.getWidth() + "x" + img.getHeight() + " - " + i.getTitle() + " - " + i.getLicense().getName());
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
