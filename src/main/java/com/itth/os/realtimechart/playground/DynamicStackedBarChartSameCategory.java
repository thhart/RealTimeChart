package com.itth.os.realtimechart.playground;

import java.time.Duration;
import java.util.Random;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class DynamicStackedBarChartSameCategory extends Application {

    private final XYChart.Series<Number, String> series = new XYChart.Series<>();
    private final Random random = new Random();

    @Override
    public void start(Stage primaryStage) {
        NumberAxis xAxis = new NumberAxis();
        CategoryAxis yAxis = new CategoryAxis();

        BarChart stackedBarChart = new CustomStackedBarChart(xAxis, yAxis);
        stackedBarChart.setTitle("Dynamic Stacked Bar Chart");
        xAxis.setLabel("Value");
        yAxis.setLabel("Category");

        series.setName("Data");
        stackedBarChart.getData().add(series);

        // Button to add new bars with random gap size, bar value, and width
        Button addButton = new Button("Add Bar");
        addButton.setOnAction(event -> {
            addBar("Category 1", counter, 100);
            counter += 125;
        });

        VBox controlBox = new VBox(addButton);
        controlBox.setPadding(new Insets(10));
        controlBox.setSpacing(10);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(stackedBarChart);
        borderPane.setRight(controlBox);

        Scene scene = new Scene(borderPane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Dynamic Stacked Bar Chart with Gaps");
        primaryStage.show();
    }
    long counter = 100;


    private void addBar(String category, long barStart, long barWidth) {
        XYChart.Data<Number, String> data = new XYChart.Data<>(barWidth, Long.toString(barStart));
        data.setExtraValue(new Period(barStart, Duration.ofNanos(barWidth)));
        series.getData().add(data);
    }

    public static class Period {
        final double start;
        final Duration duration;

        public Period(double start, Duration duration) {
            this.start = start;
            this.duration = duration;
        }
    }

    public static class CustomStackedBarChart extends BarChart<Number, String> {

        public CustomStackedBarChart(NumberAxis xAxis, CategoryAxis yAxis) {
            super(xAxis, yAxis);
        }

        @Override
        protected void layoutPlotChildren() {
            super.layoutPlotChildren();
            for (Series<Number, String> series : getData()) {
                long oY = 0;
                Node first = null;
                for (Data<Number, String> data : series.getData()) {
                    if(first == null) {
                        first = data.getNode();
                    }
                    Period period = (Period) data.getExtraValue();
                    if (data.getNode() != null) {
                        //data.getNode().setLayoutX(period.start);
                        data.getNode().setLayoutY(first.getLayoutY() + (oY));
                        oY += 25;
                        data.getNode().setScaleY(25/data.getNode().getLayoutBounds().getHeight());
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
