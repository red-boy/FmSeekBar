package com.haoda.togglebutton;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ToggleButton tg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tg = ((ToggleButton) findViewById(R.id.tg));
        tg.setOnToggleButtonChangeListener(new ToggleButton.OnToggleButtonChangeListener() {
            @Override
            public void OnToggleChange(boolean isOpen) {
                if (isOpen) {
                    Toast.makeText(MainActivity.this, "打开了", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this, "关闭了", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
