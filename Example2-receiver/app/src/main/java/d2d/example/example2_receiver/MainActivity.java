package d2d.example.example2_receiver;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.verdx.libstreaming.BasicViewModel;
import net.verdx.libstreaming.DefaultViewModel;
import net.verdx.libstreaming.StreamListAdapter;
import net.verdx.libstreaming.Streaming;
import net.verdx.libstreaming.StreamingRecord;
import net.verdx.libstreaming.StreamingRecordObserver;
import net.verdx.libstreaming.gui.StreamDetail;
import net.verdx.libstreaming.sessions.SessionBuilder;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

/**
 * A straightforward example of how to stream AMR and H.263 to some public IP using libstreaming.
 * Note that this example may not be using the latest version of libstreaming !
 */
public class MainActivity extends Activity implements OnClickListener, StreamingRecordObserver {

    private final static String TAG = "MainActivity";
    private ArrayList<StreamDetail> streamList;
    private StreamListAdapter arrayAdapter;
    private BasicViewModel mViewModel;
    private Boolean isNetworkAvailable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);\

        streamList = new ArrayList<>();
        mViewModel = new DefaultViewModel(this.getApplication());

        RecyclerView streamsListView = this.findViewById(R.id.streamsList);
        streamsListView.setLayoutManager(new LinearLayoutManager(this));
        addDefaultItemList();
        arrayAdapter = new StreamListAdapter(this, streamList, this);
        streamsListView.setAdapter(arrayAdapter);

        StreamingRecord.getInstance().addObserver(this);
        
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
    }

    private void addDefaultItemList(){
        streamList.clear();
        streamList.add(null);
        streamList.add(null);
        streamList.add(null);
        streamList.add(null);
    }

    private void removeDefaultItemList(){
        streamList.removeIf(Objects::isNull);
    }

    @Override
    public void onLocalStreamingAvailable(UUID id, String name, SessionBuilder sessionBuilder) {

    }

    @Override
    public void onLocalStreamingUnavailable() {

    }

    @Override
    public void onStreamingAvailable(Streaming streaming, boolean bAllowDispatch) {

    }

    @Override
    public void onStreamingUnavailable(Streaming streaming) {

    }

    @Override
    public void onStreamingDownloadStateChanged(Streaming streaming, boolean bIsDownloading) {

    }
}
