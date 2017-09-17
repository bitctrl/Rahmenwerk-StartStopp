package de.bsvrz.buv.plugin.startstopp.views;

import java.util.List;

import de.bsvrz.sys.startstopp.api.jsonschema.Inkarnation;

public class StartApplikationFigur extends ApplikationFigur {

	public StartApplikationFigur(String name) {
		super(true, name);
	}

	public StartApplikationFigur(String name, String rechner, List<ApplikationFigur> vorgaenger,
			Inkarnation inkarnation) {
		super(true, name, rechner, vorgaenger, inkarnation);
	}
}
