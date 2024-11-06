package com.itth.os.realtimechart.playground;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.chart.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class GanttChartExample extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create x and y axes
        NumberAxis xAxis = new NumberAxis();
        CategoryAxis yAxis = new CategoryAxis();

        // Set axis labels
        xAxis.setLabel("Time");
        yAxis.setLabel("Tasks");

        // Create a scatter chart with the axes
        ScatterChart<Number, String> ganttChart = new ScatterChart<>(xAxis, yAxis);

        // Create a data series
        XYChart.Series<Number, String> series = new XYChart.Series<>();

        // Add data points to the series
        series.getData().add(new XYChart.Data<>(0, "Task 1", 3));
        series.getData().add(new XYChart.Data<>(2, "Task 2", 4));
        series.getData().add(new XYChart.Data<>(4, "Task 3", 2));
        series.getData().add(new XYChart.Data<>(6, "Task 4", 5));

        // Add the series to the gantt chart
        ganttChart.getData().add(series);

        // Hide symbols by setting their opacity to 0
        for (XYChart.Data<Number, String> data : series.getData()) {
            data.getNode().setOpacity(0);
        }

        // Create a group to hold the scatter chart and rectangles
        Group root = new Group();

        // Add the scatter chart to the group
        root.getChildren().add(ganttChart);

        // Create rectangles for each task and add them to the group
        for (XYChart.Data<Number, String> data : series.getData()) {
            double startX = xAxis.getDisplayPosition(data.getXValue());
            double endX = xAxis.getDisplayPosition(data.getXValue().doubleValue() + (int) data.getExtraValue());
            double taskY = yAxis.getDisplayPosition(data.getYValue());

            Rectangle rectangle = new Rectangle(startX, taskY - 5, endX - startX, 10);
            rectangle.setFill(Color.BLUE);
            rectangle.setOpacity(0.5);

            root.getChildren().add(rectangle);
        }

        // Create the scene and add the group to it
        Scene scene = new Scene(root, 800, 600);

        // Set the stage properties and display the chart
        primaryStage.setTitle("Gantt Chart Example");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Bind the chart size to the scene size
        ganttChart.prefWidthProperty().bind(scene.widthProperty());
        ganttChart.prefHeightProperty().bind(scene.heightProperty());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
