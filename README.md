# Some examples of how to use libstreaming2.0

This repository contains three simple examples of how to use libstreaming2.0. All examples here use classes `DefaultNetwork` and `DefaultViewModel` to stream the video and audio using the default Android network and IP, but new implementations could be created of interfaces `BasicNetwork` and `BasicViewModel` to use a different network, such as Bluetooth or WiFi Direct.

### Libstreaming2.0 ?

You can find out more about libstreaming2.0 [here](https://github.com/verdx/libstreaming2.0).

### Example 1

Shows how to start the RTSP server as a standalone. It is mainly a small example of how to add libstreaming2.0 to a project, as it is not recommended for the RTSP Server to be used alone.

### Example 2 - Sender

Shows how to start streaming video directly from your camera, using a `StreamingRecord` and a `CameraController` to given IPs.

To use this example, write the IPs you want to send the streams to in the box and press the button to start the stream. The device's IP is written below the box.

### Example 2 - Receiver

Shows how to start receiving video streams from given IPs, using `StreamingRecord`. It also uses the UI classes in the library, such as `ViewStreamActivity` to visualize the streams, and `StreamListAdapter` and `StreamDetail` to create a RecyclerView where the incoming IPs are displayed.

To use this example, press on the list element to open an activity to watch the stream whenever a stream is received. For it to be received, this device's IP(shown in the main screen) must be inserted in the destination IPs on a sender(Example2-sender or Example3)

### Example 3

This example is a complete application, capable of sending and receiving streams, as well as downloading them and watching them in a Gallery. It also provides "multi-hopping", streams received will be sent to other devices in the network.

To use this example, you need to set the IPs with which to connect in the settings. With that done the streams from those devices will appear in the list on the main screen. To start your own stream press the 'RECORD' button and to watch a received stream press its element in the list. 

To download locally started streams set that option in the settings. To download a received stream press the download button in its element in the list and then watch it. To see any downloaded streams head to the gallery(inside the app, hamburger menu).


### Build instructions

1. Clone the repository

2. libstreaming2.0 is an Android Studio module referenced as a git submodule in this repo, so you will need to run the two following commands:
```sh
git submodule init
git submodule update
```

3. Run the **android project update** command in the libstreaming directory and in the directory of the example you wish to compile:
```sh
cd libstreaming
android update project --path . --target android-21
cd ../example3/app
android update project --path . --target android-21
```

4. Run ant
```sh
ant debug
```

**Note: you will need to run 'ant clean' before compiling another example!**
