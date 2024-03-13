package com.example.bondoman.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import android.content.Context

@Database(entities = [TransactionEntity::class], version = 1)
abstract class TransactionDatabase : RoomDatabase() {
    abstract val transactionDAO: TransactionDAO

    companion object {
        @Volatile
        private var INSTANCE: TransactionDatabase? = null
        fun getDatabase(context: Context): TransactionDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            TransactionDatabase::class.java,
                            "transaction_database"
                    )
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}