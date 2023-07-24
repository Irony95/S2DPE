module Simple2DPhysicsEngine {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.base;
	requires org.apache.commons.math4.legacy;
	requires javafx.graphics;
	
	opens application to javafx.graphics, javafx.fxml;
}
