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

package plugins.nherve.flickr;

import icy.gui.frame.IcyFrame;
import icy.gui.frame.IcyFrameEvent;
import icy.gui.frame.IcyFrameListener;
import plugins.nherve.flickr.tools.FlickrImage;
import plugins.nherve.toolbox.genericgrid.GridCellCollection;
import plugins.nherve.toolbox.genericgrid.GridPanel;

public class FlickrImageGrid extends IcyFrame implements IcyFrameListener {
	private static final int THUMB_SIZE = 105;
	private static final int THUMB_SPACING = 5;
	private GridCellCollection<FlickrImage> images;
	private GridPanel<FlickrImage> igp;

	public FlickrImageGrid() {
		super();
	}

	public void startInterface(IcyFrame parentFrame) {
		parentFrame.addFrameListener(this);

		igp = new GridPanel<FlickrImage>(THUMB_SIZE, THUMB_SPACING, false, false);
		add(igp);

		igp.setCells(images);

		setResizable(true);
		int initialSize = 5 * (THUMB_SIZE + THUMB_SPACING) + 75;
		setSize(initialSize, initialSize);
		setClosable(true);
		setVisible(true);
		center();
		addToMainDesktopPane();
		requestFocus();
	}

	@Override
	public void icyFrameOpened(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameClosing(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameClosed(IcyFrameEvent e) {
		close();
	}

	@Override
	public void icyFrameIconified(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameDeiconified(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameActivated(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameDeactivated(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameInternalized(IcyFrameEvent e) {
	}

	@Override
	public void icyFrameExternalized(IcyFrameEvent e) {
	}

	public GridCellCollection<FlickrImage> getImages() {
		return images;
	}

	public void setImages(GridCellCollection<FlickrImage> images) {
		this.images = images;
	}

	@Override
	public void close() {
		super.close();

		removeAll();
		if (igp != null) {
			igp.setCells(null);
			igp = null;
		}
	}

}
