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

package plugins.nherve.flickr;

import icy.gui.dialog.MessageDialog;
import icy.image.IcyBufferedImage;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;

import java.util.ArrayList;
import java.util.List;

import plugins.nherve.flickr.tools.FlickrException;
import plugins.nherve.flickr.tools.FlickrFrontend;
import plugins.nherve.flickr.tools.FlickrImage;
import plugins.nherve.flickr.tools.FlickrProgressListener;
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

	private List<FlickrWorkerListener> listeners;
	private FlickrFrontend flickr;
	private String tags;
	private int type;
	private int maxToGrab;
	private AbleToLogMessages display;
	private FlickrImage image;
	private GridCellCollection<FlickrImage> images;

	public FlickrWorker(FlickrFrontend flickr, AbleToLogMessages display) {
		super();
		this.flickr = flickr;
		this.display = display;
		this.type = TYPE_INTERESTINGNESS;
		this.tags = null;
		this.maxToGrab = 1;
		images = null;
		image = null;
		listeners = new ArrayList<FlickrWorkerListener>();
	}

	public boolean addListener(FlickrWorkerListener e) {
		return listeners.add(e);
	}

	private void displayImage(FlickrImage fi, IcyBufferedImage i) throws FlickrException {
		Sequence s = new Sequence(i);
		if ((fi.getTitle() == null) || (fi.getTitle().length() == 0)) {
			s.setName("FiR " + fi.getId());
		} else {
			s.setName("FiR " + fi.getId() + " - " + fi.getTitle());
		}
		Icy.addSequence(s);
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
					image = flickr.getRandomRecentImage(this);
					break;
				case TYPE_INTERESTINGNESS:
					image = flickr.getRandomInterestingImage(this);
					break;
				case TYPE_TAGS:
					image = flickr.getRandomSearchByTagImage(tags, this);
					break;
				default:
					break;
				}

				if (image != null) {
					final FlickrImage finalImage = image;
					final IcyBufferedImage i = flickr.loadImageBiggestAvailableSize(finalImage, this);

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
					images = flickr.getRandomRecentImage(maxToGrab, this);
					break;
				case TYPE_INTERESTINGNESS:
					images = flickr.getRandomInterestingImage(maxToGrab, this);
					break;
				case TYPE_TAGS:
					images = flickr.getRandomSearchByTagImage(tags, maxToGrab, this);
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

	public void setTags(String tags) {
		this.tags = tags;
		setType(TYPE_TAGS);
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

	public GridCellCollection<FlickrImage> getImages() {
		return images;
	}

	public void setImage(FlickrImage image) {
		this.image = image;
	}

	public int getType() {
		return type;
	}

	public String getTags() {
		return tags;
	}

}
