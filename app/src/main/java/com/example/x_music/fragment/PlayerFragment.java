package com.example.x_music.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.x_music.R;
import com.example.x_music.activity.ContextApp;
import com.example.x_music.adpter.PopupCollectListAdapter;
import com.example.x_music.adpter.PopupPlaylistAdapter;
import com.example.x_music.datasource.CacheDataSource;
import com.example.x_music.datasource.MusicInfoDB;
import com.example.x_music.datasource.PlaylistDB;
import com.example.x_music.entity.MusicInfo;
import com.example.x_music.entity.PlayList;
import com.example.x_music.entity.PlayerMode;
import com.example.x_music.util.LrcParser;
import com.example.x_music.util.XMusicPlayer;
import com.xuexiang.xui.widget.button.ButtonView;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.imageview.RadiusImageView;
import com.xuexiang.xui.widget.popupwindow.popup.XUIPopup;
import com.xuexiang.xui.widget.textview.supertextview.SuperTextView;

import java.io.File;
import java.util.List;


public class PlayerFragment extends Fragment {

    private  XMusicPlayer player;
    private SuperTextView metaData;
    private RadiusImageView pic;
    private SuperTextView lrc;
    private TextView time;
    private SeekBar progress;
    private ObjectAnimator picAnimator;
    private RadiusImageView playMode, prevBtn, playBtn, nextBtn, playlistBtn;
    private LrcParser lrcParser;


    @Override
    public void onStart() {
        super.onStart();
        //获取当前播放器
        player = ContextApp.getPlayer();
        //设置图片选择
        setPicRotation();

        // 当用户拖动SeekBar时，更新音乐的播放位置
        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // 跳转到指定位置
                    player.setPlayPercent((float) progress / 100);
                }
            }
            // 监听开始触摸事件，不需要实现该方法
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            // 监听停止触摸事件，不需要实现该方法
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        //设置音乐信息更新回调
        player.setMusicInfoCallback(musicInfo -> metaData.post(() -> updateMusicInfo(musicInfo)));
        //设置每秒监听
        player.setTimerUpdateCallback(() -> {
            if (player.isPlaying()) {
                int currentTime = player.getCurrentPosition() / 1000;
                int totalTime = player.getDuration() / 1000;
                @SuppressLint("DefaultLocale") String timeString = String.format("%02d:%02d / %02d:%02d",
                        currentTime / 60, currentTime % 60, totalTime / 60, totalTime % 60);
                time.setText(timeString);
                //设置进度条
                progress.setProgress((int) (((float) currentTime / totalTime) * 100));
            }
            //只能在ui线程更新
            pic.post(() -> {
                //设置图片旋转
                if (!player.isPlaying()) {
                    picAnimator.pause();
                } else {
                    picAnimator.resume();
                }
            });
            //设置滚动歌词
            if (lrcParser != null){
                lrc.post(() -> {
                    int currentTime = player.getCurrentPosition() / 1000;
                    int mm = currentTime / 60, ss = currentTime % 60, hs = ((player.getCurrentPosition() / 10))%100 ;

                    String[] lines = lrcParser.getLrcString(mm, ss, hs);
                    if (lines == null) return;
                    lrc.setCenterTopString(lines[0]);
                    lrc.setCenterString(lines[1]);
                    lrc.setCenterBottomString(lines[2]);
                });
            }
        });
        //设置播放模式
        playMode.setOnClickListener(new View.OnClickListener() {
            int idx = 0;
            @Override
            public void onClick(View view) {
                //设置播放模式
                player.setCurrentPlayMode(PlayerMode.values()[idx]);
                RadiusImageView imageView = (RadiusImageView) view;
                imageView.setImageResource(PlayerMode.values()[idx].getModeRes());
                Toast.makeText(view.getContext(), PlayerMode.values()[idx].getPlayerModeName(), Toast.LENGTH_SHORT).show();
                idx = (idx + 1) % 3;
            }
        });
        //播放前一首
        prevBtn.setOnClickListener(v -> {
            player.playPrev();
            playBtn.setImageResource(R.drawable.m_stop2);
        });
        //播放或暂停
        playBtn.setOnClickListener(v -> {
            RadiusImageView imageView = (RadiusImageView) v;
            if (player.isPlaying()) {
                imageView.setImageResource(R.drawable.m_player);
                player.pause();
            } else {
                imageView.setImageResource(R.drawable.m_stop2);
                player.resume();
            }
        });
        //播放后一首
        nextBtn.setOnClickListener(v -> {
            player.playNext();
            playBtn.setImageResource(R.drawable.m_stop2);
        });
        //点击当前播放列表
        playlistBtn.setOnClickListener(v -> {
            //弹窗
            XUIPopup mNormalPopup = new XUIPopup(getContext());
            View view = LayoutInflater.from(getContext())
                    .inflate(R.layout.popup_playlist, null);
            //设置列表
            RecyclerView recyclerView = view.findViewById(R.id.popup_playlist_list);
            // 选择你要的布局方式，这里以LinearLayout为例
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            // 将布局管理器设置到RecyclerView上
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(new PopupPlaylistAdapter(player));

            //设置按钮
            View clear = view.findViewById(R.id.popup_playlist_clear);
            clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int count = player.getCount();
                    player.clearPlayerList();
                    recyclerView.getAdapter().notifyItemRangeRemoved(0, count);
                }
            });

            //弹窗配置
            mNormalPopup.setContentView(view);
            mNormalPopup.setAnimStyle(XUIPopup.ANIM_GROW_FROM_CENTER);
            mNormalPopup.setPreferredDirection(XUIPopup.DIRECTION_TOP);
            mNormalPopup.show(v);
        });

        //点击pic收藏
        pic.setOnClickListener(v -> {
            //查询所有歌单
            List<PlayList> list = PlaylistDB.selectList();
            View view = LayoutInflater.from(getContext())
                    .inflate(R.layout.popup_collect, null);
            //选择歌单
            MaterialDialog.Builder collectDialog = new MaterialDialog.Builder(getContext())
                    .customView(view, true)
                    .title("选择收藏的歌单");
            collectDialog.show();

            //设置recyclerView数据源
            RecyclerView recyclerView = view.findViewById(R.id.popup_collect_list);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(linearLayoutManager);
            PopupCollectListAdapter listAdapter = new PopupCollectListAdapter(list);
            recyclerView.setAdapter(listAdapter);
            //插入歌单
            ButtonView createBtn = view.findViewById(R.id.popup_collect_new);
            createBtn.setOnClickListener(v1 -> new MaterialDialog.Builder(getContext())
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
        });
        recoverState();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        //寻找view
        prevBtn = view.findViewById(R.id.player_prev);
        playBtn = view.findViewById(R.id.player_play);
        nextBtn = view.findViewById(R.id.player_next);
        pic = view.findViewById(R.id.player_pic);
        time = view.findViewById(R.id.player_time);
        progress = view.findViewById(R.id.player_progress);
        playMode = view.findViewById(R.id.player_play_mode);
        lrc = view.findViewById(R.id.player_lrc);
        metaData = view.findViewById(R.id.player_metadata);
        playlistBtn = view.findViewById(R.id.player_player_list);
        return view;
    }



    /**
     * 恢复状态
     */
    private void recoverState(){
        if (player.getCurrentPlayMusicInfo() != null) updateMusicInfo(player.getCurrentPlayMusicInfo());

        if (!player.isPlaying()) {
            playBtn.setImageResource(R.drawable.m_player);
        } else {
            playBtn.setImageResource(R.drawable.m_stop2);
        }
    }



    private void updateMusicInfo(MusicInfo info){
        //设置歌曲名
        metaData.setCenterTopString(info.getTitle());
        //设置歌手
        metaData.setCenterBottomString(info.getAuthor());
        //设置歌词
        if (info.getLrc() == null || info.getLrc().isEmpty()){
            lrcParser = null;
        }else {
            //从缓存获取
            String lrcCacheId = info.getLrcCacheId();
            File file = CacheDataSource.getFileByFileName(lrcCacheId);
            if (file != null){
                lrcParser = new LrcParser(file);
            }else {
                CacheDataSource.download(info.getLrc(), info.getLrcCacheId(), file12 -> lrcParser = new LrcParser(file12));
            }
        }
        //更新封面
        pic.post(() -> {
            if (info.getPic() != null){
                //从缓存获取
                File file = CacheDataSource.getFileByFileName(info.getPicCacheId());
                if (file != null){
                    pic.setImageURI(Uri.fromFile(file));
                }else {
                    //缓存数据，得到缓存id
                    CacheDataSource.download(info.getPic(),
                            info.getPicCacheId(),
                            file1 -> pic.post(() -> pic.setImageURI(Uri.fromFile(file1))));
                    //更新数据
                    MusicInfoDB.update(info);
                }
            }
        });
    }


    /**
     * 设置封面旋转
     */
    private void setPicRotation() {
        picAnimator = ObjectAnimator.ofFloat(pic, "rotation", 0f, 360f);
        picAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                pic.setRotation(0); // 重置旋转角度为初始状态
                picAnimator.start(); // 启动下一个动画
            }

        });
        //播放速度恒定
        picAnimator.setInterpolator(new LinearInterpolator());
        picAnimator.setDuration(6000);
        picAnimator.start();
        picAnimator.pause();
    }

}