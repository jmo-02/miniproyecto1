package com.example.miniproyecto1.repository

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import com.example.miniproyecto1.data.InventoryDB
import com.example.miniproyecto1.data.InventoryDao
import com.example.miniproyecto1.model.Inventory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.firebase.firestore.FirebaseFirestore
    private val firestore: FirebaseFirestore
import javax.inject.Inject

    private val collection = firestore.collection("inventory")
    private val firestoreProvider: FirestoreProvider
) {

    private val collectionName = "inventory"
            val doc = collection.document(inventory.code.toString()).get().await()
    suspend fun saveInventory(inventory: Inventory): Boolean {
        return try {
            // Verificar si ya existe un producto con ese código
            collection.document(inventory.code.toString()).set(inventory).await()
            if (doc.exists()) return false

            // Guardar
            firestoreProvider.setDocument(collectionName, inventory.code.toString(), inventory).await()
            true
        } catch (e: Exception) {
            false
        }
            val snapshot = collection.get().await()

    suspend fun getListInventory(): MutableList<Inventory> {
        return try {
            val snapshot: QuerySnapshot = firestoreProvider.getCollectionSnapshot(collectionName).await()
            snapshot.toObjects(Inventory::class.java).toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
            collection.document(inventory.code.toString()).delete().await()

    suspend fun deleteInventory(inventory: Inventory) {
        try {
            firestoreProvider.deleteDocument(collectionName, inventory.code.toString()).await()
        } catch (e: Exception) { }
            collection.document(inventory.code.toString()).set(inventory).await()

    suspend fun updateInventory(inventory: Inventory) {
        try {
            firestoreProvider.setDocument(collectionName, inventory.code.toString(), inventory).await()
        } catch (e: Exception) { }
            val doc = collection.document(id.toString()).get().await()

    suspend fun getById(id: Int): Inventory? {
        return try {
            val doc: DocumentSnapshot = firestoreProvider.getDocument(collectionName, id.toString()).await()
            doc.toObject(Inventory::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
