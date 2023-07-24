package application;
import java.io.Serializable;
import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import physicsComponents.*;

public class SceneObjects {
	private static SceneObjects INSTANCE = null;
	public ArrayList<EntityUnit> entities = new ArrayList<EntityUnit>();
	public ArrayList<ForceActor> forceActors = new ArrayList<ForceActor>();
	public ArrayList<Material> materials = new ArrayList<Material>();
	
    private ListView<String> lvEntities;

    private ListView<String> lvForces;
    
	public static SceneObjects getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new SceneObjects();
		}
		return INSTANCE;
	}
	
	public void setEntityListView(ListView<String> view) { lvEntities = view; }
	public void setForceListView(ListView<String> view) { lvForces = view; }
	
	public void addEntity(String name, EntityUnit unit) {
		entities.add(unit);
		lvEntities.getItems().add(name);
	}
	
	public void deleteEntity(int index) {
		entities.remove(index);
		lvEntities.getItems().remove(index);
	}
	
	public void deleteForce(int index) {
		forceActors.remove(index);
		lvForces.getItems().remove(index);
	}
	
	public void addForce(String name, ForceActor actor) {
		forceActors.add(actor);
		lvForces.getItems().add(name);	
	}
	
	public String getEntityName(int index) {
		return lvEntities.getItems().get(index);
	}
}
