package com.newer.mp3;

import java.util.ArrayList;

import com.newer.mp3.MusicService;
import com.newer.mp3.R;
import com.newer.mp3.Song;
import com.newer.mp3.SongActivity;
import com.newer.mp3.SongListAdapter;
import com.newer.mp3.MusicService.LocalBinder;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends Activity implements OnItemClickListener {

	protected static final String TAG = "MainActivity";

	private MusicService musicService;
	private boolean isBound;

	private ListView listView;
	private SongListAdapter adapter;
	private ArrayList<Song> songList;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (isBound) {
				loadSongList();
			}
		}
	};

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			musicService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			musicService = ((LocalBinder) service).getService();
			isBound = true;
			Log.d(TAG, "onServiceConnected");

			loadSongList();
		}

	};

	private void loadSongList() {
		songList = musicService.getSongList();
		adapter = new SongListAdapter(getApplicationContext(), songList);
		listView.setAdapter(adapter);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG, "onCreate");

		listView = (ListView) findViewById(R.id.listView);
		listView.setOnItemClickListener(this);

		// 启动、创建服务
		startService(new Intent(MusicService.ACTION));
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");

		bindService(new Intent(MusicService.ACTION), conn, BIND_AUTO_CREATE);

		IntentFilter filter = new IntentFilter(MusicService.ACTION_BC_LOADED);
		registerReceiver(receiver, filter);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");

		unbindService(conn);
		unregisterReceiver(receiver);
	}

	/**
	 * 点击音乐列表中的曲目
	 * 
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView,
	 *      android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		if (position != musicService.getCurrentSongIndex()) {
			musicService.play(position);
		}

		startActivity(new Intent(this, SongActivity.class));
		finish();
	}

}
