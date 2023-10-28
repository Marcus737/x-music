package com.example.x_music.adpter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.x_music.R;
import com.example.x_music.callback.VoidCallBack;
import com.example.x_music.activity.ContextApp;
import com.example.x_music.datasource.MusicInfoDB;
import com.example.x_music.datasource.PlaylistDB;
import com.example.x_music.entity.MusicInfo;
import com.example.x_music.entity.PlayList;
import com.example.x_music.util.DownloadPool;
import com.example.x_music.util.ToastUtil;
import com.xuexiang.xui.widget.imageview.RadiusImageView;
import com.xuexiang.xui.widget.textview.supertextview.SuperTextView;
import org.jetbrains.annotations.NotNull;

public class PlayListDetailAdapter extends RecyclerView.Adapter<PlayListDetailAdapter.PlayListDetailAdapterHolder>{

    private String[] musicIds;
    private PlayList playList;

    public PlayListDetailAdapter(PlayList playList, String[] musicIds){
        this.musicIds = musicIds;
        this.playList = playList;
    }

    public static class PlayListDetailAdapterHolder extends RecyclerView.ViewHolder {

        SuperTextView metaData;
        RadiusImageView play, download, del;

        public PlayListDetailAdapterHolder(@NonNull View itemView) {
            super(itemView);
            //寻找view
            metaData = itemView.findViewById(R.id.item_playlist_detail_metadata);
            play = itemView.findViewById(R.id.item_playlist_detail_play);
            download = itemView.findViewById(R.id.item_playlist_detail_download);
            del = itemView.findViewById(R.id.item_playlist_detail_del);
        }
    }

    @NonNull
    @NotNull
    @Override
    public PlayListDetailAdapter.PlayListDetailAdapterHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        // 创建item的布局实例
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist_deatil, parent, false);
        // 返回ViewHolder实例
        return new PlayListDetailAdapter.PlayListDetailAdapterHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PlayListDetailAdapter.PlayListDetailAdapterHolder holder, @SuppressLint("RecyclerView") int position) {
        //根据当前musicid查找musicinfo
        MusicInfo info = MusicInfoDB.getById(Integer.parseInt(musicIds[position]));
        //设置歌曲名
        holder.metaData.setLeftTopString(info.getTitle());
        //设置歌手名
        holder.metaData.setLeftBottomString(info.getAuthor());
        //设置点击事件，点击播放
        holder.play.setOnClickListener(v -> {
            ContextApp.getPlayer().playMusic(info, false);
        });
        //下载歌曲
        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //异步下载
                DownloadPool.download(info, new VoidCallBack<MusicInfo>() {
                    @Override
                    public void onSucceed(MusicInfo musicInfo) {
                        //成功回调
                        holder.download.post(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.toast(musicInfo.getTitle() + "下载成功", holder.download.getContext());
                            }
                        });

                    }
                    @Override
                    public void onFail() {
                        //失败回调
                        holder.download.post(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.toast(info.getTitle() + "下载失败", holder.download.getContext());
                            }
                        });

                    }
                });
            }
        });
        //移除歌单
        holder.del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //更新歌单信息
                PlaylistDB.deleteMusicInfoId(playList, musicIds[position]);
                //重新复制id
                String[] ns = new String[musicIds.length - 1];
                for (int i = 0, j = 0; i < musicIds.length; i++) {
                    if (i  != position) ns[j++] = musicIds[i];
                }
                musicIds = ns;
                //通知更新
                notifyItemRemoved(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return musicIds == null ? 0 : musicIds.length;
    }
}
