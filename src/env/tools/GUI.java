package tools;

import cartago.*;
import gui.*;

public class GUI extends Artifact {

	OrchestraView view;

	void init() {
		view = new OrchestraView();
		view.setVisible(true);
	}

	@Override
	protected void dispose() {
		view.setVisible(false);
		view.dispose();
	}

	@OPERATION
	void enterOrchestra(String name, String type) {
		view.addAgent(name, type);
	}

	@OPERATION
	void exitOrchestra(String name) {
		view.removeAgent(name);
	}

	@OPERATION
	void setOrchestraSong(String name)
	{
		view.setMusic(name);
	}
}

