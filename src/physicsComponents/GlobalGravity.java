package physicsComponents;

import java.util.ArrayList;

import org.apache.commons.math4.legacy.linear.ArrayRealVector;
import org.apache.commons.math4.legacy.linear.RealVector;

import application.EngineProperties;
import application.SceneObjects;

public class GlobalGravity extends ForceActor {
	private static final long serialVersionUID = 2741073630413519563L;
	ArrayList<EntityUnit> objects;
	RealVector gravity;
	
	public GlobalGravity(double fx, double fy, ArrayList<EntityUnit> objects) {
		gravity = new ArrayRealVector(new double[] {fx, fy});
		this.objects = objects;
	}
	
	@Override
	public void applyForce() {
		for (int i = 0;i < objects.size();i ++) {
			EntityUnit object = objects.get(i);
			object.addForce(gravity.mapMultiply(object.mass));			
		}
	}

}
