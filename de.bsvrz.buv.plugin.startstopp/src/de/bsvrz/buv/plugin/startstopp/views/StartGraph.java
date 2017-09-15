package de.bsvrz.buv.plugin.startstopp.views;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import de.bsvrz.sys.startstopp.api.StartStoppClient;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;

public class StartGraph extends Composite implements PaintListener {

	private StartStoppSkript skript;
	private StartStoppClient client;
	private Map<String, ApplikationFigur> figuren = new LinkedHashMap<>();

	public StartGraph(Composite parent) {
		super(parent, SWT.NONE);
		setLayout(null);
		addPaintListener(this);
	}

	public void setClient(StartStoppClient client) {
		this.client = client;
		
		try {
			skript = client.getCurrentSkript();
			System.err.println("Skript: " + skript);
			
			for( Inkarnation inkarnation : skript.getInkarnationen()) {
				ApplikationFigur figur = new ApplikationFigur(inkarnation);
				figuren.put(inkarnation.getInkarnationsName(), figur);
			}
			
		} catch (StartStoppException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void paintControl(PaintEvent e) {

		int x = 0;
		int y = 0;
		for( ApplikationFigur figur : figuren.values()) {
			figur.setPosition(x, y);
			figur.paintControl(e);
			y += figur.getHeight() + 10;
		}
		
		e.gc.setClipping((Rectangle) null);
		e.gc.drawLine(10, 10, 200, 200);// TODO Auto-generated method stub
		
	}
}
