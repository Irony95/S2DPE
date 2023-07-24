package physicsComponents;

import java.io.Serializable;

public class Material implements Serializable {
	private static final long serialVersionUID = -7884084732262999872L;

	public String name;
	
	public double density;
	//negative to be calculated by density
	public double customMass = -1;
	//negative to be calculated by density
	public double customInertia = -1;
	//affects the air resistance, [https://en.wikipedia.org/wiki/Drag_coefficient]
	protected double dragCoefficient = 1;
	
	//affects friction between objects[https://en.wikipedia.org/wiki/Friction]
	public double staticFriction = 0.15f;
	public double dynamicFriction = 0.05f;
	//the change in velocity from collisions, [https://en.wikipedia.org/wiki/Coefficient_of_restitution]
	public double coeffOfRestitution = 0.95;
	public Material(String name, double density, double dragCoefficient, double staticFriction,
			double dynamicFriction, double restitution) {
		this.name = name;
		this.density = density;
		this.dragCoefficient = dragCoefficient;
		this.staticFriction = staticFriction;
		this.dynamicFriction = dynamicFriction;
		this.coeffOfRestitution = restitution;
	}
}
