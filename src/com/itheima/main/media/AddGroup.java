package com.itheima.main.media;

import com.itheima.main.utils.XMLUtils;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;

public class AddGroup {
    private Stage parentStage;//父窗体
    private VBox groupVBox;//父窗体中显示歌单列表的VBox对象
    private MainApp mainApp;

    //移动前的x,y坐标
    private double mouseX;
    private double mouseY;

    //本窗体的舞台对象
    private Stage stage;

    public AddGroup(Stage parentStage, VBox groupVBox, MainApp mainApp) {
        this.parentStage = parentStage;
        this.groupVBox = groupVBox;
        this.mainApp = mainApp;

        //1、新建歌单：Label
        Label label = new Label("新建歌单");
        label.setTextFill(Color.WHITE);
        label.setPrefWidth(150);
        label.setPrefHeight(50);
        label.setLayoutX(20);
        label.setLayoutY(10);

        //2、关闭按钮：ImageView
        ImageView view = new ImageView("img/topandbottom/closeDark.png");
        view.setFitHeight(13);
        view.setFitWidth(13);
        Label label2 = new Label("", view);
        label2.setMinHeight(13);
        label2.setMinWidth(13);
        label2.setPrefWidth(13);
        label2.setPrefHeight(13);
        label2.setLayoutX(270);
        label2.setLayoutY(25);
        label2.setOnMouseEntered(e -> view.setImage(new Image("img/topandbottom/close.png")));
        label2.setOnMouseExited(e -> view.setImage(new Image("img/topandbottom/closeDark.png")));
        label2.setOnMouseClicked(e -> {
            stage.hide();
        });

        //3、文本框：TextField
        TextField txtGroupName = new TextField();
        txtGroupName.setPromptText("请输入歌单名称");
        txtGroupName.setPrefHeight(15);
        txtGroupName.setPrefWidth(220);
        txtGroupName.setLayoutX(20);
        txtGroupName.setLayoutY(70);

        //4、提示标签：Label
        Label labMsg = new Label();
        labMsg.setPrefWidth(200);
        labMsg.setLayoutX(20);
        labMsg.setLayoutY(100);
        labMsg.setTextFill(Color.BLUE);

        //5、确定按钮：Button
        Button butOK = new Button("确定");
        butOK.setPrefWidth(80);
        butOK.setPrefHeight(30);
        butOK.setLayoutX(50);
        butOK.setLayoutY(190);
        butOK.setTextFill(Color.WHITE);
        butOK.setBackground(new Background(new BackgroundFill(Color.rgb(50, 45, 128), null, null)));
        butOK.setOnMouseEntered(e -> butOK.setTextFill(Color.BLACK));
        butOK.setOnMouseExited(e -> butOK.setTextFill(Color.WHITE));
        butOK.setOnMouseClicked(e -> {
            //1、获取文本框的数据
            String txt = txtGroupName.getText().trim();
            //判断
            if (txt == null || txt.length() == 0) {
                labMsg.setText("请输入歌单名称");
                return;
            }
            //2、验证新的歌单名是否重复
            List<String> listGroupName = XMLUtils.readAllGroup();
            for (String s : listGroupName) {
                if (e.equals(s)) {
                    labMsg.setText("歌单名称：" + txt + "已经存在");
                    return;
                }
            }
            //3、写入MusicGroup.xml中
            XMLUtils.addGroup(txt);

            //4、更新主窗体上的VBox列表
            //1、心形图标：ImageView
            ImageView iv1 = new ImageView("img/left/xinyuanDark.png");
            iv1.setFitWidth(15);
            iv1.setPreserveRatio(true);
            Label labX = new Label("", iv1);
            labX.setMinHeight(0);
            labX.setMinWidth(0);
            labX.setPrefHeight(15);
            labX.setPrefWidth(15);
            labX.setOnMouseEntered(ee -> iv1.setImage(new Image("img/left/xinyuan.png")));
            labX.setOnMouseExited(ee -> iv1.setImage(new Image("img/left/xinyuanDark.png")));

            //2、歌单名称：Label
            Label labGroupName = new Label(txt);
            labGroupName.setMinWidth(0);
            labGroupName.setMinHeight(0);
            labGroupName.setPrefHeight(15);
            labGroupName.setPrefWidth(150);
            labGroupName.setTextFill(Color.rgb(210, 210, 210));
            labGroupName.setOnMouseEntered(ee -> labGroupName.setTextFill(Color.rgb(200, 0, 0)));
            labGroupName.setOnMouseExited(ee -> labGroupName.setTextFill(Color.rgb(210, 210, 210)));

            //3、播放图片：ImageView
            ImageView iv2 = new ImageView("img/left/volumn_1_Dark.png");
            iv2.setFitWidth(15);
            iv2.setFitHeight(15);
            Label labTP = new Label("", iv2);
            labTP.setMinWidth(0);
            labTP.setMinHeight(0);
            labTP.setPrefWidth(15);
            labTP.setPrefHeight(15);
            labTP.setOnMouseEntered(ee -> iv2.setImage(new Image("img/left/volumn_1.png")));
            labTP.setOnMouseExited(ee -> iv2.setImage(new Image("img/left/volumn_1_Dark.png")));

            //4、+符号：ImageView
            ImageView iv3 = new ImageView("img/left/addDark.png");
            iv3.setFitWidth(15);
            iv3.setFitHeight(15);
            Label labAdd = new Label("", iv3);
            labAdd.setMinHeight(0);
            labAdd.setMinWidth(0);
            labAdd.setPrefHeight(15);
            labAdd.setPrefWidth(15);
            labAdd.setOnMouseEntered(ee -> iv3.setImage(new Image("img/left/add.png")));
            labAdd.setOnMouseExited(ee -> iv3.setImage(new Image("img/left/addDark.png")));

            //5、垃圾桶符号：ImageView
            ImageView iv4 = new ImageView("img/left/laji_1_Dark.png");
            iv4.setFitWidth(15);
            iv4.setFitHeight(15);
            Label labLJ = new Label("", iv4);
            labLJ.setMinHeight(0);
            labLJ.setMinWidth(0);
            labLJ.setPrefHeight(15);
            labLJ.setPrefWidth(15);
            labLJ.setOnMouseEntered(ee -> iv4.setImage(new Image("img/left/laji_1.png")));
            labLJ.setOnMouseExited(ee -> iv4.setImage(new Image("img/left/laji_1_Dark.png")));

            HBox hBox1 = new HBox(5);
            hBox1.getChildren().addAll(labX, labGroupName, labTP, labAdd, labLJ);
            hBox1.setPadding(new Insets(5, 0, 5, 0));
            this.groupVBox.getChildren().add(hBox1);

            //关闭此舞台
            this.stage.hide();
        });

        //6、取消按钮：Button
        Button butCancel = new Button("取消");
        butCancel.setPrefWidth(80);
        butCancel.setPrefHeight(30);
        butCancel.setLayoutX(150);
        butCancel.setLayoutY(190);
        butCancel.setTextFill(Color.WHITE);
        butCancel.setBackground(new Background(new BackgroundFill(Color.rgb(100, 100, 100), null, null)));
        butCancel.setOnMouseClicked(e -> stage.hide());
        butCancel.setOnMouseEntered(e -> butCancel.setTextFill(Color.BLACK));
        butCancel.setOnMouseExited(e -> butCancel.setTextFill(Color.WHITE));

        //创建一个新的舞台对象
        stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        //创建一个场景
        Group group = new Group();
        group.getChildren().addAll(label, label2, txtGroupName, labMsg, butOK, butCancel);

        Scene scene = new Scene(group, 300, 240);
        scene.setFill(Color.rgb(120, 100, 150));

        scene.setOnMousePressed(e -> {
            //记录原位置
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
        });
        scene.setOnMouseDragged(e -> {
            //设置新位置
            stage.setX(e.getScreenX() - mouseX);
            stage.setY(e.getScreenY() - mouseY);
        });

        //设置场景
        stage.setScene(scene);
        //显示舞台
        stage.show();
    }
}
