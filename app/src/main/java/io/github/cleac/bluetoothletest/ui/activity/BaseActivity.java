package io.github.cleac.bluetoothletest.ui.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.github.cleac.bluetoothletest.R;
import io.github.cleac.bluetoothletest.ui.fragment.BluetoothScanFragment;

/**
 * Created by cleac on 9/14/15.
 */
public class BaseActivity extends AppCompatActivity {

    @Bind(R.id.toolbar) Toolbar mToolbar;
    private Fragment mFragment;

    public Fragment getFragment() {
        return mFragment;
    }

    public void setFragment(Fragment fragment) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment,fragment)
                .commit();
        mFragment = fragment;
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public void setToolbar(Toolbar toolbar) {
        mToolbar = toolbar;
        setSupportActionBar(toolbar);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        if(savedInstanceState == null) {
            mFragment = new BluetoothScanFragment();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, mFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
