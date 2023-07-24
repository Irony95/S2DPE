package physicsComponents;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import physicsComponents.*;

public class Joint extends ForceActor implements Drawable {
	private static final long serialVersionUID = -5483592380236254538L;
	ParticleEntity parent;
	PolygonRigidBody child;
	double torque;
	
	public Joint(ParticleEntity parent, PolygonRigidBody child, double torque) {
		this.parent = parent;
		this.child = child;
		this.child.invMass = 0;
		this.child.mass = 0;
		this.torque = torque;
	}

	@Override
	public void applyForce() {
//		child.velocity.setEntry(0, 0);
//		child.velocity.setEntry(1, 0);
		child.position = parent.position;
		
		if (child instanceof PolygonRigidBody) {
			child.torque += torque;
		}
	}

	@Override
	public void draw(GraphicsContext gc) {
		gc.setStroke(Color.RED);
		gc.setLineWidth(2);
		gc.strokeLine(parent.position.getEntry(0) - 5, parent.position.getEntry(1) - 5,
				parent.position.getEntry(0) + 5, parent.position.getEntry(1) + 5);
		gc.strokeLine(parent.position.getEntry(0) + 5, parent.position.getEntry(1) - 5,
				parent.position.getEntry(0) - 5, parent.position.getEntry(1) + 5);
	}

	@Override
	public void drawWireframe(GraphicsContext gc, int index) {
		draw(gc);
	}
}
