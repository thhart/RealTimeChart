package com.itth.os.realtimechart;

import java.io.IOException;
import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class RealTimeChartLauncher extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		initialize(primaryStage);
	}

	public RealTimeChart initialize(Stage primaryStage) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("RealTimeChart.fxml"));
		Scene scene = loader.load();
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		double stageX = screenBounds.getMaxX() - scene.getWidth();
		double stageY = screenBounds.getMinY();
		primaryStage.setTitle("Time Chart");
		primaryStage.setAlwaysOnTop(true);
		primaryStage.initStyle(StageStyle.UNDECORATED);

		// restore the window position from java user preferences
		final Preferences preferences = Preferences.userNodeForPackage(RealTimeChart.class);
		stageX = preferences.getLong("window.x", (long) stageX);
		stageY = preferences.getLong("window.y", (long) stageY);
		primaryStage.setX(stageX);
		primaryStage.setY(stageY);
		primaryStage.setScene(scene);
		primaryStage.show();
		return loader.getController();
	}

}
