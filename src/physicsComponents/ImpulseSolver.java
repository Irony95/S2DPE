package physicsComponents;

import org.apache.commons.math4.legacy.linear.ArrayRealVector;
import org.apache.commons.math4.legacy.linear.RealVector;

import application.EngineProperties;

public class ImpulseSolver {
	public static double EPISILON = 1.0E-10;
	
	public static RealVector[] solveImpulse(EntityUnit objA, EntityUnit objB, RealVector normal) {
		return solveImpulse(objA, objB, normal, null, null, null, null);
	}
	
	public static RealVector[] solveImpulse(EntityUnit objA, EntityUnit objB, RealVector normal,
			PolygonCollider colA, RealVector rA) {
		return solveImpulse(objA, objB, normal, colA, null, rA, null);
	}
	
	/*
	 * Solves for both contact and friction impulse, for 2 objects
	 * */
	public static RealVector[] solveImpulse(EntityUnit objA, EntityUnit objB, RealVector normal,
			PolygonCollider colA, PolygonCollider colB,
			RealVector rA, RealVector rB) {
		
		RealVector [] impulses = new RealVector[2];
		double lowestRestitution = objA.material.coeffOfRestitution < objB.material.coeffOfRestitution ?
				objA.material.coeffOfRestitution : objB.material.coeffOfRestitution;
		RealVector relativeB = rB == null ? objB.velocity : objB.velocity.add(cross(rB, colB.getAngularVelocity()));
		RealVector relativeA = rA == null ? objA.velocity : objA.velocity.add(cross(rA, colA.getAngularVelocity()));
		RealVector rv = relativeB.subtract(relativeA);
		
		double velAlongNormal = rv.dotProduct(normal);
		if (velAlongNormal > 0) { return null; }
		
		double rACrossN = rA == null ? 0 : cross(rA, normal);
		double rBCrossN = rB == null ? 0 :cross(rB, normal);
		
		double invSum = objA.invMass + objB.invMass;
		if (rA != null) { invSum += (rACrossN * rACrossN * colA.getInvInertia()); }
		if (rB != null) { invSum += (rBCrossN * rBCrossN * colB.getInvInertia()); }
		
		double j = -(1 + lowestRestitution) * velAlongNormal;
		j /= invSum;
	
		impulses[0] = normal.mapMultiply(j);
		
		relativeB = rB == null ? objB.velocity : objB.velocity.add(cross(rB, colB.getAngularVelocity()));
		relativeA = rA == null ? objA.velocity : objA.velocity.add(cross(rA, colA.getAngularVelocity()));
		rv = relativeB.subtract(relativeA);
		//tangent to normal relative to relative velocity
		RealVector tangent = rv.subtract(normal.mapMultiply(rv.dotProduct(normal)));
		if (Math.abs(tangent.getEntry(0)) > EPISILON || Math.abs(tangent.getEntry(1)) > EPISILON) {
			tangent = tangent.unitVector();
			double jt = - tangent.dotProduct(rv);
			jt /= invSum;
			
			RealVector frictionImpulse;
			double mu = (objA.material.staticFriction + objB.material.staticFriction) / 2;
			if (Math.abs(jt) < j * mu) {
				frictionImpulse = tangent.mapMultiply(jt);
			}
			else {
				double dynamic = (objA.material.dynamicFriction + objB.material.dynamicFriction) / 2;
				frictionImpulse = tangent.mapMultiply(-j * dynamic);
			}
			impulses[1] = frictionImpulse;
		}
		return impulses;
	}
	
	/*
	 * Solves for only one object, therefore other object is perfect immovable object i.e the canvas
	 * */
	public static RealVector[] solveImpulse(EntityUnit obj, PolygonCollider col, RealVector normal, RealVector rA) {
		RealVector[] impulses = new RealVector[2];
		
		RealVector rv = obj.velocity.mapMultiply(-1);
		if (rA != null) { rv.add(cross(rA, col.getAngularVelocity())); }
		double velAlongNormal = rv.dotProduct(normal);
		
		double rACrossN = rA == null ? 0 : cross(rA, normal);
		double invSum = obj.invMass;
		if (rA != null) { invSum += (rACrossN * rACrossN * obj.invInertia);}
		
		double j = -(1 + obj.material.coeffOfRestitution) * velAlongNormal;
		j /= invSum;
		
		impulses[0] = normal.mapMultiply(j);
		
		rv = obj.velocity.mapMultiply(-1);
		if (rA != null) { rv.add(cross(rA, col.getAngularVelocity())); }
		//tangent to normal relative to relative velocity
		RealVector tangent = rv.subtract(normal.mapMultiply(rv.dotProduct(normal)));
		
		if (Math.abs(tangent.getEntry(0)) > EPISILON || Math.abs(tangent.getEntry(1)) > EPISILON) {
			tangent = tangent.unitVector();
			
			double jt = -tangent.dotProduct(rv);
			jt /= invSum;	
			
			RealVector frictionImpulse;
			double mu = (obj.material.staticFriction);
			if (Math.abs(jt) < j * mu) {
				frictionImpulse = tangent.mapMultiply(jt);
			}
			else {
				double dynamic = (obj.material.dynamicFriction);
				frictionImpulse = tangent.mapMultiply(-j * dynamic);
			}
			impulses[1] = frictionImpulse;
		}
		return impulses;
	}
	
	/*
	 * Moves the objects by a small amount, so objects dont overlap
	 * */
	public static void positionalCorrection(EntityUnit objA, EntityUnit objB,
			double penetration, RealVector collisionNormal) {
		RealVector correction = collisionNormal
				.mapMultiply(penetration / (objA.invMass + objB.invMass) * 0.5);
		objA.position = objA.position.subtract(correction.mapMultiply(objA.invMass));
		objB.position = objB.position.add(correction.mapMultiply(objB.invMass));
	}
	
	public static double cross(RealVector a, RealVector b) {
		return a.getEntry(0) * b.getEntry(1) - a.getEntry(1) * b.getEntry(0);
	}
	
	public static RealVector cross(RealVector a, double b) {
		return new ArrayRealVector(new double[] {
				a.getEntry(1) * -b,
				a.getEntry(0) * b
		});
	}
	
	public static RealVector[] clipPoints(RealVector inc1, RealVector inc2, RealVector normal, double offset) {
		RealVector[] points = new RealVector[2];
		int pointIndex = 0;
		double d1 = normal.dotProduct(inc1) - offset;
		//System.out.println(d1);
		double d2 = normal.dotProduct(inc2) - offset;
		//System.out.println(d2);
		if (d1 >= 0) {points[pointIndex] = inc1; pointIndex++; }
		if (d2 >= 0) {points[pointIndex] = inc2; pointIndex++; }
		
		if (d1* d2 < 0) {
			RealVector e = inc2.subtract(inc1);
			double u = d1/ (d1 - d2);
			e = e.mapMultiply(u);
			e = e.add(inc1);
			points[pointIndex] = e;
		}
		return points;
	}
}
