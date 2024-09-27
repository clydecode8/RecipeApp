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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _weightHistory = MutableLiveData<List<TrackerRecord>>(emptyList())
    val weightHistory: LiveData<List<TrackerRecord>> = _weightHistory

    private val _calorieHistory = MutableLiveData<List<TrackerRecord>>()
    val calorieHistory: LiveData<List<TrackerRecord>> get() = _calorieHistory

    init {
        fetchAllEntries()
    }

    private fun fetchAllEntries() {
        viewModelScope.launch {
            try {
                val entries = repository.getAllEntries()

                // Post all entries and latest entry
                _allEntries.postValue(entries)
                _latestEntry.postValue(entries.firstOrNull())

                // Update the weight and calorie histories
                updateHistories(entries)

            } catch (e: Exception) {
                Log.e("TrackerViewModel", "Error fetching entries: ${e.message}", e)
            }
        }
    }

    private fun updateHistories(entries: List<TrackerRecord>) {
        // Update weight history: Only include records where weight is greater than 0
        val weightHistory = entries.filter { it.weight > 0 }
        _weightHistory.postValue(weightHistory)

        // Update calorie history: Only include records where calories intake is greater than 0
        val calorieHistory = entries.filter { it.caloriesIntake > 0 }
        _calorieHistory.postValue(calorieHistory)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateWeight(date: LocalDate, weight: Float) {
        viewModelScope.launch {
            val record = repository.getEntryByDate(date)
            if (record != null) {
                record.weight = weight
                repository.insertOrUpdate(record)
                fetchAllEntries() // Ensure weightHistory and other data are updated
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
                fetchAllEntries() // Update histories after adding water intake
            } else {
                val newRecord = TrackerRecord(date, waterIntake = 1)
                repository.insertOrUpdate(newRecord)
                _currentWaterIntake.postValue(1) // Set to 1 as it's a new record
                fetchAllEntries() // Update histories after adding new record
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
                fetchAllEntries() // Update histories after adding calories
            } else {
                repository.insertOrUpdate(TrackerRecord(date, caloriesIntake = calories))
                fetchAllEntries() // Update histories after adding new record
            }
        }
    }
}
