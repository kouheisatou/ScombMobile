package net.iobb.koheinoapp.scombmobile.ui.task

import androidx.room.*

@Dao
interface TaskDao {

    @Query("SELECT * FROM task")
    fun getAllTask(): Array<Task>

    @Query("SELECT * FROM task WHERE reportId = :taskId")
    fun findTaskById(taskId: String): Task

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: Task)

    @Delete
    fun deleteTask(task: Task)

}