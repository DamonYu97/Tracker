package com.example.damon.tracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static String roleNo,username,userPicUrl;
    private OkHttpClient okHttpClient;
    private String baseUrl = LoginActivity.baseUrl;
    private Menu navMenu;
    private Toolbar toolbar;
    private AccountFragment accountFragment;
    private CurrentLocationFragment currentLocationFragment;
    private CircleImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initialize toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("控制台");
        setSupportActionBar(toolbar);
        //set Control fragment as Homepage
        Fragment fragment = new ControlFragment();
        setFragment(fragment);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navMenu=navigationView.getMenu();

        //get username
        SharedPreferences preferences=getSharedPreferences("USER",MODE_PRIVATE);
        View headerLayout = navigationView.getHeaderView(0);    //get nav_header_main layout
        TextView user=(TextView)headerLayout.findViewById(R.id.username);
        username = preferences.getString("username","");
        user.setText(username);
        //get permission information
        okHttpClient=new OkHttpClient();
        Permission.init();
        getPermission(preferences.getString("userID",""));
        //set user profile
        profileImageView = (CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.profile);
        userPicUrl = preferences.getString("userPicUrl","");
        Log.e("url!!!","userPicURL: "+userPicUrl);
        if (!userPicUrl.equals("null")) {
            setProfile();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                accountFragment.setSearchResult(query);
                accountFragment.setInfo();
                item.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        final MenuItem refreshItem = menu.findItem(R.id.action_refresh);
        refreshItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                currentLocationFragment.initialSearch();
                currentLocationFragment.mark();
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_message) {
            Intent intent = new Intent(MainActivity.this,MessageActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_search) {
            //initialize search view after expanding
            SearchView searchView = (SearchView)item.getActionView();
            searchView.setQueryHint("用户名");
            SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(R.id.search_src_text);
            searchAutoComplete.setTextColor(Color.parseColor("#444444"));
            searchAutoComplete.setHintTextColor(Color.parseColor("#BBBBBB"));
            searchAutoComplete.setBackgroundColor(Color.parseColor("#FFFFFF"));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        if (id == R.id.nav_control) {
           toolbar.setTitle("控制台");
           fragment = new ControlFragment();
           toolbar.getMenu().findItem(R.id.action_search).setVisible(false);
           toolbar.getMenu().findItem(R.id.action_refresh).setVisible(false);
        } else if (id == R.id.nav_accounts) {
            toolbar.setTitle("用户管理");
            fragment = new AccountFragment();
            toolbar.getMenu().findItem(R.id.action_search).setVisible(true);
            toolbar.getMenu().findItem(R.id.action_refresh).setVisible(false);
            accountFragment = (AccountFragment)fragment;
            Menu menu = toolbar.getMenu();
            menu.findItem(R.id.action_search).setVisible(true);
            MenuItem searchMenuItem = menu.findItem(R.id.action_search);
            final SearchView searchView = (SearchView)searchMenuItem.getActionView();
            searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem menuItem) {
                    //toolbar.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                    //toolbar.setBackgroundColor(getColor(R.color.colorPrimary));
                    return true;
                }
            });
        } else if (id == R.id.nav_location) {
            toolbar.setTitle("实时位置");
            fragment = new CurrentLocationFragment();
           currentLocationFragment = (CurrentLocationFragment)fragment;
            toolbar.getMenu().findItem(R.id.action_search).setVisible(false);
            toolbar.getMenu().findItem(R.id.action_refresh).setVisible(true);
        } else if (id == R.id.nav_signOut) {
            //release all user information
            SharedPreferences preferences = getSharedPreferences("USER",MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isFirst",true);
            editor.putString("username","");
            editor.putString("userID","");
            editor.commit();
            roleNo="";
            username="";
            Intent signOut = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(signOut);
            finish();
        }
        if (fragment != null){
            setFragment(fragment);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setFragment(android.support.v4.app.Fragment fragment) {
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_holder,fragment);
        fragmentTransaction.commit();
    }
    public void setProfile() {
        Request.Builder builder = new Request.Builder();
        Request request = builder.get().url(baseUrl+"profile/"+userPicUrl).build(); //request Server with user id
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(MainActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e("download:","waiting");
                InputStream is = response.body().byteStream();
                final Bitmap bitmap = BitmapFactory.decodeStream(is);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       profileImageView.setImageBitmap(bitmap);
                    }
                });
                Log.e("download:","success");
            }
        });
    }
    public void getPermission(String userID) {
        if(userID.equals("")){
            return ;
        }
        Request.Builder builder = new Request.Builder();
        Request request = builder.get().url(baseUrl+"getPermissionInfo?userID="+userID).build(); //request Server with user id
        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("getPermission Failure!",e.getMessage());
                Looper.prepare();
                Toast.makeText(MainActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String info = response.body().string();
                final Scanner in = new Scanner(info);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!info.equals("")) {
                            roleNo = in.next();
                            while(in.hasNext()){
                                String permNo = in.next();
                                Permission.isPermittedMap.put(permNo,true);
                            }
                            //remove menus which are not permitted
                            Iterator iterator = Permission.isPermittedMap.entrySet().iterator();
                            while (iterator.hasNext()){
                                Map.Entry entry = (Map.Entry)iterator.next();
                                String perm = (String)entry.getKey();
                                boolean isPermitted = (boolean)entry.getValue();
                                int menuID = Permission.permMenuIDMap.get(perm);
                                if (!isPermitted && !(menuID == 0)) {
                                    navMenu.removeItem(menuID);
                                }
                            }
                        }
                    }
                });

            }

        });
    }


}

