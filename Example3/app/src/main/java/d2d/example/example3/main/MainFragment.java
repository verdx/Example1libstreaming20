package d2d.example.example3.main;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

import d2d.example.example3.R;
import d2d.example.example3.StreamActivity;

public class MainFragment extends Fragment implements StreamingRecordObserver, View.OnClickListener {
    private TextView myStatus;
    private TextView numStreams;
    private ArrayList<StreamDetail> streamList;
    private StreamListAdapter arrayAdapter;
    private BasicViewModel mViewModel;
    private Boolean isNetworkAvailable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
          Create a new ArrayList of StreamDetail, which will store the available streams information.
         */
        streamList = new ArrayList<>();
        /*
          Create a new instance of DefaultViewModel, an intermediary class used to manage the network.
         */
        mViewModel = new DefaultViewModel(this.requireActivity().getApplication());
        ((DefaultViewModel) mViewModel).setDestinationIpsSettings(this.requireActivity().getApplication());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        /*
          Read the IPs the app will use to send and receive the streams from the settings.
         */
        ((DefaultViewModel) mViewModel).setDestinationIpsSettings(this.requireActivity().getApplication());

        /*
          Set the recycler view attached to the interfaces list view and create a LayoutManager and
          a StreamListAdapter for it.
         */
        RecyclerView streamsListView = root.findViewById(R.id.streamListView);
        streamsListView.setLayoutManager(new LinearLayoutManager(getContext()));
        arrayAdapter = new StreamListAdapter(getContext(), streamList, this.requireActivity());
        streamsListView.setAdapter(arrayAdapter);

        /*
          Add some elements to the list, purely for aesthetic purposes.
         */
        addDefaultItemList();

        /*
          Set the number of streams available to 0.
         */
        numStreams = root.findViewById(R.id.streams_available);
        numStreams.setText(getString(R.string.dispositivos_encontrados, 0));

        /*
          Add this class as an observer to the StreamingRecord static instance. This will allow
          for the class to be notified when a new stream is available or when a stream is no longer
          available.
         */
        StreamingRecord.getInstance().addObserver(this);

        /*
          Set the record button to open the StreamActivity when clicked and other UI elements.
         */
        Button record = root.findViewById(R.id.recordButton);
        record.setOnClickListener(this);
        myStatus = root.findViewById(R.id.my_status);


        /*
          An important part of the usage of this library is to set a listener on the value of variable
            isNetworkAvailable, which is a MutableLiveData<Boolean> object. This variable is set to true
            when the device is connected to a network and false when it is not. This is done by the
            ViewModel class, which is an intermediary class used to manage the network.
         */
        mViewModel.isNetworkAvailable().observe(getViewLifecycleOwner(), observedBoolean -> {
            isNetworkAvailable = observedBoolean;
            myStatus.setText(getDeviceNetworkStatus());
            if(isNetworkAvailable){
                mViewModel.initNetwork();
            }
        });

        return root;
    }


    /**
     * Add the default items to the list, purely for aesthetic purposes.
     */
    private void addDefaultItemList(){
        streamList.clear();
        streamList.add(null);
        streamList.add(null);
        streamList.add(null);
        streamList.add(null);
    }

    /**
     * Remove the default items from the list, used when a new stream is added to the list.
     */
    private void removeDefaultItemList(){
        streamList.removeIf(Objects::isNull);
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*
          Remove this class as an observer to the StreamingRecord static instance.
         */
        StreamingRecord.getInstance().removeObserver(this);
    }

    /**
     * Update the list of streams available.
     * @param on_off Boolean value indicating if the stream is now available or now not available.
     * @param uuid UUID of the stream.
     * @param name Name of the stream.
     * @param ip IP of the stream.
     * @param port Port in which the stream is being listened to.
     * @param download Boolean value indicating if the stream is being downloaded or not.
     */
    public void updateList(boolean on_off, String uuid, String name, String ip, int port, boolean download){
        /*
          First of all the default items are removed from the list, if they are present.
         */
        removeDefaultItemList();
        if(!ip.equals("0.0.0.0")) {
            /*
              A new StreamDetail object is created with the information given
             */
            StreamDetail detail = new StreamDetail(uuid, name, ip, port, download);
            /*
              If the stream is now available, it is added to the list. If it is not available, it is
              removed from the list.
             */
            if (on_off) {
                if (!streamList.contains(detail))
                    streamList.add(detail);
            } else {
                streamList.remove(detail);
            }
            /*
              The number of streams available is updated and the StreamListAdapter is notified of the
              changes.
             */
            numStreams.setText(getString(R.string.dispositivos_encontrados, streamList.size()));
            if(streamList.size() == 0) addDefaultItemList();
            arrayAdapter.setStreamsData(streamList);
        }
    }

    /**
     * Updates the download state of a stream in the list, identified by its UUID.
     * @param uuid UUID of the stream.
     * @param isDownload Boolean value indicating if the stream is being downloaded or not.
     */
    public void changeStreamDownloadState(String uuid, boolean isDownload){
        for(StreamDetail value: streamList){
            if (value.getUuid().equals(uuid)) {
                value.setDownload(isDownload);
                arrayAdapter.setStreamsData(streamList);
                return;
            }
        }
    }

    /**
     * Get the network status of the device and change the color of the status text accordingly.
     * @return String with the status of the device.
     */
    public String getDeviceNetworkStatus() {
        Pair<Boolean, String> status = mViewModel.getDeviceNetworkStatus(getContext());
        if(status.first){
            myStatus.setTextColor(getResources().getColor(R.color.colorPrimaryDark, null));
        } else {
            myStatus.setTextColor(getResources().getColor(R.color.colorRed, null));
        }
        return status.second;
    }

    /**
     * Check if the device has a camera.
     * @return Boolean value indicating if the device has a camera or not.
     */
    private boolean checkCameraHardware() {
        if (requireActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
            // This device has a camera
            return true;
        } else {
            // No camera on this device
            Toast.makeText(requireActivity().getApplicationContext(), "YOUR DEVICE HAS NO CAMERA", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * Open the StreamActivity.
     */
    private void openStreamActivity() {
        Intent streamActivityIntent = new Intent(getActivity(), StreamActivity.class);
        this.startActivity(streamActivityIntent);
    }

    @Override
    public void onLocalStreamingAvailable(UUID id, String name, SessionBuilder sessionBuilder) {}

    @Override
    public void onLocalStreamingUnavailable() {}

    @Override
    public void onStreamingAvailable(final Streaming streaming, boolean bAllowDispatch) {
        final String path = streaming.getUUID().toString();
        /*
          The updateList method is called on the UI thread to update the list of streams available
          when a new stream is made available
         */
        requireActivity().runOnUiThread(() -> updateList(true,
                path,
                streaming.getName(),
                streaming.getReceiveSession().getDestinationAddress().toString(),
                streaming.getReceiveSession().getDestinationPort(),
                streaming.isDownloading()));
    }

    @Override
    public void onStreamingDownloadStateChanged(final Streaming streaming, final boolean bIsDownload) {
        final String path = streaming.getUUID().toString();
        /*
          The changeStreamDownloadState method is called on the UI thread to update the download state
          of a stream when it changes.
         */
        requireActivity().runOnUiThread(() -> changeStreamDownloadState(path, bIsDownload));
    }

    @Override
    public void onStreamingUnavailable(final Streaming streaming) {
        final String path = streaming.getUUID().toString();
        /*
          The updateList method is called on the UI thread to update the list of streams available
          when a stream is no longer available.
         */
        requireActivity().runOnUiThread(() -> updateList(false,
                path,
                streaming.getName(),
                streaming.getReceiveSession().getDestinationAddress().toString(),
                streaming.getReceiveSession().getDestinationPort(),
                streaming.isDownloading()));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.recordButton) {
            if(checkCameraHardware()){
                if(!isNetworkAvailable){
                    Toast.makeText(MainFragment.this.getContext(), R.string.record_not_available, Toast.LENGTH_SHORT).show();
                }
                else{
                    openStreamActivity();
                }
            }
        }
    }
}
