package de.bsvrz.buv.plugin.startstopp.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Composite;

import de.bsvrz.sys.startstopp.api.StartStoppClient;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkript;

public class StoppGraph extends Composite {

	private StartStoppSkript skript;


	public StoppGraph(Composite parent) {
		super(parent, SWT.NONE);
	}


	public void setClient(StartStoppClient client) {
		try {
			skript = client.getCurrentSkript();
		} catch (StartStoppException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
