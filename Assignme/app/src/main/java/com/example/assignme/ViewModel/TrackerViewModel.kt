package com.example.assignme.ViewModel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignme.DataClass.TrackerRecord
import com.example.assignme.DataClass.TrackerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TrackerViewModel @Inject constructor(private val repository: TrackerRepository) : ViewModel() {

    private val _allEntries = MutableLiveData<List<TrackerRecord>>()
    val allEntries: LiveData<List<TrackerRecord>> get() = _allEntries

    private val _latestEntry = MutableLiveData<TrackerRecord?>()
    val latestEntry: LiveData<TrackerRecord?> get() = _latestEntry

    // New LiveData to hold the current water intake
    private val _currentWaterIntake = MutableLiveData<Int>()
    val currentWaterIntake: LiveData<Int> get() = _currentWaterIntake

    init {
        fetchAllEntries()
    }

    private fun fetchAllEntries() {
        viewModelScope.launch {
            try {
                val entries = repository.getAllEntries()
                _allEntries.postValue(entries)
                _latestEntry.postValue(entries.firstOrNull())
            } catch (e: Exception) {
                Log.e("TrackerViewModel", "Error fetching entries: ${e.message}", e)
            }
        }
    }

    fun addOrUpdateEntry(date: LocalDate, weight: Float, water: Int, calories: Float) {
        val entry = TrackerRecord(date, weight, water, calories)
        viewModelScope.launch {
            try {
                repository.insertOrUpdate(entry)
                _latestEntry.postValue(entry)
                fetchAllEntries()
            } catch (e: Exception) {
                Log.e("TrackerViewModel", "Error adding/updating entry: ${e.message}", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getEntryByDate(date: LocalDate): LiveData<TrackerRecord?> {
        val result = MutableLiveData<TrackerRecord?>()
        viewModelScope.launch {
            try {
                result.postValue(repository.getEntryByDate(date))
            } catch (e: Exception) {
                Log.e("TrackerViewModel", "Error getting entry by date: ${e.message}", e)
            }
        }
        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateWeight(date: LocalDate, weight: Float) {
        viewModelScope.launch {
            val record = repository.getEntryByDate(date)
            if (record != null) {
                record.weight = weight
                repository.insertOrUpdate(record)
            } else {
                Log.e("TrackerViewModel", "No record found for date: $date")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addWaterIntake(date: LocalDate) {
        viewModelScope.launch {
            val record = repository.getEntryByDate(date)
            if (record != null) {
                record.waterIntake += 1
                repository.insertOrUpdate(record)
                _currentWaterIntake.postValue(record.waterIntake) // Update LiveData immediately
            } else {
                val newRecord = TrackerRecord(date, waterIntake = 1)
                repository.insertOrUpdate(newRecord)
                _currentWaterIntake.postValue(1) // Set to 1 as it's a new record
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchCurrentWaterIntake(date: LocalDate) {
        viewModelScope.launch {
            try {
                val intake = repository.getEntryByDate(date)?.waterIntake ?: 0
                _currentWaterIntake.postValue(intake)
            } catch (e: Exception) {
                Log.e("TrackerViewModel", "Error fetching current water intake: ${e.message}", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addCalories(date: LocalDate, calories: Float) {
        viewModelScope.launch {
            val record = repository.getEntryByDate(date)
            if (record != null) {
                record.caloriesIntake += calories
                repository.insertOrUpdate(record)
            } else {
                repository.insertOrUpdate(TrackerRecord(date, caloriesIntake = calories))
            }
        }
    }
}
