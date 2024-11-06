package com.itth.os.realtimechart;

import java.io.IOException;
import java.util.Random;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.*;
import org.apache.logging.log4j.*;

import com.itth.os.realtimechart.GanttChart.GanttEvent;
import com.itth.os.realtimechart.GanttChart.GanttEvent.Spawn;

public class GanttChartLauncher extends Application {
	protected final static Logger logger = LogManager.getLogger(GanttChartLauncher.class);

	public static void main(String[] args) {
		Random random = new Random();
		for (int i = 0; i < 128; i++) {
			try( GanttEvent event = GanttEvent.of("Start", true)) {
				try {
					Thread.sleep(random.nextInt(24));
					try(Spawn spawn = event.spawn("E1")) {
						Thread.sleep(random.nextInt(24));
					}
					Thread.sleep(random.nextInt(24));
					try(Spawn spawn = event.spawn("E2")) {
						Thread.sleep(random.nextInt(24));
					}
					Thread.sleep(random.nextInt(24));
				} catch (InterruptedException e) {
					logger.error(e, e);
				}
			}
		}
		}

	@Override
	public void start(Stage primaryStage) throws Exception {
		initialize(primaryStage);
	}

	public GanttChart initialize(Stage primaryStage) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("GanttChart.fxml"));
		Scene scene = loader.load();
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		double stageX = screenBounds.getMaxX() - scene.getWidth();
		double stageY = screenBounds.getMinY();
		primaryStage.setTitle("Gantt Chart");
		primaryStage.setAlwaysOnTop(false);
		primaryStage.initStyle(StageStyle.UNDECORATED);
		primaryStage.setX(stageX);
		primaryStage.setY(stageY);
		primaryStage.setScene(scene);
		primaryStage.show();
		return loader.getController();
	}

}
