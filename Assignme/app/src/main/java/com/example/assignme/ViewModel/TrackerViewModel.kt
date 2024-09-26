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

    // New LiveData for weight and calorie histories
    private val _weightHistory = MutableLiveData<List<TrackerRecord>>()
    val weightHistory: LiveData<List<TrackerRecord>> get() = _weightHistory

    private val _calorieHistory = MutableLiveData<List<TrackerRecord>>()
    val calorieHistory: LiveData<List<TrackerRecord>> get() = _calorieHistory

    private val _entriesForCurrentMonth = MutableLiveData<List<TrackerRecord>>()
    val entriesForCurrentMonth: LiveData<List<TrackerRecord>> = _entriesForCurrentMonth

    init {
        fetchAllEntries()
    }

    private fun fetchAllEntries() {
        viewModelScope.launch {
            try {
                val entries = repository.getAllEntries()
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
        // Update weight and calorie histories from the fetched entries
        _weightHistory.postValue(entries.filter { it.weight > 0 }) // Only include records with weight
        _calorieHistory.postValue(entries.filter { it.caloriesIntake > 0 }) // Only include records with calories
    }

    fun addOrUpdateEntry(date: LocalDate, weight: Float, water: Int, calories: Float) {
        val entry = TrackerRecord(date, weight, water, calories)
        viewModelScope.launch {
            try {
                repository.insertOrUpdate(entry)
                _latestEntry.postValue(entry)
                fetchAllEntries() // This will update the histories as well
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
                fetchAllEntries() // Update histories after weight change
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
                fetchAllEntries() // Update histories after adding calories
            } else {
                repository.insertOrUpdate(TrackerRecord(date, caloriesIntake = calories))
                fetchAllEntries() // Update histories after adding new record
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchEntriesForCurrentMonth() {
        viewModelScope.launch {
            val entries = repository.getEntriesForCurrentMonth()
            _entriesForCurrentMonth.postValue(entries)
        }
    }
    class TrackerViewModel @Inject constructor(private val repository: TrackerRepository) : ViewModel() {
        private val _trackerRecords = MutableStateFlow<List<TrackerRecord>>(emptyList())
        val trackerRecords: StateFlow<List<TrackerRecord>> = _trackerRecords.asStateFlow()

        init {
            fetchAllEntries()
        }

        private fun fetchAllEntries() {
            viewModelScope.launch {
                _trackerRecords.value = repository.getAllEntries()
            }
        }
}
}
