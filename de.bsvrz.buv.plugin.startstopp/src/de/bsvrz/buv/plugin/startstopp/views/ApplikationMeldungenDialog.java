package de.bsvrz.buv.plugin.startstopp.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

import de.bsvrz.buv.plugin.startstopp.Activator;
import de.bsvrz.sys.startstopp.api.StartStoppClient;
import de.bsvrz.sys.startstopp.api.StartStoppException;
import de.bsvrz.sys.startstopp.api.jsonschema.Applikation;
import de.bsvrz.sys.startstopp.api.jsonschema.ApplikationLog;

public class ApplikationMeldungenDialog extends TitleAreaDialog {

	private Applikation applikation;
	private StartStoppClient client;

	public ApplikationMeldungenDialog(Shell shell, StartStoppClient client, Applikation applikation) {
		super(shell);
		this.applikation = applikation;
		this.client = client;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		ImageRegistry imageRegistry = Activator.getDefault().getImageRegistry();
		String imageFilePath = "icons/saveas_wiz.png";
		
		Image image = imageRegistry.get(imageFilePath);
		if(image == null) {
			imageRegistry.put(imageFilePath, Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, imageFilePath));
			image = imageRegistry.get(imageFilePath);
		}
		setTitleImage(image);

		setTitle("Applikation: " + applikation.getInkarnation().getInkarnationsName());
		setMessage("Startmeldungen der Appliaktion");
		

		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);

		ListViewer messageViewer = new ListViewer(panel);
		GridDataFactory.fillDefaults().grab(true, true).hint(300, 200).applyTo(messageViewer.getControl());
		messageViewer.setContentProvider(new ArrayContentProvider());

		new UIJob("Messages aktualisieren") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				fillMessages(messageViewer);
				return Status.OK_STATUS;
			};
		}.schedule();

		return super.createDialogArea(parent);
	}

	private void fillMessages(ListViewer messageViewer) {

		List<String> messages = new ArrayList<>();

		try {
			ApplikationLog applikationLog = client
					.getApplikationLog(applikation.getInkarnation().getInkarnationsName());
			messages = applikationLog.getMessages();
		} catch (StartStoppException e) {
			messages.add(e.getLocalizedMessage());
			messages.addAll(e.getMessages());
		}

		messageViewer.setInput(messages);
	}
}
