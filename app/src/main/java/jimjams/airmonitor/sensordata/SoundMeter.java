
/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jimjams.airmonitor.sensordata;


import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;

import jimjams.airmonitor.database.DBAccess;


public class SoundMeter {

    /**
     * Database access
     */
    private DBAccess access = DBAccess.getDBAccess();
    /**
     * Reference to the SensorGenerator used to get sensor data
     */
    SensorDataGenerator generator = SensorDataGenerator.getInstance();
    String data;

    static final private double EMA_FILTER = 0.6;

    private MediaRecorder mRecorder = null;
    private double mEMA = 0.0;

    private static String mFileName = null;

    private int lastSL = 0;

    public SoundMeter() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
    }

    public void start() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(mFileName);
            try {
                mRecorder.prepare();
                Log.d("Start Audio errror: ", "NO error");
            }catch(IOException e){
                //
                Log.d("Start Audio errror: ", e.toString());
            }
            mRecorder.start();
            mEMA = 0.0;
        }
    }

    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public int getAmplitude() {
        if (mRecorder != null){
            // PREPARE sound data for database entry [DESCRIPTION:VALUE:UNITS;]
            int soundLevel =  mRecorder.getMaxAmplitude();
            if(soundLevel != 0) {
                soundLevel = (int) (Math.log((double) soundLevel) * 6.9);
                data = "SoundLevel:" + Integer.toString(soundLevel) + ":DB;";
                access.updateCurrentData(generator.getData(data));
                lastSL = soundLevel;
            }else{
                access.updateCurrentData(generator.getData("SoundLevel:" + lastSL + ":DB;"));
            }
            //Log.d("tag", data);

            return soundLevel;
        }else {
            return 0;
        }
    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }
}