package d2d.example.example2_sender;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import net.verdx.libstreaming.BasicViewModel;
import net.verdx.libstreaming.DefaultViewModel;
import net.verdx.libstreaming.StreamingRecord;
import net.verdx.libstreaming.audio.AudioQuality;
import net.verdx.libstreaming.gui.AutoFitTextureView;
import net.verdx.libstreaming.sessions.Session;
import net.verdx.libstreaming.sessions.SessionBuilder;
import net.verdx.libstreaming.video.CameraController;
import net.verdx.libstreaming.video.VideoPacketizerDispatcher;
import net.verdx.libstreaming.video.VideoQuality;

import java.util.UUID;


/**
 * A straightforward example of how to stream AMR and H.263 to some public IP using libstreaming.
 * Note that this example may not be using the latest version of libstreaming !
 */
public class MainActivity extends AppCompatActivity implements OnClickListener, Session.Callback, TextureView.SurfaceTextureListener {

    private final static String TAG = "MainActivity";
    private Button mButtonRecord, mButtonSwap;
    private EditText mEditText;
    private TextView mStatusTextView;
    private Session mSession;
    private AutoFitTextureView mTextureView;
    private final String mNameStreaming = "default_stream";
    private SessionBuilder mSessionBuilder;
    private Boolean isNetworkAvailable;
    private BasicViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getPermissions();

        CameraController.initiateInstance(this);

        mTextureView = findViewById(R.id.textureView);
        mButtonRecord = findViewById(R.id.record);
        mButtonSwap = findViewById(R.id.swap);
        mEditText = findViewById(R.id.editText1);
        mStatusTextView = findViewById(R.id.statusTextView);

        mSessionBuilder = SessionBuilder.getInstance()
                .setCallback(this)
                .setPreviewOrientation(90)
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                .setAudioQuality(new AudioQuality(16000, 32000))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .setVideoQuality(new VideoQuality(320, 240, 20, 500000));

        mSession = mSessionBuilder.build();

        CameraController.initiateInstance(this);

        mTextureView.setSurfaceTextureListener(this);

        mButtonRecord.setOnClickListener(this);
        mButtonSwap.setOnClickListener(this);

        mViewModel = new DefaultViewModel(this.getApplication());

        mViewModel.isNetworkAvailable().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
               isNetworkAvailable = aBoolean;
               mStatusTextView.setText(getDeviceStatus());
               if(isNetworkAvailable){
                   mViewModel.initNetwork();
               }
            }
        });
    }

    private void getPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

    @Override
    public void onResume() {


        super.onResume();
        if (mSession.isStreaming()) {
            mButtonRecord.setText(R.string.stop);
        } else {
            mButtonRecord.setText(R.string.start);
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
        if (v.getId() == R.id.record) {
            if(isNetworkAvailable) {
                // Starts/stops streaming
                mSession.setDestination(mEditText.getText().toString());
                if (!mSession.isStreaming()) {
                    startStreaming();
                } else {
                    stopStreaming();
                }
                mButtonRecord.setEnabled(false);
            } else {
                Toast.makeText(this, "The Network is not available", Toast.LENGTH_LONG).show();
            }
        } else {
            // Switch between the two cameras
            CameraController.getInstance().switchCamera();
        }
    }

    private void stopStreaming() {

        StreamingRecord.getInstance().removeLocalStreaming();
        mSession.stop();

    }

    private void startStreaming() {

        mSession.configure();
        final UUID localStreamUUID = UUID.randomUUID();
        StreamingRecord.getInstance().addLocalStreaming(localStreamUUID, mNameStreaming, mSessionBuilder);
    }

    public String getDeviceStatus() {
        Pair<Boolean, String> status = mViewModel.getDeviceStatus(this);
        if(status.first){
            mStatusTextView.setTextColor(getResources().getColor(net.verdx.libstreaming.R.color.colorAccent, null));
            return status.second;
        }

        mStatusTextView.setTextColor(getResources().getColor(net.verdx.libstreaming.R.color.colorRed, null));
        return status.second;

    }

    @Override
    public void onBitrateUpdate(long bitrate) {
        Log.d(TAG, "Bitrate: " + bitrate);
    }

    @Override
    public void onSessionError(int message, int streamType, Exception e) {
        mButtonRecord.setEnabled(true);
        if (e != null) {
            logError(e.getMessage());
        }
        mButtonRecord.setText(R.string.start);
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
        mButtonRecord.setEnabled(true);
        mButtonRecord.setText(R.string.stop);
    }

    @Override
    public void onSessionStopped() {
        Log.d(TAG, "Session stopped.");
        mButtonRecord.setEnabled(true);
        mButtonRecord.setText(R.string.start);
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
        CameraController.getInstance().configureCamera(mTextureView, this);
        CameraController.getInstance().startCamera();
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
