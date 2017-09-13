package de.bsvrz.buv.plugin.startstopp.views;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;

import de.bsvrz.sys.startstopp.api.StartStoppClient;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppSkriptStatus;

public class ApplikationsListe extends Composite {

	private ScheduledExecutorService updater;
	private boolean disposed;
	private StartStoppClient client;

	public ApplikationsListe(Composite parent) {
		super(parent, SWT.NONE);
		addDisposeListener((event)->dispose(event));

		
		updater = Executors.newSingleThreadScheduledExecutor();
		updater.scheduleAtFixedRate(()->refresh(), 1, 3, TimeUnit.SECONDS);
	}

	public void setClient(StartStoppClient client) {
		this.client = client;
	}
	
	private void dispose(DisposeEvent event) {
		System.err.println("Dispose");
		disposed = true;
		updater.shutdown();
	}
	
	private void refresh() {
		if( disposed || client == null) {
			return;
		}

		try {
			StartStoppSkriptStatus status = client.getCurrentSkriptStatus();
			System.err.println("Aktualisiere Ansicht: " + status);
//			List<Applikation> applikationen = client.getApplikationen();
//			System.err.println("Aktualisiere Ansicht: " + applikationen);
		} catch (StartStoppException e) {
			System.err.println(e.getLocalizedMessage());
		}
	}
	

}
