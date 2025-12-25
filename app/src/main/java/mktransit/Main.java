package mktransit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application {

    private double scale = 1.5;
    private final double minScale = 1.65;
    private final double maxScale = 7;

    @Override
    public void start(Stage stage) {

        JsonReader reader = new JsonReader();
        reader.loadJsonData(); // แค่โหลด

        Map<String, Station> stationMap = reader.getStationMap(); // ดึงข้อมูล Station

        PathFinder pathFinder = new PathFinder(stationMap);

        // โหลดสถานีมาจาก JsonReader
        List<Station> stationList = new ArrayList<>(reader.getStationMap().values());
        StationUtil stationUtil = new StationUtil(stationList);

        HBox root = new HBox();

        Scene scene = new Scene(root, 1530, 790);

        Image appIcon = new Image(getClass().getResource("/app_icon.png").toExternalForm());
        stage.getIcons().add(appIcon);

        // ---------- LEFT ----------
        StackPane leftPane = new StackPane();
        leftPane.setPrefWidth(150);

        // Map image
        Image image = new Image(getClass().getResource("/Map.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.fitWidthProperty().bind(leftPane.widthProperty().multiply(0.5)); // ปรับขนาดเริ่มต้นอิงขนาด pane
        imageView.setStyle(
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.5, 0, 5); -fx-background-radius: 10;");
        Group zoomGroup = new Group(imageView);

        
        zoomGroup.setScaleX(minScale);
        zoomGroup.setScaleY(minScale);
        scale = minScale;

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(leftPane.widthProperty());
        clip.heightProperty().bind(leftPane.heightProperty());
        leftPane.setClip(clip);

        final double[] mouseAnchorX = new double[1];
        final double[] mouseAnchorY = new double[1];
        final double[] translateAnchorX = new double[1];
        final double[] translateAnchorY = new double[1];

        zoomGroup.setOnMousePressed(event -> {
            mouseAnchorX[0] = event.getSceneX();
            mouseAnchorY[0] = event.getSceneY();
            translateAnchorX[0] = zoomGroup.getTranslateX();
            translateAnchorY[0] = zoomGroup.getTranslateY();
        });

        zoomGroup.setOnMouseDragged(event -> {
            if (scale > 5.0) {
                double deltaX = event.getSceneX() - mouseAnchorX[0];
                double deltaY = event.getSceneY() - mouseAnchorY[0];
                zoomGroup.setTranslateX(translateAnchorX[0] + deltaX);
                zoomGroup.setTranslateY(translateAnchorY[0] + deltaY);
            }
        });


        imageView.setOnScroll((ScrollEvent event) -> {
            double zoomFactor = 1.1;
            double deltaY = event.getDeltaY();

            double oldScale = scale;
            if (deltaY < 0) {
                // ซูมออก: fix กลับศูนย์กลาง
                scale /= zoomFactor;
            } else {
                // ซูมเข้า: ซูมตามเมาส์
                scale *= zoomFactor;
            }

            scale = Math.max(minScale, Math.min(scale, maxScale));
            double factor = scale / oldScale;

            zoomGroup.setScaleX(scale);
            zoomGroup.setScaleY(scale);

            if (deltaY < 0) {
                // ซูมออก: reset กลับศูนย์กลาง
                zoomGroup.setTranslateX(0);
                zoomGroup.setTranslateY(0);
            } else {
                // ซูมเข้า: zoom ตามเมาส์
                Bounds bounds = zoomGroup.localToScene(zoomGroup.getBoundsInLocal());
                double dx = event.getSceneX() - (bounds.getMinX() + bounds.getWidth() / 2);
                double dy = event.getSceneY() - (bounds.getMinY() + bounds.getHeight() / 2);

                zoomGroup.setTranslateX(zoomGroup.getTranslateX() - (factor - 1) * dx);
                zoomGroup.setTranslateY(zoomGroup.getTranslateY() - (factor - 1) * dy);
            }

            event.consume();
        });

        leftPane.getChildren().add(zoomGroup);

        // ---------- RIGHT ----------
        VBox rightPane = new VBox(20);
        rightPane.setPrefWidth(750);
        rightPane.setStyle("-fx-padding: 50 100 20 100; -fx-alignment: center;"); // Top Right Bottom Left ,Padding
                                                                                  // และจัดให้อยู่ตรงกลาง

        // Logo
        Image logoImage = new Image(getClass().getResource("/logo2.png").toExternalForm());
        ImageView logoView = new ImageView(logoImage);
        logoView.setFitWidth(250); // กำหนดขนาดโลโก้
        logoView.setPreserveRatio(true);

        // Group for Project Name and TextFields
        VBox contentBox = new VBox(15); // ระยะห่างระหว่างองค์ประกอบในกรอบ
        contentBox.setStyle(
                "-fx-border-width: 2; -fx-padding: 0 0 25 0 ; -fx-background-color: #f9f9f9;-fx-alignment: center; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.5, 0, 5); -fx-background-radius: 10;"); // กำหนดกรอบและพื้นหลัง

        StackPane bgName = new StackPane();
        bgName.setStyle("-fx-background-color: #003366; -fx-padding: 10;"); // กำหนดกรอบและพื้นหลัง

        // Project Name
        Label projectName = new Label("MK Transit");
        projectName.setStyle("-fx-text-fill: white; -fx-font-size: 50px; -fx-font-weight: bold;");

        // TextField1
        Label inputLabel1 = new Label("Enter Start Station ID:");
        inputLabel1.setStyle("-fx-text-fill: #003366;-fx-font-weight: bold;-fx-font-size: 13px;");
        TextField textField1 = new TextField();
        textField1.setPromptText("Ex. N24");
        textField1.setMaxWidth(60);

        
        Label stationName1 = new Label();
        stationName1.setStyle("-fx-text-fill: #003366; -fx-font-size: 13px; -fx-font-style: italic;");

        Circle circleStation1 = new Circle(6);

        // จัด TextField และ Label ในแนวนอน
        HBox textField1Box = new HBox(10); 
        textField1Box.setStyle("-fx-alignment: center;"); // จัดให้อยู่ชิดซ้าย
        textField1Box.getChildren().addAll(textField1);

        // เพิ่ม Listener ให้ TextField1
        textField1.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                stationName1.setText("");
                textField1.setStyle(""); // ล้างชื่อสถานีหากไม่มีการป้อนข้อมูล
                textField1Box.getChildren().remove(stationName1);
                textField1Box.getChildren().remove(circleStation1);
                return;
            }

            String stationId1 = newValue.toUpperCase();
            Station someStation1 = stationMap.get(stationId1); // ดึงข้อมูลสถานีจาก map

            if (!textField1Box.getChildren().contains(stationName1)) {
                textField1Box.getChildren().add(1, stationName1);
            }

            if (someStation1 == null) {
                stationName1.setText("Station not found"); 
                textField1.setStyle("-fx-border-color: red;");
                textField1Box.getChildren().remove(circleStation1);
            } else {
                stationName1.setText(someStation1.getName());
                textField1.setStyle(""); 

                if (!textField1Box.getChildren().contains(circleStation1)) {
                    textField1Box.getChildren().add(1, circleStation1);
                }

                if (someStation1.getId().equals("CEN")) {
                    circleStation1.setFill(new LinearGradient(
                            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                            new Stop(0, Color.web("#84c469")), 
                            new Stop(1, Color.web("#328674")) 
                    ));
                } else {
                    
                    circleStation1.setFill(Color.web(someStation1.getColor()));
                }
            }
        });

        // TextField2
        Label inputLabel2 = new Label("Enter Terminal Station ID:");
        inputLabel2.setStyle("-fx-text-fill: #003366;-fx-font-weight: bold;-fx-font-size: 13px;");
        TextField textField2 = new TextField();
        textField2.setPromptText("Ex. N24");
        textField2.setMaxWidth(60);

        
        Label stationName2 = new Label();
        stationName2.setStyle("-fx-text-fill: #003366; -fx-font-size: 13px; -fx-font-style: italic;");

        Circle circleStation2 = new Circle(6);

        // จัด TextField และ Label ในแนวนอน
        HBox textField2Box = new HBox(10); 
        textField2Box.setStyle("-fx-alignment: center;"); // จัดให้อยู่ชิดซ้าย
        textField2Box.getChildren().addAll(textField2);

        // เพิ่ม Listener ให้ TextField1
        textField2.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                stationName2.setText("");
                textField2.setStyle(""); // ล้างชื่อสถานีหากไม่มีการป้อนข้อมูล
                textField2Box.getChildren().remove(stationName2);
                textField2Box.getChildren().remove(circleStation2);
                return;
            }

            String stationId2 = newValue.toUpperCase();
            Station someStation2 = stationMap.get(stationId2); 

            if (!textField2Box.getChildren().contains(stationName2)) {
                textField2Box.getChildren().add(1, stationName2);
            }

            if (someStation2 == null) {
                stationName2.setText("Station not found");
                textField2.setStyle("-fx-border-color: red;");
                textField2Box.getChildren().remove(circleStation2);
            } else {
                stationName2.setText(someStation2.getName()); 
                textField2.setStyle(""); 

                if (!textField2Box.getChildren().contains(circleStation2)) {
                    textField2Box.getChildren().add(1, circleStation2); 
                }
                if (someStation2.getId().equals("CEN")) {
                    
                    circleStation2.setFill(new LinearGradient(
                            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                            new Stop(0, Color.web("#84c469")), 
                            new Stop(1, Color.web("#328674")) 
                    ));
                } else {
                    
                    circleStation2.setFill(Color.web(someStation2.getColor()));
                }
            }
        });

        bgName.getChildren().addAll(projectName);

        // วงกลม 3 อัน
        VBox circleBox = new VBox(4); 
        circleBox.setStyle("-fx-alignment: center;"); 

        Circle circle1 = new Circle(5); 
        circle1.setStyle("-fx-fill: #003366;"); 

        Circle circle2 = new Circle(5); 
        circle2.setStyle("-fx-fill: #003366;");

        Circle circle3 = new Circle(5); 
        circle3.setStyle("-fx-fill: #003366;");

    
        circleBox.getChildren().addAll(circle1, circle2, circle3);

        // Button
        Button submitButton = new Button("Submit");
        submitButton.setStyle(
                "-fx-background-color: #003366; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15; -fx-border-radius: 5; -fx-background-radius: 5;");

        Button clearButton = new Button("Clear");
        clearButton.setStyle(
                "-fx-background-color:rgb(196, 0, 0); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15; -fx-border-radius: 5; -fx-background-radius: 5;");

        // Add action for buttons
        submitButton.setOnAction(event -> {
            String startId = textField1.getText().trim(); // ดึงค่า Start Station ID
            String endId = textField2.getText().trim(); // ดึงค่า End Station ID

            startId = startId.toUpperCase();
            endId = endId.toUpperCase();

            if (startId.isEmpty() || endId.isEmpty()) {
                System.out.println("กรุณากรอกข้อมูลให้ครบถ้วน!"); // แสดงข้อความเมื่อไม่มีการป้อนข้อมูล
                return;
            }

            // สร้างหน้าถัดไป
            HBox nextPage = new HBox(30);
            nextPage.setStyle("-fx-alignment: center; -fx-padding: 20;");
            nextPage.setPrefSize(1530, 790); 

            // ---------- ซ้าย ----------
            VBox leftPane1 = new VBox(20);
            leftPane1.setStyle("-fx-alignment: center; -fx-padding: 20;");
            leftPane1.setPrefWidth(750); 

            // Logo
            ImageView logoView1 = new ImageView(logoImage);
            logoView1.setFitWidth(250); // กำหนดขนาดโลโก้
            logoView1.setPreserveRatio(true);

            // Project Name
            Label projectName1 = new Label("MK Transit");
            projectName1.setStyle("-fx-text-fill: #003366; -fx-font-size: 50px; -fx-font-weight: bold;");

            // เพิ่มองค์ประกอบในฝั่งซ้าย
            leftPane1.getChildren().addAll(logoView1, projectName1);

            VBox rightPane1 = new VBox(30);
            rightPane1.setStyle("-fx-alignment: center; -fx-padding: 20;");
            rightPane1.setPrefWidth(750);

            VBox PathBox1 = new VBox(7); // ใช้ VBox ที่ถูกต้อง
            PathBox1.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-border-width: 2; -fx-padding: 20 0 20 25; "
                    + "-fx-background-color: #f9f9f9; -fx-alignment: left; "
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.5, 0, 5); -fx-background-radius: 10;");
            PathBox1.setMaxWidth(400);

            Label infoLabel = new Label("Travel Information:");
            infoLabel.setStyle(
                    "-fx-text-fill: #003366; -fx-font-size: 15px; -fx-font-weight: bold; -fx-alignment: left;");

            Label startIdLabel;
            Label endIdLabel;

            VBox infoBox = new VBox(10); 
            infoBox.setStyle("-fx-alignment: left; -fx-padding: 20; -fx-border-width: 2; -fx-padding: 0 0 25 0; "
                    + "-fx-background-color: #f9f9f9; -fx-alignment: left; "
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.5, 0, 5); -fx-background-radius: 7;");

            Button backButton = new Button("Back to Main Menu");
            backButton.setStyle("-fx-background-color: #003366; -fx-text-fill: white; -fx-font-weight: bold;");
            VBox.setVgrow(backButton, Priority.ALWAYS);


            backButton.setOnAction(e -> {
                stage.setScene(scene); 
            });

            rightPane1.getChildren().addAll(PathBox1, backButton);
            nextPage.getChildren().addAll(leftPane1, rightPane1);

            PathResult result = pathFinder.findShortestPath(startId, endId);
            int i = 0;

            if (result.getFullPath().isEmpty()) {
                Label errorLabel = new Label(
                        "❌ Route not found from " + stationUtil.IDtoName(startId) + " (" + startId + ")" + " to "
                                + stationUtil.IDtoName(endId) + " (" + endId + ")");
                errorLabel.setStyle(
                        "-fx-text-fill: #003366; -fx-font-size: 15px; -fx-alignment: left; -fx-text-fill: red;");
                PathBox1.setMaxWidth(600);
                PathBox1.getChildren().add(errorLabel);

            } else {
                System.out.println("✅ เจอเส้นทาง!");
                System.out.println("เส้นทางเดินทั้งหมด:");

                for (String stationId : result.getFullPath()) {
                    Station station = stationMap.get(stationId);
                    System.out.println("- " + station.getName() + " (" + station.getId() + ")");
                    i++;
                }

                List<String> fullPath = result.getFullPath();

                // Fake Path
                int currentIndex = fullPath.indexOf(startId);
                String nextStationId = fullPath.get(currentIndex + 1);
                Station nextStation = stationMap.get(nextStationId);

                PathResult resultFake = pathFinder.findShortestPath(nextStationId, endId);
                List<String> fullPathFake = resultFake.getFullPath();

                List<String> importantSteps = PathUtil.filterImportantStepsWithActualTransfers(fullPath, stationMap);
                List<String> importantStepsFake = PathUtil.filterImportantStepsWithActualTransfers(fullPathFake,
                        stationMap);

                VBox circleBox1 = new VBox(2); 
                circleBox.setStyle("-fx-alignment: center;");

                Circle circle11 = new Circle(3); 
                circle11.setStyle("-fx-fill:rgb(203, 203, 203);");

                Circle circle21 = new Circle(3);
                circle21.setStyle("-fx-fill:rgb(203, 203, 203);"); 

                Circle circle31 = new Circle(3);
                circle31.setStyle("-fx-fill:rgb(203, 203, 203);");

                circleBox1.getChildren().addAll(circle11, circle21, circle31);

                Circle circleStation11 = new Circle(7);
                Circle circleStation21 = new Circle(7);

                HBox startStationBox = new HBox(5);
                startStationBox.setSpacing(5);
                startStationBox.setStyle("-fx-alignment: center-left;");
                HBox endStationBox = new HBox(5);
                endStationBox.setSpacing(5);
                endStationBox.setStyle("-fx-alignment: center-left;");

                Label intro1 = new Label("Start Station:");
                Label intro2 = new Label("Terminal Station:");

                Station checkStation = stationMap.get(startId);
                if (importantSteps.isEmpty()) {
                    Station someStation = stationMap.get(startId);

                    startIdLabel = new Label("⚲ " + stationUtil.IDtoName(startId) + " (" + startId + ")");
                    startIdLabel.setStyle("-fx-text-fill: #003366; -fx-font-size: 15px; -fx-alignment: left;");

                    if (someStation.getId().equals("CEN")) {
                        circleStation11.setFill(new LinearGradient(
                                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                                new Stop(0, Color.web("#84c469")),
                                new Stop(1, Color.web("#328674"))
                        ));
                    } else {
                        circleStation11.setStyle("-fx-fill: " + someStation.getColor() + ";");
                    }

                    startStationBox.getChildren().addAll(startIdLabel, circleStation11);
                    someStation = stationMap.get(endId);
                    endIdLabel = new Label("⚲ " + stationUtil.IDtoName(endId) + " (" + endId + ")");
                    endIdLabel.setStyle("-fx-text-fill: #003366; -fx-font-size: 15px; -fx-alignment: left;");

                    if (someStation.getId().equals("CEN")) {
                        circleStation21.setFill(new LinearGradient(
                                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                                new Stop(0, Color.web("#84c469")),
                                new Stop(1, Color.web("#328674")) 
                        ));
                    } else {
                        circleStation21.setStyle("-fx-fill: " + someStation.getColor() + ";");
                    }

                    endStationBox.getChildren().addAll(endIdLabel, circleStation21);

                    VBox routeInfoBox = new VBox(10); 
                    routeInfoBox.setStyle(
                            "-fx-alignment: left; -fx-padding: 20; -fx-border-width: 2; -fx-padding: 10 25 10 10; "
                                    + "-fx-background-color:rgb(239, 239, 239); "
                                    + "-fx-background-radius: 5;");
                    // start
                    routeInfoBox.setMaxWidth(350);
                    HBox startBox = new HBox(10);
                    someStation = stationMap.get(startId);
                    Label startLabel = new Label(stationUtil.IDtoName(startId) + " (" + startId + ")");
                    startLabel.setStyle("-fx-text-fill: #003366; -fx-font-size: 14px;");
                    Circle startCircle = new Circle(7);

                    if (someStation.getId().equals("CEN")) {

                        startCircle.setFill(new LinearGradient(
                                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                                new Stop(0, Color.web("#84c469")), 
                                new Stop(1, Color.web("#328674"))
                        ));
                    } else {
                        startCircle.setStyle("-fx-fill: " + someStation.getColor() + ";");
                    }

                    startBox.setStyle("-fx-alignment: center-left;");
                    startBox.getChildren().addAll(startCircle, startLabel);
                    Label rec = new Label("Recommend Route:");
                    Label a = new Label("No interchanges required | Total stations: " + i);
                    rec.setStyle("-fx-text-fill:rgb(132, 132, 132); -fx-font-size: 13px; -fx-font-weight: bold;");
                    a.setStyle("-fx-text-fill: #003366; -fx-font-size: 13px; -fx-font-weight: bold;");
                    routeInfoBox.getChildren().addAll(rec, a, startBox);

                    if (checkStation.isInterchange() == true
                            && !(checkStation.getColor().equals(nextStation.getColor()))) {
                        Label emoji = new Label("🚶");
                        routeInfoBox.getChildren().add(emoji);
                        HBox betweenBox = new HBox(10);
                        someStation = stationMap.get(nextStationId);
                        Label betweenLabel = new Label(
                                stationUtil.IDtoName(nextStationId) + " (" + nextStationId + ")");
                        betweenLabel.setStyle("-fx-text-fill: #003366; -fx-font-size: 14px;");
                        Circle betweenCircle = new Circle(7);

                        if (someStation.getId().equals("CEN")) {
                            
                            betweenCircle.setFill(new LinearGradient(
                                    0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                                    new Stop(0, Color.web("#84c469")), 
                                    new Stop(1, Color.web("#328674")) 
                            ));
                        } else {
                            
                            betweenCircle.setStyle("-fx-fill: " + someStation.getColor() + ";");
                        }

                        betweenBox.setStyle("-fx-alignment: center-left;");
                        betweenBox.getChildren().addAll(betweenCircle, betweenLabel);
                        routeInfoBox.getChildren().add(betweenBox);
                    }
                    HBox endBox = new HBox(10);
                    someStation = stationMap.get(endId);
                    Label endLabel = new Label(stationUtil.IDtoName(endId) + " (" + endId + ")");
                    endLabel.setStyle("-fx-text-fill: #003366; -fx-font-size: 14px;");
                    Circle endCircle = new Circle(7);

                    if (someStation.getId().equals("CEN")) {
                        
                        endCircle.setFill(new LinearGradient(
                                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                                new Stop(0, Color.web("#84c469")), 
                                new Stop(1, Color.web("#328674")) 
                        ));
                    } else {
                        
                        endCircle.setStyle("-fx-fill: " + someStation.getColor() + ";");
                    }

                    endBox.setStyle("-fx-alignment: center-left;");
                    endBox.getChildren().addAll(endCircle, endLabel);
                    routeInfoBox.getChildren().add(endBox);
                    PathBox1.getChildren().addAll(infoLabel, intro1, startStationBox, circleBox1, intro2,
                            endStationBox, routeInfoBox);

                } else {
                    boolean isLastStationDisplayed = false;
                    Station someStation = stationMap.get(startId);
                    startIdLabel = new Label("⚲ " + stationUtil.IDtoName(startId) + " (" + startId + ")");
                    startIdLabel.setStyle("-fx-text-fill: #003366; -fx-font-size: 15px; -fx-alignment: left;");

                    if (someStation.getId().equals("CEN")) {
                        
                        circleStation11.setFill(new LinearGradient(
                                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                                new Stop(0, Color.web("#84c469")), 
                                new Stop(1, Color.web("#328674")) 
                        ));
                    } else {
                        
                        circleStation11.setStyle("-fx-fill: " + someStation.getColor() + ";");
                    }

                    startStationBox.getChildren().addAll(startIdLabel, circleStation11);
                    someStation = stationMap.get(endId);
                    endIdLabel = new Label("⚲ " + stationUtil.IDtoName(endId) + " (" + endId + ")");
                    endIdLabel.setStyle("-fx-text-fill: #003366; -fx-font-size: 15px; -fx-alignment: left;");

                    if (someStation.getId().equals("CEN")) {
                        
                        circleStation21.setFill(new LinearGradient(
                                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                                new Stop(0, Color.web("#84c469")), 
                                new Stop(1, Color.web("#328674")) 
                        ));
                    } else {
                        
                        circleStation21.setStyle("-fx-fill: " + someStation.getColor() + ";");
                    }

                    endStationBox.getChildren().addAll(endIdLabel, circleStation21);

                    VBox routeInfoBox = new VBox(10); 
                    routeInfoBox.setStyle(
                            "-fx-alignment: left; -fx-padding: 20; -fx-border-width: 2; -fx-padding: 10 25 10 10; "
                                    + "-fx-background-color:rgb(239, 239, 239); "
                                    + "-fx-background-radius: 5;");
                    routeInfoBox.setMaxWidth(350);

                    someStation = stationMap.get(startId);
                    HBox startBox = new HBox(10);
                    Label startLabel = new Label(stationUtil.IDtoName(startId) + " (" + startId + ")");
                    startLabel.setStyle("-fx-text-fill: #003366; -fx-font-size: 14px;");
                    Circle startCircle = new Circle(7);

                    if (someStation.getId().equals("CEN")) {
                        
                        startCircle.setFill(new LinearGradient(
                                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                                new Stop(0, Color.web("#84c469")), 
                                new Stop(1, Color.web("#328674")) 
                        ));
                    } else {
                        
                        startCircle.setStyle("-fx-fill: " + someStation.getColor() + ";");
                    }

                    startBox.setStyle("-fx-alignment: center-left;");
                    startBox.getChildren().addAll(startCircle, startLabel);
                    Label rec = new Label("Recommend Route:");
                    Label a = new Label("This route has interchanges | Total stations: " + i);
                    rec.setStyle("-fx-text-fill:rgb(132, 132, 132); -fx-font-size: 13px; -fx-font-weight: bold;");
                    a.setStyle("-fx-text-fill: #003366; -fx-font-size: 13px; -fx-font-weight: bold;");
                    routeInfoBox.getChildren().addAll(rec, a, startBox);

                    if (checkStation.isInterchange() == true
                            && !(checkStation.getColor().equals(nextStation.getColor()))) {
                        Label emoji1 = new Label("🚶");
                        routeInfoBox.getChildren().add(emoji1);
                        HBox betweenBox = new HBox(10);
                        someStation = stationMap.get(nextStationId);
                        Label betweenLabel = new Label(
                                stationUtil.IDtoName(nextStationId) + " (" + nextStationId + ")");
                        betweenLabel.setStyle("-fx-text-fill: #003366; -fx-font-size: 14px;");
                        Circle betweenCircle = new Circle(7);

                        if (someStation.getId().equals("CEN")) {
                            
                            betweenCircle.setFill(new LinearGradient(
                                    0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                                    new Stop(0, Color.web("#84c469")), 
                                    new Stop(1, Color.web("#328674")) 
                            ));
                        } else {
                            
                            betweenCircle.setStyle("-fx-fill: " + someStation.getColor() + ";");
                        }

                        betweenBox.setStyle("-fx-alignment: center-left;");
                        betweenBox.getChildren().addAll(betweenCircle, betweenLabel);
                        routeInfoBox.getChildren().add(betweenBox);

                        for (int j = 0; j < importantStepsFake.size(); j++) {
                            String step = importantStepsFake.get(j);
                            String[] parts = step.split("->");
                            String fromId = parts[0];
                            String toId = parts[1];

                            String fromName = stationUtil.IDtoName(fromId);
                            String toName = stationUtil.IDtoName(toId);

                            // สร้าง Circle สำหรับสถานีต้นทาง
                            Station fromStation = stationMap.get(fromId);
                            Circle fromCircle = new Circle(7); 
                            if (fromStation != null) {
                                if (fromStation.getId().equals("CEN")) {
                                    
                                    fromCircle.setFill(new LinearGradient(
                                            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                                            new Stop(0, Color.web("#84c469")), 
                                            new Stop(1, Color.web("#328674")) 
                                    ));
                                } else {
                                    
                                    fromCircle.setStyle("-fx-fill: " + fromStation.getColor() + ";");
                                }
                            } else {
                                fromCircle.setStyle("-fx-fill: transparent;"); 
                            }

                            
                            Label fromLabel = new Label(fromName + " (" + fromId + ")");
                            fromLabel.setStyle("-fx-text-fill: #003366; -fx-font-size: 14px;");

                            
                            HBox fromBox = new HBox(10);
                            fromBox.setStyle("-fx-alignment: center-left;");
                            fromBox.getChildren().addAll(fromCircle, fromLabel);

                            
                            routeInfoBox.getChildren().add(fromBox);

                            
                            Station toStation = stationMap.get(toId);
                            Circle toCircle = new Circle(7); 
                            if (toStation != null) {
                                if (toStation.getId().equals("CEN")) {
                                    
                                    toCircle.setFill(new LinearGradient(
                                            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                                            new Stop(0, Color.web("#84c469")), 
                                            new Stop(1, Color.web("#328674")) 
                                    ));
                                } else {
                                    
                                    toCircle.setStyle("-fx-fill: " + toStation.getColor() + ";");
                                }
                            } else {
                                toCircle.setStyle("-fx-fill: transparent;"); 
                            }

                            // สร้าง Label สำหรับสถานีปลายทาง
                            if (!fromId.equals("CEN") && !toId.equals("CEN")) {
                                Label emoji = new Label("🚶");
                                routeInfoBox.getChildren().add(emoji);
                            }
                            Label toLabel = new Label(toName + " (" + toId + ")");
                            toLabel.setStyle("-fx-text-fill: #003366; -fx-font-size: 14px;");

                            
                            HBox toBox = new HBox(10);
                            toBox.setStyle("-fx-alignment: center-left;");
                            toBox.getChildren().addAll(toCircle, toLabel);
                            
                            routeInfoBox.getChildren().add(toBox);
                            if (toId.equals(endId)) {
                                isLastStationDisplayed = true;
                            } else {
                                isLastStationDisplayed = false;
                            }
                        }
                    } else {
                        // วนลูปเพื่อสร้าง HBox สำหรับแต่ละสถานี
                        for (int j = 0; j < importantSteps.size(); j++) {
                            String step = importantSteps.get(j);
                            String[] parts = step.split("->");
                            String fromId = parts[0];
                            String toId = parts[1];

                            String fromName = stationUtil.IDtoName(fromId);
                            String toName = stationUtil.IDtoName(toId);

                            // สร้าง Circle สำหรับสถานีต้นทาง
                            Station fromStation = stationMap.get(fromId);
                            Circle fromCircle = new Circle(7); 
                            if (fromStation != null) {
                                if (fromStation.getId().equals("CEN")) {
                                    
                                    fromCircle.setFill(new LinearGradient(
                                            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                                            new Stop(0, Color.web("#84c469")), 
                                            new Stop(1, Color.web("#328674")) 
                                    ));
                                } else {
                                    
                                    fromCircle.setStyle("-fx-fill: " + fromStation.getColor() + ";");
                                }
                            } else {
                                fromCircle.setStyle("-fx-fill: transparent;"); 
                            }

                            
                            Label fromLabel = new Label(fromName + " (" + fromId + ")");
                            fromLabel.setStyle("-fx-text-fill: #003366; -fx-font-size: 14px;");

                            
                            HBox fromBox = new HBox(10);
                            fromBox.setStyle("-fx-alignment: center-left;");
                            fromBox.getChildren().addAll(fromCircle, fromLabel);

                            
                            routeInfoBox.getChildren().add(fromBox);

                            
                            Station toStation = stationMap.get(toId);
                            Circle toCircle = new Circle(7); 
                            if (toStation != null) {
                                if (toStation.getId().equals("CEN")) {
                                    
                                    toCircle.setFill(new LinearGradient(
                                            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                                            new Stop(0, Color.web("#84c469")), 
                                            new Stop(1, Color.web("#328674")) 
                                    ));
                                } else {
                                    
                                    toCircle.setStyle("-fx-fill: " + toStation.getColor() + ";");
                                }
                            } else {
                                toCircle.setStyle("-fx-fill: transparent;");
                            }

                            if (!fromId.equals("CEN") && !toId.equals("CEN")) {
                                Label emoji = new Label("🚶");
                                routeInfoBox.getChildren().add(emoji);
                            }
                            Label toLabel = new Label(toName + " (" + toId + ")");
                            toLabel.setStyle("-fx-text-fill: #003366; -fx-font-size: 14px;");

                            
                            HBox toBox = new HBox(10);
                            toBox.setStyle("-fx-alignment: center-left;");
                            toBox.getChildren().addAll(toCircle, toLabel);

                            
                            routeInfoBox.getChildren().add(toBox);
                            Station toStation1 = stationMap.get(toId);
                            if (toId.equals(endId)) {
                                isLastStationDisplayed = true;
                            } else {
                                isLastStationDisplayed = false;
                            }
                        }
                    }
                    Station endStation = stationMap.get(endId);
                    if (isLastStationDisplayed == false) {
                        HBox endBox = new HBox(10);
                        Label endLabel = new Label(stationUtil.IDtoName(endId) + " (" + endId + ")");
                        endLabel.setStyle("-fx-text-fill: #003366; -fx-font-size: 14px;");
                        Circle endCircle = new Circle(7);

                        if (endStation.getId().equals("CEN")) {
                            
                            endCircle.setFill(new LinearGradient(
                                    0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                                    new Stop(0, Color.web("#84c469")), 
                                    new Stop(1, Color.web("#328674")) 
                            ));
                        } else {
                            
                            endCircle.setStyle("-fx-fill: " + endStation.getColor() + ";");
                        }

                        endBox.setStyle("-fx-alignment: center-left;");
                        endBox.getChildren().addAll(endCircle, endLabel);
                        routeInfoBox.getChildren().add(endBox);
                    }

                    
                    PathBox1.getChildren().addAll(infoLabel, intro1, startStationBox, circleBox1, intro2, endStationBox,
                            routeInfoBox);
                }

                System.out.println("\n🕒 เวลารวมทั้งหมด: " + result.getTotalTime() + " นาที");
                Label timeLabel = new Label(" 🕒 Total times: " + result.getTotalTime() + " minutes");
                PathBox1.getChildren().add(timeLabel);
            }

            
            Scene nextScene = new Scene(nextPage);
            stage.setScene(nextScene);
            stage.setResizable(true); 
        });

        clearButton.setOnAction(event -> {
            textField1.clear();
            textField2.clear();
            textField1Box.getChildren().remove(circleStation1);
            textField2Box.getChildren().remove(circleStation2);
            textField1Box.getChildren().remove(stationName1);
            textField2Box.getChildren().remove(stationName2);
        });

        // Add buttons to an HBox
        HBox buttonBox = new HBox(10); 
        buttonBox.setStyle("-fx-alignment: center;"); 
        buttonBox.getChildren().addAll(submitButton, clearButton);

        
        contentBox.getChildren().addAll(bgName, inputLabel1, textField1Box, circleBox, inputLabel2,
                textField2Box, buttonBox);

        rightPane.getChildren().addAll(logoView, contentBox);

        // ---------- Layout ----------
        HBox.setHgrow(leftPane, Priority.ALWAYS);
        root.getChildren().addAll(leftPane, rightPane);

        stage.setTitle("MK Transit");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}