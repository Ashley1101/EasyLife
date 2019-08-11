package com.deepspring.lib.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.deepspring.lib.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Anonym on 2017/9/9.
 */

public abstract class BaseActivity extends AppCompatActivity {

    private Unbinder mUnbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(setLayout());
        mUnbinder = ButterKnife.bind(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                Intent intent = new Intent(this,AboutActivity.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mUnbinder !=Unbinder.EMPTY){
            mUnbinder.unbind();
        }
    }

    @Override
    public abstract boolean onCreateOptionsMenu(Menu menu);

    public abstract int setLayout();

}