package us.sauerkrause.lolightlocker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements SensorEventListener {
    private TextView mLightValue = null;
    private SensorManager mSensorManager = null;
    private Sensor mLightSensor;
    
    private void handleLightLevel(float level) {
        if(mLightValue != null) {
            mLightValue.setText(Float.toString(level));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLightValue = (TextView)findViewById(R.id.light_value);
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if(mSensorManager != null && mLightSensor != null) {
            mSensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent service = new Intent();
        service.setComponent(new ComponentName(this, LightMonitorService.class));
        startService(service);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(this, SettingsActivity.class));
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        if(mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        if(mLightSensor != null) {
            mLightSensor = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        handleLightLevel(event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
