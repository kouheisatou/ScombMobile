package net.iobb.koheinoapp.scombmobile.ui.timetable

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClassCellDao {

    @Query("SELECT * FROM classCell WHERE classId = :id")
    fun getClassCell(id: String): ClassCell?

    @Query("SELECT * FROM classCell WHERE createdDate > :date")
    fun getClassCellsAfter(date: Long): Array<ClassCell>

    @Query("SELECT * FROM classCell")
    fun getAllClassCell(): Array<ClassCell>

    @Query("SELECT * FROM classCell WHERE year = :year AND term = :term")
    fun getClasses(year: Int, term: Int): Array<ClassCell>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertClassCell(classCell: ClassCell)
}