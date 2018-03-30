package com.example.administrator.screenpet;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class CustomizeActivity extends AppCompatActivity {
    private static final int REQUEST_CONTENT_CODE = 1;
    private static final int REQUEST_PREVIEW_CODE = 2;
    private ArrayList<String> gifPaths = new ArrayList<>();
    ArrayList name;
    private ArrayList<String>ListExtra=new ArrayList<>();
    private GridView gridView;
    private GridAdapter gridAdapter;
    private TextView tv_click;
    private Button submit;
    private EditText textView;
    private GifImageView gifImageView;
    private GifDrawable gifFromFile;
    private String TAG =CustomizeActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize);
        gridView=(GridView)findViewById(R.id.gridView);
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
                    Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, REQUEST_CONTENT_CODE);
                }

                    else{
                    Toast.makeText(CustomizeActivity.this,""+position,Toast.LENGTH_SHORT).show();
                    //gifPaths.remove(position);
                    loadAdapter("",position);
                }
            }
        });
        gifPaths.add("adding");
        gridAdapter=new GridAdapter(gifPaths,CustomizeActivity.this);
        gridView.setAdapter(gridAdapter);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
    @Override
    protected void onResume(){
        super.onResume();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode==RESULT_OK){
            switch (requestCode){
                case REQUEST_CONTENT_CODE:
                    Uri uri = data.getData();
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
                    //Toast.makeText(this, "文件路径："+fileName, Toast.LENGTH_SHORT).show();
                    if(fileName.endsWith(".gif")){
                        Toast.makeText(this, "文件路径："+fileName, Toast.LENGTH_SHORT).show();

                        File file=new File(fileName);
                        gifImageView=(GifImageView)findViewById(R.id.gifImageView);


                        /*try{
                            gifFromFile = new GifDrawable(file);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        gifImageView.setImageDrawable(gifFromFile);*/
                    }
                    else{
                        Toast.makeText(this,"Error!",Toast.LENGTH_SHORT).show();
                    }
                    ListExtra.add(fileName);
                    loadAdapter(fileName,100);
                    break;
                case REQUEST_PREVIEW_CODE:
                    //ListExtra = null;

                    //loadAdapter("",100);
                    break;
            }
        }
    }
    private void loadAdapter(String paths,int options){
        if (gifPaths.contains("adding")){
            gifPaths.remove("adding");
        }
        /*if (paths.contains("adding")){
            paths.remove("adding");
        }*/
        //paths.add("adding");
        if(options==100)gifPaths.add(paths);
        else gifPaths.remove(options);
        gifPaths.add("adding");
        gridAdapter  = new GridAdapter(gifPaths,CustomizeActivity.this);
        gridView.setAdapter(gridAdapter);
        try{
            JSONArray obj = new JSONArray(gifPaths);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
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

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context
     *            The context.
     * @param uri
     *            The Uri to query.
     * @param selection
     *            (Optional) Filter used in the query.
     * @param selectionArgs
     *            (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
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

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


}
