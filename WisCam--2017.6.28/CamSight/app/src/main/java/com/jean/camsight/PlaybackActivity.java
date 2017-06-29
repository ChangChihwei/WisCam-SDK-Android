package com.jean.camsight;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import component.MainMenuButton;

public class PlaybackActivity extends Activity
{
	private LinearLayout Media_back;
	private MainMenuButton Media_Video_db;
	private MainMenuButton Media_Photo_db;
	
	public static boolean open_photo=false;//打开图片文件
	public static boolean open_video=false;//打开视频文件
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playback);
		Media_back=(LinearLayout)findViewById(R.id.media_back);
		Media_back.setOnClickListener(new Media_Back_Click());
		Media_Video_db=(MainMenuButton)findViewById(R.id.video_db);
		Media_Video_db.setOnClickListener(new Media_Video_Click());
		Media_Photo_db=(MainMenuButton)findViewById(R.id.photo_db);
		Media_Photo_db.setOnClickListener(new Media_Photo_Click());
	}
	/*********************************************************************************************************
	** 功能说明：返回上一个界面
	** 传入参数：无
	** 得到参数：无      
	*********************************************************************************************************/		
	 class Media_Back_Click implements OnClickListener
	 {

		@Override
		public void onClick(View arg0)
		{
			PlaybackActivity.this.finish();
		}		 
	 }	

	/*********************************************************************************************************
	** 功能说明：查看视频
	** 传入参数：无
	** 得到参数：无      
	*********************************************************************************************************/		
	 class Media_Video_Click implements OnClickListener
	 {

		@Override
		public void onClick(View arg0)
		{
			open_photo=false;//打开图片文件
			open_video=true;//打开视频文件
			Intent intent=new Intent();
			intent.setClass(PlaybackActivity.this, videolistActivity.class);
			startActivity(intent);
		}		 
	 }	
	/*********************************************************************************************************
	** 功能说明：查看照片
	** 传入参数：无
	** 得到参数：无      
	*********************************************************************************************************/		
	 class Media_Photo_Click implements OnClickListener
	 {

		@Override
		public void onClick(View arg0)
		{
			open_photo=true;//打开图片文件
			open_video=false;//打开视频文件
			Intent intent=new Intent();
			intent.setClass(PlaybackActivity.this, photolistActivity.class);
			startActivity(intent);
		}		 
	 }
												
	/*********************************************************************************************************
	 ** 功能说明：UI界面消息显示
	 ********************************************************************************************************/
	public void DisplayToast(String str)
	{
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}			 
	 
}
