package com.durrutia.twinpic.domain;

import lombok.extern.slf4j.Slf4j;

/**
 * Clase que relaciona 2 {@link Pic}, usada para enviar estas Pic al adaptador.
 * Created by LuisLopez on 20/12/2016.
 * @version 20162112
 */
@Slf4j
public class FotosPareadas {
    /**
     * Pic local
     */
    public Pic local;

    /**
     * Pic remota
     */
    public Pic remota;

    /**
     * Constructor de la clase
     * @param local
     * @param remota
     */
    public FotosPareadas(Pic local,Pic remota){
        this.local=local;
        this.remota=remota;
    }
}
