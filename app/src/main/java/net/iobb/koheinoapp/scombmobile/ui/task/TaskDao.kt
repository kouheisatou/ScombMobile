package net.iobb.koheinoapp.scombmobile.ui.task

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TaskDao {

    @Query("SELECT * FROM task")
    fun getAllTask(): Array<Task>

    @Query("SELECT * FROM task WHERE reportId = :taskId")
    fun findTaskById(taskId: String): Task

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: Task)
}