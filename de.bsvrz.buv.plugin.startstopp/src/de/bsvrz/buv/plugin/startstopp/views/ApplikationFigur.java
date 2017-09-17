package de.bsvrz.buv.plugin.startstopp.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Display;

import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.StartBedingung.Warteart;
import de.bsvrz.sys.startstopp.api.jsonschema.StoppBedingung;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;

public abstract class ApplikationFigur implements PaintListener {

	private boolean start;
	private String name;
	private String rechner;
	private List<ApplikationFigur> referenzen = new ArrayList<>();
	private Inkarnation inkarnation;
	private boolean kernSystem;

	private int x;
	private int y;
	private int textWidth;
	private int textHeight;

	protected ApplikationFigur(boolean start, String name) {
		this(start, name, null, null, null);
	}

	protected ApplikationFigur(boolean start, String name, String rechner, List<ApplikationFigur> vorgaenger,
			Inkarnation inkarnation) {
		this.start = start;
		this.name = name;
		this.rechner = rechner;
		if (vorgaenger != null) {
			this.referenzen.addAll(vorgaenger);
		}
		this.inkarnation = inkarnation;
	}

	@Override
	public void paintControl(PaintEvent e) {

		if (kernSystem) {
			e.gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			e.gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
		} else if (rechner != null) {
			e.gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			e.gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
		} else {
			e.gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
			e.gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_TRANSPARENT));
		}

		textWidth = e.gc.stringExtent(name).x;
		textHeight = e.gc.stringExtent(name).y;
		e.gc.fillRectangle(x, y, getWidth(), getHeight());
		e.gc.drawRectangle(x, y, getWidth(), getHeight());
		e.gc.drawString(name, x + (getWidth() - textWidth) /2, y + 10);

		String rechnerStr = rechner == null ? "lokal" : rechner;
		textWidth = e.gc.stringExtent(rechnerStr).x;
		e.gc.drawString(rechnerStr, x + (getWidth() - textWidth) /2, y + 10 + textHeight + 5);
	}

	void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	int getWidth() {
		return Math.max(150, textWidth + 20);
	}

	int getHeight() {
		return textHeight * 2 + 25;
	}

	List<ApplikationFigur> getReferenzen() {
		return referenzen;
	}
	
	public void setReferenzen(List<ApplikationFigur> referenzen) {
		this.referenzen.clear();
		this.referenzen.addAll(referenzen);
	}

	public int getXOffset() {
		int result = 0;

		if (referenzen.isEmpty()) {
			return result;
		}

		boolean startOnBeginn = false;
		if (start && inkarnation != null && inkarnation.getStartBedingung() != null) {
			startOnBeginn = inkarnation.getStartBedingung().getWarteart() == Warteart.BEGINN;
		}
		for (ApplikationFigur figur : referenzen) {
			if (startOnBeginn) {
				result = Math.max(result, figur.getXOffset());
			} else {
				result = Math.max(result, figur.getXOffset() + figur.getWidth());
			}
		}

		long warteZeit = 0;
		if (start && inkarnation != null && inkarnation.getStartBedingung() != null) {
			try {
				warteZeit = Util.convertToWarteZeitInMsec(inkarnation.getStartBedingung().getWartezeit()) / 1000;
			} catch (StartStoppException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (!start && inkarnation != null && inkarnation.getStoppBedingung() != null) {
			try {
				warteZeit = Util.convertToWarteZeitInMsec(inkarnation.getStoppBedingung().getWartezeit()) / 1000;
			} catch (StartStoppException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 

		return result + (int) warteZeit;
	}
	
	void setKernSystem(boolean kernSystem) {
		this.kernSystem = kernSystem;
	}

	int getX() {
		return x;
	}

	int getY() {
		return y;
	}

	boolean isKernSystem() {
		return kernSystem;
	}

	String getName() {
		return name;
	}

	StartBedingung getStartBedingung() {
		if( inkarnation == null) {
			return null;
		}
		return inkarnation.getStartBedingung();
	}

	StoppBedingung getStoppBedingung() {
		if( inkarnation == null) {
			return null;
		}
		return inkarnation.getStoppBedingung();
	}
	
}
