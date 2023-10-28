package com.example.x_music.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.x_music.MainActivity;
import com.example.x_music.R;
import com.example.x_music.activity.ContextApp;
import com.example.x_music.datasource.CacheDataSource;
import com.example.x_music.service.ReminderService;
import com.example.x_music.util.SharedPreferencesUtil;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.textview.supertextview.SuperTextView;

import static androidx.core.content.ContextCompat.getSystemService;


public class SettingFragment extends Fragment {

    SuperTextView downPath, timer, clearCache;

    public static final String CLOSE_TIME = "close_time";

    /**
     * 设置textview的最多显示字数
     * @param view textview
     * @param maxnLen 最多数量
     */
    private void setTextViewMaxLen(TextView view, int maxnLen){
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxnLen);
        view.setFilters(fArray);
    }

    /**
     * 将超过maxLen的字符串进行省略
     * 变成xxxxx...xxxx
     */
    private String shortH(String s, int maxLen){
        if (s.length() <= maxLen){
            return s;
        }
        //取前20
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            sb.append(s.charAt(i));
        }
        sb.append("...");
        //后20
        for (int i = Math.max(s.length() - 1 - 20, 20); i < s.length(); i++) {
            sb.append(s.charAt(i));
        }
        return sb.toString();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        downPath = view.findViewById(R.id.setting_down_path);
        timer = view.findViewById(R.id.setting_timer);
        clearCache = view.findViewById(R.id.setting_clear_cache);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //设置最多显示字数
        setTextViewMaxLen( downPath.getLeftBottomTextView(), 40);
        setTextViewMaxLen( timer.getLeftBottomTextView(), 40);
        setTextViewMaxLen( clearCache.getLeftBottomTextView(), 40);

        //将下载路径复制到粘贴板
        downPath.setOnClickListener(v -> {
            // 将文本复制到粘贴板
            ClipboardManager clipboard = getSystemService(getContext(), ClipboardManager.class);
            ClipData clipData = ClipData.newPlainText("text", ContextApp.getDownloadPath());
            clipboard.setPrimaryClip(clipData);
            Toast.makeText(getContext(), "目录已复制到粘贴板", Toast.LENGTH_SHORT).show();

        });
        //设置下载路径的文本
        downPath.setLeftBottomString(shortH(ContextApp.getDownloadPath(), 40));

        //获取之前保存的关闭时间
        String s = SharedPreferencesUtil.getString(CLOSE_TIME);
        //设置关闭时间字符串
        if (s != null) timer.setLeftBottomString(s);
        //设置点击时间
        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //进行一个输入弹窗，输入需要等待的分钟数
                new MaterialDialog.Builder(getContext())
                        .title("定时暂停")
                        .content("设置分钟")
                        .inputType(
                                InputType.TYPE_CLASS_TEXT
                                        | InputType.TYPE_CLASS_NUMBER
                                        | InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                        .input(
                                getString(R.string.app_name),
                                "",
                                false,
                                ((dialog, input) -> {}))
                        .inputRange(1, 3)
                        .positiveText("确定")
                        .negativeText("取消")
                        .onPositive(((dialog, which) -> {
                            //获取分钟数
                            int input = Integer.parseInt(dialog.getInputEditText().getText().toString());
                            if (input > 0){
                                //开启服务
                                Intent intent = new Intent(getContext(), ReminderService.class);
                                intent.putExtra("minute", input);
                                getContext().startService(intent);

                                Toast.makeText(getContext(), input + "分钟后暂停播放", Toast.LENGTH_SHORT).show();
                                //保存数据
                                SharedPreferencesUtil.putString(CLOSE_TIME, input+"分钟");
                                timer.setLeftBottomString(input+"分钟");
                            }
                        }))
                        .cancelable(false)
                        .show();

            }
        });

        //获得缓存目录下所有文件的大小
        int mb = (int) (CacheDataSource.getCacheSize() / 1024 / 1024);
        clearCache.setLeftBottomString(mb + "mb");
        clearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //清除缓存
                CacheDataSource.clearCache();
                clearCache.setLeftBottomString(0 + "mb");
                Toast.makeText(getContext(), "已清除缓存", Toast.LENGTH_SHORT).show();
            }
        });

    }
}