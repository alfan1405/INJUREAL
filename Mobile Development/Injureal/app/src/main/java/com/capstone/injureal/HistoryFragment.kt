package com.capstone.injureal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.injureal.riwayat.AppDatabase
import com.capstone.injureal.riwayat.PredictionHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryFragment : Fragment(), HistoryAdapter.OnDeleteClickListener {

    private lateinit var predictionRecyclerView: RecyclerView
    private lateinit var predictionAdapter: HistoryAdapter
    private var predictionList: MutableList<PredictionHistory> = mutableListOf()
    private lateinit var tvNotFound: TextView
    private lateinit var database: AppDatabase

    companion object {
        const val TAG = "historydata"
        const val REQUEST_HISTORY_UPDATE = 101
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater.inflate(R.layout.fragment_history, container, false)

        predictionRecyclerView = binding.findViewById(R.id.rvHistory)
        tvNotFound = binding.findViewById(R.id.tvNotFound)

        predictionAdapter = HistoryAdapter(predictionList)
        predictionAdapter.setOnDeleteClickListener(this)
        predictionRecyclerView.adapter = predictionAdapter
        predictionRecyclerView.layoutManager = LinearLayoutManager(activity)

        database = AppDatabase.getDatabase(requireContext())

        loadPredictionHistoryFromDatabase()

        return binding
    }

    private fun loadPredictionHistoryFromDatabase() {
        lifecycleScope.launch(Dispatchers.IO) {
            val historyDao = database.predictionHistoryDao()
            val history = historyDao.getAllPredictions()
            withContext(Dispatchers.Main) {
                predictionList.clear()
                predictionList.addAll(history)
                predictionAdapter.notifyDataSetChanged()
                toggleEmptyView()
            }
        }
    }

    private fun toggleEmptyView() {
        if (predictionList.isEmpty()) {
            tvNotFound.visibility = View.VISIBLE
            predictionRecyclerView.visibility = View.GONE
        } else {
            tvNotFound.visibility = View.GONE
            predictionRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onDeleteClick(position: Int) {
        val prediction = predictionList[position]
        lifecycleScope.launch(Dispatchers.IO) {
            database.predictionHistoryDao().deletePrediction(prediction)
            withContext(Dispatchers.Main) {
                predictionList.removeAt(position)
                predictionAdapter.notifyItemRemoved(position)
                toggleEmptyView()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.bottom_nav_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}