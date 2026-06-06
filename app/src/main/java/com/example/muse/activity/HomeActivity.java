package com.example.muse.activity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.muse.R;
import com.example.muse.databinding.ActivityHomeBinding;
import com.example.muse.databinding.LayoutFilterSidebarBinding;
import com.example.muse.fragment.HomeFragment;
import com.example.muse.fragment.SearchFragment;
import com.example.muse.model.FilterOptions;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private LayoutFilterSidebarBinding filterBinding;
    private FilterOptions filterOptions = new FilterOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Fix: Use the automatically generated binding for the included layout
        filterBinding = binding.layoutFilter;

        setupNavigationFix();
        setupFilterSpinners();
        setupFilterActions();
    }

    private void setupNavigationFix() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            
            binding.bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                
                // Pop backstack to start destination to avoid redundant state
                navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
                
                if (id == R.id.navigation_home) {
                    navController.navigate(R.id.navigation_home);
                } else if (id == R.id.navigation_search) {
                    navController.navigate(R.id.navigation_search);
                } else if (id == R.id.navigation_favorit) {
                    navController.navigate(R.id.navigation_favorit);
                } else if (id == R.id.navigation_user) {
                    navController.navigate(R.id.navigation_user);
                }
                return true;
            });
        }
    }

    private void setupFilterSpinners() {
        String[] eras = {"Semua Era", "Prasejarah", "Kuno (500 SM-500 M)", "Abad Pertengahan", "Renaissance", "Baroque & Klasik", "Abad 19", "Abad 20", "Kontemporer"};
        String[] locations = {"Semua Wilayah", "Eropa", "Amerika", "Asia", "Afrika", "Timur Tengah", "Mesir Kuno"};
        String[] types = {"Semua Tipe", "Lukisan", "Patung", "Fotografi", "Seni Dekoratif", "Senjata & Armor", "Manuskrip"};

        filterBinding.spinnerEra.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, eras));
        filterBinding.spinnerLocation.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, locations));
        filterBinding.spinnerType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types));
    }

    private void setupFilterActions() {
        filterBinding.btnApplyFilter.setOnClickListener(v -> {
            applyFilters();
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        });

        filterBinding.btnResetFilterSidebar.setOnClickListener(v -> {
            filterBinding.spinnerEra.setSelection(0);
            filterBinding.spinnerLocation.setSelection(0);
            filterBinding.spinnerType.setSelection(0);
            filterOptions.reset();
            notifyFilterChanged();
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        });
    }

    private void applyFilters() {
        // Map Era
        String era = (String) filterBinding.spinnerEra.getSelectedItem();
        filterOptions.setEraLabel(era);
        if (era.equals("Prasejarah")) { filterOptions.setDateBegin(-3000); filterOptions.setDateEnd(-500); }
        else if (era.equals("Kuno (500 SM-500 M)")) { filterOptions.setDateBegin(-500); filterOptions.setDateEnd(500); }
        else if (era.equals("Abad Pertengahan")) { filterOptions.setDateBegin(500); filterOptions.setDateEnd(1400); }
        else if (era.equals("Renaissance")) { filterOptions.setDateBegin(1400); filterOptions.setDateEnd(1600); }
        else if (era.equals("Baroque & Klasik")) { filterOptions.setDateBegin(1600); filterOptions.setDateEnd(1800); }
        else if (era.equals("Abad 19")) { filterOptions.setDateBegin(1800); filterOptions.setDateEnd(1900); }
        else if (era.equals("Abad 20")) { filterOptions.setDateBegin(1900); filterOptions.setDateEnd(2000); }
        else if (era.equals("Kontemporer")) { filterOptions.setDateBegin(2000); filterOptions.setDateEnd(2024); }
        else { filterOptions.setDateBegin(null); filterOptions.setDateEnd(null); }

        // Map Location
        String loc = (String) filterBinding.spinnerLocation.getSelectedItem();
        filterOptions.setLocationLabel(loc);
        if (loc.equals("Eropa")) filterOptions.setGeoLocation("Europe");
        else if (loc.equals("Amerika")) filterOptions.setGeoLocation("Americas");
        else if (loc.equals("Asia")) filterOptions.setGeoLocation("Asia");
        else if (loc.equals("Afrika")) filterOptions.setGeoLocation("Africa");
        else if (loc.equals("Timur Tengah")) filterOptions.setGeoLocation("Middle East");
        else if (loc.equals("Mesir Kuno")) filterOptions.setGeoLocation("Egypt");
        else filterOptions.setGeoLocation(null);

        // Map Type
        String type = (String) filterBinding.spinnerType.getSelectedItem();
        filterOptions.setTypeLabel(type);
        if (type.equals("Lukisan")) filterOptions.setDepartmentId(11);
        else if (type.equals("Patung")) filterOptions.setDepartmentId(13);
        else if (type.equals("Fotografi")) filterOptions.setDepartmentId(19);
        else if (type.equals("Seni Dekoratif")) filterOptions.setDepartmentId(6);
        else if (type.equals("Senjata & Armor")) filterOptions.setDepartmentId(4);
        else if (type.equals("Manuskrip")) filterOptions.setDepartmentId(8);
        else filterOptions.setDepartmentId(null);

        notifyFilterChanged();
    }

    private void notifyFilterChanged() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            List<Fragment> fragments = navHostFragment.getChildFragmentManager().getFragments();
            if (!fragments.isEmpty()) {
                Fragment currentFragment = fragments.get(0);
                if (currentFragment instanceof HomeFragment) {
                    ((HomeFragment) currentFragment).onFilterChanged(filterOptions);
                } else if (currentFragment instanceof SearchFragment) {
                    ((SearchFragment) currentFragment).onFilterChanged(filterOptions);
                }
            }
        }
    }

    public void openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
