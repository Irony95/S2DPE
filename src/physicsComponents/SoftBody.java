package physicsComponents;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import org.apache.commons.math4.legacy.linear.RealVector;
import org.apache.commons.math4.legacy.util.*;

public class SoftBody extends EntityUnit implements Drawable {
	
	ParticleEntity bodyParticles[];
	Spring bodySprings[];

	public SoftBody(double x, double y, double vx, double vy, Material mat,
			double k, double xPoints[], double yPoints[]) {
		super(x, y, vx, vy, mat);
		
		int length = xPoints.length > yPoints.length ? yPoints.length : xPoints.length;
		bodyParticles = new ParticleEntity[length];
		//initialize all the points
		for (int i = 0;i < length;i++) {
			bodyParticles[i] = new ParticleEntity(x + xPoints[i], y + yPoints[i],
					vx, vy, mat, Color.BLUE);
		}
		bodySprings = new Spring[binomial(length, 2)];
		//initialize all the springs, can be optimized for any shape in the future
//		for (int i = 0;i < length;i++) {
//			int nextObject = i+1 == length ? 0 : i+1;
//			double distance = bodyParticles[i].position.getDistance(bodyParticles[nextObject].position);
//			Spring spring = new Spring(bodyParticles[i], bodyParticles[nextObject], k, distance);
//			bodySprings[i] = spring;
//		}
		double distance = bodyParticles[0].position.getDistance(bodyParticles[1].position);
		bodySprings[0] = new Spring(bodyParticles[0], bodyParticles[1], k, distance);
		distance = bodyParticles[0].position.getDistance(bodyParticles[2].position);
		bodySprings[1] = new Spring(bodyParticles[0], bodyParticles[2], k, distance);
		distance = bodyParticles[0].position.getDistance(bodyParticles[3].position);
		bodySprings[2] = new Spring(bodyParticles[0], bodyParticles[3], k, distance);
		
		distance = bodyParticles[1].position.getDistance(bodyParticles[2].position);
		bodySprings[3] = new Spring(bodyParticles[1], bodyParticles[2], k, distance);
		distance = bodyParticles[1].position.getDistance(bodyParticles[3].position);
		bodySprings[4] = new Spring(bodyParticles[1], bodyParticles[3], k, distance);
		
		distance = bodyParticles[2].position.getDistance(bodyParticles[3].position);
		bodySprings[5] = new Spring(bodyParticles[2], bodyParticles[3], k, distance);
	}
	
	@Override
	public void update(double deltaTime) {
		for (int i =0;i < bodySprings.length;i++) {
			bodySprings[i].applyForce();
		} 
		
		for (int i =0;i < bodyParticles.length;i++) {
			bodyParticles[i].addForce(accumulatedForce);
			bodyParticles[i].update(deltaTime);
		}
		resetForce();
	}

	@Override
	public void draw(GraphicsContext gc) {
		// TODO Auto-generated method stub
		for (int i =0;i < bodyParticles.length;i++) {
			bodyParticles[i].draw(gc);
		}
		for (int i =0;i < bodySprings.length;i++) {
			bodySprings[i].draw(gc);
		}
	}

	@Override
	public void drawWireframe(GraphicsContext gc, int index) {
		// TODO Auto-generated method stub
		
	}
	
	static int binomial(final int N, final int K) {
		int ret = 1;
	    for (int k = 0; k < K; k++) {
	        ret = (ret * (N-k)) / (k+1);
	    }
	    return ret;
	}
	
}
