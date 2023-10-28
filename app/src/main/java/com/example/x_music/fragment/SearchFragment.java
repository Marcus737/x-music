package com.example.x_music.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.x_music.R;
import com.example.x_music.callback.VoidCallBack;
import com.example.x_music.adpter.SearchAdapter;
import com.example.x_music.entity.MusicInfo;
import com.example.x_music.music_api.WuSunMusicApi;
import com.xuexiang.xui.widget.button.ButtonView;
import com.xuexiang.xui.widget.searchview.MaterialSearchView;
import com.xuexiang.xui.widget.statelayout.StatefulLayout;

import java.util.ArrayList;
import java.util.List;


public class SearchFragment extends Fragment {


    private static class CacheData{
        int page;
        String keyword;
        List<MusicInfo> musicInfoList;

        public CacheData() {
            page = 0;
            keyword = "";
            musicInfoList = new ArrayList<>();
        }
    }

    RecyclerView recyclerView;
    MaterialSearchView editText;
    StatefulLayout statefulLayout;
    ButtonView btn;
    CacheData cacheData;


    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cacheData = new CacheData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cacheData = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View fragment = inflater.inflate(R.layout.fragment_search, container, false);
        recyclerView = fragment.findViewById(R.id.fragment_search_recycle_view);
        editText = fragment.findViewById(R.id.fragment_search_view);
        btn = fragment.findViewById(R.id.fragment_search_btn);
        statefulLayout = fragment.findViewById(R.id.fragment_search_stateful);
        return fragment;
    }




    @Override
    public void onStart() {
        super.onStart();
        // 选择你要的布局方式，这里以LinearLayout为例
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        // 将布局管理器设置到RecyclerView上
        recyclerView.setLayoutManager(linearLayoutManager);
        SearchAdapter adapter;
        //获取缓存的搜索数据
        List<MusicInfo> cacheList = cacheData.musicInfoList;
        if (cacheList != null) adapter = new SearchAdapter(cacheList);
        else adapter = new SearchAdapter();
        //设置适配器
        recyclerView.setAdapter(adapter);
        //滑倒底部自动加载更多数据
        recyclerView.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(androidx.recyclerview.widget.RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE) {
                    // 检测是否滑动到底部
                    int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                    int itemCount = adapter.getItemCount();

                    if (lastVisibleItemPosition == itemCount - 1) {
                        // 滑动到底部
                        ++cacheData.page;
                        searchMusic(recyclerView, adapter);
                    }

                }
            }
        });

        //搜索控件的配置
        editText.setVoiceSearch(false);
        editText.setEllipsize(true);
        editText.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //当回车键被按下，进行新的搜索
                if (query.equals(cacheData.keyword)) {
                    return false;
                }
                cacheData.musicInfoList.clear();
                cacheData.page = 1;
                cacheData.keyword = query;
                searchMusic(recyclerView, adapter);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });
        editText.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }
            @Override
            public void onSearchViewClosed() {
                btn.setVisibility(View.VISIBLE);
            }
        });
        editText.setSubmitOnClick(true);
        btn.setOnClickListener(v -> {
            btn.setVisibility(View.GONE);
            editText.showSearch();
        });
    }

    /**
     * 搜索音乐
     * @param recyclerView
     * @param adapter
     */
    private void searchMusic(RecyclerView recyclerView, SearchAdapter adapter) {
        //显示加载状态
        statefulLayout.showLoading();
        //异步搜索
        WuSunMusicApi.search(cacheData.keyword, cacheData.page, new VoidCallBack<List<MusicInfo>>() {
            @Override
            public void onSucceed(List<MusicInfo> list) {
                //成功回调
                recyclerView.post(() -> {
                    //如果当前的page大于1，说明是查找更多数据
                    if (cacheData.page > 1){
                        adapter.addList(list);
                        cacheData.musicInfoList.addAll(list);
                    }else {
                        //否则就是新的查找
                        adapter.updateList(list);
                        cacheData.musicInfoList = list;
                    }
                    //显示内容
                    statefulLayout.showContent();
                });
            }
            @Override
            public void onFail() {
                //失败回调
                statefulLayout.showContent();
            }
        });
    }


}