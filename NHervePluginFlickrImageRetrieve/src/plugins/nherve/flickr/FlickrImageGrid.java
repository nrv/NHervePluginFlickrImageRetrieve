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

import icy.gui.frame.IcyFrame;
import icy.gui.frame.IcyFrameEvent;
import icy.gui.frame.IcyFrameListener;
import icy.gui.util.WindowPositionSaver;

import java.awt.Dimension;
import java.awt.Point;

import plugins.nherve.flickr.tools.PluginFlickrImage;
import plugins.nherve.toolbox.genericgrid.GridCellCollection;
import plugins.nherve.toolbox.genericgrid.GridPanel;
import plugins.nherve.toolbox.plugin.HeadlessReadyComponent;
import plugins.nherve.toolbox.plugin.MyFrame;

public class FlickrImageGrid extends IcyFrame implements IcyFrameListener {
	private static final int THUMB_SIZE = 105;
	private static final int THUMB_SPACING = 5;
	private GridCellCollection<PluginFlickrImage> images;
	private GridPanel<PluginFlickrImage> igp;

	private HeadlessReadyComponent parent;
	
	public FlickrImageGrid(HeadlessReadyComponent parent) {
		super();
		this.parent = parent;
	}

	public void startInterface(MyFrame parentFrame) {
		parentFrame.addFrameListener(this);

		addToMainDesktopPane();
		
		igp = new GridPanel<PluginFlickrImage>(THUMB_SIZE, THUMB_SPACING, false, false);
		add(igp);

		igp.setCells(images);

		setResizable(true);
		setClosable(true);
		
		if (parent.isRunningHeadless()) {
			externalize();
		}
		
		new WindowPositionSaver(this, getClass().getName(), new Point(0, 0), new Dimension(400, 400));
		
		setVisible(true);

		if (parent.isRunningHeadless()) {
			externalize();
			setSize(getSize().width + 1, getSize().height + 1);
			setSize(getSize().width - 1, getSize().height - 1);
		}
		
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

	public GridCellCollection<PluginFlickrImage> getImages() {
		return images;
	}

	public void setImages(GridCellCollection<PluginFlickrImage> images) {
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
