package com.crskdev.photosurfer.data.local

/**
 * Created by Cristian Pela on 22.08.2018.
 */
class DaoManager(
        private val databaseOps: DatabaseOps,
        private val daos: Map<String, DataAccessor>) : DatabaseOps by databaseOps {

    @Suppress("UNCHECKED_CAST")
    fun <DAO : DataAccessor> getDao(tableName: String): DAO =
            daos[tableName] as DAO? ?: throw Exception("Dao with $tableName table name not found")
}

interface DatabaseOps {

    fun clearAll()

    fun transactionRunner(): TransactionRunner

}


class DatabaseOpsImpl(private val photoSurferDB: PhotoSurferDB,
                      private val transactionRunner: TransactionRunner) : DatabaseOps {

    override fun clearAll() {
        photoSurferDB.clearAllTables()
    }

    override fun transactionRunner(): TransactionRunner = transactionRunner

}