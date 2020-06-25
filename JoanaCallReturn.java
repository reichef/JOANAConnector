package edu.kit.joana.component.connector;

import java.nio.file.Path;

public abstract class JoanaCallReturn {

	public static JoanaCallReturn fromJson(String json) {
		return Utils.fromJson(json);
	}

	public static JoanaCallReturn load(Path path) {
		return Utils.load(path);
	}

	public void store(Path path) {
		Utils.store(path, this);
	}

	abstract boolean isError();

	abstract <T> T accept(JoanaCallReturnVisitor<T> visitor);

	public JoanaCallReturnFlows asFlows(){
		return accept(new JoanaCallReturnVisitor<JoanaCallReturnFlows>() {
			@Override public JoanaCallReturnFlows visit(JoanaCallReturnError error) {
				throw new RuntimeException("Does contain " + error);
			}

			@Override public JoanaCallReturnFlows visit(JoanaCallReturnFlows flows) {
				return flows;
			}
		});
	}
}
