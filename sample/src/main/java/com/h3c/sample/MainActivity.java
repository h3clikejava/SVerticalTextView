package com.h3c.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.h3c.sverticaltextview.SVerticalTextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SVerticalTextView tv = ((SVerticalTextView) findViewById(R.id.svtv));
        tv.setText("梅兰芳的老师说输不丢人，怕才丢人，你说呢？");

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 横竖切换
//                if(tv.getOrientation() == LinearLayout.HORIZONTAL) {
//                    tv.setOrientation(LinearLayout.VERTICAL);
//                } else {
//                    tv.setOrientation(LinearLayout.HORIZONTAL);
//                }

                // 放大缩小
                if(tv.getScaleX() == 0.5) {
                    tv.setScaleX(0.5f);
                    tv.setScaleY(0.5f);
                } else {
                    tv.setScaleX(1);
                    tv.setScaleY(1);
                }
            }
        });
    }

}
