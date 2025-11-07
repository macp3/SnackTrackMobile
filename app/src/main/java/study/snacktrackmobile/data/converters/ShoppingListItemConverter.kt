package study.snacktrackmobile.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import study.snacktrackmobile.data.model.ShoppingListItem

class ShoppingListItemConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromItems(items: List<ShoppingListItem>): String {
        return gson.toJson(items)
    }

    @TypeConverter
    fun toItems(data: String): List<ShoppingListItem> {
        val listType = object : TypeToken<List<ShoppingListItem>>() {}.type
        return gson.fromJson(data, listType)
    }
}
