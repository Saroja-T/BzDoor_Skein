package com.busydoor.app.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.busydoor.app.R
import com.busydoor.app.databinding.ActivityBottomNavBarBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.lang.reflect.Field
import java.lang.reflect.Modifier


class BottomNavigationBarActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var binding: ActivityBottomNavBarBinding

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        binding = ActivityBottomNavBarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navController = findNavController(R.id.main_fragment)
       // setupActionBarWithNavController(navController)

        binding.bottomBar.onItemSelected = {
            Log.e("onItemSelected", "Item $it selected")
        }
        binding.bottomBar.onItemReselected = {
            Log.e("onItemReselected", "Item $it selected")
        }

        setupSmoothBottomMenu()
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.another_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.another_item_1 -> {
                showToast("Another Menu Item 1 Selected")
            }

            R.id.another_item_2 -> {
                showToast("Another Menu Item 2 Selected")
            }

            R.id.another_item_3 -> {
                showToast("Another Menu Item 3 Selected")
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    //set an active fragment programmatically
//    fun setSelectedItem(pos:Int){
//        binding.bottomBar.setSelectedItem(pos)
//    }
//    //set badge indicator
//    fun setBadge(pos:Int){
//        binding.bottomBar.setBadge(pos)
//    }
//    //remove badge indicator
//    fun removeBadge(pos:Int){
//        binding.bottomBar.removeBadge(pos)
//    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun setupSmoothBottomMenu() {
        val popupMenu = PopupMenu(this, null)
        popupMenu.inflate(R.menu.menu_bottom)
        // Hide the second item in the menu
        val itemIndexToHide = 2
        popupMenu.menu.removeItem(itemIndexToHide)
        val menu = popupMenu.menu
        binding.bottomBar.setupWithNavController(menu, navController)
//        binding.bottomBar.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}