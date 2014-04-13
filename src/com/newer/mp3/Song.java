package com.newer.mp3;

public class Song {

	private boolean isPlaying;

	// 编号
	private int id;

	// 歌名
	private String title;

	// 歌手
	private String artist;

	// 专辑
	private String album;

	// 时长
	private String duration;

	// 文件路径
	private String path;

	private int albumId;

	public int getAlbumId() {
		return albumId;
	}

	public void setAlbumId(int albumId) {
		this.albumId = albumId;
	}

	public Song() {
	}

	public Song(int id, String title, String artist, String album,
			String duration, String path) {
		super();
		this.id = id;
		this.title = title;
		this.artist = artist;
		this.album = album;
		this.duration = duration;
		this.path = path;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public void setPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	@Override
	public String toString() {
		return "Song [id=" + id + ", title=" + title + ", artist=" + artist
				+ ", album=" + album + ", duration=" + duration + ", path="
				+ path + "]";
	}

}
