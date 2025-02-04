module com.itth.os.realtimechart {
	requires com.google.common;
	requires javafx.graphics;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.swing;
	requires org.apache.logging.log4j;
	requires org.apache.commons.collections4;
    requires java.prefs;
    exports com.itth.os.realtimechart;
	exports com.itth.os.realtimechart.playground;
}