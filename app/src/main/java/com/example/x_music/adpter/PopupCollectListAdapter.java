package com.example.x_music.adpter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.x_music.R;
import com.example.x_music.activity.ContextApp;
import com.example.x_music.datasource.PlaylistDB;
import com.example.x_music.entity.MusicInfo;
import com.example.x_music.entity.PlayList;
import com.xuexiang.xui.widget.imageview.RadiusImageView;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PopupCollectListAdapter extends RecyclerView.Adapter<PopupCollectListAdapter.PopupCollectViewHolder>{

    private List<PlayList> list;

    private MusicInfo musicInfo;

    public PopupCollectListAdapter(List<PlayList> list) {
        this.list = list;
    }

    public PopupCollectListAdapter(List<PlayList> list, MusicInfo info) {
        this.list = list;
        this.musicInfo = info;
    }

    /**
     * 插入歌单
     * @param playList 歌单
     */
    public void insertItem(PlayList playList) {
        list.add(playList);
        notifyItemChanged(list.size() - 1);
    }

    public static class PopupCollectViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        RadiusImageView confirm;

        public PopupCollectViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_collect_title);
            confirm = itemView.findViewById(R.id.item_collect_confirm);
        }
    }

    @NonNull
    @NotNull
    @Override
    public PopupCollectViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        // 创建item的布局实例
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collect, parent, false);
        // 返回ViewHolder实例
        return new PopupCollectViewHolder(itemView);
    }



    @Override
    public void onBindViewHolder(@NonNull @NotNull PopupCollectViewHolder holder, int position) {
        //获取当前歌单
        PlayList playList = list.get(position);
        //设置个单铭
        holder.title.setText(playList.getPlayListName());
        holder.confirm.setOnClickListener(v -> {
            //更新数据库
            PlayList pl = PlaylistDB.selectList().get(position);
            if (this.musicInfo != null) PlaylistDB.addMusicIdToPlaylist(pl, musicInfo);
            else PlaylistDB.addMusicIdToPlaylist(pl, ContextApp.getPlayer().getCurrentPlayMusicInfo());
            Toast.makeText(v.getContext(), "收藏成功", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
