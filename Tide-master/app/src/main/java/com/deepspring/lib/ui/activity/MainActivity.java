package com.deepspring.lib.ui.activity;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.deepspring.lib.R;
import com.deepspring.lib.server.MusicService;
import com.deepspring.lib.ui.adapter.ViewFragmentAdapter;
import com.deepspring.lib.ui.fragment.ClassicFragment;
import com.deepspring.lib.ui.fragment.DynamicFragment;
import com.deepspring.lib.ui.fragment.ForestFragment;
import com.deepspring.lib.ui.fragment.RainFragment;
import com.deepspring.lib.ui.fragment.WaveFragment;
import com.deepspring.lib.ui.widget.MyCircleProgress;
import com.deepspring.lib.ui.widget.MyNumberPicker;
import com.deepspring.lib.ui.widget.MyTimeCountDown;
import com.deepspring.lib.ui.widget.MyViewPager;
import com.deepspring.lib.ui.widget.OnTimeCompleteListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * todo-list:优先级3：大图片OOM && 按钮动画
 * todo-list:BUG：服务生命周期的问题 描述：当AC销毁后，服务继续运行
 */

public class MainActivity extends BaseActivity implements ViewPager.OnPageChangeListener, NoticeImp, OnTimeCompleteListener {

    //将对应控件绑定到视图上
    @BindView(R.id.toolbar)
    //定义工具栏
    Toolbar mToolbar;
    @BindView(R.id.viewpager)
    MyViewPager mViewpager;
    //bt_play -Button
    @BindView(R.id.bt_play)
    Button mBtPlay;
    @BindView(R.id.bt_pause)
    Button mBtPause;
    @BindView(R.id.bt_continute)
    Button mBtContinute;
    @BindView(R.id.bt_giveup)
    Button mBtGiveup;
    @BindView(R.id.daily_text)
    TextView mDayilText;
    @BindView(R.id.circleProgress)
    //MyCircleProgress -环形进度条
    MyCircleProgress mCircleProgress;
    @BindView(R.id.mid_text)
    TextView mMidText;
    @BindView(R.id.num)
    MyNumberPicker mNumPick;
    @BindView(R.id.timer)
    MyTimeCountDown mTimer;

    private ViewFragmentAdapter mAdapter;
    private List<Fragment> mFragments;
    private MusicService mMusicService;

    private String[] mSentenceArrays;
    private String[] mMidTextArrays;
    private String daily_sentece = null;

    public static int mPosition;
    private static final int NO_f = 0x1;  //16进制
    public static final int PROGRESS_CIRCLE_STARTING = 0x110;
    private final String[] times = {"05","10","15","20","25","30","35","45","50","55","60"};
    private int progress;  //反映计时进度
    private float countTime;
    private float t1;
    private float t2;
    private boolean isPause = false;

    //获取系统的通知服务
    private NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"Welcome");

    //一个Handler可以发送或处理和一个线程相关的任务对象。每个Handler实例都关联一个单线程及其消息队列。每当你创造一个新的Handler，
    // 它必然对应产生一个线程/消息队列，从此刻开始，它将发送消息和任务到消息队列并且当它们从消息队列出来时去处理它们。
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case PROGRESS_CIRCLE_STARTING:
                    //进程开始，用progress记录当前进程（百分比）
                    progress = mCircleProgress.getProgress();
                    mCircleProgress.setProgress(++progress);
                    if(progress >= 100){
                        //清除PROGRESS_CIRCLE_STARTING对应消息
                        handler.removeMessages(PROGRESS_CIRCLE_STARTING);
                        progress = 0;
                        mCircleProgress.setProgress(0);
                        mCircleProgress.setStatus(MyCircleProgress.Status.End);
                    }else{
                        //msg.what延迟特定时间为PROGRESS_CIRCLE_STARTING
                        handler.sendEmptyMessageDelayed(PROGRESS_CIRCLE_STARTING, (long)(t1 * 1000));
                    }
                    break;
            }
        }
    };


    //定义conn实现ServiceConnection接口
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mMusicService = ((MusicService.MyBinder) iBinder).getMusicService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mMusicService = null;
        }
    };

    //定义bindServiceConnection方法实现bindService，传递conn（ServiceConnection）接口
    private void bindServiceConnection() {
        Intent intent = new Intent(MainActivity.this,MusicService.class);
        intent.putExtra("String MainActivity", "String Key");
        //后台启动MusicService
        startService(intent);
        bindService(intent, conn, this.BIND_AUTO_CREATE);

    }

    public int setLayout() {
        return R.layout.activity_main;
    }
    //@android.support.annotation.RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在Activity 类中绑定
        ButterKnife.bind(this);  //用黄油刀的bind功能直接绑定了自己？
        mToolbar.setTitle("");
        //使用ToolBar控件替代系统自带ActionBar标题栏
        setSupportActionBar(mToolbar);
        initBtn();
        initFragments();
        initTextView();
        initNumberPicker();
        mTimer.setOnTimeCompleteListener(this);
        mCircleProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    mMidText.setVisibility(View.GONE);
                    mTimer.setVisibility(View.GONE);
                    mNumPick.setVisibility(View.VISIBLE);
            }
        });
        mMusicService = new MusicService();
        bindServiceConnection();
        //TODO:OOM TEST
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        Log.d("TAG", "Max memory is " + maxMemory + "KB");
    }

    //设置选择器
    @TargetApi(11)
    private void initNumberPicker() {
        mNumPick.setDisplayedValues(times);
        mNumPick.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);//中间不可点击
        mNumPick.setMaxValue(times.length-1);
        mNumPick.setWrapSelectorWheel(false);
        mTimer.initTime(300);
        t1 = 300/100;

        mNumPick.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mNumPick.setValue(newVal);
                /*将指定Runnable（包装成PostMessage）加入到MessageQueue中
                然后Looper不断从MessageQueue中读取Message进行处理*/
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMidText.setVisibility(View.VISIBLE);
                        mNumPick.setVisibility(View.GONE);
                        mBtPlay.setClickable(true);
                    }
                },1350);
                mBtPlay.setClickable(false);  //不允许按play按钮
                countTime = Integer.parseInt(times[newVal]);  //将字符串转化为int型
                t1 = ((60*countTime)/100);  //秒数的百分之一
                t2 = 60*countTime;  //秒数
                mTimer.initTime((long) t2);
            }
        });
    }

    //设置按钮
    private void initBtn() {
        //给play Button注册一个触碰事件监听器，并设置其按下和弹起的颜色
        mBtPlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
                    v.setBackgroundResource(R.drawable.shape_play_pressed);
                } else if (event.getAction() == MotionEvent.ACTION_BUTTON_RELEASE) {
                    v.setBackgroundResource(R.drawable.shape_play);
                }
                return false;
            }
        });

        //同上
        mBtPause.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
                    v.setBackgroundResource(R.drawable.shape_pause_pressed);
                } else if (event.getAction() == MotionEvent.ACTION_BUTTON_RELEASE) {
                    v.setBackgroundResource(R.drawable.shape_pause);
                }
                return false;
            }
        });
        mBtContinute.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
                    v.setBackgroundResource(R.drawable.shape_continute_pressed);
                } else if (event.getAction() == MotionEvent.ACTION_BUTTON_RELEASE) {
                    v.setBackgroundResource(R.drawable.shape_continute);
                }
                return false;
            }
        });
        mBtGiveup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
                    v.setBackgroundResource(R.drawable.shape_pause_pressed);
                } else if (event.getAction() == MotionEvent.ACTION_BUTTON_RELEASE) {
                    v.setBackgroundResource(R.drawable.shape_pause);
                }
                return false;
            }
        });
    }

    /*Fragment可看似迷你活动，它的出现是为了解决Android碎片化 ，
    它可作为Activity界面的组成部分，可在Activity运行中实现动态地加入、移除和交换*/
    //用集合存储一组fragment
    private void initFragments() {
        mFragments = new ArrayList<Fragment>();
        mFragments.add(new DynamicFragment());
        mFragments.add(new RainFragment());
        mFragments.add(new ForestFragment());
        mFragments.add(new WaveFragment());
        mFragments.add(new ClassicFragment());
        mAdapter = new ViewFragmentAdapter(getSupportFragmentManager(), mFragments);
        mViewpager.setAdapter(mAdapter);
        //为视图注册一个页面滑动监听器
        mViewpager.addOnPageChangeListener(this);
        //TODO:大图片OOM问题
        mViewpager.setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.a3));
    }

    private void initTextView() {
        //设置字体
        Typeface mTypeFace = Typeface.createFromAsset(getAssets(),
                "MFKeSong-Regular.ttf");
        mDayilText.setTypeface(mTypeFace);
        mMidText.setTypeface(mTypeFace);
        mTimer.setTypeface(mTypeFace);
        mSentenceArrays = this.getResources().getStringArray(R.array.daily_sentence);
        //从mSentenceArrays中随便选取每日美句
        int id = (int) (Math.random() * (mSentenceArrays.length - 1));
        daily_sentece = mSentenceArrays[id];
        mDayilText.setText(daily_sentece);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        MainActivity.mPosition = position;
        mMidTextArrays = this.getResources().getStringArray(R.array.midText);
        switch (mPosition){
            case 0:
                mMidText.setText(mMidTextArrays[0]);
                break;
            case 1:
                mMidText.setText(mMidTextArrays[1]);
                break;
            case 2:
                mMidText.setText(mMidTextArrays[2]);
                break;
            case 3:
                mMidText.setText(mMidTextArrays[3]);
                break;
            case 4:
                mMidText.setText(mMidTextArrays[4]);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    //ButterKnife的onViewClicked方法，绑定控件点击事件
    @OnClick({R.id.bt_play, R.id.bt_pause, R.id.bt_continute, R.id.bt_giveup})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bt_play:
//                mAnimation = AnimationUtils.loadAnimation(this,R.anim.play_bt);
//                mBtPlay.startAnimation(mAnimation);
                NoticePlay();  //调用专注开始方法
                //播放音乐
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mMusicService.play();
                    }
                }).start();


                mBtPlay.setVisibility(View.GONE);  //play键隐藏
                mBtPause.setVisibility(View.VISIBLE);  //暂停键可见
                mBtContinute.setVisibility(View.GONE);  //继续键隐藏
                mBtGiveup.setVisibility(View.GONE);  //放弃键隐藏
                mCircleProgress.setStatus(MyCircleProgress.Status.Starting);  //进度条开始运转
                mCircleProgress.setClickable(true);  //进度条可以点击切换至选择时间模块
                Message message = Message.obtain();
                message.what = PROGRESS_CIRCLE_STARTING;
                handler.sendMessage(message);  //传递PROGRESS_CIRCLE_STARTING给进度条？
                mTimer.setVisibility(View.VISIBLE);  //计时器可见
                mMidText.setVisibility(View.GONE);  //中心字样隐藏
                mNumPick.setVisibility(View.GONE);  //选择器隐藏
                if(isPause == true){
                    mTimer.TimeContinute();
                }else {
                    mTimer.reStart();
                }
                break;
            case R.id.bt_pause:
                NoticePause();  //调用专注暂停方法
                //mAnimation = AnimationUtils.loadAnimation(this,R.anim.pause_left);
                //mBtPause.startAnimation(mAnimation);
                mMusicService.pause();  //音乐暂停播放
                mBtPlay.setVisibility(View.GONE);  //play键隐藏
                mBtPause.setVisibility(View.GONE);  //暂停键隐藏
                mBtContinute.setVisibility(View.VISIBLE);  //继续键可见
                mBtGiveup.setVisibility(View.VISIBLE);  //放弃键可见
                mCircleProgress.setClickable(false);  //不允许点击进度条
                if(mCircleProgress.getStatus() == MyCircleProgress.Status.Starting) {
                    mCircleProgress.setStatus(MyCircleProgress.Status.End);   //转换专注模式为End
                    handler.removeMessages(PROGRESS_CIRCLE_STARTING);  //移除队列中PROGRESS_CIRCLE_STARTING)信息
                    mTimer.TimePause();  //计时暂停
                    isPause = true;  //设置为暂停状态
                    mTimer.setVisibility(View.VISIBLE);  //计时器可见
                    mMidText.setVisibility(View.GONE);  //中心字样隐藏
                    mNumPick.setVisibility(View.GONE);  //选择器隐藏
                }
                break;
            case R.id.bt_continute:
                NoticePlay();  //调用专注开始方法，专注继续
//                mAnimation = AnimationUtils.loadAnimation(this,R.anim.pause_left);
//                mBtContinute.startAnimation(mAnimation);
                mMusicService.play();  //音乐继续播放
                mBtPlay.setVisibility(View.GONE);  //play键隐藏
                mBtPause.setVisibility(View.VISIBLE);  //暂停键可见
                mBtContinute.setVisibility(View.GONE);  //继续键隐藏
                mBtGiveup.setVisibility(View.GONE);  //放弃键隐藏
                mCircleProgress.setStatus(MyCircleProgress.Status.Starting);  //进度条开始（继续）
                mCircleProgress.setClickable(false);  //不允许点击进度条
                Message message1 = Message.obtain();
                message1.what = PROGRESS_CIRCLE_STARTING;
                handler.sendMessage(message1);  //message1加入队列
                mTimer.setVisibility(View.VISIBLE);  //计时器可见
                mMidText.setVisibility(View.GONE);  //中心字样隐藏
                mNumPick.setVisibility(View.GONE);  //选择器隐藏
                if(isPause == true){
                    mTimer.TimeContinute();  //继续及时
                }else {
                    mTimer.reStart();  //重启计时
                }
                break;
            case R.id.bt_giveup:
                NoticeCancel();  //取消专注
//                mAnimation = AnimationUtils.loadAnimation(this,R.anim.pause_right);
//                mBtGiveup.startAnimation(mAnimation);
                mMusicService.pause();  //音乐播放暂停
                mBtPlay.setVisibility(View.VISIBLE);  //play键可见
                mBtPause.setVisibility(View.GONE);  //暂停键隐藏
                mBtContinute.setVisibility(View.GONE);  //继续键隐藏
                mBtGiveup.setVisibility(View.GONE);  //放弃键隐藏
                mCircleProgress.setProgress(0);  //进度条置0
                mCircleProgress.setClickable(true);  //进度条可以点击
                mTimer.setVisibility(View.GONE);  //计时器隐藏
                mNumPick.setVisibility(View.GONE);  //选择器隐藏
                mMidText.setVisibility(View.VISIBLE);  //中心字样可见
                mTimer.stop();  //计时停止
                mTimer.initTime(300);  //初始化计时器为300（5min）
                t1 = 3;  //t1应表示单位时间
                break;
        }
    }

    @Override
    /*** 此方法用于初始化菜单，其中menu参数就是即将要显示的Menu实例。 返回true则显示该menu,false 则不显示;
     (只会在第一次初始化菜单时调用) ***/
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);  //服务器解绑
        //状态栏通知的管理类，负责发通知、清楚通知等
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();  //一次性清除所有通知
    }

    @Override
    //专注开始
    public void NoticePlay() {
        Intent intent = new Intent(this, MainActivity.class);
        //取得一个用于启动一个Activity的PendingIntent对象
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);
        //自定义通知样式
        builder.setSmallIcon(R.drawable.ic_notify_icon_red);
        builder.setContentTitle("潮汐");
        builder.setContentText("正在专注");
        builder.setSmallIcon(R.drawable.ic_notify_icon_red);
        Notification n = builder.build();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NO_f, n);  //NO_f唯一标识该ID
    }

    @Override
    //专注暂停
    public void NoticePause() {
        builder.setSmallIcon(R.drawable.ic_notify_icon_red);
        builder.setContentTitle("潮汐");
        builder.setContentText("专注已暂停");
        builder.setSmallIcon(R.drawable.ic_notify_icon_red);
        Notification n = builder.build();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NO_f, n);
    }

    @Override
    //专注取消
    public void NoticeCancel() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }

    @Override
    //专注完成
    public void onTimeComplete() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);
        builder.setSmallIcon(R.drawable.ic_notify_icon_red);
        builder.setContentTitle("潮汐");
        builder.setContentText("专注结束");
        builder.setSmallIcon(R.drawable.ic_notify_icon_red);
        Notification n = builder.build();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NO_f, n);

        mMusicService.pause();
        mBtPlay.setVisibility(View.VISIBLE);
        mBtPause.setVisibility(View.GONE);
        mBtContinute.setVisibility(View.GONE);
        mBtGiveup.setVisibility(View.GONE);
        mCircleProgress.setProgress(0);
        mCircleProgress.setClickable(true);
        mTimer.setVisibility(View.GONE);
        mNumPick.setVisibility(View.GONE);
        mMidText.setVisibility(View.VISIBLE);
        mTimer.stop();
        mTimer.initTime(300);
        t1 = 3;
    }
}
