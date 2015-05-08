
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
    private SensorDataGenerator generator = SensorDataGenerator.getInstance();
    private String data;

    private MediaRecorder mRecorder = null;

    private static String mFileName = null;

    private int lastSL = 0;
    private int soundLevel, soundLevelReturn;

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
            }catch(IOException e){
            }
            mRecorder.start();
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
            soundLevel =  mRecorder.getMaxAmplitude();
            soundLevelReturn = soundLevel;
            if(soundLevel != 0) {
                soundLevel = (int) (Math.log((double) soundLevel) * access.getBluetoothCalibration());
                data = "SoundLevel:" + Integer.toString(soundLevel) + ":DB;";
                access.updateCurrentData(generator.getData(data));
                lastSL = soundLevel;
            }else{
                access.updateCurrentData(generator.getData("SoundLevel:" + lastSL + ":DB;"));
            }
            return soundLevelReturn;
        }else {
            return 0;
        }
    }
}