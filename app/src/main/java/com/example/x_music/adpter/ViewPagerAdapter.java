package com.example.x_music.adpter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.example.x_music.fragment.PlayerFragment;
import com.example.x_music.fragment.PlaylistFragment;
import com.example.x_music.fragment.SearchFragment;
import com.example.x_music.fragment.SettingFragment;
import com.example.x_music.util.XMusicPlayer;
import org.jetbrains.annotations.NotNull;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private final Context context;
    private final String[] tabTitles = new String[]{
            "播放器",
            "我的歌单",
            "搜索",
            "设置"
    };

    public ViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @NonNull
    @NotNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch (position){
            case 0:
                fragment = new PlayerFragment();
                break;
            case 1:
                fragment = new PlaylistFragment();
                break;
            case 2:
                fragment = new SearchFragment();
                break;
            case 3:
                fragment = new SettingFragment();
                break;
            default:
                fragment = new Fragment();
        }
        return fragment;
    }



    @Override
    public int getCount() {
        return tabTitles.length;
    }
}