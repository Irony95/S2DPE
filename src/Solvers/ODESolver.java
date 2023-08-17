package Solvers;


//provides a single stepped function for different ODE solvers
public class ODESolver {
	
	public interface ODEFunction {
		double F(double y, double t);
	}
	
	//abstract definition of Euler Method: https://en.wikipedia.org/wiki/Euler_method
	public static double Euler(double y, double t0, double dt, ODEFunction function) {	
		return dt * function.F(y, t0);	
	}
	//abstract definition of classic Runge-Kutta Method: https://en.wikipedia.org/wiki/Runge%E2%80%93Kutta_methods
	public static double RK4(double y, double t0, double dt, ODEFunction function) {
		double k1 = function.F(y, t0);
		double k2 = function.F(y + dt*(k1/2), t0 + dt/2);
		double k3 = function.F(y + dt*(k2/2), t0 + dt/2);
		double k4 = function.F(y + dt*k3, t0 + dt);
		return (dt/6) * (k1 + 2 * k2 + 2 * k3 + k4);
	}
}
