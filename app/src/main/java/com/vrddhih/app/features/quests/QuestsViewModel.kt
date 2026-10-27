package com.vrddhih.app.features.quests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vrddhih.app.VrddhihApplication
import com.vrddhih.app.core.data.local.entity.CategoryEntity
import com.vrddhih.app.core.data.local.entity.TaskPriority
import com.vrddhih.app.core.data.quest.QuestBook
import com.vrddhih.app.core.data.quest.QuestDraft
import com.vrddhih.app.core.data.quest.QuestDraftError
import com.vrddhih.app.core.data.quest.QuestFilter
import com.vrddhih.app.core.data.quest.QuestLists
import com.vrddhih.app.core.data.quest.QuestOccurrence
import com.vrddhih.app.core.data.quest.QuestScope
import com.vrddhih.app.core.data.quest.QuestSort
import com.vrddhih.app.core.data.repository.CategoryRepository
import com.vrddhih.app.core.data.repository.QuestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class QuestsUiState(
    val filter: QuestFilter = QuestFilter.TODAY,
    val sort: QuestSort = QuestSort.DEADLINE,
    val searchQuery: String = "",
    val occurrences: List<QuestOccurrence> = emptyList(),
    val counts: Map<QuestFilter, Int> = emptyMap(),
    val today: LocalDate = LocalDate.now(),
    val isEditorOpen: Boolean = false,
    val isEditorLoading: Boolean = false,
    val editingDraft: QuestDraft? = null,
    val editingQuestId: Long? = null,
    val editingOccurrenceDate: LocalDate? = null,
    val editingScope: QuestScope = QuestScope.ALL,
    val editorErrors: Set<QuestDraftError> = emptySet(),
    val isDetailOpen: Boolean = false,
    val selectedOccurrence: QuestOccurrence? = null,
    val deleteTarget: QuestOccurrence? = null,
    val categories: List<CategoryEntity> = emptyList(),
)

class QuestsViewModel(
    private val questRepository: QuestRepository,
    private val categoryRepository: CategoryRepository,
    private val currentDate: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    private val filterState = MutableStateFlow(QuestFilter.TODAY)
    private val sortState = MutableStateFlow(QuestSort.DEADLINE)
    private val searchState = MutableStateFlow("")
    private val todayState = MutableStateFlow(currentDate())

    private val editorState = MutableStateFlow(
        EditorDialogState(isOpen = false, isLoading = false, draft = null, questId = null, occurrenceDate = null, scope = QuestScope.ALL, errors = emptySet()),
    )
    private val detailState = MutableStateFlow<QuestOccurrence?>(null)
    private val deleteState = MutableStateFlow<QuestOccurrence?>(null)

    private data class EditorDialogState(
        val isOpen: Boolean,
        val isLoading: Boolean = false,
        val draft: QuestDraft?,
        val questId: Long?,
        val occurrenceDate: LocalDate?,
        val scope: QuestScope,
        val errors: Set<QuestDraftError>,
    )

    private data class FilterConfig(
        val filter: QuestFilter,
        val sort: QuestSort,
        val query: String,
        val today: LocalDate,
    )

    private val queryConfig = combine(
        filterState,
        sortState,
        searchState,
        todayState,
        ::FilterConfig,
    )

    private data class QuestBookData(
        val occurrences: List<QuestOccurrence>,
        val counts: Map<QuestFilter, Int>,
        val config: FilterConfig,
        val book: QuestBook,
    )

    private val questBookData = combine(
        questRepository.observeQuestBook(),
        queryConfig,
        categoryRepository.observeAll(),
    ) { book, config, cats ->
        val categoryNames = cats.associate { it.id to it.name }
        val counts = QuestFilter.entries.associateWith { f ->
            QuestLists.build(book, f, config.today, query = "", categoryNames = categoryNames).size
        }
        val occurrences = QuestLists.build(
            book,
            config.filter,
            config.today,
            query = config.query,
            sort = config.sort,
            categoryNames = categoryNames,
        )
        QuestBookData(occurrences = occurrences, counts = counts, config = config, book = book)
    }

    val uiState: StateFlow<QuestsUiState> = combine(
        questBookData,
        editorState,
        detailState,
        deleteState,
        categoryRepository.observeAll(),
    ) { bookData, editor, detail, delete, cats ->
        val config = bookData.config
        val updatedDetail = detail?.let { d ->
            bookData.book.representative(d.quest, config.today) ?: d
        }
        QuestsUiState(
            filter = config.filter,
            sort = config.sort,
            searchQuery = config.query,
            occurrences = bookData.occurrences,
            counts = bookData.counts,
            today = config.today,
            isEditorOpen = editor.isOpen,
            isEditorLoading = editor.isLoading,
            editingDraft = editor.draft,
            editingQuestId = editor.questId,
            editingOccurrenceDate = editor.occurrenceDate,
            editingScope = editor.scope,
            editorErrors = editor.errors,
            isDetailOpen = updatedDetail != null,
            selectedOccurrence = updatedDetail,
            deleteTarget = delete,
            categories = cats,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        QuestsUiState(today = currentDate()),
    )

    fun refreshToday() {
        todayState.value = currentDate()
    }

    fun setFilter(filter: QuestFilter) {
        filterState.value = filter
    }

    fun setSort(sort: QuestSort) {
        sortState.value = sort
    }

    fun setSearchQuery(query: String) {
        searchState.value = query
    }

    fun toggleCompletion(occurrence: QuestOccurrence) {
        viewModelScope.launch {
            if (occurrence.isCompleted) {
                questRepository.reopenOccurrence(occurrence.questId, occurrence.date)
            } else {
                questRepository.completeOccurrence(occurrence.questId, occurrence.date, xpAwarded = occurrence.quest.xpReward)
            }
        }
    }

    fun openCreateQuest(defaultDate: LocalDate? = null) {
        val date = defaultDate ?: todayState.value
        editorState.value = EditorDialogState(
            isOpen = true,
            isLoading = false,
            draft = QuestDraft(title = "", scheduledDate = date),
            questId = null,
            occurrenceDate = null,
            scope = QuestScope.ALL,
            errors = emptySet(),
        )
    }

    fun openEditQuest(occurrence: QuestOccurrence, initialScope: QuestScope = QuestScope.ALL) {
        val draft = QuestDraft.from(occurrence.quest).copy(
            title = occurrence.title,
            description = occurrence.notes,
            priority = occurrence.priority,
            dueTime = occurrence.dueTime,
            scheduledDate = occurrence.date,
        )
        editorState.value = EditorDialogState(
            isOpen = true,
            isLoading = false,
            draft = draft,
            questId = occurrence.questId,
            occurrenceDate = occurrence.date,
            scope = initialScope,
            errors = emptySet(),
        )
    }

    fun prepareEditor(questId: Long?, occurrenceDate: LocalDate?) {
        if (questId == null) {
            val current = editorState.value
            if (!current.isOpen || current.questId != null || current.draft == null) {
                openCreateQuest(occurrenceDate)
            }
            return
        }

        val current = editorState.value
        if (current.isOpen && current.questId == questId && current.draft != null && !current.isLoading) {
            return
        }

        editorState.value = EditorDialogState(
            isOpen = true,
            isLoading = true,
            draft = null,
            questId = questId,
            occurrenceDate = occurrenceDate,
            scope = QuestScope.ALL,
            errors = emptySet(),
        )

        viewModelScope.launch {
            val quest = questRepository.getQuest(questId)
            if (quest != null) {
                val baseDraft = QuestDraft.from(quest)
                val draft = if (occurrenceDate != null) {
                    baseDraft.copy(scheduledDate = occurrenceDate)
                } else {
                    baseDraft
                }
                editorState.value = EditorDialogState(
                    isOpen = true,
                    isLoading = false,
                    draft = draft,
                    questId = questId,
                    occurrenceDate = occurrenceDate,
                    scope = QuestScope.ALL,
                    errors = emptySet(),
                )
            } else {
                closeEditor()
            }
        }
    }

    fun updateEditorDraft(draft: QuestDraft) {
        editorState.update { it.copy(draft = draft, errors = draft.validate()) }
    }

    fun updateEditorScope(scope: QuestScope) {
        editorState.update { it.copy(scope = scope) }
    }

    fun closeEditor() {
        editorState.value = EditorDialogState(
            isOpen = false,
            isLoading = false,
            draft = null,
            questId = null,
            occurrenceDate = null,
            scope = QuestScope.ALL,
            errors = emptySet(),
        )
    }

    fun saveEditor(onSaved: (() -> Unit)? = null) {
        val current = editorState.value
        val draft = current.draft ?: return
        val errors = draft.validate()
        if (errors.isNotEmpty()) {
            editorState.update { it.copy(errors = errors) }
            return
        }

        viewModelScope.launch {
            val questId = current.questId
            if (questId == null) {
                questRepository.createQuest(draft)
            } else {
                val isRecurring = draft.recurrence != com.vrddhih.app.core.data.local.entity.QuestRecurrence.NONE
                if (!isRecurring) {
                    questRepository.updateQuest(questId, draft)
                } else {
                    when (current.scope) {
                        QuestScope.ALL -> questRepository.updateQuest(questId, draft)
                        QuestScope.FUTURE -> {
                            val from = current.occurrenceDate ?: draft.scheduledDate
                            questRepository.updateFromDate(questId, from, draft)
                        }
                        QuestScope.OCCURRENCE -> {
                            val date = current.occurrenceDate ?: draft.scheduledDate
                            questRepository.updateOccurrence(
                                questId = questId,
                                date = date,
                                title = draft.title,
                                description = draft.description,
                                priority = draft.priority,
                                dueTime = draft.dueTime,
                            )
                        }
                    }
                }
            }
            closeEditor()
            onSaved?.invoke()
        }
    }

    suspend fun createCustomCategory(name: String, iconKey: String, colorArgb: Int): CategoryEntity? =
        categoryRepository.createCustom(name, iconKey, colorArgb)

    fun openDetails(occurrence: QuestOccurrence) {
        detailState.value = occurrence
    }

    fun closeDetails() {
        detailState.value = null
    }

    fun requestDelete(occurrence: QuestOccurrence) {
        deleteState.value = occurrence
    }

    fun dismissDelete() {
        deleteState.value = null
    }

    fun confirmDelete(scope: QuestScope) {
        val target = deleteState.value ?: return
        viewModelScope.launch {
            if (!target.isRecurring) {
                questRepository.deleteQuest(target.questId)
            } else {
                when (scope) {
                    QuestScope.OCCURRENCE -> questRepository.deleteOccurrence(target.questId, target.date)
                    QuestScope.FUTURE -> questRepository.deleteFromDate(target.questId, target.date)
                    QuestScope.ALL -> questRepository.deleteQuest(target.questId)
                }
            }
            deleteState.value = null
            if (detailState.value?.questId == target.questId) {
                detailState.value = null
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as VrddhihApplication
                QuestsViewModel(
                    questRepository = app.container.questRepository,
                    categoryRepository = app.container.categoryRepository,
                )
            }
        }
    }
}
