package de.bsvrz.buv.plugin.startstopp.views;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;

public class ApplikationFigur implements PaintListener {

	private String name;
	private int x;
	private int y;
	private int textWidth;
	private int textHeight;

	public ApplikationFigur(Inkarnation inkarnation) {
		this.name = inkarnation.getInkarnationsName();
	}

	@Override
	public void paintControl(PaintEvent e) {
		textWidth = e.gc.stringExtent(name).x;
		textHeight = e.gc.stringExtent(name).y;
		e.gc.drawRectangle(x, y, getWidth(), getHeight());
		e.gc.drawString(name, x + 10, y + 10);
		e.gc.drawString("lokal", x + 10, y + 10 + textHeight + 5);
	}

	void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	int getWidth() {
		return textWidth + 20;
	}

	int getHeight() {
		return textHeight * 2 + 25;
	}
}
