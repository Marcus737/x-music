package com.example.x_music.adpter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.x_music.R;
import com.example.x_music.util.XMusicPlayer;
import com.example.x_music.entity.MusicInfo;
import com.xuexiang.xui.widget.imageview.RadiusImageView;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PopupPlaylistAdapter extends RecyclerView.Adapter<PopupPlaylistAdapter.PopupPlaylistViewHolder>{

    private List<MusicInfo> list;
    private XMusicPlayer player;

    public PopupPlaylistAdapter(XMusicPlayer player) {
        this.list = player.getPlayList();
        this.player = player;
    }

    public static class PopupPlaylistViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        RadiusImageView del;

        public PopupPlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.popup_playlist_title);
            del = itemView.findViewById(R.id.popup_playlist_del);

        }
    }

    @NonNull
    @NotNull
    @Override
    public PopupPlaylistAdapter.PopupPlaylistViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        // 创建item的布局实例
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_popup_playlist, parent, false);
        // 返回ViewHolder实例
        return new PopupPlaylistAdapter.PopupPlaylistViewHolder(itemView);
    }



    @Override
    public void onBindViewHolder(@NonNull @NotNull PopupPlaylistAdapter.PopupPlaylistViewHolder holder, int position) {
       //获取当前歌曲信息
        MusicInfo musicInfo = list.get(position);
        holder.title.setText(musicInfo.getTitle());
        //删除player的歌单id
        holder.del.setOnClickListener(v -> {
            player.delMusicInfoByPos(holder.getAbsoluteAdapterPosition());
            notifyItemRemoved(holder.getAbsoluteAdapterPosition());
        });
        //player播放指定位置的歌曲
        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.play(holder.getAbsoluteAdapterPosition());
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
