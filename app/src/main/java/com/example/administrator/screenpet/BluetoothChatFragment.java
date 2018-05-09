/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothchat;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.common.logger.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class BluetoothChatFragment extends Fragment {

    private static final String TAG = "BluetoothChatFragment";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    private Button mGetImageButton;
    private Button mSendImageButton;
    private ImageView mImageView;


    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private ArrayAdapter<String> mConversationArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }



    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mConversationView = (ListView) view.findViewById(R.id.in);
        mOutEditText = (EditText) view.findViewById(R.id.edit_text_out);
        mSendButton = (Button) view.findViewById(R.id.button_send);
        mGetImageButton = (Button) view.findViewById(R.id.btn_get_image);
        mSendImageButton = (Button) view.findViewById(R.id.btn_send_image);
        mImageView = (ImageView) view.findViewById(R.id.iv_show_image);

        mGetImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("*/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });

        mSendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] bytes = {};
                try {
                    File file = new File(imagepath_to_send);
                    if(file.exists()){
                        FileInputStream fis = new FileInputStream(file);
                        bytes = new byte[fis.available()];
                        fis.read(bytes);
                        Log.d(TAG, "We want to send image of "+bytes.length+" bytes");
                        fis.close();
                    }

                }catch (IOException e){
                    Log.d(TAG, e.getMessage());
                }
                if(bytes.length != 0){
                    sendBytes(bytes, TYPE_IMAGE);
                }
            }
        });
    }



    public static final int REQUEST_IMAGE = 7;
    private String getRealPathFromURI(Uri contentUri, Context activity) {
        String path = null;
        try {
            final String[] proj = {MediaStore.MediaColumns.DATA};
            final Cursor cursor = ((Activity) activity).managedQuery(contentUri, proj, null, null, null);
            final int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index);
        } catch (Exception e) {
        }
        if (path != null && path.length() > 0) {
            return path;
        } else return contentUri.getPath();
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);

        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
                    String message = textView.getText().toString();
                    sendMessage(message);
                }
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(getActivity(), mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            sendBytes(send, TYPE_TEXT);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText("");
        }
    }

    private void sendBytes(byte[] bytes, int type){
        if(type == TYPE_TEXT){
            // for text we will not separate them but send all immediately
            byte[] send_byte = wrap_content(bytes, TYPE_TEXT, END, 0);
            mChatService.write(send_byte);
        }
        else if(type == TYPE_IMAGE){
            int sendtimes = bytes.length/Default_Max_Package_Length;
            if(bytes.length % Default_Max_Package_Length != 0){
                sendtimes += 1;
            }
            // we need to send sendtimes
            for(int i=0; i<sendtimes; i++){
                if(i != sendtimes -1){
                    byte[] part_bytes = new byte[Default_Max_Package_Length];
                    for(int j=0; j<Default_Max_Package_Length; j++){
                        part_bytes[j] = bytes[i*Default_Max_Package_Length + j];
                    }
                    byte[] wrapped_part_bytes = wrap_content(part_bytes, TYPE_IMAGE, CONTINUE, i);
                    mChatService.write(wrapped_part_bytes);
                }else{
                    int left = bytes.length - (sendtimes-1)*Default_Max_Package_Length;
                    byte[] part_bytes = new byte[left];
                    for(int j=0; j<left; j++){
                        part_bytes[j] = bytes[(sendtimes-1)*Default_Max_Package_Length + j];
                    }
                    byte[] wrapped = wrap_content(part_bytes, TYPE_IMAGE, END, (sendtimes-1));
                    mChatService.write(wrapped);
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                }catch (InterruptedException e){
                    Log.e(TAG, e.getMessage());
                }

            }
        }else if(type == TYPE_VDIEO){
            int sendtimes = bytes.length/Default_Max_Package_Length;
            if(bytes.length % Default_Max_Package_Length != 0){
                sendtimes += 1;
            }
            // we need to send sendtimes
            for(int i=0; i<sendtimes; i++){
                if(i != sendtimes -1){
                    byte[] part_bytes = new byte[Default_Max_Package_Length];
                    for(int j=0; j<Default_Max_Package_Length; j++){
                        part_bytes[j] = bytes[i*Default_Max_Package_Length + j];
                    }
                    byte[] wrapped_part_bytes = wrap_content(part_bytes, TYPE_VDIEO, CONTINUE, i);
                    mChatService.write(wrapped_part_bytes);
                }else{
                    int left = bytes.length - (sendtimes-1)*Default_Max_Package_Length;
                    byte[] part_bytes = new byte[left];
                    for(int j=0; j<left; j++){
                        part_bytes[j] = bytes[(sendtimes-1)*Default_Max_Package_Length + j];
                    }
                    byte[] wrapped = wrap_content(part_bytes, TYPE_VDIEO, END, (sendtimes-1));
                    mChatService.write(wrapped);
                }
            }
        }
    }

    public final static int CONTINUE = 4;
    public final static int END = 5;
    public final static int TYPE_IMAGE = 1;
    public final static int TYPE_TEXT = 2;
    public final static int TYPE_VDIEO = 3;
    public final static int HEAD_IND = 123456;

    private byte[] subbytes(byte[] src, int offset, int length) {
        byte[] dst = new byte[length];
        for (int i = offset; i < offset + length; i++) {
            dst[i - offset] = src[i];
        }
        return dst;
    }

    private int min(int a, int b) {
        return (a < b ? a : b);
    }

    public static int byteArrayToInt(byte[] bRefArr) {
        int iOutcome = 0;
        byte bLoop;

        for (int i = 0; i < bRefArr.length; i++) {
            bLoop = bRefArr[i];
            iOutcome += (bLoop & 0xFF) << (8 * i);
        }
        return iOutcome;
    }

    public static byte[] intToByteArray(int source) {
        byte[] bLocalArr = new byte[4];
        for (int i = 0; (i < 4); i++) {
            bLocalArr[i] = (byte) (source >> 8 * i & 0xFF);
        }
        return bLocalArr;
    }
    private int base = 10000;
    private byte[] getHeadByte(){
        byte[] res = new byte[24];
        for(int i=0; i<6; i++){
            byte[] a = intToByteArray(i+1+base);
            res[i*4] = a[0];
            res[i*4+1] = a[1];
            res[i*4+2] = a[2];
            res[i*4+3] = a[3];
        }
        return res;
    }

    private boolean isHead(byte[] a){
        for(int i=0; i<6; i++){
            byte[] sub = subbytes(a, i*4, 4);
            int k = byteArrayToInt(sub);
            if(k != (i+1+base)) return false;
        }
        return true;
    }
    private int Default_Max_Package_Length = 512;
    private int Default_Head_Length = 40;
    private byte[] wrap_content(byte[] bytes, int type, int isContinue, int ith){
        int length = bytes.length;
        byte[] head_bytes = getHeadByte();
        byte[] type_bytes = intToByteArray(type);
        byte[] iscontinue_bytes = intToByteArray(isContinue);
        byte[] ith_bytes = intToByteArray(ith);
        byte[] length_bytes = intToByteArray(length);

        byte[] head_package = new byte[Default_Head_Length];
        for(int i=0; i<24; i++) head_package[i] = head_bytes[i];
        for(int i=24; i<28; i++) head_package[i] = type_bytes[i-24];
        for(int i=28; i<32; i++) head_package[i] = iscontinue_bytes[i-28];
        for(int i=32; i<36; i++) head_package[i] = ith_bytes[i-32];
        for(int i=36; i<40; i++) head_package[i] = length_bytes[i-36];

        byte[] wrapped_bytes = new byte[bytes.length + Default_Head_Length];
        for(int i=0; i<Default_Head_Length; i++) wrapped_bytes[i] = head_package[i];
        for(int i=0; i<length; i++) wrapped_bytes[i+Default_Head_Length] = bytes[i];
        return wrapped_bytes;
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */

    private int image_cur_ith = -1;
    private int video_cur_ith = -1;

    private ByteArrayOutputStream mImageBuffer = new ByteArrayOutputStream();
    private ByteArrayOutputStream mVideoBuffer = new ByteArrayOutputStream();

    private ByteArrayOutputStream mImageWriteBuffer = new ByteArrayOutputStream();
    private ByteArrayOutputStream mVideoWriteBuffer = new ByteArrayOutputStream();

    private boolean isImage = false;
    private boolean isText = false;

    private int head_length = 24;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            if(msg.what == Constants.MESSAGE_STATE_CHANGE){
                switch (msg.arg1) {
                    case BluetoothChatService.STATE_CONNECTED:
                        setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                        mConversationArrayAdapter.clear();
                        break;
                    case BluetoothChatService.STATE_CONNECTING:
                        setStatus(R.string.title_connecting);
                        break;
                    case BluetoothChatService.STATE_LISTEN:
                    case BluetoothChatService.STATE_NONE:
                        setStatus(R.string.title_not_connected);
                        break;
                }
            }
            else if(msg.what == Constants.MESSAGE_WRITE){
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                byte[] receive_bytes = new byte[writeBuf.length];
                for(int i=0; i<writeBuf.length; i++){
                    receive_bytes[i] = writeBuf[i];
                }
                byte[] head_bytes = subbytes(receive_bytes, 0, head_length);
                byte[] type_bytes = new byte[4];
                byte[] iscontinue_bytes = new byte[4];
                byte[] ith_bytes = new byte[4];
                byte[] length_bytes = new byte[4];


                for(int i=0; i<4; i++){
                    type_bytes[i] = receive_bytes[i+head_length];
                    iscontinue_bytes[i] = receive_bytes[i+head_length + 4];
                    ith_bytes[i] = receive_bytes[i+head_length+8];
                    length_bytes[i] = receive_bytes[i+head_length+12];
                }
                int type_int = byteArrayToInt(type_bytes);
                int iscontinue_int = byteArrayToInt(iscontinue_bytes);
                int ith_int = byteArrayToInt(ith_bytes);
                int length_int = byteArrayToInt(length_bytes);

                if(!isHead(head_bytes)){
                    Log.d(TAG, "Error: it's not 123456");
                    return;
                }

                if(type_int == TYPE_TEXT){
                    byte[] txt_bytes = new byte[length_int];
                    for(int i=0; i<length_int; i++){
                        txt_bytes[i] = receive_bytes[i+Default_Head_Length];
                    }
                    String writeMessage = new String(txt_bytes);
                    mConversationArrayAdapter.add("Me:  " + writeMessage);

                }
                else if(type_int == TYPE_IMAGE ){
                    // add process to deal with IMAGE

                    byte[] image_bytes = new byte[length_int];
                    for(int i=0; i<length_int; i++){
                        image_bytes[i] = receive_bytes[i+Default_Head_Length];
                    }
                    if(iscontinue_int == CONTINUE){
                        try {
                            mImageWriteBuffer.write(image_bytes);
                            Log.d(TAG, "We receive "+ith_int+" part image.");
                        }catch (IOException e){
                            Log.d(TAG, e.getMessage());
                        }
                    }else{
                        try {
                            mImageWriteBuffer.write(image_bytes);
                            Log.d(TAG, "We receive "+ith_int+" part image.");
                        }catch (IOException e){
                            Log.d(TAG, e.getMessage());
                        }
                        byte[] total_bytes = mImageWriteBuffer.toByteArray();
                        // show in the image
                        Bitmap bmp = BitmapFactory.decodeByteArray(total_bytes,0, total_bytes.length);
                        mImageView.setImageBitmap(bmp);
                    }

                }else if(type_int == TYPE_VDIEO){
                    // add process to deal with VIDEO
                }
            }
            else if(msg.what == Constants.MESSAGE_READ){
                byte[] recBuf =(byte[]) msg.obj;
                int recLen = msg.arg1;
                byte[] receive_bytes = new byte[recLen];
                for(int i=0; i<recLen; i++){
                    receive_bytes[i] = recBuf[i];
                }
                try {
                    mImageBuffer.write(receive_bytes);
                }catch (IOException e){
                    Log.d(TAG, e.getMessage());
                }
                boolean isEnd = false;
                if(last_received == null){
                    byte[] temp_total = receive_bytes;
                    for(int i=0; i<temp_total.length - head_length; i++){
                            if(i+head_length+12 < temp_total.length){
                                byte[] temp = subbytes(temp_total, i, head_length);
                                int temp_ith = byteArrayToInt(subbytes(temp_total, i+head_length+8, 4));
                                if(isHead(temp)){
                                    int temp_isContinue = byteArrayToInt(subbytes(temp_total, i+head_length+4, 4));
                                    if(temp_isContinue == END){
                                        isEnd = true;
                                    }
                                }
                        }
                    }
                    last_received = receive_bytes;
                }else{
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        baos.write(last_received);
                        baos.write(receive_bytes);
                        byte[] temp_total = baos.toByteArray();
                        // check if should stop
                        for(int i=0; i<temp_total.length-head_length; i++){
                            byte[] temp = subbytes(temp_total, i, head_length);
                            if(isHead(temp)){
                                int temp_isContinue = byteArrayToInt(subbytes(temp_total, i+head_length+4, 4));
                                int temp_ith = byteArrayToInt(subbytes(temp_total, i+head_length+8, 4));
                                if(temp_isContinue == END){
                                    isEnd = true;
                                    last_received = null;
                                    break;
                                }
                            }
                        }
                    }catch (IOException e){ Log.d(TAG, e.getMessage());}
                    last_received = receive_bytes;
                }
                if(isEnd){
                    // process all the data
                    byte[] total_bytes = mImageBuffer.toByteArray();
                    if(total_bytes.length != 0){
                        int first_head_index;
                        for(first_head_index=0;first_head_index < total_bytes.length;first_head_index ++){
                            byte[] temp = subbytes(total_bytes, first_head_index, head_length);
                            if(isHead(temp)){
                                break;
                            }
                        }
                        ByteArrayOutputStream pure_boas = new ByteArrayOutputStream();
                        int data_isEnd = CONTINUE;
                        int data_type = byteArrayToInt(subbytes(total_bytes, first_head_index+head_length, 4));
                        int fix_length = byteArrayToInt(subbytes(total_bytes, first_head_index+head_length+12, 4));
                        int package_length = fix_length + head_length + 4*4;
                        int index = 0;
                        while(data_isEnd != END){
                            int data_length = byteArrayToInt(subbytes(total_bytes, first_head_index+package_length * index + head_length+12, 4));
                            data_isEnd = byteArrayToInt(subbytes(total_bytes, first_head_index + package_length * index + head_length+4, 4));
                            byte[] data_clip = subbytes(total_bytes, first_head_index + package_length * index + Default_Head_Length, data_length);
                            try {
                                pure_boas.write(data_clip);
                            }catch (IOException e){
                                Log.d(TAG, e.getMessage());
                            }
                            index += 1;
                        }
                        byte[] pure_total_data = pure_boas.toByteArray();
                        if(data_type == TYPE_TEXT){
                            String str = new String(pure_total_data);
                            mConversationArrayAdapter.add(mConnectedDeviceName + ":" + str);
                        }else if(data_type == TYPE_IMAGE){
                            Bitmap bmp = BitmapFactory.decodeByteArray(pure_total_data, 0, pure_total_data.length);
                            mImageView.setImageBitmap(bmp);
                        }
                    }
                }

            } else if(msg.what == Constants.MESSAGE_DEVICE_NAME){
                mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                if (null != activity) {
                    Toast.makeText(activity, "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                }
            }else if(msg.what == Constants.MESSAGE_TOAST){
                if (null != activity) {
                    Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
    private byte[] last_received = null;
    private int receive_times = 0;
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
                break;
            case REQUEST_IMAGE:
                if(resultCode == Activity.RESULT_OK){
                    Uri uri = data.getData();
                    String path = getRealPathFromURI(uri, getContext());
                    imagepath_to_send = path;
                    Log.d(TAG, "The path is: "+path);

                }
                break;
        }
    }
    private String imagepath_to_send;
    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
        }
        return false;
    }

}
