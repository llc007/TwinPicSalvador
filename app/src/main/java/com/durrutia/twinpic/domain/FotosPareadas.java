package com.durrutia.twinpic.domain;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by LuisLopez on 20/12/2016.
 */
@Slf4j
public class FotosPareadas {
    public Pic local;
    public Pic remota;

    public FotosPareadas(Pic local,Pic remota){
        this.local=local;
        this.remota=remota;
    }
}
