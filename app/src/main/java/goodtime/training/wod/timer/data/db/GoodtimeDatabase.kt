package goodtime.training.wod.timer.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.widget.Toast
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import goodtime.training.wod.timer.GoodtimeApplication
import goodtime.training.wod.timer.data.model.*
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

@Database(
    entities = [Session::class, SessionSkeleton::class, CustomWorkoutSkeleton::class, WeeklyGoal::class],
    version = 1, exportSchema = false
)
@TypeConverters(TypeConverter::class)
abstract class GoodtimeDatabase : RoomDatabase() {
    abstract fun sessionsDao(): SessionDao
    abstract fun sessionSkeletonDao(): SessionSkeletonDao
    abstract fun customWorkoutSkeletonDao(): CustomWorkoutSkeletonDao
    abstract fun weeklyGoalDao(): WeeklyGoalDao


    companion object {
        private const val TAG = "GoodtimeDatabase"

        private val LOCK = Any()
        private var INSTANCE: GoodtimeDatabase? = null

        private val dbToInstanceId = ConcurrentHashMap<Int, String>()
        private val threadToInstanceId = ConcurrentHashMap<Long, String>()

        const val DATABASE_NAME = "goodtime-training-db"

        fun getDatabase(context: Context): GoodtimeDatabase {
            if (INSTANCE == null || !INSTANCE!!.isOpen) {
                synchronized(LOCK) {
                    if (INSTANCE == null || !INSTANCE!!.isOpen) {
                        INSTANCE = recreateInstance(context)
                    }
                }
            }
            return INSTANCE!!
        }

        fun closeInstance() {
            if (INSTANCE!!.isOpen) {
                INSTANCE!!.openHelper.close()
            }
        }

        fun recreateInstance(context: Context): GoodtimeDatabase {
            // keep track of which thread belongs to which local database
            val instanceId = UUID.randomUUID().toString()

            // custom thread with an exception handler strategy
            val executor = Executors.newCachedThreadPool { runnable: Runnable? ->
                val defaultThreadFactory =
                    Executors.defaultThreadFactory()
                val thread = defaultThreadFactory.newThread(runnable)
                thread.uncaughtExceptionHandler = resetDatabaseOnUnhandledException
                threadToInstanceId[thread.id] = instanceId
                thread
            } as ThreadPoolExecutor

            val db = Room.databaseBuilder(context, GoodtimeDatabase::class.java, DATABASE_NAME)
                .setJournalMode(JournalMode.TRUNCATE)
                .fallbackToDestructiveMigration()
                .setQueryExecutor(executor)
                .build()
            dbToInstanceId[db.hashCode()] = instanceId
            return db
        }

        private var resetDatabaseOnUnhandledException =
            Thread.UncaughtExceptionHandler { thread, throwable ->
                val message = "uncaught exception in a LocalDatabase thread, resetting the database"
                Log.e(TAG, message, throwable)
                Toast.makeText(GoodtimeApplication.context, message, Toast.LENGTH_SHORT).show()
                synchronized(LOCK) {
                    // there is no active local database to clean up
                    if (INSTANCE == null) return@UncaughtExceptionHandler
                    val instanceIdOfThread: String? = threadToInstanceId[thread.id]
                    val instanceIdOfActiveLocalDb: String? = dbToInstanceId[INSTANCE.hashCode()]
                    if (instanceIdOfThread == null || instanceIdOfThread != instanceIdOfActiveLocalDb) {
                        // the active local database instance is not the one
                        // that caused this thread to fail, so leave it as is
                        return@UncaughtExceptionHandler
                    }
                    INSTANCE!!.tryResetDatabase()
                }
            }
    }

    private fun tryResetDatabase() {
        try {
            // try closing existing connections
            try {
                if (this.openHelper.writableDatabase.isOpen) {
                    this.openHelper.writableDatabase.close()
                }
                if (this.openHelper.readableDatabase.isOpen) {
                    this.openHelper.readableDatabase.close()
                }
                if (this.isOpen) {
                    this.close()
                }
                if (this == INSTANCE) INSTANCE = null
            } catch (ex: Exception) {
                Log.e(TAG, "Could not close LocalDatabase", ex)
            }

            // try deleting database file
            val f: File = GoodtimeApplication.context.getDatabasePath(DATABASE_NAME)
            if (f.exists()) {
                val deleteSucceeded = SQLiteDatabase.deleteDatabase(f)
                if (!deleteSucceeded) {
                    Log.e(TAG, "Could not delete LocalDatabase")
                }
            }

            val tmp: GoodtimeDatabase = recreateInstance(GoodtimeApplication.context)
            tmp.query("SELECT * from Session", null)
            tmp.close()

            this.openHelper.readableDatabase
            this.openHelper.writableDatabase
            this.query("SELECT * from Session", null)
        } catch (ex: Exception) {
            Log.e(TAG, "Could not reset LocalDatabase", ex)
        }
    }
}
