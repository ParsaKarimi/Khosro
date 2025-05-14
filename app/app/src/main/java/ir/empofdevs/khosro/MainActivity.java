package ir.empofdevs.khosro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.fragment_container);
        viewPager.setOffscreenPageLimit(3);
        FragmentStateAdapter pagerAdapter = new PagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        viewPager.registerOnPageChangeCallback(new mainPagerEvents(bottomNav));
        bottomNav.setOnItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.home);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private static class mainPagerEvents extends ViewPager2.OnPageChangeCallback {
        BottomNavigationView bottomNavBar;

        final private int[] pages = {
                R.id.animation,
                R.id.home,
                R.id.mode
        };

        public mainPagerEvents(BottomNavigationView bottomNavBar) {
            this.bottomNavBar = bottomNavBar;
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            bottomNavBar.getMenu().findItem(pages[position]).setChecked(true);
        }
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {

        if (item.getItemId() == R.id.animation) viewPager.setCurrentItem(0);
        else if (item.getItemId() == R.id.home) viewPager.setCurrentItem(1);
        else if (item.getItemId() == R.id.mode) viewPager.setCurrentItem(2);
        return true;

    };

    private static class PagerAdapter extends FragmentStateAdapter {

        Fragment[] pages = {
                new AnimationFragment(),
                new HomeFragment(),
                new ModeFragment()
        };

        public PagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return (pages[position]);
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

}