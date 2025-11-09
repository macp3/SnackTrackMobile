package study.snacktrackmobile.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable
import study.snacktrackmobile.data.converters.ShoppingListItemConverter

@Entity(tableName = "shopping_lists")
data class ShoppingList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @TypeConverters(ShoppingListItemConverter::class)
    val items: List<ShoppingListItem> = emptyList(),
    val date: String,
    val userEmail: String
)

@Serializable
data class ShoppingListItem(
    val name: String,
    val quantity: String,
    val description: String = "",
    val bought: Boolean = false
)
