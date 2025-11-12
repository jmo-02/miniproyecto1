package com.example.miniproyecto1.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.miniproyecto1.model.Inventory
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    // 1) Observador reactivo para la UI
    @Query("SELECT * FROM inventory")
    fun getAllItemsLive(): LiveData<List<Inventory>>

    // 2) Alternativa con Flow
    @Query("SELECT * FROM inventory")
    fun getAllItemsFlow(): Flow<List<Inventory>>

    // 3) Método suspend (para repositorio/VM si lo usas)
    @Query("SELECT * FROM inventory")
    suspend fun getAllItemsList(): List<Inventory>

    // 4) Método síncrono para el widget (NO suspend)
    // Requiere allowMainThreadQueries() en InventoryDB (ya lo tienes)
    @Query("SELECT * FROM inventory")
    fun getAllItemsListSync(): List<Inventory>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertItem(item: Inventory)

    @Update
    suspend fun updateItem(item: Inventory)

    @Delete
    suspend fun deleteItem(item: Inventory)

    @Query("SELECT * FROM inventory WHERE id = :id")
    suspend fun getItemById(id: Int): Inventory?
}