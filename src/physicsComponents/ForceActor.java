package physicsComponents;

import java.io.Serializable;

public abstract class ForceActor implements Serializable {
	private static final long serialVersionUID = -5579173343187295550L;

	public abstract void applyForce();
}
