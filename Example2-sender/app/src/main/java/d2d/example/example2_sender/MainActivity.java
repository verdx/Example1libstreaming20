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
import net.verdx.libstreaming.Streaming;
import net.verdx.libstreaming.StreamingRecord;
import net.verdx.libstreaming.StreamingRecordObserver;
import net.verdx.libstreaming.audio.AudioQuality;
import net.verdx.libstreaming.gui.AutoFitTextureView;
import net.verdx.libstreaming.sessions.SessionBuilder;
import net.verdx.libstreaming.video.CameraController;
import net.verdx.libstreaming.video.VideoPacketizerDispatcher;
import net.verdx.libstreaming.video.VideoQuality;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;


/**
 * A straightforward example of how to stream AMR and H.263 to some public IP using libstreaming.
 * Note that this example may not be using the latest version of libstreaming !
 */
public class MainActivity extends AppCompatActivity implements OnClickListener, TextureView.SurfaceTextureListener, StreamingRecordObserver {

    private final static String TAG = "MainActivity";
    private Button mButtonRecord, mButtonSwap;
    private EditText mEditText;
    private TextView mStatusTextView;
    private AutoFitTextureView mTextureView;
    private final String mStreamName = "defaultName_sender";
    private SessionBuilder mSessionBuilder;
    private Boolean isNetworkAvailable;
    private boolean isStreaming = false;
    private BasicViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getPermissions();

        CameraController.initiateInstance(this);
        StreamingRecord.getInstance().addObserver(this);

        mTextureView = findViewById(R.id.textureView);
        mButtonRecord = findViewById(R.id.record);
        mButtonSwap = findViewById(R.id.swap);
        mEditText = findViewById(R.id.editText1);
        mStatusTextView = findViewById(R.id.statusTextView);

        mSessionBuilder = SessionBuilder.getInstance()
                .setPreviewOrientation(90)
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                .setAudioQuality(new AudioQuality(16000, 32000))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .setVideoQuality(new VideoQuality(320, 240, 20, 500000));

        CameraController.initiateInstance(this);

        mTextureView.setSurfaceTextureListener(this);

        mButtonRecord.setOnClickListener(this);
        mButtonSwap.setOnClickListener(this);

        mViewModel = new DefaultViewModel(this.getApplication());
        ((DefaultViewModel)mViewModel).setDestinationIpsArray(new ArrayList<String>());

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
        if (isStreaming) {
            mButtonRecord.setText(R.string.stop);
        } else {
            mButtonRecord.setText(R.string.start);
        }
    }

    @Override
    public void onDestroy() {
        if(isStreaming) {
            stopStreaming();
        }
        VideoPacketizerDispatcher.stop();
        CameraController.getInstance().stopCamera();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.record) {
            if(isNetworkAvailable) {
                // Starts/stops streaming
                try {
                    setDestinationIps();
                    if (!isStreaming) {
                        if (mEditText.getText().toString().equals("")) {
                            Toast.makeText(this, "Please enter the destination IP", Toast.LENGTH_LONG).show();
                            return;
                        }
                        mButtonRecord.setEnabled(false);
                        startStreaming();
                    } else {
                        mButtonRecord.setEnabled(false);
                        stopStreaming();
                    }
                } catch (IllegalArgumentException e) {
                    Toast.makeText(this,  e.getMessage(), Toast.LENGTH_LONG).show();
                }
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
        isStreaming = false;
    }

    private void startStreaming() {

        final UUID localStreamUUID = UUID.randomUUID();
        StreamingRecord.getInstance().addLocalStreaming(localStreamUUID, mStreamName, mSessionBuilder);
        isStreaming = true;
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

    private void setDestinationIps() {
        String[] ipArray = mEditText.getText().toString().replaceAll("\\s","").split(",");
        ArrayList<String> ipList = new ArrayList<>();
        Collections.addAll(ipList, ipArray);
        ((DefaultViewModel)mViewModel).setDestinationIpsArray(ipList);
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
    public void onLocalStreamingAvailable(UUID id, String name, SessionBuilder sessionBuilder) {
        Log.d(TAG, "Streaming started.");
        mButtonRecord.setEnabled(true);
        mButtonRecord.setText(R.string.stop);
    }

    @Override
    public void onLocalStreamingUnavailable() {
        Log.d(TAG, "Streaming stopped.");
        mButtonRecord.setEnabled(true);
        mButtonRecord.setText(R.string.start);
    }


    // NOT USED
    @Override
    public void onStreamingAvailable(Streaming streaming, boolean bAllowDispatch) {

    }

    @Override
    public void onStreamingUnavailable(Streaming streaming) {

    }

    @Override
    public void onStreamingDownloadStateChanged(Streaming streaming, boolean bIsDownloading) {

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
