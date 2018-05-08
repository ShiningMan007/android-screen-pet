package com.example.administrator.screenpet;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class CustomizeActivity extends AppCompatActivity {
    private static final int REQUEST_GIF_CODE_HAPPY = 1;
    private static final int REQUEST_GIF_CODE_SAD = 2;
    private static final int REQUEST_MUS_CODE = 3;
    private ArrayList<String> gifPaths = new ArrayList<>();
    private ArrayList<String> gifPaths_sad = new ArrayList<>();
    private ArrayList<String> musPaths = new ArrayList<>();
    ArrayList name;
    private ArrayList<String>ListExtra_happy=new ArrayList<>();
    private ArrayList<String>ListExtra_sad=new ArrayList<>();
    private MyGridView gridView;
    private MyListView musiclistview;
    private GridAdapter gridAdapter;
    private MusicAdapter musicadapter;
    private GridAdapter gridAdapter_sad;
    private Button submit;
    private EditText textView;
    private GifImageView gifImageView;
    private GifDrawable gifFromFile;
    private String TAG =CustomizeActivity.class.getSimpleName();
    private GridView gridView_sad;
    public static final String filedir_happy="happydirs.data";
    public static final String filedir_sad="saddirs.data";
    public static final String musicdir = "music.data";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize);
        gridView=(MyGridView)findViewById(R.id.gridView);
        submit=(Button)findViewById(R.id.submit);
        int cols = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().densityDpi;
        cols = cols < 3 ? 3 : cols;
        gridView.setNumColumns(cols);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){

                String imgs = (String) parent.getItemAtPosition(position);
                if ("adding".equals(imgs) ){
                    //Toast.makeText(CustomizeActivity.this,"adding",Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("*/*");
                    startActivityForResult(intent, REQUEST_GIF_CODE_HAPPY);
                }
                else{
                    Toast.makeText(CustomizeActivity.this,""+position,Toast.LENGTH_SHORT).show();
                    //gifPaths.remove(position);
                    showNormalDialog(position, REQUEST_GIF_CODE_HAPPY);
                }
            }
        });
        gridView_sad = (GridView)findViewById(R.id.gridView_sad);
        gridView_sad.setNumColumns(cols);
        gridView_sad.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String imgs = (String) parent.getItemAtPosition(position);
                if ("adding".equals(imgs) ){
                    //Toast.makeText(CustomizeActivity.this,"adding",Toast.LENGTH_SHORT).show();

                    Intent intent=new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("*/*");

                    startActivityForResult(intent, REQUEST_GIF_CODE_SAD);
                }

                else{
                    Toast.makeText(CustomizeActivity.this,""+position,Toast.LENGTH_SHORT).show();
                    //gifPaths.remove(position);
                    showNormalDialog(position, REQUEST_GIF_CODE_SAD);
                }
            }
        });

        gifPaths = readfile(filedir_happy);
        gifPaths.add("adding");
        gridAdapter=new GridAdapter(gifPaths,CustomizeActivity.this);
        gifPaths_sad = readfile(filedir_sad);
        gifPaths_sad.add("adding");
        gridAdapter_sad = new GridAdapter(gifPaths_sad, CustomizeActivity.this);

        gridView.setAdapter(gridAdapter);
        gridView_sad.setAdapter(gridAdapter_sad);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("*/*");
                startActivityForResult(intent, REQUEST_MUS_CODE);

            }
        });
        musiclistview = (MyListView)findViewById(R.id.list_view);
        musPaths = readfile(musicdir);
        musicadapter=new MusicAdapter(CustomizeActivity.this, R.layout.item_music, musPaths);
        musiclistview.setAdapter(musicadapter);
    }
    @Override
    protected void onResume(){
        super.onResume();
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode==RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GIF_CODE_HAPPY:
                    Uri uri = data.getData();
                    String fileName= getRealPathFromURI(uri, getBaseContext());

                    if(fileName.endsWith(".gif")){
                        Toast.makeText(this, "文件路径："+fileName, Toast.LENGTH_SHORT).show();

                        File file=new File(fileName);
                        gifImageView=(GifImageView)findViewById(R.id.gifImageView);
                    }
                    else{
                        Toast.makeText(this,"Error!",Toast.LENGTH_SHORT).show();
                    }
                    ListExtra_happy.add(fileName);
                    loadAdapter(fileName,100, requestCode);
                    break;
                case REQUEST_GIF_CODE_SAD:
                    Uri uri2 = data.getData();
                    String fileName2= getRealPathFromURI(uri2, getBaseContext());

                    if(fileName2.endsWith(".gif")){
                        Toast.makeText(this, "文件路径："+fileName2, Toast.LENGTH_SHORT).show();

                        File file=new File(fileName2);
                        gifImageView=(GifImageView)findViewById(R.id.gifImageView);
                    }
                    else{
                        Toast.makeText(this,"Error!",Toast.LENGTH_SHORT).show();
                    }
                    ListExtra_sad.add(fileName2);
                    loadAdapter(fileName2,100, requestCode);
                    break;
                case REQUEST_MUS_CODE:
                    Uri uri1 = data.getData();
                    String fileName1=getRealPathFromURI(uri1, getBaseContext());
                    if(fileName1.endsWith(".mp3") || fileName1.endsWith(".wav")) {

                        musPaths.add(fileName1);
                        writefile(musicdir, musPaths.toString(), MODE_PRIVATE);
                        musicadapter.notifyDataSetChanged();
                    }
                    else{
                        Toast.makeText(this,"Error!",Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    private void showNormalDialog(final int position, final int code){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(CustomizeActivity.this);
        normalDialog.setIcon(R.drawable.a80_1);
        normalDialog.setTitle("Do you want to delete it?");
        normalDialog.setMessage("If you choose yes, this gif would be removed from this setting(not permenant removed).");
        normalDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadAdapter("", position, code);
            }
        });
        normalDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        normalDialog.show();
    }

    private boolean writefile(String writedir, String content ,int mode){
        try {
            FileOutputStream fos = openFileOutput(writedir, mode);
            fos.write(content.getBytes());
            fos.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
            return false;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    ArrayList<String> str2list(String str){
        String pattern = ",[\\s]*|\\[|\\]";
        String[] splits = str.split(pattern);
        ArrayList<String> res = new ArrayList<>();
        for(int i =1; i<splits.length; i++){
            res.add(splits[i]);
        }
        return res;
    }
    private ArrayList<String> readfile(String readdir){
        ArrayList<String> res = new ArrayList<>();
        byte[] bytes = {};
        try{
            FileInputStream fis = openFileInput(readdir);
            bytes = new byte[fis.available()];
            fis.read(bytes);
            String pathstr = new String(bytes);
            res = str2list(pathstr);
        }catch (FileNotFoundException e){
            e.printStackTrace();
            return res;
        }catch (IOException e){
            e.printStackTrace();
            return res;
        }
        return res;
    }
    private void loadAdapter(String paths,int options, int code){
        if(code == REQUEST_GIF_CODE_HAPPY) {
            if (gifPaths.contains("adding")){
                gifPaths.remove("adding");
            }
            /*if (paths.contains("adding")){
                paths.remove("adding");
            }*/
            //paths.add("adding");
            if(options==100)gifPaths.add(paths);
            else gifPaths.remove(options);
            writefile(filedir_happy, gifPaths.toString(), Context.MODE_PRIVATE);
            gifPaths.add("adding");
            gridAdapter  = new GridAdapter(gifPaths,CustomizeActivity.this);
            gridView.setAdapter(gridAdapter);
            try{
                JSONArray obj = new JSONArray(gifPaths);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        else if(code == REQUEST_GIF_CODE_SAD){
            if (gifPaths_sad.contains("adding")){
                gifPaths_sad.remove("adding");
            }
            /*if (paths.contains("adding")){
                paths.remove("adding");
            }*/
            //paths.add("adding");
            if(options==100)gifPaths_sad.add(paths);
            else gifPaths_sad.remove(options);
            writefile(filedir_sad, gifPaths_sad.toString(), Context.MODE_PRIVATE);
            gifPaths_sad.add("adding");
            gridAdapter_sad  = new GridAdapter(gifPaths_sad,CustomizeActivity.this);
            gridView_sad.setAdapter(gridAdapter_sad);
            try{
                JSONArray obj = new JSONArray(gifPaths_sad);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    private String checkFileName(Uri uri,Intent data){
        String fileName="";
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
            if (data.getScheme().toString().compareTo("content") == 0) {
                cursor = getContentResolver().query(uri,
                        new String[] {MediaStore.Audio.Media.DATA}, null, null, null);
                if (cursor.moveToFirst()) {
                    fileName = cursor.getString(0);
                }
            }else if (data.getScheme().toString().compareTo("file") == 0)         //file:///开头的uri
            {
                //fileName = data.toString();
                fileName = data.toString().replace("file://", "");
                //替换file://
                if(!fileName.startsWith("/mnt")){
                    //加上"/mnt"头
                    fileName += "/mnt";
                }
            }
        }
        else{
            fileName=getPath(this,uri);
        }
        return fileName;
    }
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }

                    // TODO handle non-primary volumes
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {

                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[] { split[1] };

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }

        return null;
    }
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


}
