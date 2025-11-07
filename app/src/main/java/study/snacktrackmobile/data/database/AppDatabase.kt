package study.snacktrackmobile.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import study.snacktrackmobile.data.converters.ShoppingListItemConverter
import study.snacktrackmobile.data.dao.ShoppingListDao
import study.snacktrackmobile.data.model.ShoppingList

@Database(entities = [ShoppingList::class], version = 1, exportSchema = false)
@TypeConverters(ShoppingListItemConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun shoppingListDao(): ShoppingListDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "snacktrack_db"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}
