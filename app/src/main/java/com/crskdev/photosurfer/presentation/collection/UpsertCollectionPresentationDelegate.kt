package com.crskdev.photosurfer.presentation.collection

import androidx.lifecycle.LiveData
import com.crskdev.photosurfer.data.repository.collection.CollectionRepository
import com.crskdev.photosurfer.entities.Collection
import com.crskdev.photosurfer.util.livedata.SingleLiveEvent

class UpsertCollectionPresentationDelegate(
        private val collectionRepository: CollectionRepository,
        private val editingCollectionId: Int? = null) {

    val errorLiveData: LiveData<Throwable> = SingleLiveEvent<Throwable>()

    val successLiveData: LiveData<Unit> = SingleLiveEvent<Unit>()

    val editingCollectionLiveData: LiveData<Collection> = if (editingCollectionId != null) {
        collectionRepository.getCollectionLiveData(editingCollectionId)
    } else
        SingleLiveEvent<Collection>()

    fun submit(title: String?, description: String? = null, private: Boolean, edit: Boolean = false) {
        val cleanTitle = title?.trim()
        if (cleanTitle == null || cleanTitle.isEmpty()) {
            (errorLiveData as SingleLiveEvent).value = Error("Empty Title")
        } else {
            val collection = Collection.just(cleanTitle, description, private)
            if (edit && editingCollectionId != null) {
                collectionRepository.editCollection(collection.copy(id = editingCollectionId))
            } else {
                collectionRepository.createCollection(collection)
            }
            (successLiveData as SingleLiveEvent<Unit>).value = Unit
        }
    }

}