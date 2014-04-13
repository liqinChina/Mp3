package com.newer.mp3;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.newer.mp3.Util;
import com.newer.mp3.MusicService.LocalBinder;
import com.newer.mp3.Song;
import com.newer.mp3.MusicService;
import com.newer.mp3.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SongActivity extends Activity implements OnSeekBarChangeListener {

	private SeekBar seekBar;
	private TextView textViewTitle;
	private TextView textViewArtist;
	private TextView textViewAlbum;
	private TextView textViewPosition;
	private TextView textViewDuration;

	private Button buttonPlay;

	private ImageView imageViewCover;

	private MusicService musicService;
	private boolean isBound;

	// 服务连接
	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			musicService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			musicService = ((LocalBinder) service).getService();
			isBound = true;
			updateSong(musicService.getCurrentSong());
		}
	};

	// UI 更新
	private Runnable actionUpdate = new Runnable() {

		@Override
		public void run() {
			if (isBound) {
				seekBar.setMax(musicService.getDuration());

				int position = musicService.getCurrentPosition();
				seekBar.setProgress(position);

				textViewPosition.setText(Util.formatTime(String
						.valueOf(position)));
			}

			seekBar.postDelayed(this, 1000);
		}
	};

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(MusicService.ACTION_BC_PLAY_NEXT)) {
				updateSong(musicService.getCurrentSong());
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_song);

		seekBar = (SeekBar) findViewById(R.id.seekBar);
		textViewTitle = (TextView) findViewById(R.id.textView_title);
		textViewArtist = (TextView) findViewById(R.id.textView_artist);
		textViewAlbum = (TextView) findViewById(R.id.textView_album);
		textViewPosition = (TextView) findViewById(R.id.textView_position);
		textViewDuration = (TextView) findViewById(R.id.textView_duration);
		buttonPlay = (Button) findViewById(R.id.button_play);
		imageViewCover = (ImageView) findViewById(R.id.imageView_cover);
		seekBar.setOnSeekBarChangeListener(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		bindService(new Intent(MusicService.ACTION), conn, BIND_AUTO_CREATE);
	}

	@Override
	protected void onResume() {
		super.onResume();

		seekBar.post(actionUpdate);

		IntentFilter filter = new IntentFilter();
		filter.addAction(MusicService.ACTION_BC_PLAY_NEXT);
		registerReceiver(receiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();

		seekBar.removeCallbacks(actionUpdate);

		unregisterReceiver(receiver);
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindService(conn);
	}

	/**
	 * 按钮事件处理
	 * 
	 * @param v
	 */
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_play:
			if (musicService.play()) {
				buttonPlay.setText(getText(R.string.pause));
			} else {
				buttonPlay.setText(getText(R.string.start));
			}
			break;

		case R.id.button_next:
			musicService.playNext();
			updateSong(musicService.getCurrentSong());
			break;

		case R.id.button_previous:
			musicService.playPrevious();
			updateSong(musicService.getCurrentSong());
			break;
		}
	}

	private void updateSong(Song currentSong) {
		textViewTitle.setText(currentSong.getTitle());
		textViewArtist.setText(currentSong.getArtist());
		textViewAlbum.setText(currentSong.getAlbum());
		textViewDuration.setText(currentSong.getDuration());
		buttonPlay.setText(getText(R.string.pause));
		
		try {
			imageViewCover.setImageBitmap(musicService.getCover());
		} catch (FileNotFoundException e) {
			imageViewCover.setImageResource(R.drawable.albumart_mp_unknown);
		} catch (IOException e) {
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.widget.SeekBar.OnSeekBarChangeListener#onProgressChanged(android.widget.SeekBar,
	 *      int, boolean)
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {

		if (fromUser) {
			musicService.seekTo(progress);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

}
