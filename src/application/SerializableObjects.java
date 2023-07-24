package application;

import java.io.Serializable;
import java.util.ArrayList;
import physicsComponents.*;

public class SerializableObjects implements Serializable {
	private static final long serialVersionUID = -5608502759665792888L;
	public ArrayList<EntityUnit> entities = new ArrayList<EntityUnit>();
	public ArrayList<ForceActor> forceActors = new ArrayList<ForceActor>();
	
	public SerializableObjects(ArrayList<EntityUnit> entities, ArrayList<ForceActor> forceActors) {
		this.entities = new ArrayList<>(entities);
		this.forceActors = new ArrayList<>(forceActors);
	}
}
