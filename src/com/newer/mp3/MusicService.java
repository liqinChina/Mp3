package com.newer.mp3;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.newer.mp3.MusicService;
import com.newer.mp3.SongActivity;
import com.newer.mp3.Song;
import com.newer.mp3.Util;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;

public class MusicService extends Service implements OnCompletionListener {

	private static final String TAG = "MusicService";

	public static final String ACTION = "com.newer.mp3.ACTION_MUSIC";
	public static final String ACTION_BC_LOADED = "com.newer.mp3.BC_LOADED";
	public static final String ACTION_BC_PLAY_NEXT = "com.newer.mp3.BC_PLAY_NEXT";

	private static final int ID_PLAYING = 1;

	// 播放器
	private MediaPlayer player;

	// 音乐列表
	private ArrayList<Song> songList;

	private LocalBinder localBinder = new LocalBinder();

	// 正在播放的曲目
	private int currentSongIndex;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");

		player = new MediaPlayer();
		player.setOnCompletionListener(this);

		songList = new ArrayList<Song>();

		// 初始化音乐列表
		new SongListLoader().start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		return localBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "onUnbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");

		player.release();
	}

	/**
	 * 循环播放
	 * 
	 * @see android.media.MediaPlayer.OnCompletionListener#onCompletion(android.media.MediaPlayer)
	 */
	@Override
	public void onCompletion(MediaPlayer mp) {
		playNext();

		sendBroadcast(new Intent(ACTION_BC_PLAY_NEXT));
	}

	// ------------------------------------------
	// 业务逻辑
	// ------------------------------------------
	/**
	 * 获得音乐列表
	 * 
	 * @return
	 */
	public ArrayList<Song> getSongList() {
		Log.d(TAG, "getSongList");

		return songList;
	}

	public int getCurrentSongIndex() {
		return currentSongIndex;
	}

	/**
	 * 获得正在播放的曲目
	 * 
	 * @return
	 */
	public Song getCurrentSong() {
		return songList.get(currentSongIndex);
	}

	/**
	 * 获得音乐时长
	 * 
	 * @return
	 */
	public int getDuration() {
		return player.getDuration();
	}

	/**
	 * 获得当前播放的位置
	 * 
	 * @return
	 */
	public int getCurrentPosition() {
		return player.getCurrentPosition();
	}

	/**
	 * 播放当前曲目
	 */
	public boolean play() {
		Log.d(TAG, "play");
		return play(currentSongIndex);
	}

	/**
	 * 播放制定的音乐
	 * 
	 * @param position
	 */
	public boolean play(int position) {

		Song song = songList.get(position);

		if (position != currentSongIndex) {
			Log.d(TAG, "play " + position);

			songList.get(currentSongIndex).setPlaying(false);
			currentSongIndex = position;

			song.setPlaying(true);

			try {
				player.reset();
				player.setDataSource(song.getPath());
				player.prepare();
				player.start();

				sendNotification(song);

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			if (player.isPlaying()) {
				Log.d(TAG, "pause " + position);
				player.pause();

				stopForeground(true);
				return false;
			} else {
				Log.d(TAG, "start " + position);
				player.start();

				sendNotification(song);
			}
		}

		return true;
	}

	/**
	 * 发送通知
	 * 
	 * @param song
	 */
	private void sendNotification(Song song) {
		Notification notification = new Notification(
				android.R.drawable.ic_media_play, song.getTitle(),
				System.currentTimeMillis());

		Intent intent = new Intent(getApplicationContext(), SongActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		PendingIntent contentIntent = PendingIntent.getActivity(
				getApplicationContext(), 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		
		notification.setLatestEventInfo(getApplicationContext(),
				song.getTitle(), song.getArtist(), contentIntent);

		startForeground(ID_PLAYING, notification);
	}

	/**
	 * 播放下一首
	 */
	public void playNext() {
		Log.d(TAG, "playNext");
		int position;

		if (currentSongIndex == songList.size() - 1) {
			position = 0;
		} else {
			position = currentSongIndex + 1;
		}

		play(position);
	}

	/**
	 * 播放下一首
	 */
	public void playPrevious() {
		Log.d(TAG, "playPrevious");
		int position;

		if (currentSongIndex == 0) {
			position = songList.size() - 1;
		} else {
			position = currentSongIndex - 1;
		}

		play(position);
	}

	public void seekTo(int progress) {
		player.seekTo(progress);
	}

	/**
	 * 获得专辑的封面
	 * 
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Bitmap getCover() throws FileNotFoundException, IOException {

		// 专辑的ID
		int id = songList.get(currentSongIndex).getAlbumId();

		Uri uri = Uri.parse("content://media/external/audio/albumart");

		return android.provider.MediaStore.Images.Media.getBitmap(
				getContentResolver(),
				Uri.withAppendedPath(uri, String.valueOf(id)));
	}

	/**
	 * 
	 * @author wtao
	 * 
	 */
	class LocalBinder extends Binder {

		MusicService getService() {
			return MusicService.this;
		}
	}

	/**
	 * 
	 * @author wtao
	 * 
	 */
	class SongListLoader extends Thread {

		@Override
		public void run() {
			String[] projection = { Media._ID, Media.TITLE, Media.ARTIST,
					Media.ALBUM, Media.DATA, Media.DURATION, Media.ALBUM_ID };

			Cursor cursor = getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
					null, null, Media.ALBUM);

//			try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}

			while (cursor.moveToNext()) {
				int id = cursor.getInt(0);
				String title = cursor.getString(1);
				String artist = cursor.getString(2);
				String album = cursor.getString(3);
				String path = cursor.getString(4);
				String duration = cursor.getString(5);
				int albumId = cursor.getInt(6);

				Song song = new Song(id, title, artist, album,
						Util.formatTime(duration), path);
				song.setAlbumId(albumId);

				songList.add(song);
			}
			cursor.close();

			sendBroadcast(new Intent(ACTION_BC_LOADED));

			Log.d(TAG, "SongListLoader: " + songList.toString());
		}
	}

}
