package us.sauerkrause.lolightlocker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

/**
 * Created by krause on 8/28/15.
 */
public class LightMonitorService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "LightMonitorService";
    private static final boolean DEBUG = false;
    private SensorManager mSensors = null;
    private Sensor mLight = null;
    private PowerManager mPower = null;
    private SharedPreferences mPreferences = null;
    private PowerManager.WakeLock mWakeLock = null;
    private static final float DEFAULT_THRESHOLD = 10.0f;
    private float mThreshold = -1.0f;
    /*package*/ static final String THRESHOLD_KEY = "threshold";

    private SensorEventListener mSensorListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            handleLightLevel(event.values[0]);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ret = super.onStartCommand(intent, flags, startId);
        // Some stuff
        if(mPower == null) {
            mPower = (PowerManager)getSystemService(Context.POWER_SERVICE);
        }
        if(mSensors == null) {
            mSensors = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        }

        mPreferences = getSharedPreferences("config", Context.MODE_MULTI_PROCESS);
        mPreferences.registerOnSharedPreferenceChangeListener(this);
        if(mPower != null && mSensors != null) {
            handlePowerManagerAcquired();
        }
        return ret;
    }

    private void handlePowerManagerAcquired() {
        if(mLight == null)
            mLight = mSensors.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensors.registerListener(mSensorListener, mLight, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private synchronized float getThreshold() {
        if(mThreshold < 0.0f)
            refreshThreshold();
        return mThreshold;
    }

    private synchronized void refreshThreshold() {
        mThreshold = mPreferences.getFloat(THRESHOLD_KEY, DEFAULT_THRESHOLD);
    }

    private synchronized void handleLightLevel(float level) {
        float threshold = getThreshold();
        if(DEBUG)
            Log.d(TAG, String.format("Light level: %f", level));
        if(mWakeLock == null) {
            mWakeLock = mPower.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "wakeywakey");
        }

        if(level > threshold) {
            if(!mWakeLock.isHeld()) {
                mWakeLock.acquire();
                Log.d(TAG, String.format("Lock acquired. %f > %f", level, threshold));
            }
        } else {
            if(mWakeLock.isHeld()) {
                mWakeLock.release();
                mWakeLock = null;
                Log.d(TAG, String.format("Lock released. %f <= %f", level, threshold));
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // De-acquire managers and locks and listeners
        if(mSensors != null) {
            mSensors.unregisterListener(mSensorListener);
        }
        if(mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        if(mPreferences != null) {
            mPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(THRESHOLD_KEY))
            refreshThreshold();
    }
}
