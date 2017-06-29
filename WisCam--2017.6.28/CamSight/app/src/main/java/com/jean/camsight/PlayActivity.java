package com.jean.camsight;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.sdk.Controller;
import com.demo.sdk.Enums;
import com.demo.sdk.Module;
import com.demo.sdk.Player;
import com.demo.sdk.Scanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import component.RequestPermission;

public class PlayActivity extends Activity {
    KeyguardManager mKeyguardManager = null;//声明键盘管理器
    private KeyguardManager.KeyguardLock mKeyguardLock = null;//声明键盘锁
    private PowerManager pm;//声明电源管理器
    private PowerManager.WakeLock wakeLock;
    static int screenWidth;
    static int screenHeigh;

    com.demo.sdk.DisplayView  Video_Play;//播放视频
    private String Device_Ip="192.168.100.1";
    private Scanner _scanner;
    private ImageView Recording;//录像标志
    private TextView Recordtime;//录像计时

    private component.MainMenuButton _btnBack;
    private component.MainMenuButton _btnOpen;
    private component.MainMenuButton _btnTakePhoto;
    private component.MainMenuButton _btnRecord;
    private component.MainMenuButton _btnPlayBack;
    private component.MainMenuButton _btnVoice;
    private LinearLayout _layoutMenu;
    private Loading _imgConnecting;

    private SoundPool sp;//声明一个SoundPool
    private int music;//定义一个整型用load（）；来设置suondID
    private int music_begin;
    private int music_end;

    private int fps=25;
    private Handler handler=new Handler();
    private boolean _isExit=false;

    private RequestPermission _requestPermission;
    private int count=0;
    private boolean _isPlayUrl=false;
    private String _urlString="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);//获取电源的服务
        mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);//获取系统服务
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);//去除title
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉Activity上面的状态栏
        setContentView(R.layout.activity_play);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏设置
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        _requestPermission=new RequestPermission();
        wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");//点亮亮屏
        wakeLock.acquire();
        mKeyguardLock = mKeyguardManager.newKeyguardLock("");//初始化键盘锁，可以锁定或解开键盘锁
        mKeyguardLock.disableKeyguard(); //禁用显示键盘锁定
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm); //获取屏幕信息
        screenWidth = dm.widthPixels;
        screenHeigh = dm.heightPixels;

        sp= new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);//第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        music = sp.load(this, R.raw.shutter, 1); //把你的声音素材放到res/raw里，第2个参数即为资源文件，第3个为音乐的优先级
        music_begin = sp.load(this, R.raw.begin_record, 2);
        music_end = sp.load(this, R.raw.end_record, 3);

        Intent intent=getIntent();
        SharedPreferences p = getSharedPreferences("PLAYURL", MODE_PRIVATE);
        _urlString=p.getString("PLAYURL", "");
        _isPlayUrl=intent.getBooleanExtra("PLAYURL",false);
        Video_Play=(com.demo.sdk.DisplayView)findViewById(R.id.video_paly);
        //Video_Play.setOnClickListener(Video_Play_Click);
        Recording=(ImageView)findViewById(R.id.recording);
        Recordtime=(TextView)findViewById(R.id.recordtime);
        _btnBack=(component.MainMenuButton)findViewById(R.id.play_back);
        _btnBack.setOnClickListener(_btnBackClick);
        _btnOpen=(component.MainMenuButton)findViewById(R.id.btn_open);
        _btnOpen.setOnClickListener(_btnOpenClick);
        _btnTakePhoto=(component.MainMenuButton)findViewById(R.id.btn_take_photo);
        _btnTakePhoto.setOnClickListener(_btnTakePhotoClick);
        _btnRecord=(component.MainMenuButton)findViewById(R.id.btn_record);
        _btnRecord.setOnClickListener(_btnRecordClick);
        _btnPlayBack=(component.MainMenuButton)findViewById(R.id.btn_playback);
        _btnPlayBack.setOnClickListener(_btnPlayBackClick);
        _btnVoice=(component.MainMenuButton)findViewById(R.id.btn_voice);
        _btnVoice.setOnClickListener(_btnVoiceClick);
        _imgConnecting=(Loading) findViewById(R.id.img_connecting);
        _layoutMenu=(LinearLayout) findViewById(R.id.layout_menu);
        _imgConnecting.setText(getApplication().getString(R.string.connecting));
        _imgConnecting.setVisibility(View.VISIBLE);
        //startAnimation();

        _btnTakePhoto.setEnabled(false);
        _btnRecord.setEnabled(false);
        count=0;
        if (_isPlayUrl){
            PlayVideo();
        }
        else{
            WifiManager wifiManager1 = (WifiManager) getSystemService(WIFI_SERVICE);
            if(wifiManager1.isWifiEnabled()) {
                _scanner = new Scanner(this);
                _scanner.setOnScanOverListener(new Scanner.OnScanOverListener() {
                    @Override
                    public void onResult(Map<InetAddress, String> data, InetAddress gatewayAddress) {
                        if(_isExit){
                            return;
                        }
                        if(data.size()==0){
                            _imgConnecting.setText(getApplication().getString(R.string.connecting));
                            _imgConnecting.setVisibility(View.VISIBLE);
                            if(count>5){
                                _imgConnecting.setText(getApplication().getString(R.string.no_device));
                                _imgConnecting.setVisibility(View.VISIBLE);
                                count=0;
                            }
                            handler.post(_scanRunnable);
                            count++;
                            return;
                        }
                        for (Map.Entry<InetAddress, String> entry : data.entrySet()) {
                            if (data != null) {
                                //_txtConnecting.setVisibility(View.GONE);
                                handler.removeCallbacks(_scanRunnable);
                                Device_Ip = entry.getKey().getHostAddress();
                                DisplayToast(Device_Ip);
                                ParametersConfig http = new ParametersConfig(Device_Ip + ":" + 80, "admin");
                                http.setOnResultListener(new ParametersConfig.OnResultListener() {
                                    @Override
                                    public void onResult(ParametersConfig.Response result) {
                                        if (result.type == ParametersConfig.GET_FPS) {
                                            if (result.statusCode == 200) {
                                                String ff=result.body.replace(" ","");
                                                String keyStr="\"value\":\"";
                                                int index=ff.indexOf(keyStr);
                                                if (index!=-1){
                                                    int index2=ff.indexOf("\"",index+keyStr.length());
                                                    if(index2!=-1){
                                                        String fpsStr=ff.substring(index+keyStr.length(),index2);
                                                        fps=Integer.parseInt(fpsStr);
                                                    }
                                                }
                                            }
                                        }
                                        PlayVideo();
                                    }
                                });
                                http.getFps(0);
                            }
                            else{
                                _imgConnecting.setText(getApplication().getString(R.string.connecting));
                                _imgConnecting.setVisibility(View.VISIBLE);
                                if(count>5){
                                    _imgConnecting.setText(getApplication().getString(R.string.no_device));
                                    _imgConnecting.setVisibility(View.VISIBLE);
                                    count=0;
                                }
                                handler.post(_scanRunnable);
                                count++;
                            }
                        }
                    }
                });
                _scanner.scan();
            } else{
                DisplayToast("Please open WLAN first!");
                finish();
            }
        }
    }

    void startAnimation(){
        LinearInterpolator lin=new LinearInterpolator();
        Animation am=new RotateAnimation(0,+360,
                Animation.RELATIVE_TO_SELF,0.5f,
                Animation.RELATIVE_TO_SELF,0.5f);
        am.setRepeatCount(-1);
        am.setDuration(500);
        am.setRepeatMode(Animation.INFINITE);
        am.setInterpolator(lin);
        _imgConnecting.setAnimation(am);
        am.startNow();
    }

    Runnable _scanRunnable=new Runnable() {
        @Override
        public void run() {
            _scanner.scan();
        }
    };

    @Override
    protected void onStop()
    {
        //stop();
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        stop();
        super.onDestroy();
    }

    /**
     * Stop
     **/
    void stop(){
        _isExit=true;
        _getTraffic=false;
        _stopTraffic = true;
        handler.removeCallbacks(_scanRunnable);
        if(_player!=null) {
            if (_player.getState() == Enums.State.IDLE) {
            }
            else {
                if(_player!=null)
                    _player.stop();
            }
        }
    }

    /**
     * Back
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            stop();
            finish();
        }
        return false;
    }

    View.OnClickListener _btnBackClick=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            stop();
            finish();
        }
    };

    View.OnClickListener _btnVoiceClick=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            _openVoice=!_openVoice;
            if (_openVoice){
                _btnVoice.setImageResource(R.drawable.voice_pre);
            }
            else{
                _btnVoice.setImageResource(R.drawable.voice_nor);
            }
            _player.setAudioOutput(_openVoice);
        }
    };

    /**
     * Show Record Time
     */
    public String showTimeCount(long time)
    {
        if(time >= 360000)
        {
            return "00:00:00";
        }
        String timeCount = "";
        long hourc = time/3600;
        String hour = "0" + hourc;
        hour = hour.substring(hour.length()-2, hour.length());

        long minuec = (time-hourc*3600)/(60);
        String minue = "0" + minuec;
        minue = minue.substring(minue.length()-2, minue.length());

        long secc = (time-hourc*3600-minuec*60);
        String sec = "0" + secc;
        sec = sec.substring(sec.length()-2, sec.length());
        timeCount = hour + ":" + minue + ":" + sec;
        return timeCount;
    }

    /**
     * Play Video
     */
    private static Module _module;
    private Player _player;
    private Controller _controller;
    private boolean _recording = false;
    private Enums.Pipe _pipe = Enums.Pipe.MJPEG_PRIMARY;
    private Thread _trafficThread;
    private long _traffic;
    private long _lastTraffic;
    private boolean _getTraffic = false;
    private boolean _stopTraffic = false;
    private boolean _openVoice = false;
    private long videotime=0;
    public static FileOutputStream videofile;//视频数据流
    public static FileOutputStream photofile;//照片数据流
    public static String photofile_path;
    public static String videofile_path;
    public static String voicefile_path;
    private int _connectTime=0;
    public void PlayVideo()
    {
        _connectTime=0;
        if (_module == null)
        {
            _module = new Module(this);
        }
        else
        {
            _module.setContext(this);
        }

        _module.setLogLevel(Enums.LogLevel.VERBOSE);
        _module.setUsername("admin");
        _module.setPassword("admin");
        _module.setPlayerPort(554);
        _module.setModuleIp(Device_Ip);
        _controller = _module.getController();
        _player = _module.getPlayer();
        _player.setRecordFrameRate(fps);
        _player.setAudioOutput(_openVoice);

        _recording = _player.isRecording();
        Video_Play.setFullScreen(true);
        _player.setDisplayView(getApplication(),Video_Play,null,0);
        _player.setTimeout(20000);
        _player.setOnTimeoutListener(new Player.OnTimeoutListener()
        {
            @Override
            public void onTimeout() {
                // TODO Auto-generated method stub
            }
        });
        _player.setOnStateChangedListener(new Player.OnStateChangedListener()
        {
            @Override
            public void onStateChanged(Enums.State state) {
                updateState(state);
            }
        });
        _player.setOnVideoSizeChangedListener(new Player.OnVideoSizeChangedListener()
        {
            @Override
            public void onVideoSizeChanged(int width, int height)
            {

            }

            @Override
            public void onVideoScaledSizeChanged(int arg0, int arg1)
            {
                // TODO Auto-generated method stub

            }
        });
        if (_player.getState() == Enums.State.IDLE)
        {
            _pipe = Enums.Pipe.MJPEG_PRIMARY;
            try {
                if (_isPlayUrl){
                    _player.playUrl(_urlString,Enums.Transport.UDP);
                }
                else{
                    _player.play(_pipe, Enums.Transport.UDP);
                }
            }
            catch (Exception e){
                Log.e("====>","psk error");
            }
        }
        else
        {
            if(_player!=null)
                _player.stop();
        }
        updateState(_player.getState());
        final int id = android.os.Process.myUid();
        _lastTraffic = TrafficStats.getUidRxBytes(id);

        _trafficThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (;; ) {
                    if (_stopTraffic) {
                        break;
                    }
                    if (_getTraffic) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                long currentTraffic = TrafficStats.getUidRxBytes(id);
//                                _traffic = (currentTraffic - _lastTraffic) / 1024;
//                                //Log.e("_traffic==>",""+_traffic);
//                                _lastTraffic = currentTraffic;
//                                if(_traffic<15)
//                                {
//                                    _imgConnecting.clearAnimation();
//                                    _imgConnecting.setVisibility(View.GONE);
//                                    _txtConnecting.setVisibility(View.VISIBLE);
//                                    _txtConnecting.setText(getString(R.string.no_video));
//                                }
//                                else{
//                                    _txtConnecting.setVisibility(View.GONE);
//                                    _imgConnecting.clearAnimation();
//                                    _imgConnecting.setVisibility(View.GONE);
//                                }
//                            }
//                        });
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            WifiManager wifiManager1 = (WifiManager) getSystemService(WIFI_SERVICE);
                            if(!wifiManager1.isWifiEnabled()) {
                                return;
                            }
                            ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                            if (!mWifi.isConnected()) {
                                return;
                            }
                            //检测到断开进行重连
                            if(_player!=null){
                                Log.e("Reconnect...","");
                                if(_player.getState()== Enums.State.IDLE){
                                    _player.stop();
                                    _pipe = Enums.Pipe.MJPEG_PRIMARY;
                                    //_imgConnecting.clearAnimation();
                                    _imgConnecting.setVisibility(View.GONE);
                                    //startAnimation();
                                    _imgConnecting.setText(getApplication().getString(R.string.no_video));
                                    _imgConnecting.setVisibility(View.VISIBLE);
                                    if (_isPlayUrl){
                                        _player.playUrl(_urlString,Enums.Transport.UDP);
                                    }
                                    else{
                                        _player.play(_pipe, Enums.Transport.UDP);
                                    }
                                }
                            }


                            if(_recording)
                            {
                                //录像、录音计时
                                videotime++;
                                if(videotime%2==0)
                                {
                                    Recording.setVisibility(View.INVISIBLE);
                                }
                                else
                                {
                                    Recording.setVisibility(View.VISIBLE);
                                }

                                Recordtime.setVisibility(View.VISIBLE);
                                Recordtime.setText(showTimeCount(videotime));
                            }
                            else
                            {
                                videotime=0;
                                Recording.setVisibility(View.INVISIBLE);
                                Recordtime.setVisibility(View.INVISIBLE);
                            }
                        }
                    });

                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {}
                }
            }
        });

        _trafficThread.start();
    }

    private void updateState(Enums.State state) {
        switch (state) {
            case IDLE:
                break;
            case PREPARING:
                break;
            case PLAYING:
                _getTraffic = true;
                _btnTakePhoto.setEnabled(true);
                _btnRecord.setEnabled(true);
                //_imgConnecting.clearAnimation();
                _btnTakePhoto.setImageResource(R.drawable.photo_nor);
                _btnRecord.setImageResource(R.drawable.record_start_nor);
                _imgConnecting.setVisibility(View.GONE);

            case STOPPED:
                break;
        }
    }

    /**
     * Show or Hide control
     */
    View.OnClickListener _btnOpenClick=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(_layoutMenu.getVisibility()==View.VISIBLE){
                _layoutMenu.setVisibility(View.GONE);
            }
            else{
                _layoutMenu.setVisibility(View.VISIBLE);
            }
        }
    };

    /**
     * TakePhoto
     */
    View.OnClickListener _btnTakePhotoClick=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(photofile_path==null)
            {
                _requestPermission.requestWriteSettings(PlayActivity.this);
                return;
            }
            File file = new File(photofile_path);//获取本地已有视频数量
            if(file==null){
                _requestPermission.requestWriteSettings(PlayActivity.this);
                return;
            }
            File[] filephoto = file.listFiles();
            if(filephoto==null){
                _requestPermission.requestWriteSettings(PlayActivity.this);
                return;
            }

            sp.play(music, 1, 1, 0, 0, 1);
            SimpleDateFormat formatter = new SimpleDateFormat("HH-mm-ss");
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            String str   = formatter.format(curDate);
            int photolength=filephoto.length;

            int length=1;
            for(int i=0;i<photolength;i++)
            {
                int start=filephoto[i].getName().indexOf("_");
                int end=filephoto[i].getName().indexOf("  ");
                String aa=filephoto[i].getName().substring(start+1, end);
                if(Integer.parseInt(aa)>=length)
                {
                    length=Integer.parseInt(aa);
                    length=length+1;
                }
            }

            try
            {

                if(length<10)
                    photofile=new FileOutputStream(photofile_path+"/IMG "+"_0"+length+"  "+str+".jpg");
                else
                    photofile=new FileOutputStream(photofile_path+"/IMG "+"_"+length+"  "+str+".jpg");
            }
            catch (Exception e)
            {
                // TODO: handle exception
            }

            if(photofile!=null)
            {
                _player.takePhoto().compress(Bitmap.CompressFormat.JPEG, 100, photofile);
                if(length<10)
                    DisplayToast("photo is saved to: " + photofile_path + "/IMG " + "_0" + length + "  " + str + ".jpg");
                else
                    DisplayToast("photo is saved to: " + photofile_path + "/IMG " + "_" + length + "  " + str + ".jpg");

                try
                {
                    photofile.flush();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                try
                {
                    photofile.close();
                } catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                photofile=null;
            }
        }
    };

    /**
     * Record
     */
    private String path="";
    View.OnClickListener _btnRecordClick=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (_recording)
            {
                sp.play(music_end, 1, 1, 0, 0, 1);
                _btnRecord.setImageResource(R.drawable.record_start_nor);
                _player.endRecord();
                _recording = false;
                DisplayToast("video is saved to: " + path);
            }
            else
            {
                if(videofile_path==null)
                {
                    _requestPermission.requestWriteSettings(PlayActivity.this);
                    return;
                }
                File file = new File(videofile_path);//获取本地已有视频数量
                if(file==null){
                    _requestPermission.requestWriteSettings(PlayActivity.this);
                    return;
                }
                File[] filephoto = file.listFiles();
                if(filephoto==null){
                    _requestPermission.requestWriteSettings(PlayActivity.this);
                    return;
                }
                sp.play(music_begin, 1, 1, 0, 0, 1);
                _btnRecord.setImageResource(R.drawable.record_stop_nor);
                videotime=0;
                SimpleDateFormat formatter = new SimpleDateFormat("HH-mm-ss");
                Date curDate = new  Date(System.currentTimeMillis());//获取当前时间
                String str   = formatter.format(curDate);

                int photolength=filephoto.length;
                int length=1;
                for(int i=0;i<photolength;i++)
                {
                    int start=filephoto[i].getName().indexOf("_");
                    int end=filephoto[i].getName().indexOf("  ");
                    String aa=filephoto[i].getName().substring(start+1, end);
                    if(Integer.parseInt(aa)>=length)
                    {
                        length=Integer.parseInt(aa);
                        length=length+1;
                    }
                }
                if(length<10)
                    path=videofile_path+"/VIDEO "+"_0"+length+"  "+str+".mp4";
                else
                    path=videofile_path+"/VIDEO "+"_"+length+"  "+str+".mp4";

                if (_player.beginRecord0(videofile_path, "/VIDEO "+"_"+length+"  "+str))
                {
                    _recording = true;
                }
            }
        }
    };

    /**
     * PlayBack
     */
    View.OnClickListener _btnPlayBackClick=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent=new Intent();
            intent.setClass(PlayActivity.this,PlaybackActivity.class);
            startActivity(intent);
        }
    };

    /**
     * Show message
     **/
    void DisplayToast(String str)
    {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }


    void NoteDialog(){
        final Dialog deleteDialog = new Dialog(this,R.style.myDialogTheme);
        LayoutInflater delete_Dialog_inflater =getLayoutInflater();
        View delete_Dialog_admin=delete_Dialog_inflater.inflate(R.layout.delete_admin, (ViewGroup) findViewById(R.id.delete_admin1));
        TextView device_delete_admin_title =(TextView)delete_Dialog_admin.findViewById(R.id.del_title);
        TextView device_delete_admin_note =(TextView)delete_Dialog_admin.findViewById(R.id.del_note);
        TextView device_delete_admin_ok =(TextView)delete_Dialog_admin.findViewById(R.id.del_ok_btn);
        TextView device_delete_admin_cancel=(TextView)delete_Dialog_admin.findViewById(R.id.del_cancel_btn);
        device_delete_admin_title.setText(R.string.permission_title_note);
        device_delete_admin_note.setText(R.string.permission_admin_note);
        device_delete_admin_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
            }
        });
        device_delete_admin_cancel.setVisibility(View.GONE);
        deleteDialog.setCanceledOnTouchOutside(false);
        deleteDialog.setContentView(delete_Dialog_admin);
        deleteDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == _requestPermission.WRITE_EXTERNAL_STORAGE) {
            int grantResult = grantResults[0];
            boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;
            if (granted){
                _requestPermission.createSDCardDir(MainActivity.CamSight);
                _requestPermission.createSDCardDir(MainActivity.CamSight_Photo);
                _requestPermission.createSDCardDir(MainActivity.CamSight_Video);
            }else{
            }
        }
    }
}
