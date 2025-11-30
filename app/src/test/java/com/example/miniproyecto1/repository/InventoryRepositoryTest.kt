package com.example.miniproyecto1.repository

import com.example.miniproyecto1.model.Inventory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class InventoryRepositoryTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var collection: CollectionReference
    private lateinit var repo: InventoryRepository

    @Before
    fun setup() {
        firestore = Mockito.mock(FirebaseFirestore::class.java)
        collection = Mockito.mock(CollectionReference::class.java)
        Mockito.`when`(firestore.collection("inventory")).thenReturn(collection)
        repo = InventoryRepository(firestore)
    }

    @Test
    fun `saveInventory returns false when firestore throws on document`() = runBlocking {
        val inv = Inventory(id = 1, code = 100, name = "X", price = 10, quantity = 5)

        Mockito.`when`(collection.document(inv.code.toString())).thenThrow(RuntimeException("boom"))

        val result = repo.saveInventory(inv)
        assertFalse(result)
    }

    @Test
    fun `getById returns null on exception`() = runBlocking {
        Mockito.`when`(collection.document("200")).thenThrow(RuntimeException("Boom"))
        val result = repo.getById(200)
        assertNull(result)
    }
}
