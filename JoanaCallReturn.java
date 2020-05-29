package edu.kit.joana.component.connector;

import java.nio.file.Path;

public class JoanaCallReturn {

	public static final String FILE_ENDING = ".json";
	public final Flows flows;

	public JoanaCallReturn(Flows flows) {
		this.flows = flows;
	}

	public void store(Path path) {
		Util.store(path, this);
	}

	public static JoanaCallReturn load(Path path) {
		return Util.load(path);
	}

	@Override
	public String toString() {
		return flows.toString();
	}
}
