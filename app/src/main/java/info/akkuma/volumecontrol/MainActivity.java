package info.akkuma.volumecontrol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;


public class MainActivity extends Activity {

    private MyArrayAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView) findViewById(R.id.listView);
        mAdapter = new MyArrayAdapter(this, VolumeItems.values());
        listView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    private enum VolumeItems {
        VOICE_CALL(AudioManager.STREAM_VOICE_CALL, R.string.label_voice_call, android.R.color.primary_text_light),
        MUSIC(AudioManager.STREAM_MUSIC, R.string.label_music, android.R.color.primary_text_light),
        ALARM(AudioManager.STREAM_ALARM, R.string.label_alarm, android.R.color.primary_text_light),
        RING(AudioManager.STREAM_RING, R.string.label_ring, android.R.color.secondary_text_light),
        NOTIFICATION(AudioManager.STREAM_NOTIFICATION, R.string.label_notification, android.R.color.secondary_text_light),
        SYSTEM(AudioManager.STREAM_SYSTEM, R.string.label_system, android.R.color.tertiary_text_light),
        DTMF(AudioManager.STREAM_DTMF, R.string.label_dtmf, android.R.color.tertiary_text_light);

        private final int streamId;
        private final int labelId;
        private final int colorId;

        VolumeItems(int streamId, int labelId, int colorId) {
            this.streamId = streamId;
            this.labelId = labelId;
            this.colorId = colorId;
        }
    }

    private static class MyArrayAdapter extends ArrayAdapter<VolumeItems> implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

        private AudioManager mAudioManager;

        public MyArrayAdapter(Context context, VolumeItems[] objects) {
            super(context, R.layout.list_item_volume, R.id.label, objects);
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            VolumeItems item = getItem(position);

            TextView label = (TextView) view.findViewById(R.id.label);
            label.setText(item.labelId);
            label.setTextColor(getContext().getResources().getColor(item.colorId));
            label.setTag(item);
            label.setOnClickListener(this);

            SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar);
            int max = mAudioManager.getStreamMaxVolume(item.streamId);
            seekBar.setMax(max);
            int volume = mAudioManager.getStreamVolume(item.streamId);
            seekBar.setProgress(volume);
            seekBar.setTag(item);
            seekBar.setOnSeekBarChangeListener(this);

            return view;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) return;
            VolumeItems item = (VolumeItems) seekBar.getTag();
            if (item == null) return;
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean playSound = pref.getBoolean(getContext().getString(R.string.pref_key_play_sound), false);

            mAudioManager.setStreamMute(item.streamId, false);
            mAudioManager.setStreamVolume(item.streamId, progress, playSound ? AudioManager.FLAG_PLAY_SOUND : 0);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            notifyDataSetChanged();
        }

        @Override
        public void onClick(View v) {
            VolumeItems item = (VolumeItems) v.getTag();
            if (item == null) return;
            int current = mAudioManager.getStreamVolume(item.streamId);
            mAudioManager.setStreamVolume(item.streamId, current, AudioManager.FLAG_SHOW_UI);
        }
    }
}
