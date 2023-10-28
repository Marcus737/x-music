package com.example.x_music;

import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.example.x_music.adpter.ViewPagerAdapter;
import com.example.x_music.datasource.CacheDataSource;
import com.google.android.material.tabs.TabLayout;
import com.xuexiang.xui.XUI;
import com.xuexiang.xui.widget.textview.MarqueeTextView;
import com.xuexiang.xui.widget.textview.marqueen.DisplayEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        XUI.initTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTabLayout();
    }

    private void initTabLayout(){
        ViewPager viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        viewPager.setAdapter(new ViewPagerAdapter(this, getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
        int[] icons = new int[]{
                R.drawable.play,
                R.drawable.my_playlist,
                R.drawable.search,
                R.drawable.setting
        };
        for (int i = 0; i < icons.length; i++) {
            tabLayout.getTabAt(i).setIcon(icons[i]);
        }
    }


}