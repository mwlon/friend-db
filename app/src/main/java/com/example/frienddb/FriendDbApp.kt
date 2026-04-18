package com.example.frienddb

import android.app.Application
import com.example.frienddb.data.db.FriendDbDatabase
import com.example.frienddb.data.repository.FriendRepository

class FriendDbApp : Application() {
    val database by lazy { FriendDbDatabase.getInstance(this) }
    val repository by lazy { FriendRepository(database) }
}
