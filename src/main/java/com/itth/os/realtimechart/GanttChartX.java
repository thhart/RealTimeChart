package com.itth.os.realtimechart;

import javafx.application.Application;
import javafx.collections.*;
import javafx.geometry.Side;
import javafx.scene.*;
import javafx.scene.chart.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

public class GanttChartX extends Application {

	private XYChart<Number, String> chart;

	@Override
	public void start(Stage primaryStage) {
		CategoryAxis yAxis = new CategoryAxis();
		yAxis.setLabel("Tasks");
		yAxis.setSide(Side.LEFT);

		NumberAxis xAxis = new NumberAxis();
		xAxis.setLabel("Time");
		xAxis.setAutoRanging(false);
		xAxis.setLowerBound(-50);
		xAxis.setUpperBound(50);
		//xAxis.setTickUnit(10);

		chart = new NoSymbolScatterChart<>(xAxis, yAxis);

		XYChart.Series<Number, String> series = new XYChart.Series<>();
		ObservableList<XYChart.Data<Number, String>> dataList = FXCollections.observableArrayList();
		dataList.add(createTask("Task A", 0, 20, Color.LIGHTBLUE));
		//dataList.add(createTask("Task A", 10, 30, Color.ORANGE));
		//dataList.add(createTask("Task A", 20, 40, Color.LIGHTGREEN));
		//dataList.add(createTask("Task A", 30, 50, Color.PINK));

		series.setData(dataList);
		chart.getData().add(series);
		chart.setLegendVisible(false);


		StackPane root = new StackPane(chart);
		Scene scene = new Scene(root, 800, 600);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Gantt Chart Single Row Example");
		primaryStage.show();
	}

	private XYChart.Data<Number, String> createTask(String taskName, int start, int end, Color color) {
		Rectangle rectangle = new Rectangle(0, 0, (end - start), 18);
		rectangle.setFill(color);
		rectangle.setStroke(Color.DARKGRAY);
    rectangle.setTranslateX(120);
		scale(rectangle);
		chart.getXAxis().widthProperty().addListener((observable, oldValue, newValue) -> scale(rectangle));

		XYChart.Data<Number, String> data = new XYChart.Data<>(start, taskName);
		final Node group = new StackPane(rectangle);
		//group.setStyle("-fx-border-color: green;");
		data.setNode(group);

		return data;
	}


    class NoSymbolScatterChart<X, Y> extends ScatterChart<X, Y> {

        public NoSymbolScatterChart(Axis<X> xAxis, Axis<Y> yAxis) {
            super(xAxis, yAxis);
        }

        protected void dataItemAdded(Series<X, Y> series, int itemIndex, Data<X, Y> item) {
            getPlotChildren().add(item.getNode());
        }

    }

	private void scale(Rectangle rectangle) {
		final double v = ((NumberAxis)chart.getXAxis()).lowerBoundProperty().get();
		final double u = ((NumberAxis)chart.getXAxis()).upperBoundProperty().get();
		final double s = chart.getXAxis().widthProperty().get() / Math.abs(u - v);
		rectangle.setScaleX(s);
		rectangle.setStrokeWidth(1 / s);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
