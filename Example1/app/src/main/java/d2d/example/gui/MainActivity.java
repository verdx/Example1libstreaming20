package d2d.example.gui;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.WindowManager;

import com.example.gui.R;

import d2d.testing.streaming.rtsp.RtspServer;
import d2d.testing.streaming.sessions.SessionBuilder;

/**
 * A straightforward example of how to use the RTSP server included in libstreaming.
 */
public class MainActivity extends Activity {

	private final static String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		// Sets the port of the RTSP server to 1234
		Editor editor = getDefaultSharedPreferences(this).edit();
		editor.putString(RtspServer.KEY_PORT, String.valueOf(1234));
		editor.commit();

		// Configures the SessionBuilder
		SessionBuilder.getInstance()
		.setPreviewOrientation(90)
		.setContext(getApplicationContext())
		.setAudioEncoder(SessionBuilder.AUDIO_NONE)
		.setVideoEncoder(SessionBuilder.VIDEO_H264);
		
		// Starts the RTSP server
		this.startService(new Intent(this,RtspServer.class));

	}
	
}
