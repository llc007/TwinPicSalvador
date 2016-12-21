package com.durrutia.twinpic.logic;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;

import com.durrutia.twinpic.R;
import com.durrutia.twinpic.domain.FotosPareadas;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;

/**
 * @author  LuisLopez on 20/12/2016.
 * @version 20162112
 * Metodo que implementa un adaptador el cual usa picasso para poner las imagenes en el ImageButton
 */
@Slf4j
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

    /**
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if(convertView == null){
            LayoutInflater inf = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inf.inflate(R.layout.activity_separador,null);
        }
        //detecto el par de fotos
        final FotosPareadas pic = pictures.get(position);
        final ImageButton imgLeft = (ImageButton) v.findViewById(R.id.imgLocal);
        final ImageButton imgRight = (ImageButton) v.findViewById(R.id.imgRemota);

        //Direccion del servidor
        String server ="http://172.16.35.2:8080/";
        //log.debug("DENTRO DEL ADAPTADOR",server + pic.local.getUrl());

        Picasso.with(this.activity)
                .load(server + pic.local.getUrl())
                .resize(600,600)
                .centerCrop()
                .noPlaceholder()
                .into(imgLeft);

        //log.debug("DENTRO DEL ADP REMOTA",server + pic.remota.getUrl());
        Picasso.with(this.activity)
                .load(server + pic.remota.getUrl())
                .resize(600,600)
                .centerCrop()
                .noPlaceholder()
                .into(imgRight);

        return v;

    }
}
