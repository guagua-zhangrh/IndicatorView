package com.zhangrenhua.indicatorview.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.zhangrenhua.indicatorview.CircleIndicatorView;
import com.zhangrenhua.indicatorview.R;
import com.zhangrenhua.indicatorview.RectIndicatorView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private ViewPager mViewPager;
    private List<View> mViews;

    private void init() {
        mViewPager = (ViewPager) findViewById(R.id.viewpage);

        mViews = new ArrayList<View>();
        View view0 = new View(this);
        view0.setBackgroundColor(Color.WHITE);
        mViews.add(view0);
        View view1 = new View(this);
        view1.setBackgroundColor(Color.WHITE);
        mViews.add(view1);
        View view2 = new View(this);
        view2.setBackgroundColor(Color.WHITE);
        mViews.add(view2);
        View view3 = new View(this);
        view3.setBackgroundColor(Color.WHITE);
        mViews.add(view3);

        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mViews.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(mViews.get(position));
                return (mViews.get(position));
            }

            @Override
            public int getCount() {
                if (mViews != null)
                    return mViews.size();
                return 0;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        });

        CircleIndicatorView circleIndicatorView0 = (CircleIndicatorView) findViewById(R.id.circle_indicator_view0);
        CircleIndicatorView circleIndicatorView1 = (CircleIndicatorView) findViewById(R.id.circle_indicator_view1);
        CircleIndicatorView circleIndicatorView2 = (CircleIndicatorView) findViewById(R.id.circle_indicator_view2);
        CircleIndicatorView circleIndicatorView3 = (CircleIndicatorView) findViewById(R.id.circle_indicator_view3);
        circleIndicatorView0.setViewPager(mViewPager);
        circleIndicatorView1.setViewPager(mViewPager);
        circleIndicatorView2.setViewPager(mViewPager);
        circleIndicatorView3.setViewPager(mViewPager);

        RectIndicatorView rectIndicatorView0 = (RectIndicatorView) findViewById(R.id.rect_indicator_view0);
        RectIndicatorView rectIndicatorView1 = (RectIndicatorView) findViewById(R.id.rect_indicator_view1);
        RectIndicatorView rectIndicatorView2 = (RectIndicatorView) findViewById(R.id.rect_indicator_view2);
        rectIndicatorView0.setViewPager(mViewPager);
        rectIndicatorView1.setViewPager(mViewPager);
        rectIndicatorView2.setViewPager(mViewPager);
    }
}
