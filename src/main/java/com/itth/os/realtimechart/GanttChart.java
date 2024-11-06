package com.itth.os.realtimechart;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import com.google.common.cache.*;
import com.google.common.collect.EvictingQueue;
import com.google.common.util.concurrent.RateLimiter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.chart.*;
import javafx.scene.chart.XYChart.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.*;

import com.itth.os.realtimechart.GanttChart.GanttEvent.Spawn;

public class GanttChart {
	protected final static Logger logger = LogManager.getLogger(GanttChart.class);
	private volatile static GanttChart ganttChart = null;
	private final XYChart<Number, String> chart;
	private double scale = 1;
	private int visibleTimeWindow = 10 * 1000; // Anzahl der Sekunden, die auf der X-Achse angezeigt werden sollen
	private final NumberAxis xAxis;
	private final CategoryAxis yAxis;
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
	private final static EvictingQueue<GanttEvent> queue = EvictingQueue.create(128);
	private static final Semaphore semaphore = new Semaphore(0);

	private static final Random random = new Random();

	private static final LoadingCache<String, Color> cache = CacheBuilder.newBuilder().build(new CacheLoader<>() {
		public Color load(String s) {
			return new Color(random.nextDouble(), random.nextDouble(), random.nextDouble(), 0.5);
		}
	});

	static {

		new Thread(() -> {
			if (ganttChart == null) {
				synchronized (GanttChart.class) {
					if (ganttChart == null) {
						final CountDownLatch latch = new CountDownLatch(1);
						Platform.runLater(() -> {
							try {
								ganttChart = new GanttChartLauncher().initialize(new Stage());
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
						final GanttEvent ganttEvent;
						synchronized (queue) {
							synchronized (queue) {
								ganttEvent = queue.poll();
							}
						}
						if (ganttChart != null && ganttEvent != null) {
							ganttChart.add(ganttEvent);
						}
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}).start();
	}

	public GanttChart() {
		// X-Achse mit Zeitansage
		xAxis = new NumberAxis(0, visibleTimeWindow, 1);
		xAxis.setAutoRanging(false); // Automatisches Skalieren der Achse deaktivieren
		xAxis.setMinorTickCount(10); // Anzahl der kleineren Teilstriche auf 10 reduzieren
		xAxis.setTickUnit(visibleTimeWindow / 5D);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

		xAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(xAxis) {
			@Override
			public String toString(Number object) {
				LocalDateTime time = LocalDateTime.ofEpochSecond(object.longValue() / 1000, 0, OffsetTime.now().getOffset());
				return formatter.format(time);
			}
		});

		// Y-Achse
		yAxis = new CategoryAxis();
		yAxis.setLabel("Task");
		//yAxis.setAutoRanging(false);
		// Chart erstellen
		chart = new NoSymbolScatterChart<>(xAxis, yAxis);
		chart.setTitle("Gantt Chart");
		chart.setAnimated(false);
		chart.setLegendVisible(false);
		//chart.getData().add(series);


		chart.setOnScroll(event -> {
			visibleTimeWindow = (int)Math.max(10, visibleTimeWindow - event.getDeltaY() * (event.isShiftDown() ? 100 : 1));
			fireCheckBounds();
		});
		
		chart.setOnMousePressed(event -> {
			lastX = event.getX();
			dragging = true;
			chart.setCursor(Cursor.E_RESIZE);
		});

		chart.setOnMouseDragged(event -> {
			if (dragging) {
				double deltaX = event.getX() - lastX;
				double xScale = chart.getXAxis().getScaleX();
				deltaValue = deltaLast + deltaX / xScale / scale * (event.isShiftDown() ? 100 : 10);
				deltaValue = Math.min(limitUpper - limitLower - visibleTimeWindow + 1000, deltaValue);
				deltaValue = Math.max(-1000, deltaValue);
			}
			fireCheckBounds();
		});

		chart.setOnMouseReleased(event -> {
			dragging = false;
			deltaLast = deltaValue;
			chart.setCursor(Cursor.DEFAULT);
			fireCheckBounds();
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


		//Thread thread = new Thread(() -> {
		//	while (true) {
		//		limiterUpdate.tryAcquire(42, TimeUnit.DAYS);
		//		// Zufälligen Wert erzeugen und zur Serie hinzufügen
		//		//if (limiterAdd.tryAcquire()) {
		//		//	series.getData().add(new Data<>(System.currentTimeMillis(), random.nextInt(100)));
		//		//}
		//		fireCheckBounds();
		//	}
		//});
		//thread.setDaemon(true);
		//thread.start();
	}

	private void fireCheckBounds() {
		Platform.runLater(() -> {
					try {
						for (Series<Number, String> series : chart.getData()) {
							while (series.getData().size() > 4096) {
								series.getData().remove(0);
							}
						}
						final List<Data<Number, String>> collect;
						final ArrayList<Series<Number, String>> list = new ArrayList<>(chart.getData());
						collect = list.stream().flatMap(series -> series.getData().stream()).collect(Collectors.toList());
						long max = collect.stream().mapToLong(value -> ((GanttEvent)value.getExtraValue()).getEndInMillis()).max().orElse(System.currentTimeMillis());
						long min = collect.stream().mapToLong(value -> ((GanttEvent)value.getExtraValue()).getStartedInMillis()).min().orElse(System.currentTimeMillis() - visibleTimeWindow);
						limitUpper = max;
						limitLower = min;
						final double bU = max + visibleTimeWindow - deltaValue;
						xAxis.setUpperBound(bU); // Obere Grenze der Achse aktualisieren
						final double bL = max - visibleTimeWindow - deltaValue;
						xAxis.setLowerBound(bL); // Untere Grenze der Achse aktualisieren
						scale = chart.getXAxis().widthProperty().get() / Math.abs(bU - bL);
					} catch (Exception e) {
						logger.error(e, e);
					}
				}
		);
	}

	@SuppressWarnings("unused")
	public static void send(GanttEvent ganttEvent) {
		synchronized (queue) {
			queue.add(ganttEvent);
		}
		semaphore.release();
	}

	private final Map<String, Series<Number, String>> map = new ConcurrentHashMap<>();

	private XYChart.Data<Number, String> createNode(final GanttEvent event) {
		final long start = event.getStartedInMillis();
		XYChart.Data<Number, String> data = new XYChart.Data<>(start, event.name);
		data.setExtraValue(event);
		return data;
	}

	class NoSymbolScatterChart<X, Y> extends ScatterChart<X, Y> {

		public NoSymbolScatterChart(Axis<X> xAxis, Axis<Y> yAxis) {
			super(xAxis, yAxis);
		}

		protected void layoutPlotChildren() {
			super.layoutPlotChildren();
			for (int seriesIndex = 0; seriesIndex < chart.getData().size(); seriesIndex++) {
				Series<Number, String> series = chart.getData().get(seriesIndex);
				for (Data<Number, String> item : series.getData()) {
					double x = xAxis.getDisplayPosition(item.getXValue());
					double y = yAxis.getDisplayPosition(item.getYValue());
					if (Double.isNaN(x) || Double.isNaN(y)) {
						continue;
					}
					final Node symbol = item.getNode();
					if (symbol != null && symbol.getParent() != null && item.getExtraValue() instanceof GanttEvent) {
						final Parent parent = symbol.getParent();
						if (item.getExtraValue() != null) {
							final double v = ((NumberAxis)chart.getXAxis()).lowerBoundProperty().get();
							final double u = ((NumberAxis)chart.getXAxis()).upperBoundProperty().get();
							final double scale = chart.getXAxis().widthProperty().get() / Math.abs(u - v);
							symbol.setVisible(false);
							final GanttEvent event = (GanttEvent)item.getExtraValue();
							addShape(x, y, parent, scale, event.rectangle, event.box);
							for (Spawn spawn : event.spawns) {
								addShape(x, y, parent, scale, spawn.rectangle, spawn.box);
								if(!parent.getChildrenUnmodifiable().contains(spawn.text) && parent instanceof Group) {
									spawn.text.translateXProperty().bind(spawn.rectangle.translateXProperty());
									spawn.text.translateYProperty().bind(spawn.rectangle.translateYProperty().subtract(2));
									((Group)parent).getChildren().add(spawn.text);
								}
								try {
									spawn.rectangle.setFill(cache.get(event.name + spawn.name));
								} catch (ExecutionException e) {
									logger.error(e, e);
								}
							}
						}
					}
				}
				fireCheckBounds();
			}

		}

		private void addShape(double x, double y, Parent parent, double s, final Rectangle rectangle, final BoundingBox boundingBox) {
			final double width = Math.max(2, boundingBox.getWidth() * s);
			final double height = Math.max(2, boundingBox.getHeight());
			rectangle.setX(0);
			rectangle.setY(0);
			rectangle.setTranslateX(x + boundingBox.getMinX() * s);
			rectangle.setTranslateY(y - height/2);
			rectangle.setWidth(width);
			rectangle.setHeight(height);
			if(!parent.getChildrenUnmodifiable().contains(rectangle) && parent instanceof Group) {
				((Group)parent).getChildren().add(rectangle);
			}
		}
	}

	private void add(GanttEvent event) {
		final Series<Number, String> sx = map.computeIfAbsent(event.name, s -> {
			final Series<Number, String> series = new Series<>();
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
						sx.getData().add(createNode(event));
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
	public static class GanttEvent implements AutoCloseable {
		private final List<Spawn> spawns = new CopyOnWriteArrayList<>();
		private final String name;
		private final long started;
		public Rectangle rectangle;
		private BoundingBox box;
		private long end = 0;
		private boolean send = false;
		private Spawn spawn = null;

		private GanttEvent(String name, long started) {
			this.name = name;
			this.started = started;
		}

		public static GanttEvent of(String name) {
			return of(name, System.currentTimeMillis());
		}

		public static GanttEvent of(String name, boolean send) {
			final GanttEvent event = of(name, System.currentTimeMillis());
			event.send = send;
			return event;
		}

		public static GanttEvent of(String name, long started) {
			return new GanttEvent(name, started);
		}

		public void close() {
			spawns.forEach(Spawn::close);
			if (send) {
				GanttChart.send(this);
			}
			end = System.currentTimeMillis();
			rectangle = new Rectangle(Math.max(2, getEndInMillis() - started), 18);
			rectangle.getStyleClass().add("rectangle");
			box = new BoundingBox(0, 0, getEndInMillis() - started, 16);
		}

		public long getEndInMillis() {
			if(end == 0) {
				logger.warn("not closed");
				close();
			}
			return spawns.stream().map(spawn -> spawn.start + spawn.duration.toMillis()).max(Long::compareTo).orElse(started);
		}

		public long getStartedInMillis() {
			return spawns.stream().map(spawn -> spawn.start).min(Long::compareTo).orElse(started);
		}

		public Spawn spawn() {
			return spawn("");
		}

		public Spawn spawn(String name) {
			if(spawn != null) {
				spawn.close();
			}
			spawn = new Spawn(name);
			return spawn;
		}

		public class Spawn implements AutoCloseable {
			private final long start;
			private Rectangle rectangle;
			private BoundingBox box;
			private Duration duration = null;
			private final String name;
			private final AtomicBoolean closed = new AtomicBoolean(false);
			private Text text;

			public Spawn() {
				this("");
			}

			public Spawn(String name) {
				this.start = System.currentTimeMillis();
				this.name = name;
				spawns.add(this);
			}

			public void close() {
				if (closed.compareAndSet(false, true)) {
					duration = Duration.ofMillis(System.currentTimeMillis() - start);
					box = new BoundingBox(start - started, 0, duration.toMillis(), 16);
					rectangle = new Rectangle(start -  - started, 0, duration.toMillis(), 16);
					text = new Text(name + ": " + duration.toMillis() + " ms");
					text.setFont(new Font(text.getFont().getName(), 8));
					text.setFill(Color.BLACK);
				}
			}
		}
	}
}


