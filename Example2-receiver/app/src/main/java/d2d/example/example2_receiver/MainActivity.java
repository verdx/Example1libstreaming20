package d2d.example.example2_receiver;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Pair;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
public class MainActivity extends AppCompatActivity implements StreamingRecordObserver, SwipeRefreshLayout.OnRefreshListener {

    private final static String TAG = "MainActivity";
    private ArrayList<StreamDetail> mStreamList;
    private StreamListAdapter mArrayAdapter;
    private BasicViewModel mViewModel;
    private TextView mStatusTextView;
    private TextView mNumStreams;
    private SwipeRefreshLayout mArrayListRefresh;
    private Boolean mIsNetworkAvailable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Set the UI, which includes a RecyclerView to display incoming streams
         */
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /*
        Initialize the UI elements
         */
        mStatusTextView = findViewById(R.id.my_status);
        mArrayListRefresh = findViewById(R.id.swiperefresh);
        mArrayListRefresh.setOnRefreshListener(this);

        /*
        Initialize the RecyclerView with an adapter and an array list
         */
        mStreamList = new ArrayList<>();
        RecyclerView streamsListView = this.findViewById(R.id.streamListView);
        streamsListView.setLayoutManager(new LinearLayoutManager(this));
        addDefaultItemList();
        mArrayAdapter = new StreamListAdapter(this, mStreamList, this);
        streamsListView.setAdapter(mArrayAdapter);

        /*
        Initialize the StreamingRecord singleton and add this activity as an observer
         */
        StreamingRecord.getInstance().addObserver(this);

        /*
        Initialize the ViewModel, set the Incoming IPs(in this example from a EditText) and observe the network status
         */
        mViewModel = new DefaultViewModel(this.getApplication());
        mViewModel.isNetworkAvailable().observe(this, (Observer<Boolean>) aBoolean -> {
            mIsNetworkAvailable = aBoolean;
            mStatusTextView.setText(getDeviceStatus());
            if(mIsNetworkAvailable){
                mViewModel.initNetwork();
            }
        });

        /*
          Set the number of streams available to 0.
         */
        mNumStreams = this.findViewById(R.id.streams_available);
        mNumStreams.setText(getString(R.string.streams_available, 0));
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
    public void onLocalStreamingAvailable(UUID id, String name, SessionBuilder sessionBuilder) {}

    @Override
    public void onLocalStreamingUnavailable() {}

    @Override
    public void onStreamingAvailable(Streaming streaming, boolean bAllowDispatch) {
        final String path = streaming.getUUID().toString();
        this.runOnUiThread(() -> updateList(true,
                path,
                streaming.getName(),
                streaming.getReceiveSession().getDestinationAddress().toString(),
                streaming.getReceiveSession().getDestinationPort(),
                streaming.isDownloading()));
    }

    @Override
    public void onStreamingUnavailable(Streaming streaming) {
        final String path = streaming.getUUID().toString();
        this.runOnUiThread(() -> updateList(false,
                path,
                streaming.getName(),
                streaming.getReceiveSession().getDestinationAddress().toString(),
                streaming.getReceiveSession().getDestinationPort(),
                streaming.isDownloading()));
    }

    @Override
    public void onStreamingDownloadStateChanged(Streaming streaming, boolean bIsDownloading) {
        final String path = streaming.getUUID().toString();
        this.runOnUiThread(() -> setStreamDownload(path, bIsDownloading));
    }

    @Override
    public void onRefresh() {
        ArrayList<Streaming> streamList = new ArrayList<>(StreamingRecord.getInstance().getStreamings());
        if (mIsNetworkAvailable) mViewModel.initNetwork();
        for(Streaming stream: streamList){
            final String path = stream.getUUID().toString();
            this.runOnUiThread(() -> updateList(true,
                    path,
                    stream.getName(),
                    stream.getReceiveSession().getDestinationAddress().toString(),
                    stream.getReceiveSession().getDestinationPort(),
                    stream.isDownloading()));
        }
        mArrayListRefresh.setRefreshing(false);
    }

    private void addDefaultItemList(){
        mStreamList.clear();
        mStreamList.add(null);
        mStreamList.add(null);
        mStreamList.add(null);
    }

    private void removeDefaultItemList(){
        mStreamList.removeIf(Objects::isNull);
    }


    public String getDeviceStatus() {
        Pair<Boolean, String> status = mViewModel.getDeviceNetworkStatus(this);
        if(status.first){
            mStatusTextView.setTextColor(getResources().getColor(net.verdx.libstreaming.R.color.colorAccent, null));
            return status.second;
        }

        mStatusTextView.setTextColor(getResources().getColor(net.verdx.libstreaming.R.color.colorRed, null));
        return status.second;

    }

    public void updateList(boolean on_off, String uuid, String name, String ip, int port, boolean download){
        removeDefaultItemList();
        if(!ip.equals("0.0.0.0")) {
            StreamDetail detail = new StreamDetail(uuid, name, ip, port, download);
            if (on_off) {
                if (!mStreamList.contains(detail))
                    mStreamList.add(detail);
            } else {
                mStreamList.remove(detail);
            }
            mNumStreams = this.findViewById(R.id.streams_available);
            mNumStreams.setText(getString(R.string.streams_available, mStreamList.size()));
            if(mStreamList.size() == 0) addDefaultItemList();
            mArrayAdapter.setStreamsData(mStreamList);
        }
    }

    public void setStreamDownload(String uuid, boolean isDownload){
        for(StreamDetail value: mStreamList){
            if (value.getUuid().equals(uuid)) {
                value.setDownload(isDownload);
                mArrayAdapter.setStreamsData(mStreamList);
                return;
            }
        }
    }
}
