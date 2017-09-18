package de.bsvrz.buv.plugin.startstopp.views;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.progress.UIJob;

import de.bsvrz.sys.startstopp.api.StartStoppClient;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.StartStoppStatus;

public class OnlineStatusPanel extends Composite {

	private Label statusLabel;
	private Label bmLabel;
	private Label abfrageLabel;

	public OnlineStatusPanel(Composite parent) {
		super(parent, SWT.NONE);

		setLayout(new FillLayout());
		
		Group group = new Group(this, SWT.NONE);
		group.setText("Status");
		group.setLayout(new GridLayout());
		
		abfrageLabel = new Label(group, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(abfrageLabel);

		statusLabel = new Label(group, SWT.NONE);
		statusLabel.setText("NICHT VERBUNDEN");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(statusLabel);
		
		bmLabel = new Label(group, SWT.NONE);
		bmLabel.setText("BM: ???");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(bmLabel);
	}

	public void refresh(StartStoppClient client) {

		try {
			StartStoppStatus startStoppStatus = client.getStartStoppStatus();
			updateStatus(startStoppStatus);
		} catch (StartStoppException e) {
			updateStatus(null);
		}
	}

	private void updateStatus(StartStoppStatus startStoppStatus) {
		new UIJob("StatusUpdate") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if( !abfrageLabel.isDisposed()) {
					abfrageLabel.setText(DateFormat.getDateTimeInstance().format(new Date()));
				}
				
				if (!statusLabel.isDisposed()) {
					if (startStoppStatus == null) {
						statusLabel.setText("NICHT VERBUNDEN");
					} else {
						statusLabel.setText(startStoppStatus.getStatus().name());
					}
				}
				if (!bmLabel.isDisposed()) {
					if (startStoppStatus == null) {
						bmLabel.setText("");
					} else {
						if (startStoppStatus.getBetriebsmeldungen()) {
							bmLabel.setText("BM: EIN");
						} else {
							bmLabel.setText("BM: AUS");
						}
					}
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}
}
