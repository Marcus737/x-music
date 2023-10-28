package com.example.x_music.adpter;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.x_music.R;
import com.example.x_music.callback.VoidCallBack;
import com.example.x_music.activity.ContextApp;
import com.example.x_music.datasource.PlaylistDB;
import com.example.x_music.entity.MusicInfo;
import com.example.x_music.entity.PlayList;
import com.example.x_music.util.DownloadPool;
import com.example.x_music.util.ToastUtil;
import com.xuexiang.xui.widget.button.ButtonView;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.imageview.RadiusImageView;
import com.xuexiang.xui.widget.textview.supertextview.SuperTextView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.xuexiang.xui.utils.ResUtils.getString;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder>{


    private List<MusicInfo> list;

    public SearchAdapter( ){
        this.list = new ArrayList<>();
    }

    public SearchAdapter( List<MusicInfo> list){
        this.list = list;
    }

    /**
     * 直接换新的列表
     * @param list 列表
     */
    public void updateList(List<MusicInfo> list){
        this.list = list;
        notifyItemRangeChanged(0, list.size());
    }

    /**
     * 在旧的列表上添加新的数据
     * @param list 列表
     */
    public void addList(List<MusicInfo> list) {
        int r = list.size() - 1;
        this.list.addAll(list);
        notifyItemRangeChanged(r, list.size());
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {


        SuperTextView metaData;
        RadiusImageView play, download, addToPlaylist;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            metaData = itemView.findViewById(R.id.item_search_metadata);
            play = itemView.findViewById(R.id.item_search_play);
            download = itemView.findViewById(R.id.item_search_download);
            addToPlaylist = itemView.findViewById(R.id.item_search_add_to_playlist);
        }
    }

    @NonNull
    @NotNull
    @Override
    public SearchAdapter.SearchViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        // 创建item的布局实例
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false);
        // 返回ViewHolder实例
        return new SearchAdapter.SearchViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SearchAdapter.SearchViewHolder holder, int position) {
        //获取当前的音乐信息
        MusicInfo musicInfo = list.get(position);

        holder.metaData.setLeftTopString(musicInfo.getTitle());
        holder.metaData.setLeftBottomString(musicInfo.getAuthor());
        //播放指定的音乐
        holder.play.setOnClickListener(view -> {
            ContextApp.getPlayer().playMusic(musicInfo, false);
        });
        //下载音乐
        holder.download.setOnClickListener(view -> {
            //异步下载
            DownloadPool.download(musicInfo, new VoidCallBack<MusicInfo>() {
                @Override
                public void onSucceed(MusicInfo musicInfo) {
                    //成功回调
                    holder.download.post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.toast(musicInfo.getTitle() + "下载成功");
                        }
                    });

                }

                @Override
                public void onFail() {
                    //失败回调
                    holder.download.post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.toast(musicInfo.getTitle() + "下载失败");
                        }
                    });

                }
            });
        });
        //添加到歌单
        holder.addToPlaylist.setOnClickListener(v -> {
            Context context = holder.addToPlaylist.getContext();
            //查询所有歌单
            List<PlayList> list = PlaylistDB.selectList();
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.popup_collect, null);
            //设置recyclerView数据源
            RecyclerView recyclerView = view.findViewById(R.id.popup_collect_list);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(linearLayoutManager);
            //设置dapter
            PopupCollectListAdapter listAdapter = new PopupCollectListAdapter(list, musicInfo);
            recyclerView.setAdapter(listAdapter);
            //插入歌单
            ButtonView createBtn = view.findViewById(R.id.popup_collect_new);
            createBtn.setOnClickListener(v1 -> new MaterialDialog.Builder(context)
                    .title("输入歌单名")
                    .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                    .input(getString(R.string.app_name), "", false, ((dialog, input) -> {}))
                    .positiveText("确认")
                    .negativeText("取消")
                    .onPositive(((dialog, which) -> {
                        //插入歌单
                        String name = dialog.getInputEditText().getText().toString();
                        PlayList playList = PlayList.builder()
                                .playListName(name)
                                .build();
                        PlaylistDB.insert(playList);
                        //通知更新
                        listAdapter.insertItem(playList);
                    }))
                    .cancelable(false)
                    .show());
            //选择歌单
            new MaterialDialog.Builder(context)
                    .customView(view, true)
                    .title("选择收藏的歌单")
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
