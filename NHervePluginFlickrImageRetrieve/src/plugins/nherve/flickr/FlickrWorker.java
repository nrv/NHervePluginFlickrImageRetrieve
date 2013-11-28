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

package plugins.nherve.flickr;

import icy.gui.dialog.MessageDialog;
import icy.image.IcyBufferedImage;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;

import java.util.ArrayList;
import java.util.List;

import name.herve.flickrlib.FlickrException;
import name.herve.flickrlib.FlickrProgressListener;
import plugins.nherve.flickr.tools.PluginFlickrFrontend;
import plugins.nherve.flickr.tools.PluginFlickrImage;
import plugins.nherve.toolbox.AbleToLogMessages;
import plugins.nherve.toolbox.genericgrid.GridCellCollection;

/**
 * 
 * @author Nicolas HERVE - n.herve@laposte.net
 */
public class FlickrWorker implements Runnable, FlickrProgressListener {
	public final static int TYPE_RECENT = 1;
	public final static int TYPE_INTERESTINGNESS = 2;
	public final static int TYPE_TAGS = 3;
	public final static int TYPE_IMAGE = 4;
	public final static int TYPE_EXPERT = 5;

	private List<FlickrWorkerListener> listeners;
	private PluginFlickrFrontend flickr;
	private String queryParameters;
	private int type;
	private int maxToGrab;
	private AbleToLogMessages display;
	private PluginFlickrImage image;
	private GridCellCollection<PluginFlickrImage> images;

	public FlickrWorker(PluginFlickrFrontend flickr, AbleToLogMessages display) {
		super();
		this.flickr = flickr;
		this.display = display;
		this.type = TYPE_INTERESTINGNESS;
		this.queryParameters = null;
		this.maxToGrab = 1;
		images = null;
		image = null;
		listeners = new ArrayList<FlickrWorkerListener>();
	}

	public boolean addListener(FlickrWorkerListener e) {
		return listeners.add(e);
	}

	private void displayImage(PluginFlickrImage fi, IcyBufferedImage i) throws FlickrException {
		Sequence s = new Sequence(i);
		if ((fi.getTitle() == null) || (fi.getTitle().length() == 0)) {
			s.setName("FiR " + fi.getId());
		} else {
			s.setName("FiR " + fi.getId() + " - " + fi.getTitle());
		}
		Icy.getMainInterface().addSequence(s);
		display.displayMessage(fi.getImageWebPageURL().toString());
	}

	@Override
	public void notifyNewProgressionStep(final String step) {
		ThreadUtil.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (FlickrWorkerListener l : listeners) {
					l.notifyNewProgressionStep(step);
				}
			}
		});
	}

	@Override
	public boolean notifyProgress(final double position, final double length) {
		ThreadUtil.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (FlickrWorkerListener l : listeners) {
					l.notifyProgress(position, length);
				}
			}
		});
		return true;
	}

	@Override
	public void run() {
		try {
			if (maxToGrab == 1) {
				switch (type) {
				case TYPE_RECENT:
					image = flickr.getRandomRecentIcyImage(this);
					break;
				case TYPE_INTERESTINGNESS:
					image = flickr.getRandomInterestingIcyImage(this);
					break;
				case TYPE_TAGS:
					image = flickr.getRandomSearchByTagIcyImage(queryParameters, this);
					break;
				case TYPE_EXPERT:
					image = flickr.getIcySearchByExpertQuery(queryParameters, this).get(0);
					break;
				default:
					break;
				}

				if (image != null) {
					final PluginFlickrImage finalImage = image;
					final IcyBufferedImage i = flickr.loadIcyImageBiggestAvailableSize(finalImage, this);

					ThreadUtil.invokeLater(new Runnable() {
						@Override
						public void run() {
							try {
								displayImage(finalImage, i);
							} catch (FlickrException e) {
								MessageDialog.showDialog(e.getMessage(), MessageDialog.ERROR_MESSAGE);
							}
						}
					});
				}
			} else {
				switch (type) {
				case TYPE_RECENT:
					images = flickr.getRandomRecentIcyImage(maxToGrab, this);
					break;
				case TYPE_INTERESTINGNESS:
					images = flickr.getRandomInterestingIcyImage(maxToGrab, this);
					break;
				case TYPE_TAGS:
					images = flickr.getRandomSearchByTagIcyImage(queryParameters, maxToGrab, this);
					break;
				case TYPE_EXPERT:
					images = flickr.getIcySearchByExpertQuery(queryParameters, this);
					break;
				default:
					break;
				}
			}
		} catch (final FlickrException e) {
			ThreadUtil.invokeLater(new Runnable() {
				@Override
				public void run() {
					MessageDialog.showDialog(e.getMessage(), MessageDialog.ERROR_MESSAGE);
				}
			});
		} finally {
			final FlickrWorker finalWorker = this;
			ThreadUtil.invokeLater(new Runnable() {
				@Override
				public void run() {
					for (FlickrWorkerListener l : listeners) {
						l.notifyProcessEnded(finalWorker);
					}
				}
			});
		}
	}

	public void setQueryParameters(String queryParameters) {
		this.queryParameters = queryParameters;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setMaxToGrab(int maxToGrab) {
		this.maxToGrab = maxToGrab;
	}

	public int getMaxToGrab() {
		return maxToGrab;
	}

	public GridCellCollection<PluginFlickrImage> getImages() {
		return images;
	}

	public void setImage(PluginFlickrImage image) {
		this.image = image;
	}

	public int getType() {
		return type;
	}

	public String getQueryParameters() {
		return queryParameters;
	}

}
