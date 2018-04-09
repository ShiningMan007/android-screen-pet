package com.example.administrator.screenpet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Lucky on 2018/3/30.
 */

public class MusicAdapter extends ArrayAdapter<String> {
    private int resourceId;
    public MusicAdapter(Context context, int textViewResourceId, ArrayList<String>object){
        super(context,textViewResourceId,object);
        resourceId=textViewResourceId;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        String objects=getItem(position);
        View view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        TextView musicPath=(TextView)view.findViewById(R.id.music_list);
        musicPath.setText(objects);
        return view;
    }
}
