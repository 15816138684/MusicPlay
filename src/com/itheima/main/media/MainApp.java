package com.itheima.main.media;

import com.itheima.main.utils.ImageUtils;
import com.itheima.main.utils.XMLUtils;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.framebody.FrameBodyAPIC;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class MainApp extends Application {
    //1、全局的“舞台”变量
    public static Stage staticprimary;
    //2、最大化之前的x，y坐标
    private double resetX;
    private double resetY;
    //3、最大化之前的宽度，高度
    private double resetWidth;
    private double resetHeight;

    //4、窗体移动前，相对于Scene的x，y坐标
    private double moustX;
    private double moustY;

    //5、显示歌单列表中的VBox对象
    private VBox groupVBox;

    //6.改变窗体前，X,Y坐标
    private double xOffset;
    private double yOffset;

    //7、歌单名称标签
    private Label labGroupName;

    //8.播放列表的TableView
    private TableView<PlayBean> tableView;

    //9、当前播放歌曲的索引
    private int currentIndex;

    //10、当前播放的时间的前一秒--设置滚动条
    private int prevSecond;

    //11、当前播放的PlayBean
    private PlayBean currentPlayBean;

    //12、下侧面板的：总时间
    private Label labelAllTime;

    //13、碟片的ImageView对象
    private ImageView panImageView;

    //14、旋转的时间轴对象
    private Timeline timeline;

    //15、背景
    private ImageView backImageView;

    //16、播放按钮的ImageView对象
    private ImageView PlayImage;

    //17、播放按钮的Label对象
    private Label labPlay;

    //18、当前播放模式
    private int playMode = 1;//1、列表循环，2、顺序循环，3、单曲循环

    //19、播放时间的滚动条
    private Slider sliderSong;

    //20、已播放时间的Label
    private Label labelTime;

    //21、音量滚动条
    private Slider sliderVolume;

    //22、音量的进度条
    private ProgressBar volumeProgress;

    //23、记录静音前的音量
    private double prevVolumn;

    //24、显示歌词的VBox容器
    private VBox lrcVBox;

    //25、存储歌词时间的ArrayList
    private ArrayList<BigDecimal> lrcList = new ArrayList<>();

    //26、当前歌词的索引
    private int currentLrcIndex;

    //27、记录歌名方便显示
    private String songName;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // TODO Auto-generated method stub
        staticprimary = primaryStage;
        // 设置舞台
        // 1、创建一个BorderPane对象
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(getTopPane());
        borderPane.setLeft(getLeftPane());
        borderPane.setBottom(getBottomPane());
        borderPane.setCenter(getCenterPane());
        borderPane.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        // 2、创建一个场景
        Scene scene = new Scene(borderPane, 1000, 600);// 场景宽度：1000像素；场景高度：600像素
        // 3.将场景设置到舞台
        primaryStage.setScene(scene);
        // 4.将舞台的标题栏去掉
        primaryStage.initStyle(StageStyle.UNDECORATED);
        // 显示舞台
        primaryStage.show();
    }

    //创建一个中间的面板
    private BorderPane getCenterPane() {
        //1、读取上次关闭时，播放的歌单和歌曲
        String[] playInfo = XMLUtils.readPrevPlayInfo();

        //2、歌单，标签
        Label label = new Label("歌单，");
        label.setTextFill(Color.rgb(180, 0, 0));//文字，暗红色
        BorderStroke bs = new BorderStroke(
                Color.rgb(180, 0, 0),//四个边的颜色
                Color.rgb(180, 0, 0),
                Color.rgb(180, 0, 0),
                Color.rgb(180, 0, 0),
                BorderStrokeStyle.SOLID,//四个边的线性--实线
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID,
                new CornerRadii(1),
                new BorderWidths(1),
                new Insets(1, 2, 1, 2)
        );
        label.setBorder(new Border(bs));
        label.setLayoutX(30);
        label.setLayoutY(10);
        label.setPrefWidth(50);
        label.setPrefHeight(25);
        label.setAlignment(Pos.CENTER);

        //3、歌单名称：标签
        labGroupName = new Label(playInfo == null ? " " : playInfo[0]);
        labGroupName.setLayoutX(90);
        labGroupName.setLayoutY(9);
        labGroupName.setTextFill(Color.BLUE);
        labGroupName.setFont(new Font("黑体", 18));
        labGroupName.setPrefWidth(200);
        labGroupName.setPrefHeight(25);
        labGroupName.setAlignment(Pos.CENTER_LEFT);

        //4、碟片的图片
        panImageView = new ImageView("img/center/pan_default.jpg");
        panImageView.setFitHeight(140);
        panImageView.setFitWidth(140);
        Label label2 = new Label("", panImageView);
        label2.setLayoutX(20);
        label2.setLayoutY(40);

        //定义一个圆
        Circle circle = new Circle();
        circle.setCenterX(70);
        circle.setCenterY(70);
        circle.setRadius(70);//圆的半径

        panImageView.setClip(circle);

        //定义一个"时间轴"动画
        timeline = new Timeline();
        timeline.getKeyFrames().addAll(
                new KeyFrame(new Duration(0), new KeyValue(panImageView.rotateProperty(), 0)),
                new KeyFrame(new Duration(8 * 1000), new KeyValue(panImageView.rotateProperty(), 360))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);//无限循环
        //timeline.play();

        //5、歌词的VBox容器
        lrcVBox = new VBox(15);
        lrcVBox.setPadding(new Insets(20, 20, 20, 20));
        lrcVBox.setLayoutX(250);
        lrcVBox.setLayoutY(0);

        //6、歌单列表标签
        Label label3 = new Label("歌单列表");
        label3.setPrefWidth(60);
        label3.setPrefHeight(20);
        label3.setTextFill(Color.WHITE);
        label3.setAlignment(Pos.CENTER);
        label3.setBackground(new Background(new BackgroundFill(Color.rgb(180, 0, 0), null, null)));
        label3.setLayoutX(5);
        label3.setLayoutY(180);

        //7、模糊背景
        Image image = new Image("img/center/pan_default.jpg");
        //获取“像素读取器”
        PixelReader pixelReader = image.getPixelReader();
        //创建一个WritableImage
        WritableImage writableImage = new WritableImage(
                (int) image.getWidth(),
                (int) image.getHeight()
        );
        //创建一个“像素写入器”
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        //循环读取image中的每个像素
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                Color color = pixelReader.getColor(i, j);
                for (int k = 0; k < 4; k++) {//四次淡化
                    color = color.darker();//将当前的颜色淡化
                }
                pixelWriter.setColor(i, j, color);
            }
        }

        backImageView = new ImageView(writableImage);
        backImageView.setLayoutX(0);
        backImageView.setLayoutY(0);
        backImageView.setFitWidth(300);
        backImageView.setFitHeight(300);

        //高斯模糊
        GaussianBlur gaussianBlur = new GaussianBlur();
        gaussianBlur.setRadius(63);

        backImageView.setEffect(gaussianBlur);

        //一条红线：Label
        Label labLine = new Label();
        labLine.setMinWidth(0);
        labLine.setPrefWidth(2);
        labLine.setBackground(new Background(new BackgroundFill(Color.rgb(180, 0, 0), null, null)));
        labLine.setLayoutX(0);
        labLine.setLayoutY(label3.getLayoutY() + label3.getPrefHeight());

        //AnchorPane
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
        anchorPane.getChildren().addAll(backImageView, label, labGroupName, label2, lrcVBox, label3, labLine);

        //上侧的ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPadding(new Insets(0, 0, 0, 0));
        scrollPane.setContent(anchorPane);
        scrollPane.setPrefHeight(204);
        scrollPane.setMouseTransparent(true);//使scrollPane不接受鼠标事件

        anchorPane.prefWidthProperty().bind(scrollPane.widthProperty());
        anchorPane.prefHeightProperty().bind(scrollPane.heightProperty());
        backImageView.fitWidthProperty().bind(scrollPane.widthProperty());
        backImageView.fitHeightProperty().bind(scrollPane.heightProperty());
        labLine.prefWidthProperty().bind(scrollPane.widthProperty());

        /******************下侧：歌单列表**********************/
        tableView = new TableView<>();
        tableView.setPrefWidth(600);
        tableView.getStylesheets().add("css/playTable.css");

        TableColumn column1 = new TableColumn("序号");
        column1.setPrefWidth(80);
        column1.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn column2 = new TableColumn("音乐标题");
        column2.setPrefWidth(200);
        column2.setCellValueFactory(new PropertyValueFactory<>("soundName"));

        TableColumn column3 = new TableColumn("歌手");
        column3.setPrefWidth(120);
        column3.setCellValueFactory(new PropertyValueFactory<>("artList"));

        TableColumn column4 = new TableColumn("专辑");
        column4.setPrefWidth(120);
        column4.setCellValueFactory(new PropertyValueFactory<>("album"));

        TableColumn column5 = new TableColumn("大小");
        column5.setPrefWidth(100);
        column5.setCellValueFactory(new PropertyValueFactory<>("length"));

        TableColumn column6 = new TableColumn("时间");
        column6.setPrefWidth(80);
        column6.setCellValueFactory(new PropertyValueFactory<>("time"));

        TableColumn column7 = new TableColumn("操作");
        column7.setPrefWidth(60);
        column7.setCellValueFactory(new PropertyValueFactory<>("labDelete"));

        tableView.getColumns().addAll(column1, column2, column3, column4, column5, column6, column7);

        tableView.setRowFactory(tv -> {
            TableRow<PlayBean> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                //验证双击
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    //1、获取选中行的索引
                    this.currentIndex = row.getIndex();
                    //2、将前一秒置为0
                    this.prevSecond = 0;
                    //3、判断当前是否正在播放，如果是，将其停止
                    if (this.currentPlayBean != null) {
                        this.currentPlayBean.getMediaPlayer().stop();
                    }
                    //4、获取当前的PlayBean
                    this.currentPlayBean = row.getItem();
                    //5、播放
                    play();
                }
            });
            return row;
        });

        /******************总的BorderPane*********************/
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(scrollPane);
        borderPane.setCenter(tableView);

        //将"操作列"随着窗体的大小改变而改变,可以改做其他列
        column7.prefWidthProperty().bind(borderPane.widthProperty());

        return borderPane;
    }

    //播放
    private void play() {
        //读取歌词
        loadLrc();

        //1、设置总时间
        this.labelAllTime.setText(this.currentPlayBean.getTime());

        //设置滚动条的总的值
        this.sliderSong.setMax(this.currentPlayBean.getTotalSeconds());
        this.sliderSong.setMajorTickUnit(1);//每次前进一格
        this.sliderSong.setValue(0);
        prevSecond = 0;

        //设置初始音量
        this.currentPlayBean.getMediaPlayer().setVolume(this.volumeProgress.getProgress());

        //2、开始播放
        new Thread() {
            @Override
            public void run() {
                currentPlayBean.getMediaPlayer().play();
            }
        }.start();

        //3、设置碟片
        if (this.currentPlayBean.getImage() != null) {
            this.panImageView.setImage(this.currentPlayBean.getImage());
        } else {
            this.panImageView.setImage(new Image("img/center/pan_default.jpg"));
        }
        //4、设置旋转
        this.timeline.stop();
        this.timeline.play();

        //5、设置背景
        WritableImage wImage = this.currentPlayBean.getImage();
        if (wImage != null) {
            //虚化
            WritableImage newwritableImage = new WritableImage(
                    (int) wImage.getWidth(),
                    (int) wImage.getHeight()
            );
            PixelReader pr = wImage.getPixelReader();
            PixelWriter pw = newwritableImage.getPixelWriter();
            for (int i = 0; i < wImage.getHeight(); i++) {
                for (int j = 0; j < wImage.getWidth(); j++) {
                    Color color = pr.getColor(i, j);
                    //四次变淡
                    for (int k = 0; k < 4; k++) {
                        color = color.darker();
                    }
                    //输出
                    pw.setColor(i, j, color);
                }
            }
            this.backImageView.setImage(newwritableImage);
        } else {
            Image img = new Image("img/center/pan_default.jpg");
            PixelReader pr = img.getPixelReader();
            WritableImage writableImage = new WritableImage(
                    (int) img.getHeight(),
                    (int) img.getWidth()
            );
            PixelWriter pw = writableImage.getPixelWriter();
            for (int i = 0; i < img.getHeight(); i++) {
                for (int j = 0; j < img.getWidth(); j++) {
                    Color color = pr.getColor(i, j);
                    //四次变淡
                    for (int k = 0; k < 4; k++) {
                        color = color.darker();
                    }
                    //输出
                    pw.setColor(i, j, color);
                }
            }
            this.backImageView.setImage(writableImage);
        }
        //播放按钮设置为：播放
        this.PlayImage.setImage(new Image("img/topandbottom/PauseDark.png"));
        this.labPlay.setOnMouseEntered(e -> PlayImage.setImage(new Image("img/topandbottom/Pause.png")));
        this.labPlay.setOnMouseExited(e -> PlayImage.setImage(new Image("img/topandbottom/PauseDark.png")));
    }

    //加载正在播放的歌曲Lrc文件（歌词文件）
    private void loadLrc() {
        if (this.currentPlayBean == null) {
            return;
        }

        //初始化lrcVBox
        this.lrcVBox.getChildren().clear();
        this.lrcVBox.setLayoutY(50 * 2 - 10);
        this.lrcList.clear();
        this.currentLrcIndex = 0;

        //读取MP3文件
        File mp3File = new File(this.currentPlayBean.getFilePath());
        //查找歌词文件
        File lrcFile = new File(mp3File.getParent(), mp3File.getName().substring(0, mp3File.getName().indexOf(".")) + ".lrc");
        if (!lrcFile.exists()) {
            return;
        }

        //读取每一行，封装歌词Label
        try {
            BufferedReader bufferIn = new BufferedReader(new InputStreamReader(new FileInputStream(lrcFile), "GBK"));
            String row = null;
            while ((row = bufferIn.readLine()) != null) {
                if (row.indexOf("[") == -1 || row.indexOf("]") == -1) {
                    continue;
                }
                if (row.charAt(1) < '0' || row.charAt(1) > '9') {
                    continue;
                }

                String strTime = row.substring(1, row.indexOf("]"));//00:03.29
                String strMinute = strTime.substring(0, strTime.indexOf(":"));//取出：分钟
                String strSecond = strTime.substring(strTime.indexOf(":") + 1);//取出，秒和毫秒
                //转换为int分钟
                int intMinute = Integer.parseInt(strMinute);
                //换算为总的毫秒
                BigDecimal totalMilli = new BigDecimal(intMinute * 60).add(new BigDecimal(strSecond)).multiply(new BigDecimal("1000"));
                this.lrcList.add(totalMilli);

                //创建歌词Label
                Label label = new Label(row.trim().substring(row.indexOf("]") + 1));
                label.setMinWidth(400);
                label.setMinHeight(29);
                label.setMaxHeight(29);

                label.setPrefWidth(400);
                label.setPrefHeight(29);
                label.setTextFill(Color.rgb(53, 53, 53));
                label.setFont(new Font("黑体", 14));
                label.setAlignment(Pos.CENTER);

                //判断是否第一个是歌词，如果是，改为22号，黄色
                if (this.lrcVBox.getChildren().size() == 0) {
                    label.setTextFill(Color.YELLOW);
                    label.setFont(new Font("黑体", 24));
                }
                //判断是否是第二行
                if (this.lrcVBox.getChildren().size() == 1) {
                    label.setTextFill(Color.WHITE);
                }
                //将歌词Label添加到lrcVBox中
                this.lrcVBox.getChildren().add(label);
            }

            //最后添加一行自选文字
            if (this.currentPlayBean.getMediaPlayer().getTotalDuration().toMillis()
                    - this.lrcList.get(this.lrcList.size() - 1).doubleValue() > 0) {
                Label lab = new Label("谢幕");
                lab.setMinWidth(400);
                lab.setMinHeight(29);
                lab.setMaxHeight(29);

                lab.setPrefWidth(400);
                lab.setPrefHeight(29);
                lab.setTextFill(Color.rgb(255, 0, 0));
                lab.setFont(new Font("黑体", 24));
                lab.setAlignment(Pos.CENTER);
                this.lrcVBox.getChildren().add(lab);
                this.lrcList.add(new BigDecimal(this.currentPlayBean.getMediaPlayer().getTotalDuration().toMillis()));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //创建下侧面板
    private BorderPane getBottomPane() {
        /******************左侧的三个按钮*********************/
        //1、上一首
        ImageView imageView1 = new ImageView("img/topandbottom/LastDark.png");
        imageView1.setFitWidth(40);
        imageView1.setFitHeight(40);
        Label label1 = new Label("", imageView1);
        label1.setOnMouseEntered(e -> imageView1.setImage(new Image("img/topandbottom/Last.png")));
        label1.setOnMouseExited(e -> imageView1.setImage(new Image("img/topandbottom/LastDark.png")));
        label1.setOnMouseClicked(e -> {
            if (this.currentPlayBean != null) {
                this.currentPlayBean.getMediaPlayer().stop();
            }
            //停止光盘的旋转
            this.timeline.stop();
            //让当前的索引-1
            this.currentIndex--;
            if (currentIndex < 0) {
                if (this.playMode == 1) {//列表循环
                    this.currentIndex = this.tableView.getItems().size() - 1;//定位到最后一首歌曲
                } else {
                    this.currentIndex = 0;
                }
            }
            //设置Table的选中
            this.tableView.getSelectionModel().select(currentIndex);
            //设置播放PlayBean对象
            this.currentPlayBean = this.tableView.getItems().get(currentIndex);
            //开始播放
            play();
        });

        //2、播放按钮
        PlayImage = new ImageView("img/topandbottom/PlayDark.png");
        PlayImage.setFitWidth(40);
        PlayImage.setFitHeight(40);
        labPlay = new Label("", PlayImage);
        labPlay.setOnMouseEntered(e -> PlayImage.setImage(new Image("img/topandbottom/Play.png")));
        labPlay.setOnMouseExited(e -> PlayImage.setImage(new Image("img/topandbottom/PlayDark.png")));
        labPlay.setOnMouseClicked(e -> {
            //判断如果当前正在播放，暂停
            if (this.currentPlayBean.getMediaPlayer().getStatus() == MediaPlayer.Status.PLAYING) {
                //设置播放器暂停
                this.currentPlayBean.getMediaPlayer().pause();
                //设置播放器图片为：播放
                PlayImage.setImage(new Image("img/topandbottom/PlayDark.png"));
                labPlay.setOnMouseEntered(ee -> PlayImage.setImage(new Image("img/topandbottom/Play.png")));
                labPlay.setOnMouseExited(ee -> PlayImage.setImage(new Image("img/topandbottom/PlayDark.png")));
                //暂停旋转的光碟
                this.timeline.pause();
            } else if (this.currentPlayBean.getMediaPlayer().getStatus() == MediaPlayer.Status.PAUSED) {
                this.currentPlayBean.getMediaPlayer().play();
                this.timeline.play();
                PlayImage.setImage(new Image("img/topandbottom/PauseDark.png"));
                labPlay.setOnMouseEntered(ee -> PlayImage.setImage(new Image("img/topandbottom/Pause.png")));
                labPlay.setOnMouseExited(ee -> PlayImage.setImage(new Image("img/topandbottom/PauseDark.png")));
            }
        });

        //3、下一首
        ImageView imageView3 = new ImageView("img/topandbottom/NextDark.png");
        imageView3.setFitWidth(40);
        imageView3.setFitHeight(40);
        Label label3 = new Label("", imageView3);
        label3.setOnMouseEntered(e -> imageView3.setImage(new Image("img/topandbottom/Next.png")));
        label3.setOnMouseExited(e -> imageView3.setImage(new Image("img/topandbottom/NextDark.png")));
        label3.setOnMouseClicked(e -> {
            if (this.currentPlayBean != null) {
                this.currentPlayBean.getMediaPlayer().stop();
            }
            //停止光盘的旋转
            this.timeline.stop();
            //让当前的索引+1
            this.currentIndex++;
            if (currentIndex >= this.tableView.getItems().size()) {
                if (this.playMode == 1) {//列表循环
                    this.currentIndex = 0;//定位到第一首歌曲
                } else {
                    this.currentIndex = this.tableView.getItems().size() - 1;
                }
            }
            //设置Table的选中
            this.tableView.getSelectionModel().select(currentIndex);
            //设置播放PlayBean对象
            this.currentPlayBean = this.tableView.getItems().get(currentIndex);
            //开始播放
            play();
        });

        HBox hBox = new HBox(30);
        hBox.setPrefWidth(255);
        hBox.setPadding(new Insets(5, 10, 5, 30));
        hBox.getChildren().addAll(label1, labPlay, label3);

        /***********************中间滚动条部分****************************/
        //1、已播放的时间
        labelTime = new Label("00:00");
        labelTime.setPrefWidth(50);
        labelTime.setPrefHeight(40);
        labelTime.setTextFill(Color.WHITE);

        //2、滚动条
        sliderSong = new Slider();
        sliderSong.setMinHeight(0);
        sliderSong.setMinWidth(0);
        sliderSong.setPrefWidth(300);
        sliderSong.setPrefHeight(12);
        sliderSong.getStylesheets().add("css/TopAndBottomPage.css");

        //3、进度条
        ProgressBar progressBar1 = new ProgressBar(0);
        progressBar1.setProgress(0);
        progressBar1.setMinHeight(0);
        progressBar1.setMinWidth(0);

        progressBar1.setMaxWidth(5000);
        progressBar1.setPrefWidth(300);
        progressBar1.getStylesheets().add("css/TopAndBottomPage.css");

        //Slider值发生改变时
        sliderSong.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                //设置进度条
                if (currentPlayBean != null) {
                    progressBar1.setProgress((newValue.doubleValue() + 1) / currentPlayBean.getTotalSeconds());
                }
            }
        });

        //Slider的鼠标抬起事件中
        sliderSong.setOnMouseReleased(e -> {
            if (currentPlayBean != null) {
                Duration duration = new Duration(sliderSong.getValue() * 1000);
                currentPlayBean.getMediaPlayer().seek(duration);//设置新的播放时间

                //同时设置Label
                Date date = new Date();
                date.setTime((long) currentPlayBean.getMediaPlayer().getCurrentTime().toMillis());
                labelTime.setText(new SimpleDateFormat("mm:ss").format(date));
                //设置前一秒
                prevSecond = (int) duration.toSeconds() - 1;
            }
        });

        //使用StackPane来存储进度条和滚动条
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(progressBar1, sliderSong);

        //3、总时间标签
        labelAllTime = new Label("00:00");
        labelAllTime.setPrefWidth(50);
        labelAllTime.setPrefHeight(40);
        labelAllTime.setTextFill(Color.WHITE);
        labelAllTime.setAlignment(Pos.CENTER_RIGHT);

        BorderPane borderPane = new BorderPane();
        borderPane.setLeft(labelTime);
        borderPane.setCenter(stackPane);
        borderPane.setRight(labelAllTime);
        borderPane.setPrefHeight(50);

        labelTime.prefHeightProperty().bind(borderPane.prefHeightProperty());
        sliderSong.prefHeightProperty().bind(borderPane.prefHeightProperty());
        labelAllTime.prefHeightProperty().bind(borderPane.prefHeightProperty());

        /***********************右侧的几个控件****************************/
        //1、音量图片
        ImageView soundImage = new ImageView("img/topandbottom/VolumnDark.png");
        soundImage.setFitHeight(17);
        soundImage.setFitWidth(17);
        Label labelImage = new Label("", soundImage);
        labelImage.setOnMouseEntered(e -> soundImage.setImage(new Image("img/topandbottom/Volumn.png")));
        labelImage.setOnMouseExited(e -> soundImage.setImage(new Image("img/topandbottom/VolumnDark.png")));
        labelImage.setOnMouseClicked(e -> {
            if (this.currentPlayBean != null) {
                //判断当前的音量
                if (this.currentPlayBean.getMediaPlayer().getVolume() != 0) {//此时是未静音状态
                    //将当前音量存储起来
                    this.prevVolumn = this.currentPlayBean.getMediaPlayer().getVolume();
                    //设置为静音
                    this.currentPlayBean.getMediaPlayer().setVolume(0);
                    //设置图片
                    soundImage.setImage(new Image("img/left/volumnZero_1_Dark.png"));
                    labelImage.setOnMouseEntered(ee -> soundImage.setImage(new Image("img/left/volumnZero_1.png")));
                    labelImage.setOnMouseExited(ee -> soundImage.setImage(new Image("img/left/volumnZero_1_Dark.png")));
                } else {//此时是静音状态
                    //恢复原音量
                    this.currentPlayBean.getMediaPlayer().setVolume(this.prevVolumn);
                    //恢复图片
                    soundImage.setImage(new Image("img/topandbottom/VolumnDark.png"));
                    labelImage.setOnMouseEntered(ee -> soundImage.setImage(new Image("img/topandbottom/Volumn.png")));
                    labelImage.setOnMouseExited(ee -> soundImage.setImage(new Image("img/topandbottom/VolumnDark.png")));
                }
            }
        });

        //2、音量滚动条
        sliderVolume = new Slider();
        sliderVolume.setMax(100);
        sliderVolume.setValue(50);
        sliderVolume.setMajorTickUnit(1);//每前进一格，增加多少的量

        sliderVolume.setMinHeight(0);
        //sliderVolume.setPrefHeight(10);
        sliderVolume.setPrefWidth(100);
        sliderVolume.getStylesheets().add("css/TopAndBottomPage.css");

        //进度条
        volumeProgress = new ProgressBar(0);
        volumeProgress.setMinHeight(0);
        volumeProgress.setProgress(0.5);//初始化在中间的位置
        volumeProgress.setPrefWidth(100);
        volumeProgress.setPrefHeight(10);
        volumeProgress.prefWidthProperty().bind(sliderVolume.prefWidthProperty());
        volumeProgress.getStylesheets().add("css/TopAndBottomPage.css");

        //监听进度条的值发生变化时
        sliderVolume.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                //1、设置它的进度条
                volumeProgress.setProgress(sliderVolume.getValue() / 100);
                //2、设置音量
                if (currentPlayBean != null) {
                    currentPlayBean.getMediaPlayer().setVolume(volumeProgress.getProgress());
                }
                soundImage.setImage(new Image("img/topandbottom/VolumnDark.png"));
                labelImage.setOnMouseEntered(e -> soundImage.setImage(new Image("img/topandbottom/Volumn.png")));
                labelImage.setOnMouseExited(e -> soundImage.setImage(new Image("img/topandbottom/VolumnDark.png")));
            }
        });

        StackPane stackPane2 = new StackPane();
        stackPane2.getChildren().addAll(volumeProgress, sliderVolume);

        //3、播放模式图片
        ImageView imageViewPlay = new ImageView("img/topandbottom/RepeatDark.png");
        imageViewPlay.setFitWidth(25);
        imageViewPlay.setFitHeight(25);
        Label labelPlay = new Label("", imageViewPlay);
        labelPlay.setOnMouseEntered(e -> imageViewPlay.setImage(new Image("img/topandbottom/Repeat.png")));
        labelPlay.setOnMouseExited(e -> imageViewPlay.setImage(new Image("img/topandbottom/RepeatDark.png")));
        labelPlay.setOnMouseClicked(e -> {
            //此处只处理playMode，实现，放在播放的事件中实现
            this.playMode++;
            if (this.playMode > 3) {
                this.playMode = 1;
            }
            switch (this.playMode) {
                case 1:
                    imageViewPlay.setImage(new Image("img/topandbottom/RepeatDark.png"));
                    labelPlay.setOnMouseEntered(ee -> imageViewPlay.setImage(new Image("img/topandbottom/Repeat.png")));
                    labelPlay.setOnMouseExited(ee -> imageViewPlay.setImage(new Image("img/topandbottom/RepeatDark.png")));
                    break;
                case 2:
                    imageViewPlay.setImage(new Image("img/topandbottom/OrderPlayDark.png"));
                    labelPlay.setOnMouseEntered(ee -> imageViewPlay.setImage(new Image("img/topandbottom/OrderPlay.png")));
                    labelPlay.setOnMouseExited(ee -> imageViewPlay.setImage(new Image("img/topandbottom/OrderPlayDark.png")));
                    break;
                case 3:
                    imageViewPlay.setImage(new Image("img/topandbottom/RepeatInOneDark.png"));
                    labelPlay.setOnMouseEntered(ee -> imageViewPlay.setImage(new Image("img/topandbottom/RepeatInOne.png")));
                    labelPlay.setOnMouseExited(ee -> imageViewPlay.setImage(new Image("img/topandbottom/RepeatInOneDark.png")));
                    break;
            }
        });

        //4、歌词图片
        ImageView imageViewGC = new ImageView("img/topandbottom/ciDark.png");
        imageViewGC.setFitHeight(25);
        imageViewGC.setFitWidth(25);
        Label labelGC = new Label("", imageViewGC);
        labelGC.setOnMouseEntered(e -> imageViewGC.setImage(new Image("img/topandbottom/ci.png")));
        labelGC.setOnMouseExited(e -> imageViewGC.setImage(new Image("img/topandbottom/ciDark.png")));

        //5、拖拽图片
        ImageView imageViewMove = new ImageView("img/topandbottom/right_drag.png");
        imageViewMove.setFitWidth(30);
        imageViewMove.setFitHeight(50);
        Label labelMove = new Label("", imageViewMove);
        //当鼠标按下时
        labelMove.setOnMousePressed(e -> {
            //记录当前鼠标在屏幕的X，Y坐标
            xOffset = e.getScreenX();
            yOffset = e.getScreenY();
        });
        //当鼠标移动时
        labelMove.setOnMouseMoved(e -> {
            if (e.getY() > 34 && e.getY() < 50 && e.getX() > 0 && e.getX() < 30) {
                //改变鼠标形状
                labelMove.setCursor(Cursor.NW_RESIZE);
            } else {
                labelMove.setCursor(Cursor.DEFAULT);
            }
        });
        //当鼠标拖拽时
        labelMove.setOnMouseDragged(e -> {
            if (staticprimary.getWidth() + (e.getScreenX() - xOffset) >= 1000) {
                staticprimary.setWidth(staticprimary.getWidth() + (e.getScreenX() - xOffset));
                xOffset = e.getScreenX();
            }
            if (staticprimary.getHeight() + (e.getScreenY() - yOffset) >= 600) {
                staticprimary.setHeight(staticprimary.getHeight() + (e.getScreenY() - yOffset));
                yOffset = e.getScreenY();
            }
        });

        HBox hBox1 = new HBox(15);
        hBox1.setPadding(new Insets(0, 0, 0, 10));
        hBox1.setPrefWidth(270);
        hBox1.setAlignment(Pos.CENTER_LEFT);
        hBox1.getChildren().addAll(labelImage, stackPane2, labelPlay, labelGC, labelMove);

        /***********************总的BorderPane***************************/
        BorderPane bottomPane = new BorderPane();
        bottomPane.setLeft(hBox);
        bottomPane.setCenter(borderPane);
        bottomPane.setRight(hBox1);
        return bottomPane;
    }

    //创建一个左侧面板
    private BorderPane getLeftPane() {
        //1、作者图片
        ImageView v1 = new ImageView("img/center/2.jpg");
        v1.setFitWidth(230);
        //v1.setPreserveRatio(true);//根据高度。设置等比例的宽度
        v1.setFitHeight(120);

        //2、作者：Label
        Label labelAuthor = new Label("Author：小白");
        labelAuthor.setPrefWidth(200);
        labelAuthor.setTextFill(Color.YELLOW);
        labelAuthor.setFont(new Font("黑体", 18));
        labelAuthor.setAlignment(Pos.CENTER);

        //3、日期：Label
        Label labelDate = new Label("Data：2019-07-14");
        labelDate.setPrefWidth(200);
        labelDate.setTextFill(Color.YELLOW);
        labelDate.setFont(new Font("黑体", 18));
        labelDate.setAlignment(Pos.CENTER);

        //4、已创建歌单：Label
        Label labelGD = new Label("已创建歌单");
        labelGD.setPrefWidth(200);
        labelGD.setPrefHeight(20);
        labelGD.setTextFill(Color.rgb(0, 0, 255));

        //5、+符号：ImageView
        ImageView v2 = new ImageView("img/left/create_2_Dark.png");
        v2.setFitWidth(15);
        v2.setPreserveRatio(true);

        Label lab = new Label("", v2);
        lab.setPrefWidth(15);
        lab.setPrefHeight(15);
        lab.setOnMouseEntered(e -> v2.setImage(new Image("img/left/create_2.png")));
        lab.setOnMouseExited(e -> v2.setImage(new Image("img/left/create_2_Dark.png")));
        lab.setOnMouseClicked(e -> {
            //创建一个新的舞台，进行添加歌曲
            new AddGroup(staticprimary, groupVBox, this);
        });

        //封装4和5的控件HBox(水平布局)
        HBox hBox = new HBox(5);
        hBox.getChildren().addAll(labelGD, lab);

        //将1，2，3，HBox{4，5}封装到一个VBox
        VBox vBox = new VBox(15);//15表示元素之间的间距
        vBox.setPrefWidth(220);
        vBox.setPrefHeight(200);
        vBox.setPadding(new Insets(5, 5, 5, 10));
        vBox.getChildren().addAll(v1, labelAuthor, labelDate, hBox);

        /************************读取所有已创建的歌单**********************/
        List<String> groupList = XMLUtils.readAllGroup();
        //将每个"歌单名字"封装为一个"HBox"对象
        List<HBox> hBoxList = new ArrayList<>();
        for (String groupName : groupList) {
            //1、心形图标：ImageView
            ImageView iv1 = new ImageView("img/left/xinyuanDark.png");
            iv1.setFitWidth(15);
            iv1.setPreserveRatio(true);
            Label labX = new Label("", iv1);
            labX.setMinHeight(0);
            labX.setMinWidth(0);
            labX.setPrefHeight(15);
            labX.setPrefWidth(15);

            //2、歌单名称：Label
            Label labGroupName = new Label(groupName);
            labGroupName.setMinWidth(0);
            labGroupName.setMinHeight(0);
            labGroupName.setPrefHeight(15);
            labGroupName.setPrefWidth(150);
            labGroupName.setTextFill(Color.rgb(210, 210, 210));
            labGroupName.setOnMouseEntered(e -> labGroupName.setTextFill(Color.rgb(200, 0, 0)));
            labGroupName.setOnMouseExited(e -> labGroupName.setTextFill(Color.rgb(210, 210, 210)));
            labGroupName.setOnMouseClicked(e -> {
                //1、设置"歌单名称"
                this.labGroupName.setText(labGroupName.getText().trim());
                readAllSoundByGroup();
            });

            //3、播放图片：ImageView
            ImageView iv2 = new ImageView("img/left/volumn_1_Dark.png");
            iv2.setFitWidth(15);
            iv2.setFitHeight(15);
            Label labTP = new Label("", iv2);
            labTP.setMinWidth(0);
            labTP.setMinHeight(0);
            labTP.setPrefWidth(15);
            labTP.setPrefHeight(15);
            labTP.setOnMouseEntered(e -> iv2.setImage(new Image("img/left/volumn_1.png")));
            labTP.setOnMouseExited(e -> iv2.setImage(new Image("img/left/volumn_1_Dark.png")));
            labTP.setOnMouseClicked(e -> {
                //1、设置"歌单名称"
                this.labGroupName.setText(labGroupName.getText().trim());
                readAllSoundByGroup();
            });

            //4、+符号：ImageView
            ImageView iv3 = new ImageView("img/left/addDark.png");
            iv3.setFitWidth(15);
            iv3.setFitHeight(15);
            Label labAdd = new Label("", iv3);
            labAdd.setMinHeight(0);
            labAdd.setMinWidth(0);
            labAdd.setPrefHeight(15);
            labAdd.setPrefWidth(15);
            labAdd.setOnMouseEntered(e -> iv3.setImage(new Image("img/left/add.png")));
            labAdd.setOnMouseExited(e -> iv3.setImage(new Image("img/left/addDark.png")));
            labAdd.setOnMouseClicked(e -> {
                //显示“打开文件对话框”
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("打开音乐文件");
                //过滤文件
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("MP3", "*.mp3"),
                        new FileChooser.ExtensionFilter("flac", "*.flac"),
                        new FileChooser.ExtensionFilter("所有文件", "*.*")
                );
                List<File> files = fileChooser.showOpenMultipleDialog(staticprimary);
                if (files != null && files.size() > 0) {
                    //将集合中的每个文件的路径写入到xml文件中
                    XMLUtils.insertSounds(labGroupName.getText().trim(), files);
                }
            });

            //5、垃圾桶符号：ImageView
            ImageView iv4 = new ImageView("img/left/laji_1_Dark.png");
            iv4.setFitWidth(15);
            iv4.setFitHeight(15);
            Label labLJ = new Label("", iv4);
            labLJ.setMinHeight(0);
            labLJ.setMinWidth(0);
            labLJ.setPrefHeight(15);
            labLJ.setPrefWidth(15);
            labLJ.setOnMouseEntered(e -> iv4.setImage(new Image("img/left/laji_1.png")));
            labLJ.setOnMouseExited(e -> iv4.setImage(new Image("img/left/laji_1_Dark.png")));

            HBox hBox1 = new HBox(5);
            hBox1.getChildren().addAll(labX, labGroupName, labTP, labAdd, labLJ);
            hBox1.setPadding(new Insets(5, 0, 5, 0));

            hBoxList.add(hBox1);

            labLJ.setOnMouseClicked(e -> {
                //弹出提示
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("确认删除");
                alert.setHeaderText("你确定删除歌单【" + labGroupName.getText().trim() + "】吗？");
                Optional<ButtonType> buttonType = alert.showAndWait();
                if (buttonType.get() == ButtonType.OK) {
                    //调用XMLUtils进行删除
                    XMLUtils.deleteGroup(labGroupName.getText().trim());

                    //从VBox中删除
                    this.groupVBox.getChildren().remove(hBox1);
                }
            });
        }

        groupVBox = new VBox(10);
        groupVBox.setPrefWidth(255);
        groupVBox.setPadding(new Insets(10, 0, 0, 10));
        for (HBox hb : hBoxList) {
            groupVBox.getChildren().add(hb);
        }

        //总面板
        BorderPane leftPane = new BorderPane();
        leftPane.setTop(vBox);
        leftPane.setCenter(groupVBox);

        return leftPane;
    }

    //读取某个歌单的所有歌曲
    private void readAllSoundByGroup() {
        //1.读取此歌单下，所有的歌曲：
        List<SoundBean> soundList = XMLUtils.findSoundByGroupName(this.labGroupName.getText().trim());
        //2.解析每个歌曲文件，封装PlayBean
        List<PlayBean> playBeanList = new ArrayList<>();
        for (int i = 0; i < soundList.size(); i++) {
            SoundBean soundBean = soundList.get(i);
            PlayBean playBean = new PlayBean();
            playBean.setId(i + 1);

            //读取音频文件
            File file = new File(soundBean.getFilePath());

            //解析文件
            MP3File mp3File = null;
            try {
                mp3File = new MP3File(file);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TagException e) {
                e.printStackTrace();
            } catch (ReadOnlyFileException e) {
                e.printStackTrace();
            } catch (CannotReadException e) {
                e.printStackTrace();
            } catch (InvalidAudioFrameException e) {
                e.printStackTrace();
            }

            //获取MP3文件的头信息
            MP3AudioHeader audioHeader = (MP3AudioHeader) mp3File.getAudioHeader();

            //获取字符串形式的时长：
            String strLength = audioHeader.getTrackLengthAsString();

            //转换为int类型的时长
            int intLength = audioHeader.getTrackLength();

            Set<String> keySet = mp3File.getID3v2Tag().frameMap.keySet();
            songName = null;//歌名
            String artList = null;//演唱者
            String album = null;//专辑名称

            if (keySet.contains("TIT2")) {
                songName = mp3File.getID3v2Tag().frameMap.get("TIT2").toString();
            }
            if (keySet.contains("TPE1")) {
                artList = mp3File.getID3v2Tag().frameMap.get("TPE1").toString();
            }
            if (keySet.contains("TALB")) {
                album = mp3File.getID3v2Tag().frameMap.get("TALB").toString();
            }
            System.out.println("歌名：" + songName + " 演唱者：" + artList + " 专辑名称：" + album);
            if (songName != null && !songName.equals("null")) {
                songName = songName.substring(songName.indexOf("\"") + 1, songName.lastIndexOf("\""));
            }
            if (artList != null && !artList.equals("null")) {
                artList = artList.substring(artList.indexOf("\"") + 1, artList.lastIndexOf("\""));

            }
            if (album != null && !album.equals("null")) {
                album = album.substring(album.indexOf("\"") + 1, album.lastIndexOf("\""));
            }

            //为PlayBean赋值
            playBean.setSoundName(songName);
            playBean.setArtList(artList);
            playBean.setAlbum(album);
            playBean.setFilePath(soundBean.getFilePath());

            URI uri = file.toURI();
            Media media = new Media(uri.toString());
            MediaPlayer mp = new MediaPlayer(media);

            //监听播放器播放时的事件
            mp.currentTimeProperty().addListener(new ChangeListener<Duration>() {
                @Override
                public void changed(ObservableValue<? extends Duration> observable,
                                    Duration oldValue, Duration newValue) {
                    //此方法用于在媒体播放器播放时自动调用，每隔100毫秒调用一次
                    //1、由于是每秒使滚动条前进一次，获取newValue中的“秒”
                    int currentSecond = (int) newValue.toSeconds();

                    //2、设置滚动条，一秒一次
                    if (currentSecond == prevSecond + 1) {
                        //设置滚动条
                        sliderSong.setValue(sliderSong.getValue() + 1);
                        //设置前一秒
                        prevSecond++;
                        //设置新的播放时间
                        Date date = new Date();
                        date.setTime((int) sliderSong.getValue() * 1000);
                        labelTime.setText(new SimpleDateFormat("mm:ss").format(date));
                    }

                    //设置歌词
                    //1、获取当前的播放时间
                    double millis = newValue.toMillis();

                    //2、判断此次是否在正常的播放区间
                    double min = 0;
                    double max = 0;
                    if (currentLrcIndex == 0) {
                        min = 0;
                    } else {
                        min = lrcList.get(currentLrcIndex).doubleValue();
                    }
                    if (currentLrcIndex != lrcList.size() - 1) {
                        max = lrcList.get(currentLrcIndex + 1).doubleValue();
                    } else {
                        max = lrcList.get(currentLrcIndex).doubleValue();
                    }
                    //判断是否在正常的区间
                    if (millis >= min && millis < max) {
                        return;
                    }

                    if (currentLrcIndex < lrcList.size() - 1 &&
                            millis >= lrcList.get(currentLrcIndex + 1).doubleValue()) {
                        currentLrcIndex++;//当前歌词索引的指示器
                        //上移
                        //时间轴动画
                        Timeline timeline1 = new Timeline(new KeyFrame(Duration.millis(15),//每隔15毫秒执行一次
                                new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {//每次执行时，会执行此方法
                                        lrcVBox.setLayoutY(lrcVBox.getLayoutY() - 1);
                                    }
                                }
                        ));
                        timeline1.setCycleCount(45);//执行50次
                        timeline1.play();

                        //当前歌词变量，字号：22
                        Label lab_current = (Label) lrcVBox.getChildren().get(currentLrcIndex);
                        lab_current.setTextFill(Color.YELLOW);
                        //字号：22（动画）
                        Timeline timeline2 = new Timeline(new KeyFrame(Duration.millis(30),
                                new EventHandler<ActionEvent>() {
                                    int startSize = 14;

                                    @Override
                                    public void handle(ActionEvent event) {
                                        lab_current.setFont(new Font("黑体", startSize++));
                                    }
                                }
                        ));
                        timeline2.setCycleCount(12);
                        timeline2.play();

                        //前一行变小，变为：浅灰色
                        Label lab_Pre_1 = (Label) lrcVBox.getChildren().get(currentLrcIndex - 1);
                        if (lab_Pre_1 != null) {
                            Timeline timeline3 = new Timeline(new KeyFrame(Duration.millis(30),
                                    new EventHandler<ActionEvent>() {
                                        int startSize = 24;

                                        @Override
                                        public void handle(ActionEvent event) {
                                            lab_Pre_1.setFont(new Font("黑体", startSize--));
                                        }
                                    }
                            ));
                            timeline3.setCycleCount(12);
                            timeline3.play();
                            timeline3.setOnFinished(e -> lab_Pre_1.setTextFill(Color.rgb(114, 114, 114)));
                        }

                        //前两行的判断
                        if (currentLrcIndex - 2 >= 0) {
                            Label lab_Pre_2 = (Label) lrcVBox.getChildren().get(currentLrcIndex - 2);
                            lab_Pre_2.setTextFill(Color.rgb(53, 53, 53));
                        }

                        //当前行的后一行，白色
                        if (currentLrcIndex + 1 < lrcList.size()) {
                            Label lab_next_1 = (Label) lrcVBox.getChildren().get(currentLrcIndex + 1);
                            lab_next_1.setTextFill(Color.WHITE);
                        }
                    } else if (currentLrcIndex > 0 && millis < lrcList.get(currentLrcIndex).doubleValue()) {
                        //拖动滚动条，回退
                        currentLrcIndex--;
                        //歌词VBox的下移
                        Timeline t1 = new Timeline(new KeyFrame(Duration.millis(15),//每隔15毫秒执行一次
                                new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {//每次执行时，会执行此方法
                                        lrcVBox.setLayoutY(lrcVBox.getLayoutY() + 1);
                                    }
                                }
                        ));
                        t1.setCycleCount(45);//执行50次
                        t1.play();

                        //当前歌词变量
                        Label lab_current = (Label) lrcVBox.getChildren().get(currentLrcIndex);
                        lab_current.setTextFill(Color.YELLOW);

                        //字号：22（动画）
                        Timeline t2 = new Timeline(new KeyFrame(Duration.millis(30),
                                new EventHandler<ActionEvent>() {
                                    int startSize = 14;

                                    @Override
                                    public void handle(ActionEvent event) {
                                        lab_current.setFont(new Font("黑体", startSize++));
                                    }
                                }
                        ));
                        t2.setCycleCount(12);
                        t2.play();

                        //前一行变为：浅灰色
                        if (currentLrcIndex - 1 >= 0) {
                            Label label = (Label) lrcVBox.getChildren().get(currentLrcIndex - 1);
                            label.setTextFill(Color.rgb(114, 114, 114));
                        }
                        //后一行变为白色，字号：14
                        if (currentLrcIndex + 1 < lrcVBox.getChildren().size()) {
                            Label label = (Label) lrcVBox.getChildren().get(currentLrcIndex + 1);
                            label.setTextFill(Color.WHITE);
                            //动画
                            Timeline t3 = new Timeline(new KeyFrame(Duration.millis(30),
                                    new EventHandler<ActionEvent>() {
                                        int startSize = 24;

                                        @Override
                                        public void handle(ActionEvent event) {
                                            label.setFont(new Font("黑体", startSize--));
                                        }
                                    }
                            ));
                            t3.setCycleCount(12);
                            t3.play();
                        }
                        //后两行，变成浅灰色
                        if (currentLrcIndex + 2 < lrcVBox.getChildren().size()) {
                            Label label = (Label) lrcVBox.getChildren().get(currentLrcIndex + 2);
                            label.setTextFill(Color.rgb(114, 114, 114));
                        }
                        //后三行,变为深灰色
                        if (currentLrcIndex + 3 < lrcVBox.getChildren().size()) {
                            Label label = (Label) lrcVBox.getChildren().get(currentLrcIndex + 2);
                            label.setTextFill(Color.rgb(53, 53, 53));
                        }
                    }
                }
            });

            //监听播放完毕时
            mp.setOnEndOfMedia(() -> {
                //1、停止当前播放器的播放
                this.currentPlayBean.getMediaPlayer().stop();

                //2、停止光盘的旋转
                this.timeline.stop();

                //设置歌词位置
                this.lrcVBox.getChildren().clear();
                this.lrcVBox.setLayoutY(50 * 2 - 10);
                this.lrcList.clear();
                this.currentLrcIndex = 0;

                //根据当前的播放模式选择下一首歌
                switch (this.playMode) {
                    case 1://循环播放
                        this.currentIndex++;
                        if (this.currentIndex >= this.tableView.getItems().size()) {
                            currentIndex = 0;
                        }
                        this.currentPlayBean = tableView.getItems().get(this.currentIndex);
                        break;
                    case 2://列表顺序播放
                        this.currentIndex++;
                        if (currentIndex >= this.tableView.getItems().size()) {
                            return;
                        }
                        this.currentPlayBean = tableView.getItems().get(this.currentIndex);
                        break;
                    case 3://单曲循环
                        this.currentPlayBean.getMediaPlayer().seek(new Duration(0));
                        break;
                }
                this.tableView.getSelectionModel().select(currentIndex);
                play();
            });

            playBean.setMediaPlayer(mp);

            //计算文件大小
            BigDecimal bigDecimal = new BigDecimal(file.length());//文件大小，单位：字节
            BigDecimal result = bigDecimal.divide(new BigDecimal(1024 * 1024), 2, RoundingMode.HALF_UP);
            playBean.setLength(result.toString() + " M");//字符串的文件大小

            playBean.setTime(strLength);//字符串时间
            playBean.setTotalSeconds(intLength);//总秒数

            //设置删除图片
            ImageView iv = new ImageView("img/left/laji_2_Dark.png");
            iv.setFitWidth(15);
            iv.setFitHeight(15);

            Label labDelete = new Label("", iv);
            labDelete.setOnMouseEntered(e -> iv.setImage(new Image("img/left/laji_2.png")));
            labDelete.setOnMouseExited(e -> iv.setImage(new Image("img/left/laji_2_Dark.png")));

            labDelete.setAlignment(Pos.CENTER);
            playBean.setLabDelete(labDelete);

            //设置图像
            AbstractID3v2Tag tag = mp3File.getID3v2Tag();
            AbstractID3v2Frame frame = (AbstractID3v2Frame) tag.getFrame("APIC");
            if (frame != null) {
                FrameBodyAPIC body = (FrameBodyAPIC) frame.getBody();
                byte[] imageData = body.getImageData();
                //将字节数组转换为Image对象
                java.awt.Image image = Toolkit.getDefaultToolkit().createImage(imageData, 0, imageData.length);
                BufferedImage bufferedImage = ImageUtils.toBufferedImage(image);
                WritableImage writableImage = SwingFXUtils.toFXImage(bufferedImage, null);
                playBean.setImage(writableImage);
            }

            //将PlayBean封装到集合中
            playBeanList.add(playBean);
        }

        //将PlayBeanList中的数据显示到表格中
        ObservableList<PlayBean> data = FXCollections.observableList(playBeanList);
        this.tableView.getItems().clear();//清空表格
        this.tableView.setItems(data);
    }

    //创建上侧面板
    private BorderPane getTopPane() {
        // 1、左侧的Logo
        ImageView imageView = new ImageView("img/topandbottom/liwu.png");//(后期)自定义改名以及改图片
        imageView.setFitHeight(30);//设置图片像素高度，30像素
        imageView.setPreserveRatio(true);//根据图片设置的高度，保持宽高比
        Label labelText = new Label("音乐播放器", imageView);
        labelText.setTextFill(Color.RED);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);//左对齐
        hBox.setPrefWidth(400);
        hBox.setPrefHeight(30);
        hBox.setMaxHeight(30);
        hBox.setPadding(new Insets(20, 0, 10, 40));//设置内部元素与四边的间距
        hBox.getChildren().add(labelText);

        // 2、右侧的最小化按钮
        ImageView v1 = new ImageView("img/topandbottom/MinmizeDark.png");
        v1.setFitHeight(15);
        v1.setFitWidth(15);
        Label label = new Label("", v1);
        label.setMinHeight(0);//设置label的最小高度
        label.setMinWidth(0);//设置label的最小宽度
        label.setPrefHeight(15);
        label.setPrefWidth(15);

        // 3、右侧的最大化按钮
        ImageView v2 = new ImageView("img/topandbottom/MaximizeDark.png");
        v2.setFitWidth(15);
        v2.setFitHeight(15);
        Label label1 = new Label("", v2);
        label1.setMinHeight(0);//设置label的最小高度
        label1.setMinWidth(0);//设置label的最小宽度
        label1.setPrefHeight(15);
        label1.setPrefWidth(15);

        // 4、右侧的关闭按钮
        ImageView v3 = new ImageView("img/topandbottom/CloseDark.png");
        v3.setFitHeight(15);
        v3.setFitWidth(15);
        Label label2 = new Label("", v3);
        label2.setMinHeight(15);//设置label的最小高度
        label2.setMinWidth(15);//设置label的最小宽度
        label2.setPrefHeight(15);
        label2.setPrefWidth(15);

        //鼠标移入移出事件
        label.setOnMouseEntered(e -> v1.setImage(new Image("img/topandbottom/Minmize.png")));
        label.setOnMouseExited(e -> v1.setImage(new Image("img/topandbottom/MinmizeDark.png")));
        label1.setOnMouseEntered(e -> v2.setImage(new Image("img/topandbottom/maximize.png")));
        label1.setOnMouseExited(e -> v2.setImage(new Image("img/topandbottom/MaximizeDark.png")));
        label2.setOnMouseEntered(e -> v3.setImage(new Image("img/topandbottom/Close.png")));
        label2.setOnMouseExited(e -> v3.setImage(new Image("img/topandbottom/CloseDark.png")));

        //鼠标点击事件
        label.setOnMouseClicked(e -> staticprimary.setIconified(true));//窗口最小化的实现
        label1.setOnMouseClicked(e -> {
            //如果当前窗体是正常，应最大化
            if (!staticprimary.isMaximized()) {//正常模式
                //记录正常模式下的x，y坐标以及宽度和高度
                resetX = staticprimary.getX();
                resetY = staticprimary.getY();
                resetWidth = staticprimary.getWidth();
                resetHeight = staticprimary.getHeight();
                //最大化
                staticprimary.setMaximized(true);

                //设置图片
                v2.setImage(new Image("img/topandbottom/resetDark.png"));
                label1.setOnMouseEntered(ee -> v2.setImage(new Image("img/topandbottom/reset.png")));
                label1.setOnMouseExited(ee -> v2.setImage(new Image("img/topandbottom/resetDark.png")));
            } else {
                //如果当前窗口是最大化，就还原窗口
                staticprimary.setX(resetX);
                staticprimary.setY(resetY);
                staticprimary.setWidth(resetWidth);
                staticprimary.setHeight(resetHeight);
                //设置还原状态
                staticprimary.setMaximized(false);
                label1.setOnMouseEntered(ee -> v2.setImage(new Image("img/topandbottom/maximize.png")));
                label1.setOnMouseExited(ee -> v2.setImage(new Image("img/topandbottom/maximizeDark.png")));
            }
        });//窗口最大化的实现
        label2.setOnMouseClicked(e -> {
            //后期实现：记录当前正在播放的歌曲
            System.exit(0);//结束JAVA
            //staticprimary.close();//同样可实现关闭退出程序
        });//窗口关闭的实现

        HBox hBox1 = new HBox(15);//内部元素之间的间距，10像素
        hBox1.setAlignment(Pos.CENTER_LEFT);
        hBox1.setPrefHeight(50);
        hBox1.setPrefWidth(100);
        hBox1.getChildren().addAll(label, label1, label2);

        //下侧的红线
        Rectangle rct = new Rectangle();
        rct.setX(0);
        rct.setY(0);
        rct.setHeight(1);
        rct.setWidth(100);

        //设置背景色--渐变
        Stop[] stops = new Stop[]{
                new Stop(0, Color.rgb(120, 8, 14)),
                new Stop(0.5, Color.RED),
                new Stop(1, Color.rgb(90, 50, 20))
        };
        rct.setFill(new LinearGradient(0, 0, 1, 1,
                true, CycleMethod.NO_CYCLE, stops));

        BorderPane borderPane = new BorderPane();
        borderPane.setLeft(hBox);
        borderPane.setRight(hBox1);
        borderPane.setBottom(rct);

        //将rct的宽度绑定到stage的宽度上
        rct.widthProperty().bind(staticprimary.widthProperty());

        //当鼠标按下时
        borderPane.setOnMousePressed(e -> {
            //记录鼠标相对于窗体(scene)的x，y坐标
            moustX = e.getSceneX();
            moustY = e.getScreenY();
        });

        //当鼠标拖拽时
        borderPane.setOnMouseDragged(e -> {
            //设置新的x，y
            staticprimary.setX(e.getScreenX() - moustX);
            staticprimary.setY(e.getScreenY() - moustY);
        });

        //borderPane.setBackground(new Background(new BackgroundFill(Color.RED,null,null)));
        //borderPane.setPrefHeight(30);

        return borderPane;
    }

    public static void main(String[] args) {
        launch(args);
    }
}