package study.snacktrackmobile.data.dao
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import study.snacktrackmobile.data.model.ShoppingList

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_lists")
    suspend fun getAll(): List<ShoppingList>

    @Query("SELECT * FROM shopping_lists WHERE date = :date AND userEmail = :email ORDER BY id DESC")
    fun getByDateAndUser(date: String, email: String): Flow<List<ShoppingList>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: ShoppingList): Long

    @Update
    suspend fun update(list: ShoppingList)

    @Delete
    suspend fun delete(list: ShoppingList)

    @Query("SELECT * FROM shopping_lists WHERE date = :date AND userEmail = :email")
    suspend fun getByDateAndUserOnce(date: String, email: String): List<ShoppingList>

    @Query("SELECT * FROM shopping_lists WHERE userEmail = :email ORDER BY date DESC LIMIT 20")
    suspend fun getLastLists(email: String): List<ShoppingList>
}
