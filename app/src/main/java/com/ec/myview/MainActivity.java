package com.ec.myview;

import androidx.appcompat.app.AppCompatActivity;
import pw.xiaohaozi.view.AutoScrollView;
import pw.xiaohaozi.view.BubbleView;
import pw.xiaohaozi.view.LoginView;

import android.graphics.Path;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private BubbleView mMyBubble;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMyBubble = findViewById(R.id.my_bubble);
        AutoScrollView asv = findViewById(R.id.asv);
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
                tv_gun.setText("天王盖地虎，小鸡炖蘑菇，蘑菇炖不烂，还得加大蒜，宝塔镇5河妖，河妖镇不住+++");
            }

        });
        tv_gun.postDelayed(new Runnable() {
            @Override
            public void run() {
                tv_gun.setText("你好，现在尺寸发生了改变了");
            }
        }, 5000);
        tv_gun.postDelayed(new Runnable() {
            @Override
            public void run() {
                tv_gun.setText("天王盖地虎，小鸡炖蘑菇，蘑菇炖不烂，还得加大蒜，宝塔镇河妖，河妖镇不住");
            }
        }, 20000);


        /** loginview*****************/
        final LoginView login_view = findViewById(R.id.login_view);
        login_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        login_view.loadComplete(new LoginView.CallBack() {
                            @Override
                            public void call() {
                                // login_view.setText("登录成功");
                            }
                        });
                    }
                }, 2000);
            }
        });


        mMyBubble.setDrawIndicator(new BubbleView.DrawIndicator() {
            @Override
            public void drawLeft(Path path, int left, int top, int right, int bottom) {
                int w = right - left;
                int h = bottom - top;
                path.moveTo(right, top + h / 2);
//                path.lineTo(left, top);
                path.arcTo(
                        left,
                        top - h * 3 / 2,
                        right + h,
                        bottom - h / 2,
                        90,
                        72,
                        false
                );
                path.arcTo(
                        left,
                        top - h,
                        right + h,
                        bottom,
                        180,
                        -90,
                        false
                );
                path.lineTo(right, bottom);

            }

            @Override
            public void drawTop(Path path, int left, int top, int right, int bottom) {

            }

            @Override
            public void drawRight(Path path, int left, int top, int right, int bottom) {

            }

            @Override
            public void drawBottom(Path path, int left, int top, int right, int bottom) {

            }
        });
    }
}
