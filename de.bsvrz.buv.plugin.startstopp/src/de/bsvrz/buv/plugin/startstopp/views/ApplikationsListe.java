package de.bsvrz.buv.plugin.startstopp.views;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.progress.UIJob;

import de.bsvrz.sys.startstopp.api.StartStoppClient;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.Util;

public class ApplikationsListe extends Composite {

	public class ApplikationTableLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof Applikation) {
				
				Applikation applikation = (Applikation) element;
				
				switch (columnIndex) {
				case 0:
					return applikation.getInkarnation().getInkarnationsName();
				case 1:
					return applikation.getInkarnation().getStartArt().getOption().toString();
				case 2:
					return applikation.getStatus().name();
				case 3:
					return applikation.getStartMeldung();
				}
			}
			return getText(element);
		}

	}

	private ScheduledExecutorService updater;
	private boolean disposed;
	private StartStoppClient client;
	private TableViewer applikationViewer;
	private Label messageLabel;

	public ApplikationsListe(Composite parent) {
		super(parent, SWT.NONE);

		setLayout(new GridLayout());

		Composite tablePanel = new Composite(this, SWT.NONE);
		tablePanel.setLayout(new FillLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tablePanel);
		
		applikationViewer = new TableViewer(tablePanel);
		applikationViewer.getTable().setHeaderVisible(true);

		TableLayout tableLayout = new TableLayout();
		TableColumn column = new TableColumn(applikationViewer.getTable(), SWT.NONE);
		column.setText("Name");
		tableLayout.addColumnData(new ColumnWeightData(2));

		column = new TableColumn(applikationViewer.getTable(), SWT.NONE);
		column.setText("Typ");
		tableLayout.addColumnData(new ColumnWeightData(1));

		column = new TableColumn(applikationViewer.getTable(), SWT.NONE);
		column.setText("Status");
		tableLayout.addColumnData(new ColumnWeightData(1));

		column = new TableColumn(applikationViewer.getTable(), SWT.NONE);
		column.setText("Meldung");
		tableLayout.addColumnData(new ColumnWeightData(4));

		applikationViewer.getTable().setLayout(tableLayout);

		applikationViewer.setContentProvider(new ArrayContentProvider());
		applikationViewer.setLabelProvider(new ApplikationTableLabelProvider());
		applikationViewer.getTable().setHeaderBackground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));

		messageLabel = new Label(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(messageLabel);

		addDisposeListener((event) -> dispose(event));
		updater = Executors.newSingleThreadScheduledExecutor();
		updater.scheduleAtFixedRate(() -> refresh(), 1, 3, TimeUnit.SECONDS);
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
		if (disposed || client == null) {
			return;
		}

		try {
			List<Applikation> applikationen = client.getApplikationen();
			updater.schedule(()->aktualisiereAnsicht(applikationen), 0, TimeUnit.MICROSECONDS);
		} catch (StartStoppException e) {
			updater.schedule(()->aktualisiereAnsicht(Collections.emptyList(), e.getLocalizedMessage()), 0, TimeUnit.MICROSECONDS);
			System.err.println();
		}
	}

	private void aktualisiereAnsicht(List<Applikation> applikationen) {
		aktualisiereAnsicht(applikationen, "");
	}

	private void aktualisiereAnsicht(List<Applikation> applikationen, String message) {
		new UIJob("") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				applikationViewer.setInput(applikationen.toArray());
				messageLabel.setText(Util.nonEmptyString(message));
				return Status.OK_STATUS;
			}
		}.schedule();
	}

}
