package com.example.muse.activity;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.example.muse.R;
import com.example.muse.databinding.ActivityHomeBinding;
import com.example.muse.fragment.HomeFragment;
import com.example.muse.fragment.SearchFragment;
import com.example.muse.model.FilterOptions;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private final FilterOptions filterOptions = new FilterOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigationFix();
        setupSidebar();
    }

    private void setupNavigationFix() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            binding.bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
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

    private void setupSidebar() {
        binding.navigationView.setNavigationItemSelectedListener(item -> {
            handleFilterSelection(item);
            return false; // Keep drawer open for multi-selection
        });

        binding.btnResetFilterSidebar.setOnClickListener(v -> resetFilters());
    }

    private void handleFilterSelection(MenuItem item) {
        int id = item.getItemId();
        item.setChecked(!item.isChecked()); // Toggle checked state

        // ERA / PERIODE
        if (id == R.id.era_baroque) {
            filterOptions.toggleEra("Baroque", 1600, 1750, null);
        } else if (id == R.id.era_renaissance) {
            filterOptions.toggleEra("Renaissance", 1400, 1600, null);
        } else if (id == R.id.era_modern) {
            filterOptions.toggleEra("Modern", 1850, 2025, null);
        }

        // WILAYAH
        else if (id == R.id.loc_europe) {
            filterOptions.toggleCulture("French|Dutch|Italian|German|British|Spanish");
        } else if (id == R.id.loc_asia) {
            filterOptions.toggleCulture("Chinese|Japanese|Indian|Korean|Vietnamese");
        } else if (id == R.id.loc_america) {
            filterOptions.toggleCulture("American");
        }

        // TIPE KARYA
        else if (id == R.id.type_painting) {
            filterOptions.toggleClassification("Paintings");
        } else if (id == R.id.type_sculpture) {
            filterOptions.toggleClassification("Sculpture");
        }

        notifyFilterChanged();
    }

    private void resetFilters() {
        filterOptions.reset();

        // Clear all check marks in the menu
        int[] menuIds = {
            R.id.era_baroque, R.id.era_renaissance, R.id.era_modern,
            R.id.loc_europe, R.id.loc_asia, R.id.loc_america,
            R.id.type_painting, R.id.type_sculpture
        };

        for (int id : menuIds) {
            MenuItem item = binding.navigationView.getMenu().findItem(id);
            if (item != null) item.setChecked(false);
        }

        notifyFilterChanged();
        closeDrawer();
    }

    private void notifyFilterChanged() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            List<Fragment> fragments = navHostFragment.getChildFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                if (fragment instanceof HomeFragment) {
                    ((HomeFragment) fragment).onFilterChanged(filterOptions);
                } else if (fragment instanceof SearchFragment) {
                    ((SearchFragment) fragment).onFilterChanged(filterOptions);
                }
            }
        }
    }

    public void openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START);
    }

    public void closeDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START);
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
