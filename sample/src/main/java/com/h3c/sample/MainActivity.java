package com.h3c.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.h3c.sverticaltextview.SVerticalTextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView tt = ((TextView) findViewById(R.id.tv));
        tt.setText("Why are you so Diao?");

        final SVerticalTextView tv = ((SVerticalTextView) findViewById(R.id.svtv));
//        tv.setText("天荒看你了呢\n地老昆士兰弗兰克阿莱克斯能否离开我呢份", false, true);
//        tv.setText("Why are you so Diao", true, true);
        tv.setText("What are you doing!?\nDo you like ice?", true, true);

        final SVerticalTextView tv2 = ((SVerticalTextView) findViewById(R.id.svtv2));
        tv2.setText("天荒看你了呢\n地老昆士兰弗兰克阿莱克斯能否离开我呢份快乐饿了咖啡能为了肯放开论文疯了快五年翻了可能为分开两年未开发为嫩肤\n你\nsnnw 问哦", false, true);
//        tv2.setText("天荒看你了呢\n地老昆士兰弗兰克阿莱克斯能否离开我呢份", false, true);

        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 横竖切换
                if(tv2.getOrientation() == LinearLayout.HORIZONTAL) {
                    tv2.setOrientation(LinearLayout.VERTICAL);
                } else {
                    tv2.setOrientation(LinearLayout.HORIZONTAL);
                }
            }
        });

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 横竖切换
                if(tv.getOrientation() == LinearLayout.HORIZONTAL) {
                    tv.setOrientation(LinearLayout.VERTICAL);
                } else {
                    tv.setOrientation(LinearLayout.HORIZONTAL);
                }
            }
        });
    }

}
