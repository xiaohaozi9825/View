package com.ec.myview;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import pw.xiaohaozi.view.AutoScrollView;
import pw.xiaohaozi.view.AutoScrollView2;
import pw.xiaohaozi.view.LoginView;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AutoScrollView2 asv = findViewById(R.id.asv);
        final TextView tv_gun = findViewById(R.id.tv_gun);

        asv.setScrollCallBack(new AutoScrollView.ScrollCallBack() {
            @Override
            public void onStart(View child) {

            }

            @Override
            public void onEnd(View child) {

            }

            @Override
            public void onRepeat(View child) {
//                tv_gun.setText("天王盖地虎，小鸡炖蘑菇，蘑菇炖不烂，还得加大蒜，宝塔镇5河妖，河妖镇不住+++");
            }

        });
//      tv_gun.setText("布局定义了界面的视觉结构，如 Activity 或应用微件的界面。您可通过两种方式声明布局：Android 框架让您可以灵活地使用这两种或其中一种方");
////        tv_gun.postDelayed(() -> asv.start(),5000);
////        tv_gun.postDelayed(() -> {
////            tv_gun.setText("你好，现在尺寸发生了改变了");
////
////        }, 5_000);
//        tv_gun.postDelayed(() -> tv_gun.setText("通知是指 Android 在您应用的界面之外显示的消息，旨在向用户提供提醒、来自他人的通信信息或您应用中的其他实时信息。用户可以点按通知"), 30_000);
//        tv_gun.postDelayed(() -> tv_gun.setText("天王盖地虎，小鸡炖蘑菇，"), 60_000);
//        tv_gun.postDelayed(() -> tv_gun.setText("布局定义了界面的视觉结构，如 Activity 或应用微件的界面。您可通过两种方式声明布局：Android 框架让您可以灵活地使用这两种或其中一种方"), 90_000);
//        tv_gun.postDelayed(() -> tv_gun.setText("布局定义了界面的视觉结构，如 Activity 或应用微件的界面。您可通过两种方式声明布局：Android 框架让您可以灵活地使用这两种或其中一种方"), 120_000);
//        tv_gun.postDelayed(() -> tv_gun.setText("天王盖地虎，小鸡炖蘑菇，蘑菇炖不烂，还得加大蒜，宝塔镇河妖，河妖镇不住"), 150_000);
//

        /** loginview*****************/
        final LoginView login_view = findViewById(R.id.login_view);
        login_view.setOnClickListener(v -> login_view.postDelayed(() -> login_view.loadComplete(() -> {
            // login_view.setText("登录成功");
        }), 2000));

    }
}
