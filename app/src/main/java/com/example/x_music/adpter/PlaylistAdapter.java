package com.example.x_music.adpter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.x_music.R;
import com.example.x_music.activity.PlayListDetailActivity;
import com.example.x_music.datasource.CacheDataSource;
import com.example.x_music.datasource.MusicInfoDB;
import com.example.x_music.datasource.PlaylistDB;
import com.example.x_music.entity.MusicInfo;
import com.example.x_music.entity.PlayList;
import com.xuexiang.xui.widget.textview.supertextview.SuperTextView;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

/**
 * 播放列表适配器
 */
public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlayListViewHolder> {

    private static final String TAG = PlaylistAdapter.class.getName();

    private final List<PlayList> playLists ;

    public PlaylistAdapter(List<PlayList> playLists) {
        this.playLists = playLists;
    }


    // 创建内部类MyViewHolder，继承自RecyclerView.ViewHolder
    public static class PlayListViewHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        SuperTextView metadata;
        LinearLayout layout;

//        private void setPopup(View mview){
//            String[] listItems = new String[]{
//                    "播放歌单",
//                    "下载歌单",
//                    "重命名歌单",
//                    "删除歌单"
//            };
//            XUISimpleAdapter adapter = XUISimpleAdapter.create(mview.getContext(), listItems);
//
//            XUIListPopup mListPopup = new XUIListPopup(mview.getContext(), adapter);
//            mListPopup.create(DensityUtils.dp2px(200), DensityUtils.dp2px(150), new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                    Toast.makeText(mview.getContext(), (i + 1)+"", Toast.LENGTH_SHORT).show();
//                    mListPopup.dismiss();
//                }
//            });
//            mListPopup.setAnimStyle(XUIPopup.ANIM_GROW_FROM_CENTER);
//            mListPopup.setPreferredDirection(XUIPopup.DIRECTION_TOP);
//            mListPopup.show(itemView);
//        }

        public PlayListViewHolder(@NonNull View itemView) {
            super(itemView);
            //寻找id
            icon = itemView.findViewById(R.id.playlist_imageview_icon);
            metadata = itemView.findViewById(R.id.playlist_textview_metadata);
            layout = itemView.findViewById(R.id.playlist_all);
//            icon.setOnLongClickListener(view -> {
//                setPopup(itemView);
//                return true;
//            });
//            metadata.setOnLongClickListener(view -> {
//                setPopup(itemView);
//                return true;
//            });
        }
    }

    @NonNull
    @Override  
    public PlayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 创建item的布局实例  
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        // 返回ViewHolder实例
        return new PlayListViewHolder(itemView);
    }  
  
    @Override  
    public void onBindViewHolder(@NonNull PlayListViewHolder holder, @SuppressLint("RecyclerView") int position) {
        //获取当前歌单
        PlayList playList = playLists.get(position);
        //获取歌单下的歌曲id
        String[] idsArray = PlaylistDB.getMusicInfoIdsArray(playList);
        try {
            //拿第一首的歌曲id
            String id0 = idsArray[0];
            if (!id0.isEmpty()){
                //根据歌曲id查询循序
                MusicInfo info = MusicInfoDB.getById(Integer.parseInt(id0));
                //从缓存中获取图片
                File fileByFileName = CacheDataSource.getFileByFileName(info.getPicCacheId());
                if (fileByFileName != null){
                    //设置图片
                    holder.icon.setImageURI(Uri.fromFile(fileByFileName));
                }else {
                    //缓存中没有，异步下载后设置图片
                    CacheDataSource.download(info.getPic(), info.getPicCacheId(),file -> {
                        if (file != null) holder.icon.post(() -> holder.icon.setImageURI(Uri.fromFile(file)));
                    });
                }
            }
        }catch (Exception e){
            Log.i(TAG, "onBindViewHolder: 图片设置失败");
        }
        //设置歌单名
        holder.metadata.setLeftTopString(playList.getPlayListName());
        //设置歌曲数
        holder.metadata.setLeftBottomString(idsArray.length + "首");
        //点击跳转
        holder.icon.setOnClickListener(v -> startActivity(v.getContext(), playList.getId()));
        //点击跳转
        holder.metadata.setOnClickListener(v -> startActivity(v.getContext(), playList.getId()));
    }

    /**
     * 跳转activity
     * @param context
     * @param id
     */
    private static void startActivity(Context context, int id) {
        Intent intent = new Intent(context, PlayListDetailActivity.class);
        intent.putExtra("id", id);
        context.startActivity(intent);
    }

    // 返回数据的数量
    @Override  
    public int getItemCount() {

        return playLists.size();
    }

}