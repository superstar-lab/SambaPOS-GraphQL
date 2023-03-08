package com.eft.positivelauncher.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.eft.positivelauncher.R;
import com.eft.positivelauncher.fragments.ChooseOptionFragment;
import com.eft.positivelauncher.fragments.InputAmountFragment;
import com.eft.positivelauncher.fragments.ResponseFragment;

import static com.eft.positivelauncher.activities.MainActivity.CHOOSE_OPTION_FRAGMENT;
import static com.eft.positivelauncher.activities.MainActivity.INPUT_AMOUNT_FRAGMENT;
import static com.eft.positivelauncher.activities.MainActivity.RESPONSE_FRAGMENT;

public class CustomActivity extends AppCompatActivity {
    private String fragToLaunch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            fragToLaunch = extras.getString("fragment_to_launch");
        }
        setContentView(R.layout.fragments_container);
        Fragment fragmentToLaunch = null;
        switch (fragToLaunch) {
            case INPUT_AMOUNT_FRAGMENT:
                fragmentToLaunch = new InputAmountFragment();
                break;
            case CHOOSE_OPTION_FRAGMENT:
                fragmentToLaunch = new ChooseOptionFragment();
                break;
            case RESPONSE_FRAGMENT:
                fragmentToLaunch = new ResponseFragment();
                break;
            default:
                fragmentToLaunch = new ChooseOptionFragment();
                break;
        }
        fragmentToLaunch.setArguments(extras);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.custom_fragment, fragmentToLaunch).commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.back:
                FragmentManager fm = getFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                } else {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    hideKeyboard(this);
                }
                return false;

            default:
                break;
        }

        return false;
    }


    public static void hideKeyboard(Context context) {
        try {
            Activity activity = (Activity) context;
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            if (activity.getCurrentFocus() != null && (activity.getCurrentFocus().getWindowToken() != null)) {
                ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
