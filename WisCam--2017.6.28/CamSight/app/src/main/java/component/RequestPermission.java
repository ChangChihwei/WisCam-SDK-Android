package component;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.jean.camsight.MainActivity;
import com.jean.camsight.PlayActivity;

import java.io.File;

/**
 * Created by Jean on 2016/7/21.
 */
public class RequestPermission {

    public int WRITE_EXTERNAL_STORAGE = 1;
    public void requestWriteSettings(Activity _activity) {
        ActivityCompat.requestPermissions(_activity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
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
        }
        PlayActivity.photofile_path=sdcardDir.getPath()+ MainActivity.CamSight_Photo;
        PlayActivity.videofile_path=sdcardDir.getPath()+ MainActivity.CamSight_Video;
    }
}
