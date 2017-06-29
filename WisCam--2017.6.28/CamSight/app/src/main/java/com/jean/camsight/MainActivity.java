package com.jean.camsight;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    private component.MainMenuButton _homePlay;
    private component.MainMenuButton _editBtn;
    private LinearLayout _editView;
    private EditText _homeUrlText;
    private Button _homePlayUrl;
    public static String CamSight="/WisCam";
    public static String CamSight_Photo="/WisCam/Photo";
    public static String CamSight_Video="/WisCam/Video";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _homePlay=(component.MainMenuButton)findViewById(R.id.home_play);
        _homePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(MainActivity.this,PlayActivity.class);
                intent.putExtra("PLAYURL",false);
                startActivity(intent);
            }
        });

        _editBtn=(component.MainMenuButton)findViewById(R.id.edit_btn);
        _editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_editView.getVisibility()==View.VISIBLE){
                    _editView.setVisibility(View.GONE);
                    _editBtn.setImageResource(R.mipmap.edit_img);
                }
                else{
                    _editView.setVisibility(View.VISIBLE);
                    _editBtn.setImageResource(R.mipmap.delete_img);
                }
            }
        });
        _editView=(LinearLayout) findViewById(R.id.edit_view);
        _homeUrlText=(EditText)findViewById(R.id.home_url_text);
        SharedPreferences p = getSharedPreferences("PLAYURL", MODE_PRIVATE);
        _homeUrlText.setText(p.getString("PLAYURL", ""));
        _homePlayUrl=(Button)findViewById(R.id.home_play_url);
        _homePlayUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_homeUrlText.getText().toString().equals("")){
                    Toast.makeText(getApplication(),"The rtsp url can not be empty",Toast.LENGTH_SHORT).show();
                    return;
                }
                SharedPreferences.Editor editor = getSharedPreferences("PLAYURL", MODE_PRIVATE).edit();
                editor.putString("PLAYURL", _homeUrlText.getText().toString());
                editor.commit();
                Intent intent=new Intent();
                intent.setClass(MainActivity.this,PlayActivity.class);
                intent.putExtra("PLAYURL",true);
                startActivity(intent);
            }
        });

        createSDCardDir(CamSight);
        createSDCardDir(CamSight_Photo);
        createSDCardDir(CamSight_Video);
    }

    /**
     * CreateSDCardDir
     */
    public void createSDCardDir(String path)
    {
        File sdcardDir = Environment.getExternalStorageDirectory();
        String pathcat=sdcardDir.getPath()+path;
        File path1 = new File(pathcat);

        if (!path1.exists())
        {
            path1.mkdirs();
            setTitle("path ok,path:"+path);
        }
        PlayActivity.photofile_path=sdcardDir.getPath()+CamSight_Photo;
        PlayActivity.videofile_path=sdcardDir.getPath()+CamSight_Video;
    }

    /**
     * Exit
     **/
    private static Boolean isExit = false;
    void exit()
    {
        Timer tExit = null;
        if (isExit == false)
        {
            isExit = true;
            String exitString;
            exitString="Press once more time to exit";
            Toast.makeText(this, exitString , Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    isExit = false;
                }
            }, 2000);

        }
        else
        {
            finish();
            System.exit(0);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            exit();
        }
        return false;
    }
}
