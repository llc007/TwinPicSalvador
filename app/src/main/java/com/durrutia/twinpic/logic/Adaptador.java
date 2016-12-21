package com.durrutia.twinpic.logic;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;

import com.durrutia.twinpic.R;
import com.durrutia.twinpic.domain.FotosPareadas;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by LuisLopez on 20/12/2016.
 * Metodo que implementa un adaptador el cual usa picasso para poner las imagenes en el ImageButton
 */

public class Adaptador extends BaseAdapter {
    private Activity activity;
    private ArrayList<FotosPareadas> pictures;

    public Adaptador(Activity activity, ArrayList<FotosPareadas> pictures){
        this.activity = activity;
        this.pictures = pictures;
    }

    @Override
    public int getCount() {
        return pictures.size();
    }

    @Override
    public Object getItem(int position) {
        return pictures.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if(convertView == null){
            LayoutInflater inf = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inf.inflate(R.layout.activity_separador,null);
        }
        final FotosPareadas pic = pictures.get(position);

        final ImageButton imgLeft = (ImageButton) v.findViewById(R.id.imageButton1);
        final ImageButton imgRight = (ImageButton) v.findViewById(R.id.imageButton2);

        String server ="http://192.168.1.103:8080/";
        Log.d("DENTRO DEL ADAPTADOR",server + pic.local.getUrl());
        Picasso.with(this.activity)
                .load(server + pic.local.getUrl())
                .resize(600,600)
                .centerCrop()
                .noPlaceholder()
                .into(imgLeft);

        Log.d("DENTRO DEL ADP REMOTA",server + pic.remota.getUrl());
        Picasso.with(this.activity)
                .load(server + pic.remota.getUrl())
                .resize(600,600)
                .centerCrop()
                .noPlaceholder()
                .into(imgRight);

        return v;

    }
}
