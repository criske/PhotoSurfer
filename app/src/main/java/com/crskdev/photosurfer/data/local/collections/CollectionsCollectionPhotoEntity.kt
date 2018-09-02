package com.crskdev.photosurfer.data.local.collections

import androidx.room.*
import com.crskdev.photosurfer.data.local.Contract

/**
 * Created by Cristian Pela on 30.08.2018.
 */
@Entity(
        tableName = Contract.TABLE_COLLECTIONS_COLLECTION_PHOTOS,
        foreignKeys = [
            ForeignKey(
                    entity = CollectionEntity::class,
                    parentColumns = ["id"],
                    childColumns = ["collection_id"],
                    onDelete = ForeignKey.CASCADE),
            ForeignKey(
                    entity = CollectionPhotoEntity::class,
                    parentColumns = ["id"],
                    childColumns = ["photo_id"],
                    onDelete = ForeignKey.CASCADE)
        ],
        indices = [Index(value = ["collection_id", "photo_id"], unique = true)]
)
class CollectionsCollectionPhotoEntity {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo(name = "collection_id")
    var collectionId: Int = -1

    @ColumnInfo(name = "photo_id")
    lateinit var photoId: String

}