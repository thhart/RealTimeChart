package com.itth.os.realtimechart;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import com.google.common.collect.EvictingQueue;
import com.google.common.util.concurrent.RateLimiter;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealTimeChart {
	protected final static Logger logger = LogManager.getLogger(GanttChart.class);
	private volatile static RealTimeChart realTimeChart = null;
	private final LineChart<Number, Number> chart;
	private final Random random = new Random();
	//private final XYChart.Series<Number, Number> series = new XYChart.Series<>();
	private final int visibleTimeWindow = 60 * 1000; // Anzahl der Sekunden, die auf der X-Achse angezeigt werden sollen
	@FXML
	public AnchorPane anchorPane;
	@FXML
	public StackPane stackPane;
	boolean dragging = false;
	double lastX = 0;
	private double deltaLast;
	private double deltaValue;
	private long limitLower;
	private long limitUpper;
	private final static EvictingQueue<RealTimeEvent> queue = EvictingQueue.create(128);
	private static final Semaphore semaphore = new Semaphore(0);

	static {
		new Thread(() -> {
			if (realTimeChart == null) {
				synchronized (GanttChart.class) {
					if (realTimeChart == null) {
						final CountDownLatch latch = new CountDownLatch(1);
						new JFXPanel();
						Platform.runLater(() -> {
							try {
								realTimeChart = new RealTimeChartLauncher().initialize(new Stage());
								latch.countDown();
							} catch (Exception e) {
								logger.error(e, e);
							}
						});
						try {
							latch.await(42, TimeUnit.SECONDS);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
				}
			}
			while (true) {
				try {
					if (semaphore.tryAcquire(42, TimeUnit.DAYS)) {
						final RealTimeEvent realTimeEvent;
						synchronized (queue) {
							synchronized (queue) {
								realTimeEvent = queue.poll();
							}
						}
						if (realTimeChart != null && realTimeEvent != null) {
							realTimeChart.add(realTimeEvent);
						}
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}).start();
	}

	public RealTimeChart() {
		// X-Achse mit Zeitansage
		NumberAxis xAxis = new NumberAxis(0, visibleTimeWindow, 1);
		//xAxis.setLabel("Time");
		xAxis.setAutoRanging(false); // Automatisches Skalieren der Achse deaktivieren
		xAxis.setMinorTickCount(10); // Anzahl der kleineren Teilstriche auf 10 reduzieren
		xAxis.setTickUnit(20000);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

		xAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(xAxis) {
			@Override
			public String toString(Number object) {
				LocalDateTime time = LocalDateTime.ofEpochSecond(object.longValue() / 1000, 0, OffsetTime.now().getOffset());
				return formatter.format(time);
			}
		});

		// Y-Achse
		NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel("[ms]");
		yAxis.setAutoRanging(false);
		// Chart erstellen
		chart = new LineChart<>(xAxis, yAxis);
		chart.setTitle("Time Chart");
		chart.setAnimated(false);
		chart.setCreateSymbols(false);
		// chart.setOpacity(0.75);
		// chart.getData().add(series);

		//chart.setOnMouseEntered(event -> chart.getScene().getWindow().setOpacity(1));
		//chart.setOnMouseExited(event -> chart.getScene().getWindow().setOpacity(0.125));

		chart.setOnMousePressed(event -> {
			lastX = event.getX();
			dragging = true;
			chart.setCursor(Cursor.E_RESIZE);
		});

		chart.setOnMouseDragged(event -> {
			if (dragging && event.isPrimaryButtonDown()) {
				double deltaX = event.getX() - lastX;
				double xScale = chart.getXAxis().getScaleX();
				deltaValue = deltaLast + deltaX / xScale * 100;
				deltaValue = Math.min(limitUpper - limitLower - visibleTimeWindow + 1000, deltaValue);
				deltaValue = Math.max(-1000, deltaValue);
			} else {
				// change window position to mouse
				chart.getScene().getWindow().setX(event.getScreenX());
				chart.getScene().getWindow().setY(event.getScreenY());
				// save the position to java user preferences
				final Preferences preferences = Preferences.userNodeForPackage(RealTimeChart.class);
				preferences.putLong("window.x", (long) event.getScreenX());
				preferences.putLong("window.y", (long) event.getScreenY());
                try {
                    preferences.flush();
                } catch (BackingStoreException e) {
                    logger.error(e, e);
                }
            }
		});

		chart.setOnMouseReleased(event -> {
			dragging = false;
			deltaLast = deltaValue;
			chart.setCursor(Cursor.DEFAULT);
		});

		//for (int i = 0; i < 1024; i++) {
		//	series.getData().add(new Data<>(System.currentTimeMillis() - 120 * 1024 + i * 120, random.nextInt(100)));
		//}

		// Chart aktualisieren
		//RateLimiter limiterAdd = RateLimiter.create(0.00010);
		RateLimiter limiterUpdate = RateLimiter.create(12);
		//for (XYChart.Data<Number, Number> data : series.getData()) {
		//	Tooltip tooltip = new Tooltip();
		//	tooltip.setText("X: " + data.getXValue() + "\nY: " + data.getYValue());
		//	Tooltip.install(data.getNode(), tooltip);
		//}

		Platform.runLater(() -> {
					try {
						stackPane.getChildren().add(chart);
					} catch (Exception e) {
						logger.error(e, e);
					}
				}
		);


		Thread thread = new Thread(() -> {
			while (true) {
				limiterUpdate.tryAcquire(42, TimeUnit.DAYS);
				// Zufälligen Wert erzeugen und zur Serie hinzufügen
				//if (limiterAdd.tryAcquire()) {
				//	series.getData().add(new Data<>(System.currentTimeMillis(), random.nextInt(100)));
				//}
				Platform.runLater(() -> {
							try {
								for (Series<Number, Number> series : chart.getData()) {
									while (series.getData().size() > 4096) {
										series.getData().remove(0);
									}
								}
								final List<Data<Number, Number>> collect;
								final ArrayList<Series<Number, Number>> list = new ArrayList<>(chart.getData());
								collect = list.stream().flatMap(series -> series.getData().stream()).collect(Collectors.toList());
								final Data<Number, Number> max = collect.stream().max(Comparator.comparingLong(value -> value.getXValue().longValue())).orElse(new Data<>(System.currentTimeMillis(), 0));
								final Data<Number, Number> min = collect.stream().min(Comparator.comparingLong(value -> value.getXValue().longValue())).orElse(new Data<>(System.currentTimeMillis() - visibleTimeWindow, 0));
								limitUpper = max.getXValue().longValue();
								limitLower = min.getXValue().longValue();
								final double bU = max.getXValue().longValue() - deltaValue;
								xAxis.setUpperBound(bU); // Obere Grenze der Achse aktualisieren
								final double bL = max.getXValue().longValue() - visibleTimeWindow - deltaValue;
								xAxis.setLowerBound(bL); // Untere Grenze der Achse aktualisieren
								final double yMax = list.stream().flatMap(series -> series.getData().stream())
										.filter(data -> (data.getXValue().doubleValue() > bL && data.getXValue().doubleValue() < bU))
										.map(data -> data.getYValue().doubleValue()).max(Double::compareTo).orElse(250D);
								final double yMin = list.stream().flatMap(series -> series.getData().stream())
										.filter(data -> (data.getXValue().doubleValue() > bL && data.getXValue().doubleValue() < bU))
										.map(data -> data.getYValue().doubleValue()).min(Double::compareTo).orElse(0D);
								//final Double yMin = list.stream().flatMap(series -> series.getData().stream())
								//		.filter(data -> (data.getXValue().doubleValue() > bL && data.getXValue().doubleValue() < bU))
								//		.map(data -> data.getYValue().doubleValue()).max(Double::compareTo).orElse(0D);
								final double vMax = Math.round(yMax + yMax / 8);
								yAxis.setTickUnit(Math.round(vMax/10));
								yAxis.setUpperBound(vMax);
								yAxis.setLowerBound(yMin);
								yAxis.setTickUnit(Math.abs(vMax - yMin) / 10);
							} catch (Exception e) {
								logger.error(e, e);
							}
						}
				);
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	@SuppressWarnings("unused")
	public static void send(RealTimeEvent realTimeEvent) {
		synchronized (queue) {
			queue.add(realTimeEvent);
		}
		semaphore.release();
	}

	private final Map<String, Series<Number, Number>> map = new ConcurrentHashMap<>();

	private void add(RealTimeEvent event) {
		final Series<Number, Number> sx = map.computeIfAbsent(event.name, s -> {
			final Series<Number, Number> series = new Series<>();
			series.setName(s);
			Platform.runLater(() -> {
						try {
							chart.getData().add(series);
						} catch (Exception e) {
							logger.error(e, e);
						}
					}
			);
			return series;
		});
		Platform.runLater(() -> {
					try {
							sx.getData().add(new Data<>(event.timeSinceEpochInMillis, event.duration.toMillis()));
					} catch (Exception e) {
						logger.error(e, e);
					}
				}
		);
	}

	@SuppressWarnings("unused")
	public AnchorPane getAnchorPane() {
		return anchorPane;
	}

	@SuppressWarnings("unused")
	public static class RealTimeEvent {
		final protected Duration duration;
		final protected String name;
		final protected long timeSinceEpochInMillis;

		private RealTimeEvent(long timeSinceEpochInMillis, Duration duration, String name) {
			this.timeSinceEpochInMillis = timeSinceEpochInMillis;
			this.duration = duration;
			this.name = name;
		}

		public static RealTimeEvent of(String name, long timeSinceEpochInMillis, Duration duration) {
			return new RealTimeEvent(timeSinceEpochInMillis, duration, name);
		}

		public static RealTimeEvent of(String name, Duration duration) {
			return of(name, System.currentTimeMillis(), duration);
		}

		public static RealTimeEvent of(String name, long timeSinceEpochInMillis, long durationInMilliseconds) {
			return new RealTimeEvent(timeSinceEpochInMillis, Duration.ofMillis(durationInMilliseconds), name);
		}
	}

}

   
