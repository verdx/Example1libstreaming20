package d2d.example.example2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import d2d.testing.streaming.StreamingRecord;
import d2d.testing.streaming.audio.AudioQuality;
import d2d.testing.streaming.gui.AutoFitTextureView;
import d2d.testing.streaming.sessions.Session;
import d2d.testing.streaming.sessions.SessionBuilder;
import d2d.testing.streaming.video.CameraController;
import d2d.testing.streaming.video.VideoPacketizerDispatcher;
import d2d.testing.streaming.video.VideoQuality;

/**
 * A straightforward example of how to stream AMR and H.263 to some public IP using libstreaming.
 * Note that this example may not be using the latest version of libstreaming !
 */
public class MainActivity extends Activity implements OnClickListener, Session.Callback, TextureView.SurfaceTextureListener {

    private final static String TAG = "MainActivity";

    private Button mButton1, mButton2;
    private EditText mEditText;
    private Session mSession;
    private AutoFitTextureView mTextureView;
    private CameraController mCameraController;
    private boolean mRecording = false;
    private String mNameStreaming = "default_stream";
    private SessionBuilder mSessionBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        CameraController.initiateInstance(this);

        mTextureView = findViewById(R.id.textureView);
        mButton1 = findViewById(R.id.button1);
        mButton2 = findViewById(R.id.button2);
        mEditText = findViewById(R.id.editText1);

        mSessionBuilder = SessionBuilder.getInstance()
                .setCallback(this)
                .setPreviewOrientation(90)
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_NONE)
                .setAudioQuality(new AudioQuality(16000, 32000))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .setVideoQuality(new VideoQuality(320, 240, 20, 500000));

        mSession = mSessionBuilder.build();


        mTextureView.setSurfaceTextureListener(this);


        mButton1.setOnClickListener(this);
        mButton2.setOnClickListener(this);

    }

    @Override
    public void onResume() {


        super.onResume();
        if (mSession.isStreaming()) {
            mButton1.setText(R.string.stop);
        } else {
            mButton1.setText(R.string.start);
        }
    }

    @Override
    public void onDestroy() {
        VideoPacketizerDispatcher.stop();
        CameraController.getInstance().stopCamera();
        super.onDestroy();
        mSession.release();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button1) {
            // Starts/stops streaming
            mSession.setDestination(mEditText.getText().toString());
            if (!mSession.isStreaming()) {
                mSession.configure();

                final UUID localStreamUUID = UUID.randomUUID();
                StreamingRecord.getInstance().addLocalStreaming(localStreamUUID, mNameStreaming, mSessionBuilder);
            } else {

                StreamingRecord.getInstance().removeLocalStreaming();
                mSession.stop();
            }

            mButton1.setEnabled(false);
        } else {
            // Switch between the two cameras
            //mSession.switchCamera();
        }
    }

    @Override
    public void onBitrateUpdate(long bitrate) {
        Log.d(TAG, "Bitrate: " + bitrate);
    }

    @Override
    public void onSessionError(int message, int streamType, Exception e) {
        mButton1.setEnabled(true);
        if (e != null) {
            logError(e.getMessage());
        }
    }

    @Override

    public void onPreviewStarted() {
        Log.d(TAG, "Preview started.");
    }

    @Override
    public void onSessionConfigured() {
        Log.d(TAG, "Preview configured.");
        // Once the stream is configured, you can get a SDP formatted session description
        // that you can send to the receiver of the stream.
        // For example, to receive the stream in VLC, store the session description in a .sdp file
        // and open it with VLC while streaming.
        Log.d(TAG, mSession.getSessionDescription());
        mSession.start();
    }

    @Override
    public void onSessionStarted() {
        Log.d(TAG, "Session started.");
        mButton1.setEnabled(true);
        mButton1.setText(R.string.stop);
    }

    @Override
    public void onSessionStopped() {
        Log.d(TAG, "Session stopped.");
        mButton1.setEnabled(true);
        mButton1.setText(R.string.start);
    }

    /**
     * Displays a popup to report the error to the user
     */
    private void logError(final String msg) {
        final String error = (msg == null) ? "Error unknown" : msg;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(error).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        mCameraController = CameraController.getInstance();
        List<Surface> surfaces = new ArrayList<>();
        String cameraId = mCameraController.getCameraIdList()[0];
        Size[] resolutions = mCameraController.getPrivType_2Target_MaxResolutions(cameraId, SurfaceTexture.class, MediaCodec.class);

        mTextureView.setAspectRatio(resolutions[0].getWidth(), resolutions[0].getHeight());
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(resolutions[0].getWidth(), resolutions[0].getHeight());
        Surface surface1 = new Surface(surfaceTexture);
        surfaces.add(surface1);

        try {
            VideoPacketizerDispatcher.start(PreferenceManager.getDefaultSharedPreferences(this), VideoQuality.DEFAULT_VIDEO_QUALITY);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to start camera preview", Toast.LENGTH_LONG).show();
        }
        surfaces.add(VideoPacketizerDispatcher.getEncoderInputSurface());

        mCameraController.startCamera(cameraId,surfaces);
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

    }
}
