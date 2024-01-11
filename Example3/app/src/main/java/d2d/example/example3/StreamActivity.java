package d2d.example.example3;


import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.verdx.libstreaming.SaveStream;
import net.verdx.libstreaming.StreamingRecord;
import net.verdx.libstreaming.gui.AutoFitTextureView;
import net.verdx.libstreaming.sessions.SessionBuilder;
import net.verdx.libstreaming.video.CameraController;
import net.verdx.libstreaming.video.VideoPacketizerDispatcher;
import net.verdx.libstreaming.video.VideoQuality;

import java.util.UUID;

import d2d.example.example3.R;


public class StreamActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, CameraController.Callback,  View.OnClickListener {

    private final static String TAG = "StreamActivity";

    private AutoFitTextureView mTextureView;

    private SessionBuilder mSessionBuilder;

    private FloatingActionButton recordButton;
    private FloatingActionButton switchButton;
    public boolean mRecording = false;
    private final VideoQuality mVideoQuality = VideoQuality.DEFAULT_VIDEO_QUALITY;

    private boolean isDownload;

    private SaveStream saveStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /* Initializes the TextureView and sets this activity as a listener to it. */
        mTextureView = findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(this);

        /* Configures the SessionBuilder */
        mSessionBuilder = SessionBuilder.getInstance()
                .setPreviewOrientation(90)
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                .setVideoEncoder(SessionBuilder.VIDEO_NONE)
                .setVideoQuality(mVideoQuality);

        /* Initializes the buttons and sets listeners on them */
        recordButton = findViewById(R.id.button_capture);
        switchButton = findViewById(R.id.button_switch_camera);
        recordButton.setOnClickListener(this);
        switchButton.setOnClickListener(this);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    /**
     * Starts a streaming session by adding a new local stream to the StreamingRecord.
     * It also changes the icon of the record button to a stop icon.
     * If the user has enabled the download of the stream, it starts the download.
     */
    public void startStreaming() {
        final UUID localStreamUUID = UUID.randomUUID();
        final String mNameStreaming = "defaultStream";
        StreamingRecord.getInstance().addLocalStreaming(localStreamUUID, mNameStreaming, mSessionBuilder);

        recordButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_stop));
        mRecording = true;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isDownload = preferences.getBoolean("saveMyStreaming", false);
        if(isDownload) {
            saveStream = new SaveStream(getApplicationContext(), localStreamUUID.toString());
            saveStream.startDownload();
        }
    }

    /**
     * Stops the streaming session by removing the local stream from the StreamingRecord.
     * It also changes the icon of the record button to a start icon.
     * If the user has enabled the download of the stream, it stops the download.
     */
    private void stopStreaming() {
        StreamingRecord.getInstance().removeLocalStreaming();
        recordButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.videocam));
        mRecording = false;
        if(isDownload) saveStream.stopDownload();
        Toast.makeText(this,getString(R.string.stream_stop_str), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy(){
        if(mRecording) {
            stopStreaming();
        }
        VideoPacketizerDispatcher.stop();
        CameraController.getInstance().stopCamera();

        super.onDestroy();
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        /* Configures the camera and starts it when the SurfaceTexture is available*/
        CameraController.getInstance().configureCamera(mTextureView, this);
        CameraController.getInstance().startCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}

    @Override
    public void cameraStarted() {

    }

    @Override
    public void cameraError(int error) {
        Toast.makeText(this, "Camera error: " + error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void cameraError(Exception ex) {
        Toast.makeText(this, "Camera error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void cameraClosed() {
        Toast.makeText(this, "Camera closed", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onClick(View view) {
        /*
        Starts or stops the streaming session when the record button is clicked
        If the switch camera button is clicked, it switches the camera using the
        CameraController.
        */
        if (view.getId() == R.id.button_capture){
            if(!mRecording) {
                startStreaming();
            } else {
                stopStreaming();
            }
        } else if (view.getId() == R.id.button_switch_camera) {
            CameraController.getInstance().switchCamera();
        }
    }
}