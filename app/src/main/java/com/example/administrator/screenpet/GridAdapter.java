package com.example.administrator.screenpet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by Lucky on 2018/3/16.
 */

public class GridAdapter extends BaseAdapter {
    private ArrayList<String> listUrls;
    private LayoutInflater inflater;
    private GifDrawable gifFromFile;
    public GridAdapter(ArrayList<String> listUrls,Context context) {
        this.listUrls = listUrls;
        if(listUrls.size() == 7){
            listUrls.remove(listUrls.size()-1);
        }
        inflater = LayoutInflater.from(context);
    }

    public int getCount(){
        return  listUrls.size();
    }
    @Override
    public String getItem(int position) {
        return listUrls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_gif, parent,false);
            holder.image = (ImageView) convertView.findViewById(R.id.imageView1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();

        }

        final String path=listUrls.get(position);
        if (path.equals("adding")){
            holder.image.setImageResource(R.drawable.find_add_img);
        }else {
            //holder.image.setVisibility(View.GONE);
            holder.image=(GifImageView)convertView.findViewById(R.id.gifImageView);
            convertView.setTag(holder);
            File file=new File(path);
            try{
                gifFromFile = new GifDrawable(file);
            }catch (Exception e){
                e.printStackTrace();
            }
            holder.image.setImageDrawable(gifFromFile);

        }
        return convertView;
    }
    class ViewHolder {
        ImageView image;
    }
}
