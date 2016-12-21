package com.durrutia.twinpic;

import android.content.Context;

import com.durrutia.twinpic.domain.Pic;
import com.durrutia.twinpic.domain.Pic_Table;
import com.durrutia.twinpic.domain.Twin;
import com.durrutia.twinpic.util.DeviceUtils;
import com.google.common.base.Stopwatch;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Test principal del backend.
 *
 * @author Diego P. Urrutia Astorga
 * @version 20161102
 */
@Slf4j
public final class Test {

    /**
     * Testing the bd.
     *
     * @param context
     */
    public static void testDatabase(final Context context) {

        // Remove database
        context.deleteDatabase(Database.NAME + ".db");

        // Cronometro
        final Stopwatch stopwatch = Stopwatch.createStarted();

        log.debug("Testing database ..");

        // Inicializacion
        {
            FlowManager.init(new FlowConfig.Builder(context)
                    .openDatabasesOnInit(true)
                    .build());

            log.debug("DB initialized in: {}.", stopwatch);
        }
        stopwatch.reset().start();

        // Insert into db
        {
            // Ciclo para insertar 100 objetos en la bd
            for (int i = 1; i <= 100; i++) {

                stopwatch.reset().start();

                final Pic pic = Pic.builder()
                        .deviceId(DeviceUtils.getDeviceId(context))
                        .latitude(RandomUtils.nextDouble())
                        .longitude(RandomUtils.nextDouble())
                        .date("00")
                        .url("http://" + RandomStringUtils.randomAlphabetic(20))
                        .positive(RandomUtils.nextInt(0, 100))
                        .negative(RandomUtils.nextInt(0, 100))
                        .warning(RandomUtils.nextInt(0, 2))
                        .build();

                // Commit
                pic.save();

                log.debug("Saved {} in {}.", i, stopwatch);
            }

        }
        stopwatch.reset().start();

        // Select from database
        {
            List<Pic> pics = SQLite.select().from(Pic.class).queryList();
            log.debug("Result: {} in {}.", pics.size(), stopwatch);

            stopwatch.reset().start();

            for (final Pic p : pics) {
                log.debug("{}", p);
            }
        }
        stopwatch.reset().start();

        // Relations
        {
            for (long i = 1; i <= 6; i = i + 2) {
                final Pic local = SQLite.select().from(Pic.class).where(Pic_Table.id.is(i)).querySingle();
                final Pic remote = SQLite.select().from(Pic.class).where(Pic_Table.id.is(i + 1)).querySingle();

                final Twin twin = Twin.builder().local(local).remota(remote).build();
                log.debug("Twin: {}", twin);

                twin.save();
            }

            log.debug("Relation in {}.", stopwatch);
        }
        stopwatch.reset().start();

        // Get from relation
        {
            final List<Twin> twins = SQLite.select().from(Twin.class).queryList();
            log.debug("Twin size: {} in {}.", twins.size(), stopwatch);

            for (final Twin t : twins) {
                log.debug("Twin: {}.", t);
            }
        }
        stopwatch.reset().start();

        // Destroy the world.
        {
            FlowManager.destroy();

            log.debug("Finished in {}.", stopwatch);
        }

    }
}
