package com.deepspring.lib.server;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.deepspring.lib.R;
import com.deepspring.lib.ui.activity.MainActivity;
import com.deepspring.lib.ui.activity.SportActivity;


import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by Anonym on 2017/9/22.
 */

public class MusicService extends Service {
    public MainActivity mActivity = new MainActivity();
    public SportActivity sActivity = new SportActivity();

    public final Binder mBinder = new MyBinder();

    public MediaPlayer mp = new MediaPlayer();
    public int Current_duration = mp.getDuration();

    public String run[];
    public String walk[];
    public String jog[];

    public String From_Activity;
    public int Start_Position = 0;

    //内部类
    public class MyBinder extends Binder {
        public MusicService getMusicService() {
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        From_Activity = intent.getStringExtra("Key");
        return mBinder;
    }

    // 存储一个音频文件数组到list当中
    ArrayList<Uri> RunFile = new ArrayList<Uri>();
    ArrayList<Uri> WalkFile = new ArrayList<Uri>();
    ArrayList<Uri> JogFile = new ArrayList<Uri>();

    //随机获取运动音乐的music
    public void GetSportMusic(){
        try {
            run = getAssets().list("sportmusic/runmusic");
            walk = getAssets().list("sportmusic/walkmusic");
            jog = getAssets().list("sportmusic/jogmusic");
        } catch (IOException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (int i=0;i<run.length;i++){
            Uri rmusic=Uri.parse("android.resource://com.deepspring.tide/raw/"+run[i]);
            RunFile.add(rmusic);
        }
        for (int i=0;i<walk.length;i++){
            Uri wmusic=Uri.parse("android.resource://com.deepspring.tide/raw/"+walk[i]);
            WalkFile.add(wmusic);
        }
        for (int i=0;i<jog.length;i++){
            Uri jmusic=Uri.parse("android.resource://com.example.myapplication/raw/"+jog[i]);
            JogFile.add(jmusic);
        }
    }

    /**
     * UI中 继续和开始 都直接使用play
     * @param
     */
    public void play() {
        if ("SportActivity".equals(From_Activity)){
            GetSportMusic();   //获取运动模块的音乐
            switch (sActivity.mPosition) {
                case 0:
                    if (Start_Position==0) {
                        int id_walk = (int) (Math.random() * (walk.length - 1));
                        AssetFileDescriptor afd = null;
                        try {
                            afd = getAssets().openFd("sportmusic/walkmusic/" + walk[id_walk]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            mp.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    mp.seekTo(Start_Position+1);
                    mp.start();
                    break;
                case 1:
                    if (Start_Position==0) {
                        int id_run = (int) (Math.random() * (run.length - 1));
                        AssetFileDescriptor afd = null;
                        try {
                            afd = getAssets().openFd("sportmusic/runmusic/" + run[id_run]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            mp.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    mp.seekTo(Start_Position+1);
                    mp.start();
                    break;
                case 2:
                    if (Start_Position==0) {
                        int id_jog = (int) (Math.random() * (jog.length - 1));
                        AssetFileDescriptor afd = null;
                        try {
                            afd = getAssets().openFd("sportmusic/jogmusic/" + jog[id_jog]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            mp.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    mp.seekTo(Start_Position+1);
                    mp.start();
                    break;
                default:
            }
        }
        else if("MainActivity".equals(From_Activity)) {
            switch (mActivity.mPosition) {
                case 0:
                    break;
                case 1:
                    mp = MediaPlayer.create(this, R.raw.rain);
                    mp.start();
                    break;
                case 2:
                    mp = MediaPlayer.create(this, R.raw.forest);
                    mp.start();
                    break;
                case 3:
                    mp = MediaPlayer.create(this, R.raw.wave);
                    mp.start();
                    break;
                case 4:
                    mp = MediaPlayer.create(this, R.raw.classic);
                    mp.start();
                    break;
                default:
            }
        }
    }

    /**
     * 暂停、放弃
    **/
     public void pause() {
         Start_Position=mp.getCurrentPosition();
         mp.pause();
    }
    public void stop(){
        Start_Position=0;
        mp.pause();
        mp = new MediaPlayer();
    }

}
