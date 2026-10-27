package com.vrddhih.app.features.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vrddhih.app.VrddhihApplication
import com.vrddhih.app.core.data.local.entity.CategoryEntity
import com.vrddhih.app.core.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryEditorState(
    val isOpen: Boolean = false,
    val editingCategory: CategoryEntity? = null,
    val name: String = "",
    val iconKey: String = "person",
    val colorArgb: Int = 0xFF7CD3FF.toInt(),
    val nameError: String? = null,
)

data class CategoriesUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val editor: CategoryEditorState = CategoryEditorState(),
    val deleteTarget: CategoryEntity? = null,
    val affectedQuestCount: Int = 0,
)

class CategoriesViewModel(
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val editorState = MutableStateFlow(CategoryEditorState())
    private val deleteState = MutableStateFlow<CategoryEntity?>(null)
    private val affectedCountState = MutableStateFlow(0)

    val uiState: StateFlow<CategoriesUiState> = combine(
        categoryRepository.observeAll(),
        editorState,
        deleteState,
        affectedCountState,
    ) { categories, editor, deleteTarget, count ->
        CategoriesUiState(
            categories = categories,
            editor = editor,
            deleteTarget = deleteTarget,
            affectedQuestCount = count,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CategoriesUiState(),
    )

    fun openCreate() {
        editorState.value = CategoryEditorState(
            isOpen = true,
            editingCategory = null,
            name = "",
            iconKey = "person",
            colorArgb = 0xFF7CD3FF.toInt(),
        )
    }

    fun openEdit(category: CategoryEntity) {
        if (category.isBuiltIn) return
        editorState.value = CategoryEditorState(
            isOpen = true,
            editingCategory = category,
            name = category.name,
            iconKey = category.iconKey,
            colorArgb = category.colorArgb,
        )
    }

    fun setName(name: String) {
        editorState.update { it.copy(name = name, nameError = null) }
    }

    fun setIconKey(key: String) {
        editorState.update { it.copy(iconKey = key) }
    }

    fun setColor(argb: Int) {
        editorState.update { it.copy(colorArgb = argb) }
    }

    fun closeEditor() {
        editorState.value = CategoryEditorState()
    }

    fun saveEditor() {
        val state = editorState.value
        val trimmed = state.name.trim()
        if (trimmed.isBlank()) {
            editorState.update { it.copy(nameError = "Name is required") }
            return
        }
        viewModelScope.launch {
            if (state.editingCategory == null) {
                // Create new custom category
                val result = categoryRepository.createCustom(trimmed, state.iconKey, state.colorArgb)
                if (result == null) {
                    editorState.update { it.copy(nameError = "A category with this name already exists") }
                } else {
                    editorState.value = CategoryEditorState()
                }
            } else {
                // Update existing
                val ok = categoryRepository.updateCustom(state.editingCategory, trimmed, state.iconKey, state.colorArgb)
                if (!ok) {
                    editorState.update { it.copy(nameError = "A category with this name already exists") }
                } else {
                    editorState.value = CategoryEditorState()
                }
            }
        }
    }

    fun requestDelete(category: CategoryEntity) {
        if (category.isBuiltIn) return
        viewModelScope.launch {
            // Count quests that will be reassigned.
            val all = categoryRepository.getAll()
            // We count via a simple approach: get all categories, we don't have direct count
            // so set 0 and let the confirmation dialog be generic.
            affectedCountState.value = 0
            deleteState.value = category
        }
    }

    fun dismissDelete() {
        deleteState.value = null
        affectedCountState.value = 0
    }

    fun confirmDelete() {
        val target = deleteState.value ?: return
        viewModelScope.launch {
            categoryRepository.deleteCustom(target)
            deleteState.value = null
            affectedCountState.value = 0
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as VrddhihApplication
                CategoriesViewModel(categoryRepository = app.container.categoryRepository)
            }
        }
    }
}
