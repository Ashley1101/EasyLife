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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.deepspring.tide.lib.ISportStepInterface;
import com.deepspring.lib.R;
import com.deepspring.tide.lib.TodayStepManager;
import com.deepspring.tide.lib.TodayStepService;
import com.deepspring.lib.server.MusicService;
import com.deepspring.lib.ui.adapter.ViewFragmentAdapter;
import com.deepspring.lib.ui.fragment.JogFragment;
import com.deepspring.lib.ui.fragment.RunFragment;
import com.deepspring.lib.ui.fragment.WalkFragment;
import com.deepspring.lib.ui.widget.MyCircleProgress;
import com.deepspring.lib.ui.widget.MyViewPager;
import com.deepspring.tide.lib.TodayStepManager;
import com.deepspring.tide.lib.TodayStepService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class SportActivity extends BaseActivity implements ViewPager.OnPageChangeListener, com.deepspring.lib.ui.activity.NoticeImp {
    private static final int REFRESH_STEP_WHAT = 0;
    //循环取当前时刻的步数中间的间隔时间
    private long TIME_INTERVAL_REFRESH = 3000;  //更新时间间隔
    private Handler mDelayHandler = new Handler(new TodayStepCounterCall());
    public static int mStepSum;  //当前步数
    private ISportStepInterface iSportStepInterface;  //计步接口
    private int Btplay_ClickTimes=0;  //记录此时状态
/*****************************************************************************/
    //将对应控件绑定到视图上
   @Nullable
    @BindView(R.id.toolbar)
    //定义标题栏
    Toolbar mToolbar;
    @BindView(R.id.sviewpager)
    MyViewPager mViewpager;
    //bt_play -Button
    @BindView(R.id.sbt_play)
    Button mBtPlay;
    @BindView(R.id.sbt_pause)
    Button mBtPause;
    @BindView(R.id.sbt_continute)
    Button mBtContinute;
    @BindView(R.id.sbt_giveup)
    Button mBtGiveup;
    @BindView(R.id.sdaily_text)
    TextView mDayilText;
    @BindView(R.id.scircleProgress)
    //MyCircleProgress -环形进度条
    MyCircleProgress mCircleProgress;
    @BindView(R.id.smid_text)
    TextView mMidText;
    @BindView(R.id.stepArrayButton)
    Button mStepArrayButton;
    @BindView(R.id.stepArrayTextView)
    TextView mStepArrayTextView;  //每日计步文本
    @BindView(R.id.stepTextView)
    TextView mStepTextView;  //当前步数文本
    @BindView(R.id.timeTextView)
    TextView timeTextView;  //计时文本

    private ViewFragmentAdapter mAdapter;
    private List<Fragment> mFragments;
    private MusicService mMusicService;

    private String[] mSentenceArrays;
    private String[] mMidTextArrays;
    private String daily_sentece = null;

    public static int mPosition;
    private static final int NO_f = 0x1;  //16进制
    public static final int PROGRESS_CIRCLE_STARTING = 0x110;
    private int progress=0;  //反映计时进度
    private double t1;
    private boolean isPause = true;

    //获取系统的通知服务
    private NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"Welcome");

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case PROGRESS_CIRCLE_STARTING:
                    //进程开始，用progress记录当前进程（百分比）
                    progress = mCircleProgress.getProgress();
                    mCircleProgress.setProgress(++progress);
                    if(progress >= 100){
                        progress = 0;
                        mCircleProgress.setProgress(0);
                        mMusicService.stop();
                        mMusicService.play();
                        t1=mMusicService.mp.getDuration()/100000.0;
                        // mCircleProgress.setStatus(MyCircleProgress.Status.End);
                    }
                        //msg.what延迟特定时间为PROGRESS_CIRCLE_STARTING
                    handler.sendEmptyMessageDelayed(PROGRESS_CIRCLE_STARTING, (long)(t1 * 1000));
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
        Intent intent = new Intent(SportActivity.this,MusicService.class);
        intent.putExtra("Key", "SportActivity");
        //后台启动MusicService
        startService(intent);
        bindService(intent, conn, this.BIND_AUTO_CREATE);

    }

    private void CountSteps()
    {
        //初始化计步模块
        TodayStepManager.startTodayStepService (getApplication());
        if (!isPause) {
            //开启计步Service，同时绑定Activity进行aidl通信
            Intent intent = new Intent(SportActivity.this, TodayStepService.class);
          //  Intent intent = new Intent("com.deepspring.tide.lib.ISportStepInterface");
            startService(intent);
            bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    //Activity和Service通过aidl进行通信
                    iSportStepInterface = ISportStepInterface.Stub.asInterface(service);
                    try {
                        mStepSum = iSportStepInterface.getCurrentTimeSportStep();
                        updateStepCount();
                    } catch (RemoteException e) {
                        e.printStackTrace(); }
                    mDelayHandler.sendEmptyMessageDelayed(REFRESH_STEP_WHAT, TIME_INTERVAL_REFRESH); }
                @Override
                public void onServiceDisconnected(ComponentName name) { }
            }, this.BIND_AUTO_CREATE);
        }
        //计时器
    }

    public int setLayout() {
        return R.layout.activity_sport;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport);
        ButterKnife.bind(this);  //用黄油刀的bind功能直接绑定了自己？
        //setContentView(R.layout.activity_sport);

        mToolbar.setTitle("");
        //使用ToolBar控件替代系统自带ActionBar标题栏
        setSupportActionBar(mToolbar);
        initBtn();
        initFragments();
        initTextView();
        //初始时设置当前步数和计时不可见
        mStepTextView.setVisibility(View.GONE);
        timeTextView.setVisibility(View.GONE);
        mStepArrayTextView.setVisibility(View.GONE);

        mStepArrayTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMidText.setVisibility(View.VISIBLE);
                mStepArrayTextView.setVisibility(View.GONE);
            }
        });
        mMidText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMidText.setVisibility(View.GONE);
                mStepArrayTextView.setVisibility(View.VISIBLE);
            }
        });
        mStepTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStepTextView.setVisibility(View.GONE);
                timeTextView.setVisibility(View.VISIBLE);
            }
        });
        timeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStepTextView.setVisibility(View.VISIBLE);
                timeTextView.setVisibility(View.GONE);
            }
        });
        mMusicService = new MusicService();
        bindServiceConnection();
        //TODO:OOM TEST
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        Log.d("TAG", "Max memory is " + maxMemory + "KB");

    }

    class TodayStepCounterCall implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_STEP_WHAT: {
                    //每隔500毫秒获取一次计步数据刷新UI
                    if (null != iSportStepInterface) {
                        int step = 0;
                        try {
                            step = iSportStepInterface.getCurrentTimeSportStep();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        if (mStepSum != step) {
                            mStepSum = step;
                            updateStepCount();
                        }
                    }
                    mDelayHandler.sendEmptyMessageDelayed(REFRESH_STEP_WHAT, TIME_INTERVAL_REFRESH);

                    break;
                }
            }
            return false;
        }
    }

    //加载当前步数
    private void updateStepCount() {
        //Log.e(TAG, "updateStepCount : " + mStepSum);
        mStepTextView.setText(mStepSum + "步");
    }

    //每日步数？
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.stepArrayButton: {
                //获取所有步数列表
                if (null != iSportStepInterface) {
                    try {
                        String stepArray = iSportStepInterface.getTodaySportStepArray();
                        mStepArrayTextView.setText(stepArray);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        SportActivity.mPosition = position;
        mMidTextArrays = this.getResources().getStringArray(R.array.sport_midText);
        switch (mPosition){
            case 0:
                mMidText.setText(mMidTextArrays[0]);
                mViewpager.setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.s1));
                break;
            case 1:
                mMidText.setText(mMidTextArrays[1]);
                mViewpager.setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.s2));
                break;
            case 2:
                mMidText.setText(mMidTextArrays[2]);
                mViewpager.setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.s3));
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

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
        mFragments.add(new WalkFragment());
        mFragments.add(new RunFragment());
        mFragments.add(new JogFragment());
        mAdapter = new ViewFragmentAdapter(getSupportFragmentManager(), mFragments);
        mViewpager.setAdapter(mAdapter);
        mViewpager.addOnPageChangeListener(this);
        //为视图注册一个页面滑动监听器
        mViewpager.setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.s1));
    }

    private void initTextView() {
        //设置字体
        Typeface mTypeFace = Typeface.createFromAsset(getAssets(),
                "MFKeSong-Regular.ttf");
        mDayilText.setTypeface(mTypeFace);
        mMidText.setTypeface(mTypeFace);
    //    mTimer.setTypeface(mTypeFace);
        mSentenceArrays = this.getResources().getStringArray(R.array.sdaily_sentence);
        //从mSentenceArrays中随便选取每日美句
        int id = (int) (Math.random() * (mSentenceArrays.length - 1));
        daily_sentece = mSentenceArrays[id];
        mDayilText.setText(daily_sentece);
    }

    //ButterKnife的onViewClicked方法，绑定控件点击事件
    @OnClick({R.id.sbt_play, R.id.sbt_pause, R.id.sbt_continute, R.id.sbt_giveup})
    public void onViewClicked(View view)  {
        switch (view.getId()) {
            case R.id.sbt_play:
//                mAnimation = AnimationUtils.loadAnimation(this,R.anim.play_bt);
//                mBtPlay.startAnimation(mAnimation);
                NoticePlay();  //调用专注开始方法
                //播放音乐
                mMusicService.play();
                t1=mMusicService.mp.getDuration()/100000.0;

                Btplay_ClickTimes ++;
                mBtPlay.setVisibility(View.GONE);  //play键隐藏
                mBtPause.setVisibility(View.VISIBLE);  //暂停键可见
                mBtContinute.setVisibility(View.GONE);  //继续键隐藏
                mBtGiveup.setVisibility(View.GONE);  //放弃键隐藏

                mCircleProgress.setStatus(MyCircleProgress.Status.Starting);  //进度条开始运转
                mCircleProgress.setClickable(true);  //进度条可以点击
                Message message = Message.obtain();
                message.what = PROGRESS_CIRCLE_STARTING;
                handler.sendMessage(message);  //传递PROGRESS_CIRCLE_STARTING给进度条
                int a=mCircleProgress.getProgress();

                isPause=false;
                CountSteps();
                mhandmhandlele.post(timeRunable);  //设置计时
                mStepArrayTextView.setVisibility(View.GONE);  //每日步数隐藏
                mMidText.setVisibility(View.GONE);  //中心字样隐藏
                timeTextView.setVisibility(View.VISIBLE);  //计时可见
                mStepTextView.setVisibility(View.GONE);
                break;

            case R.id.sbt_pause:
                NoticePause();  //调用专注暂停方法
                mMusicService.pause();  //音乐暂停播放

                mBtPlay.setVisibility(View.GONE);  //play键隐藏
                mBtPause.setVisibility(View.GONE);  //暂停键隐藏
                mBtContinute.setVisibility(View.VISIBLE);  //继续键可见
                mBtGiveup.setVisibility(View.VISIBLE);  //放弃键可见
                mCircleProgress.setClickable(false);  //不允许点击进度条
                if(mCircleProgress.getStatus() == MyCircleProgress.Status.Starting) {
                    mCircleProgress.setStatus(MyCircleProgress.Status.End);   //转换专注模式为End
                    handler.removeMessages(PROGRESS_CIRCLE_STARTING);  //移除队列中PROGRESS_CIRCLE_STARTING)信息

                    isPause = true;  //设置为暂停状态
                    mMidText.setVisibility(View.GONE);  //中心字样隐藏
                    mStepArrayTextView.setVisibility(View.GONE);  //每日步数隐藏
                    if (timeTextView.VISIBLE == 0){
                        timeTextView.setVisibility(View.VISIBLE); //计时可见
                        mStepTextView.setVisibility(View.GONE); //当前步数隐藏
                    }
                    else{
                        timeTextView.setVisibility(View.GONE); //计时隐藏
                        mStepTextView.setVisibility(View.VISIBLE); //当前步数可见
                    }
                }
                break;

            case R.id.sbt_continute:
                NoticePlay();  //调用专注开始方法，专注继续
                mMusicService.play();  //音乐继续播放

                mBtPlay.setVisibility(View.GONE);  //play键隐藏
                mBtPause.setVisibility(View.VISIBLE);  //暂停键可见
                mBtContinute.setVisibility(View.GONE);  //继续键隐藏
                mBtGiveup.setVisibility(View.GONE);  //放弃键隐藏
                mCircleProgress.setStatus(MyCircleProgress.Status.Starting);  //进度条开始（继续）
                mCircleProgress.setClickable(true);  //不允许点击进度条
                Message message1 = Message.obtain();
                message1.what = PROGRESS_CIRCLE_STARTING;
                handler.sendMessage(message1);  //message1加入队列

                isPause = false;  //暂停状态取消
                CountSteps();
                mhandmhandlele.post(timeRunable);  //设置计时
                mStepArrayTextView.setVisibility(View.GONE);  //每日步数隐藏
                mMidText.setVisibility(View.GONE);  //中心字样隐藏
                if (timeTextView.VISIBLE==0){
                    timeTextView.setVisibility(View.VISIBLE); //计时可见
                    mStepTextView.setVisibility(View.GONE); //当前步数隐藏
                }
                else{
                    timeTextView.setVisibility(View.GONE); //计时隐藏
                    mStepTextView.setVisibility(View.VISIBLE); //当前步数可见
                }
                break;

            case R.id.sbt_giveup:
                NoticeCancel();  //取消专注
                mMusicService.stop();  //音乐播放暂停
                isPause = true;  //设置当前为暂停状态
                currentSecond = 0;

                mBtPlay.setVisibility(View.VISIBLE);  //play键可见
                mBtPause.setVisibility(View.GONE);  //暂停键隐藏
                mBtContinute.setVisibility(View.GONE);  //继续键隐藏
                mBtGiveup.setVisibility(View.GONE);  //放弃键隐藏
                mCircleProgress.setProgress(0);  //进度条置0
                mCircleProgress.setClickable(true);  //进度条可以点击
                mMidText.setVisibility(View.VISIBLE);  //中心字样可见
                mStepTextView.setVisibility(View.GONE);  //当前步数隐藏
                timeTextView.setVisibility(View.GONE);  //计时隐藏

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
        Intent intent = new Intent(this, com.deepspring.lib.ui.activity.MainActivity.class);
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

    /*****************计时器*******************/
    private Runnable timeRunable = new Runnable() {
        @Override
        public void run() {

            currentSecond = currentSecond + 1000;
            timeTextView.setText(getFormatHMS(currentSecond));
            if (!isPause) {
                //递归调用本runable对象，实现每隔一秒一次执行任务
                mhandmhandlele.postDelayed(this, 1000);
            }
        }
    };
    //计时器
    private Handler mhandmhandlele = new Handler();
  //  private boolean isPause = false;//是否暂停
    private long currentSecond = 0;//当前毫秒数
/*****************计时器*******************/

    /**
     * 根据毫秒返回时分秒
     *
     * @param time
     * @return
     */

    //显示计时
    public static String getFormatHMS(long time) {
        time = time / 1000;//总秒数
        int s = (int) (time % 60);//秒
        int m = (int) (time / 60);//分
        int h = (int) (time / 3600);//时
        return String.format("%02d:%02d:%02d", h, m, s);
    }

}
