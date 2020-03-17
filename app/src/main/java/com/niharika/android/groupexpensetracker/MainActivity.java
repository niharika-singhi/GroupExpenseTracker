package com.niharika.android.groupexpensetracker;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.facebook.login.LoginManager;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String ARG_MEMBER_ID = "member_id";
    private DrawerLayout mDrawer;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private static final String ARG_PAGE_DISPLAY = "page";
    private boolean mToolBarNavigationListenerIsRegistered = false;
    private TextView mEmailLabel, mNavHeader;
    private ImageView mProfilePic;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawer = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mNavHeader = navigationView.getHeaderView(0).findViewById(R.id.nav_header);
        mEmailLabel = navigationView.getHeaderView(0).findViewById(R.id.email_label);
        mProfilePic = navigationView.getHeaderView(0).findViewById(R.id.nav_profile_pic);
        toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            public void onDrawerSlide(View drawerView, float slideOffset) {
                String label;
                if (AccountLab.get(MainActivity.this).getUser() != null && AccountLab.get(MainActivity.this).getUser().getMemberName() != null)
                    label = AccountLab.get(MainActivity.this).getUser().getMemberName();
                else
                    label = "Hi User";
                mNavHeader.setText(label);
                String subLabel = "";
                if (AccountLab.get(MainActivity.this).getUser() != null && AccountLab.get(MainActivity.this).getUser().getEmailId() != null)
                    subLabel = AccountLab.get(MainActivity.this).getUser().getEmailId();
                else if (AccountLab.get(MainActivity.this).getUser() != null && AccountLab.get(MainActivity.this).getUser().getMobNo() != null)
                    subLabel = AccountLab.get(MainActivity.this).getUser().getMobNo();
                mEmailLabel.setText(subLabel);
                mNavHeader.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!AccountLab.get(MainActivity.this).isNetworkAvailableAndConnected())
                            Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment).navigate(R.id.errFragment);
                        else
                            Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment).navigate(R.id.editMemberFragment);
                    }
                });
                mEmailLabel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!AccountLab.get(MainActivity.this).isNetworkAvailableAndConnected())
                            Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment).navigate(R.id.errFragment);
                        else
                            Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment).navigate(R.id.editMemberFragment);
                    }
                });
                mProfilePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!AccountLab.get(MainActivity.this).isNetworkAvailableAndConnected())
                            Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment).navigate(R.id.errFragment);
                        else
                            Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment).navigate(R.id.editMemberFragment);
                    }
                });
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void showDrawer(boolean enable) {
        if (!enable) {
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            toggle.setDrawerIndicatorEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // when DrawerToggle is disabled , navigation icon clicks are disabled i.e. the UP button will not work.
            // We need to add a listener, as in below, so DrawerToggle will forward click events to this listener.
            if (!mToolBarNavigationListenerIsRegistered) {
                toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Doesn't have to be onBackPressed
                        onBackPressed();
                    }
                });
                mToolBarNavigationListenerIsRegistered = true;
            }
        } else {
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            toggle.setDrawerIndicatorEnabled(true);
            toggle.setToolbarNavigationClickListener(null);
            mToolBarNavigationListenerIsRegistered = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_drawer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /*int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_PAGE_DISPLAY, getString(R.string.page_type_login));
        switch (item.getItemId()) {
            case R.id.nav_logout:
                AccountLab.get(this).delAccountLab();
                FirebaseAuth.getInstance().signOut();
                if (LoginManager.getInstance() != null)
                    LoginManager.getInstance().logOut();
                navController.navigate(R.id.registerFragment, bundle);
                break;
            case R.id.nav_home:
                if (currentUser != null)
                    navController.navigate(R.id.accountTabFragment);
                else
                    navController.navigate(R.id.registerFragment, bundle);
                break;
            case R.id.nav_profile:
                if (!AccountLab.get(MainActivity.this).isNetworkAvailableAndConnected())
                    Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment).navigate(R.id.errFragment);
                else
                    navController.navigate(R.id.editMemberFragment);
                break;

            case R.id.nav_share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                //intent.setAction(Intent.ACTION_VIEW);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                shareIntent.putExtra(Intent.EXTRA_TEXT,getString(R.string.playstoreLink)
                        +BuildConfig.APPLICATION_ID );
                startActivity(Intent.createChooser(shareIntent, "Choose one"));
                break;
            case R.id.nav_settings:
                if (currentUser != null)
                    navController.navigate(R.id.accountTabFragment);
                else
                    navController.navigate(R.id.registerFragment, bundle);
                break;

            case R.id.nav_faq:
                Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment).navigate(R.id.FAQFragment);
                break;

            default:
                mDrawer.closeDrawer(GravityCompat.START);
                return true;
        }
        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void disableActionBar() {
        //This function sets both nav drawer and Action Bar Up button disabled.Its used in login and Register Fragment
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.syncState();
    }
}