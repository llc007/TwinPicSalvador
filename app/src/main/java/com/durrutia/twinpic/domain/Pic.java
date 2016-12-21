package com.durrutia.twinpic.domain;

import com.durrutia.twinpic.Database;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Pic
 *
 * @author Luis Lopez
 * @version 20162112
 */
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        database = Database.class,
        cachingEnabled = true,
        orderedCursorLookUp = true, // https://github.com/Raizlabs/DBFlow/blob/develop/usage2/Retrieval.md#faster-retrieval
        cacheSize = Database.CACHE_SIZE
)
public class Pic extends BaseModel {

    /**
     * Identificador unico
     */
    @Getter     @Setter
    @Column
    @PrimaryKey(autoincrement = true)
    Long id;

    /**
     * Identificador del dispositivo
     */
    @Getter     @Setter
    @Column
    String deviceId;

    /**
     * Fecha de la foto
     */
    @Column
    @Getter     @Setter
    String date;

    /**
     * URL de la foto
     */
    @Column
    @Getter     @Setter
    String url;

    /**
     * Latitud
     */
    @Column
    @Getter     @Setter
    Double latitude;

    /**
     * Longitud
     */
    @Column
    @Getter     @Setter
    Double longitude;

    /**
     * Numero de likes
     */
    @Column
    @Getter     @Setter
    Integer positive;

    /**
     * Numero de dis-likes
     */
    @Column
    @Getter     @Setter
    Integer negative;

    /**
     * Numero de warnings
     */
    @Column
    @Getter     @Setter
    Integer warning;

    /**
     * Foto codificada
     */
    @Column
    @Getter     @Setter
    String foto;

}
