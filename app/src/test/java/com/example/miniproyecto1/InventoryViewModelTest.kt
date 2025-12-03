package com.example.miniproyecto1

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.miniproyecto1.model.Inventory
import com.example.miniproyecto1.repository.InventoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import com.example.miniproyecto1.viewmodel.InventoryViewModel


@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()


    private lateinit var viewModel: InventoryViewModel
    private val repository = mock(InventoryRepository::class.java)

    @Before
    fun setup() {
        viewModel = InventoryViewModel(repository)
    }

    // ---------- GET LIST TEST ----------

    @Test
    fun `getListInventory loads inventory list`() = runTest {
        val fakeList = mutableListOf(
                Inventory(id = 1, code = 100, name = "Item", price = 10, quantity = 2)
        )

        `when`(repository.getListInventory()).thenReturn(fakeList)

        viewModel.getListInventory()
        advanceUntilIdle()

        assertEquals(fakeList, viewModel.listInventory.value)
    }

    // ---------- SAVE INVENTORY TEST ----------

    @Test
    fun `saveInventory returns true on success and updates list`() = runTest {
        val item = Inventory(id = 0, code = 200, name = "New Item", price = 20, quantity = 5)
        val updatedList = mutableListOf(item)

        `when`(repository.saveInventory(item)).thenReturn(true)
        `when`(repository.getListInventory()).thenReturn(updatedList)

        val result = viewModel.saveInventory(item)
        advanceUntilIdle()

        assertTrue(result)
        assertEquals(updatedList, viewModel.listInventory.value)
    }

    @Test
    fun `saveInventory returns false on failure`() = runTest {
        val item = Inventory(id = 0, code = 200, name = "New Item", price = 20, quantity = 5)
        `when`(repository.saveInventory(item)).thenReturn(false)

        val result = viewModel.saveInventory(item)
        advanceUntilIdle()

        assertFalse(result)
    }

    // ---------- DELETE TEST ----------

    @Test
    fun `deleteInventory calls repository and refreshes list`() = runTest {
        val item = Inventory(id = 1, code = 101, name = "Del", price = 5, quantity = 1)
        val list = mutableListOf<Inventory>()

        `when`(repository.getListInventory()).thenReturn(list)

        viewModel.deleteInventory(item)
        advanceUntilIdle()

        verify(repository).deleteInventory(item)
    }

    // ---------- UPDATE TEST ----------

    @Test
    fun `updateInventory calls repository`() = runTest {
        val item = Inventory(id = 1, code = 101, name = "Update", price = 5, quantity = 3)

        viewModel.updateInventory(item)
        advanceUntilIdle()

        verify(repository).updateInventory(item)
    }

    // ---------- GET BY ID ----------

    @Test
    fun `getById returns item`() = runTest {
        val item = Inventory(1, 100, "Found", 10, 1)

        `when`(repository.getById(1)).thenReturn(item)

        var result: Inventory? = null
        viewModel.getById(1) { result = it }
        advanceUntilIdle()

        assertEquals(item, result)
    }

    // ---------- TOTAL PRODUCT ----------

    @Test
    fun `totalProducto calculates correctly`() {
        val result = viewModel.totalProducto(10.0, 3)
        assertEquals(30.0, result, 0.0)
    }
}

