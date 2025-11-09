package study.snacktrackmobile.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import study.snacktrackmobile.data.converters.ShoppingListItemConverter
import study.snacktrackmobile.data.dao.ShoppingListDao
import study.snacktrackmobile.data.model.ShoppingList

@Database(entities = [ShoppingList::class], version = 4, exportSchema = false)
@TypeConverters(ShoppingListItemConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun shoppingListDao(): ShoppingListDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // MIGRACJA z wersji 1 do 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE shopping_lists ADD COLUMN date TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "snacktrack_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
