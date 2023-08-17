package application;

import java.io.Serializable;
import java.util.ArrayList;
import physicsComponents.*;

public class SerializableObjects implements Serializable {
	private static final long serialVersionUID = -5608502759665792888L;
	public ArrayList<EntityUnit> entities = new ArrayList<EntityUnit>();
	public ArrayList<String> entityNames = new ArrayList<String>();
	public ArrayList<ForceActor> forceActors = new ArrayList<ForceActor>();
	public ArrayList<String> forceNames = new ArrayList<String>();
	
	public SerializableObjects(ArrayList<EntityUnit> entities, ArrayList<ForceActor> forceActors,
			ArrayList<String> entityNames, ArrayList<String> forceNames) {
		this.entities = new ArrayList<>(entities);
		this.forceActors = new ArrayList<>(forceActors);
		this.entityNames = new ArrayList<>(entityNames);
		this.forceNames = new ArrayList<>(forceNames);
	}
}
