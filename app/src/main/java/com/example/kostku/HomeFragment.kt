package com.example.kostku

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kostku.adapter.RoomTypeAdapter
import com.example.kostku.data.UserPreferencesManager
import com.example.kostku.databinding.FragmentHomeBinding
import com.example.kostku.model.RoomType
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferencesManager: UserPreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        // biar menu toolbar bisa muncul
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // init prefs
        userPreferencesManager = UserPreferencesManager(requireContext())

        // pasang toolbar dari fragment ke AppCompatActivity
        (activity as? AppCompatActivity)?.apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(true)
        }

        // siapkan data dummy
        val roomTypes = listOf(
            RoomType("A", 3000000, 5, "Kasur, Meja & Kursi, AC, Kamar Mandi Dalam"),
            RoomType("B", 2000000, 5, "Kasur, Meja & Kursi, AC, Kamar Mandi Dalam"),
            RoomType("C", 1000000, 5, "Kasur, Meja & Kursi, AC, Kamar Mandi Dalam")
        )

        // setup RecyclerView
        binding.rvKostList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = RoomTypeAdapter(roomTypes) { roomType ->
                startActivity(RoomTypeDetailActivity.createIntent(requireContext(), roomType))
            }
        }

        // tampilkan nama user
        viewLifecycleOwner.lifecycleScope.launch {
            val prefs = userPreferencesManager.userPreferencesFlow.first()
            binding.tvWelcome.text = "Selamat Datang, ${prefs.name}"
        }
    }

    // inflate menu toolbar
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    userPreferencesManager.clearUserPreferences()
                    Intent(requireContext(), LoginActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                    }
                    requireActivity().finish()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
