/*
 * Copyright (C) 2014 Thalmic Labs Inc.
 * Distributed under the Myo SDK license agreement. See LICENSE.txt for details.
 */

package com.thalmic.android.sample.helloworld;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.Vector3;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;

public class HelloWorldActivity extends Activity implements AdapterView.OnItemClickListener {

    private TextView mLockStateView;
    private TextView mTextView;

    //どのデータを閲覧しているか(待機状態 = -1, Quaternion = 0, Orientation = 1, Accelerometer = 2, Gyroscope = 3)
    int isGraph = -1;
    //データのグラフ表示に必要な関数
    private ListView itemList;
    String[] names = new String[]{"x-value or roll", "y-value or pitch", "z-value or yaw", "w-value"};
    int[] colors = new int[]{Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA};
    LineChart mChart;

    //リターンボタン
    private Button preview;

    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    private DeviceListener mListener = new AbstractDeviceListener() {

        // onConnect() is called whenever a Myo has been connected.
        @Override
        public void onConnect(Myo myo, long timestamp) {
            // Set the text color of the text view to cyan when a Myo connects.
            mTextView.setTextColor(Color.CYAN);
            mTextView.setText(R.string.sync);
            itemList.setEnabled(true);
        }

        // onDisconnect() is called whenever a Myo has been disconnected.
        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            // Set the text color of the text view to red when a Myo disconnects.
            mTextView.setTextColor(Color.RED);
            mTextView.setText(R.string.unsync);
            itemList.setEnabled(false);
        }

        // onArmSync() is called whenever Myo has recognized a Sync Gesture after someone has put it on their
        // arm. This lets Myo know which arm it's on and which way it's facing.
        @Override
        public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            mTextView.setText(myo.getArm() == Arm.LEFT ? R.string.arm_left : R.string.arm_right);
        }

        // onArmUnsync() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
        // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
        // when Myo is moved around on the arm.
        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
            mTextView.setText(R.string.hello_world);
        }

        // onUnlock() is called whenever a synced Myo has been unlocked. Under the standard locking
        // policy, that means poses will now be delivered to the listener.
        @Override
        public void onUnlock(Myo myo, long timestamp) {
            mLockStateView.setText(R.string.unlocked);
        }

        // onLock() is called whenever a synced Myo has been locked. Under the standard locking
        // policy, that means poses will no longer be delivered to the listener.
        @Override
        public void onLock(Myo myo, long timestamp) {
            mLockStateView.setText(R.string.locked);
        }

        // onOrientationData() is called whenever a Myo provides its current orientation,
        // represented as a quaternion.
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            //Quaternion.roll(rotation)だとラジアンが表示される
            //Math.toDegreesを使って角度表記に変更する

            if (isGraph == 0) {
                float[] values = {(float)rotation.x(), (float)rotation.y(), (float)rotation.z(), (float)rotation.w()};
                LineData data = mChart.getLineData();
                if (data != null) {
                    for (int i = 0; i < 4; i++) { // 3軸なのでそれぞれ処理します
                        ILineDataSet set = data.getDataSetByIndex(i);
                        if (set == null) {
                            set = createSet(names[i], colors[i]); // ILineDataSetの初期化は別メソッドにまとめました
                            data.addDataSet(set);
                        }

                        data.addEntry(new Entry(set.getEntryCount(), values[i]), i); // 実際にデータを追加する
                        data.notifyDataChanged();
                    }

                    mChart.notifyDataSetChanged(); // 表示の更新のために変更を通知する
                    mChart.setVisibleXRangeMaximum(50); // 表示の幅を決定する
                    mChart.moveViewToX(data.getEntryCount()); // 最新のデータまで表示を移動させる
                }
            }
            else if (isGraph == 1) {
                float[] values = {(float)Math.toDegrees(Quaternion.roll(rotation)), (float)Math.toDegrees(Quaternion.pitch(rotation)), (float)Math.toDegrees(Quaternion.yaw(rotation))};
                LineData data = mChart.getLineData();
                if (data != null) {
                    for (int i = 0; i < 3; i++) { // 3軸なのでそれぞれ処理します
                        ILineDataSet set = data.getDataSetByIndex(i);
                        if (set == null) {
                            set = createSet(names[i], colors[i]); // ILineDataSetの初期化は別メソッドにまとめました
                            data.addDataSet(set);
                        }

                        data.addEntry(new Entry(set.getEntryCount(), values[i]), i); // 実際にデータを追加する
                        data.notifyDataChanged();
                    }

                    mChart.notifyDataSetChanged(); // 表示の更新のために変更を通知する
                    mChart.setVisibleXRangeMaximum(50); // 表示の幅を決定する
                    mChart.moveViewToX(data.getEntryCount()); // 最新のデータまで表示を移動させる
                }
            }
        }

        // onAccelerometerData() is called when an attached Myo has provided new accelerometer data
        //単位は g (1G = 9.80665 m/s^2)
        @Override
        public void onAccelerometerData(Myo myo, long timestamp, Vector3 accel) {
            if (isGraph == 2) {
                float[] values = {(float)accel.x(), (float)accel.y(), (float)accel.z()};
                LineData data = mChart.getLineData();
                if (data != null) {
                    for (int i = 0; i < 3; i++) { // 3軸なのでそれぞれ処理します
                        ILineDataSet set = data.getDataSetByIndex(i);
                        if (set == null) {
                            set = createSet(names[i], colors[i]); // ILineDataSetの初期化は別メソッドにまとめました
                            data.addDataSet(set);
                        }

                        data.addEntry(new Entry(set.getEntryCount(), values[i]), i); // 実際にデータを追加する
                        data.notifyDataChanged();
                    }

                    mChart.notifyDataSetChanged(); // 表示の更新のために変更を通知する
                    mChart.setVisibleXRangeMaximum(50); // 表示の幅を決定する
                    mChart.moveViewToX(data.getEntryCount()); // 最新のデータまで表示を移動させる
                }
            }
        }

        // onGyroscopeData() is called when an attached Myo has provided new gyroscope data
        @Override
        public void onGyroscopeData(Myo myo, long timestamp, Vector3 gyro) {
            if (isGraph == 3) {
                float[] values = {(float)gyro.x(), (float)gyro.y(), (float)gyro.z()};
                LineData data = mChart.getLineData();
                if (data != null) {
                    for (int i = 0; i < 3; i++) { // 3軸なのでそれぞれ処理します
                        ILineDataSet set = data.getDataSetByIndex(i);
                        if (set == null) {
                            set = createSet(names[i], colors[i]); // ILineDataSetの初期化は別メソッドにまとめました
                            data.addDataSet(set);
                        }

                        data.addEntry(new Entry(set.getEntryCount(), values[i]), i); // 実際にデータを追加する
                        data.notifyDataChanged();
                    }

                    mChart.notifyDataSetChanged(); // 表示の更新のために変更を通知する
                    mChart.setVisibleXRangeMaximum(50); // 表示の幅を決定する
                    mChart.moveViewToX(data.getEntryCount()); // 最新のデータまで表示を移動させる
                }
            }
        }

        // onPose() is called whenever a Myo provides a new pose.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Handle the cases of the Pose enumeration, and change the text of the text view
            // based on the pose we receive.
            switch (pose) {
                case UNKNOWN:
                    mTextView.setText(getString(R.string.hello_world));
                    break;
                case REST:
                case DOUBLE_TAP:
                    int restTextId = R.string.hello_world;
                    switch (myo.getArm()) {
                        case LEFT:
                            restTextId = R.string.arm_left;
                            break;
                        case RIGHT:
                            restTextId = R.string.arm_right;
                            break;
                    }
                    mTextView.setText(getString(restTextId));
                    break;
                case FIST:
                    mTextView.setText(getString(R.string.pose_fist));
                    break;
                case WAVE_IN:
                    mTextView.setText(getString(R.string.pose_wavein));
                    break;
                case WAVE_OUT:
                    mTextView.setText(getString(R.string.pose_waveout));
                    break;
                case FINGERS_SPREAD:
                    mTextView.setText(getString(R.string.pose_fingersspread));
                    break;
            }

            if (pose != Pose.UNKNOWN && pose != Pose.REST) {
                // Tell the Myo to stay unlocked until told otherwise. We do that here so you can
                // hold the poses without the Myo becoming locked.
                myo.unlock(Myo.UnlockType.HOLD);

                // Notify the Myo that the pose has resulted in an action, in this case changing
                // the text on the screen. The Myo will vibrate.
                myo.notifyUserAction();
            } else {
                // Tell the Myo to stay unlocked only for a short period. This allows the Myo to
                // stay unlocked while poses are being performed, but lock after inactivity.
                myo.unlock(Myo.UnlockType.TIMED);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_world);

        mLockStateView = (TextView) findViewById(R.id.lock_state);
        mTextView = (TextView) findViewById(R.id.text);

        itemList = (ListView)findViewById(R.id.items);
        itemList.setOnItemClickListener(this);
        itemList.setEnabled(false);

        // First, we initialize the Hub singleton with an application identifier.
        Hub hub = Hub.getInstance();
        if (!hub.init(this, getPackageName())) {
            // We can't do anything with the Myo device if the Hub can't be initialized, so exit.
            Toast.makeText(this, "Couldn't initialize Hub", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // We don't want any callbacks when the Activity is gone, so unregister the listener.
        Hub.getInstance().removeListener(mListener);

        if (isFinishing()) {
            // The Activity is finishing, so shutdown the Hub. This will disconnect from the Myo.
            Hub.getInstance().shutdown();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (R.id.action_scan == id) {
            onScanActionSelected();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onScanActionSelected() {
        // Launch the ScanActivity to scan for Myos to connect to.
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }


    //グラフ描画の準備用メソッド
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setContentView(R.layout.activity_show_data);
        isGraph = position;
        preview = (Button) findViewById(R.id.preview);
        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setScreenMain();
            }
        });
        mChart = (LineChart) findViewById(R.id.lineChart);
        mChart.setBackgroundColor(Color.WHITE);
        switch(isGraph) {
            case 0:
                mChart.setDescription("Quaternion"); // 表のタイトル
                break;
            case 1:
                mChart.setDescription("Orientation");
                break;
            case 2:
                mChart.setDescription("Accelerometer");
                break;
            case 3:
                mChart.setDescription("Gyroscope");
                break;
        }
        mChart.setData(new LineData()); // 空のLineData型インスタンスを追加
    }

    //グラフの諸設定
    private LineDataSet createSet(String label, int color) {
        LineDataSet set = new LineDataSet(null, label);
        set.setLineWidth(2.5f); // 線の幅を指定
        set.setColor(color); // 線の色を指定
        set.setDrawCircles(false); // ポイントごとの円を表示しない
        set.setDrawValues(false); // 値を表示しない

        return set;
    }

    //Returnボタンが押されたとき用メソッド
    private void setScreenMain() {
        setContentView(R.layout.activity_hello_world);
        isGraph = -1;
        itemList = (ListView)findViewById(R.id.items);
        itemList.setOnItemClickListener(this);
        mLockStateView = (TextView) findViewById(R.id.lock_state);
        mTextView = (TextView) findViewById(R.id.text);
        mTextView.setTextColor(Color.CYAN);
        mTextView.setText(R.string.sync);
    }
}
