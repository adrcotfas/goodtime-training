package com.adrcotfas.wod

import android.app.Application
import androidx.room.Room
import com.adrcotfas.wod.data.db.Database
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton

class GoodtimeApplication : Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        bind<Database>() with eagerSingleton {
            Room.databaseBuilder(this@GoodtimeApplication, Database::class.java,
                "goodtime-training-db")
                .build() }
    }
}
