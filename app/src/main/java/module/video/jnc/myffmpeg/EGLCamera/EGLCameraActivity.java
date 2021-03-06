package module.video.jnc.myffmpeg.EGLCamera;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import javax.microedition.khronos.opengles.GL10;

import module.video.jnc.myffmpeg.R;
import module.video.jnc.myffmpeg.opengl.NewMyRender;

import static android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY;

public class EGLCameraActivity extends Activity{

    MyCameraView myCameraView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        myCameraView = new MyCameraView(this);
        setContentView(myCameraView);
    }


    @Override
    protected void onPause() {
        super.onPause();
        myCameraView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        myCameraView.onResume();
    }
}
