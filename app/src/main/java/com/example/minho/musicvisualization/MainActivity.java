package com.example.minho.musicvisualization;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.minho.musicvisualization.R;

public class MainActivity extends Activity {
    private ListView mListView;
    private MusicAdapter mMusicAdapter;

    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private Equalizer mEqualizer;
    int band1, band2, band3, band4, band5, band6, band7;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Layout¿∏∑Œ ∫Œ≈Õ ListViewø° ¥Î«— ∞¥√º∏¶ æÚ¥¬¥Ÿ. */
        mListView = (ListView)findViewById(R.id.mylist);

        mMusicAdapter = new MusicAdapter(this);

        mListView.setAdapter(mMusicAdapter);

        /* Listener for selecting a item */
        mListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
                Uri musicURI = Uri.withAppendedPath(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + mMusicAdapter.getMusicID(position));

                playMusic(musicURI);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /* Release resources allocated to player */
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    public void playMusic(Uri uri) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(this, uri);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            int sessionId = mMediaPlayer.getAudioSessionId();
            try {
                mEqualizer = null;
                mEqualizer = new Equalizer(0, sessionId );
                mEqualizer.setEnabled(true);
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
            }
            band1 = mEqualizer.getBand(40);
            band2 = mEqualizer.getBand(100);
            band3 = mEqualizer.getBand(200);
            band4 = mEqualizer.getBand(2000);
            band5 = mEqualizer.getBand(9000);
            band6 = mEqualizer.getBand(10000);
            band7 = mEqualizer.getBand(20000);


            mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    // TODO
                    // Do something when playing is completed
                }
            });

        } catch (IOException e) {
            Log.v("SimplePlayer", e.getMessage());
        }
    }

    public void playMusic(String path) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            int sessionId = mMediaPlayer.getAudioSessionId();
            try {
                mEqualizer = null;
                mEqualizer = new Equalizer(0, sessionId );
                mEqualizer.setEnabled(true);
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
            }
            band1 = mEqualizer.getBand(40);
            band2 = mEqualizer.getBand(100);
            band3 = mEqualizer.getBand(200);
            band4 = mEqualizer.getBand(2000);
            band5 = mEqualizer.getBand(9000);
            band6 = mEqualizer.getBand(10000);
            band7 = mEqualizer.getBand(20000);

            mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    // Do something when playing is completed
                }
            });

        } catch (IOException e) {
            Log.v("SimplePlayer", e.getMessage());
        }
    }


    /**==========================================
     *              Adapter class
     * ==========================================*/
    public class MusicAdapter extends BaseAdapter {
        private ArrayList<String> mMusicIDList;
        private ArrayList<String> mAlbumartIDList;
        private ArrayList<String> mMusiceTitleList;
        private ArrayList<String> mSingerList;
        private Context mContext;
        MusicAdapter(Context c){
            mContext = c;
            mMusicIDList = new ArrayList<String>();
            mAlbumartIDList = new ArrayList<String>();
            mMusiceTitleList = new ArrayList<String>();
            mSingerList = new ArrayList<String>();
            getMusicInfo();
        }
        public boolean deleteSelected(int sIndex){
            return true;
        }

        public int getCount() {
            return mMusicIDList.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public int getMusicID(int position)
        {
            return Integer.parseInt((mMusicIDList.get(position)));
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View listViewItem = convertView;
            if (listViewItem == null) {
                /* Item.xml¿ª Inflate«ÿ Layout ±∏º∫µ» View∏¶ æÚ¥¬¥Ÿ. */
                LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                listViewItem = vi.inflate(R.layout.item, null);
            }
            /* Album Art Bitmap¿ª æÚ¥¬¥Ÿ. */
            ImageView iv = (ImageView) listViewItem.findViewById(R.id.albumart);
            Bitmap albumArt = MainActivity.getArtworkQuick(mContext, Integer.parseInt((mAlbumartIDList.get(position))), 50, 50);
            iv.setImageBitmap(albumArt);

            /* Title º≥¡§ */
            TextView tv = (TextView) listViewItem.findViewById(R.id.title);
            tv.setText(mMusiceTitleList.get(position));

            /* Singer º≥¡§ */
            TextView tv1 = (TextView) listViewItem.findViewById(R.id.singer);
            tv1.setText(mSingerList.get(position));

            /* ±∏º∫µ» ListView Item¿ª ∏Æ≈œ«ÿ ¡ÿ¥Ÿ. */
            return listViewItem;
        }

        private void getMusicInfo(){
            /* ¿Ã øπ¡¶ø°º≠¥¬ ¥‹º¯»˜ ID (Media¿« ID, Album¿« ID)∏∏ æÚ¥¬¥Ÿ.*/
            String[] proj = {MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST
            };

            Cursor musicCursor = managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    proj, null, null, null);

            if (musicCursor != null && musicCursor.moveToFirst()){
                String musicID;
                String albumID;
                String musicTitle;
                String singer;

                int musicIDCol = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int albumIDCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int musicTitleCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int singerCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                do {
                    musicID = musicCursor.getString(musicIDCol);
                    albumID = musicCursor.getString(albumIDCol);
                    musicTitle = musicCursor.getString(musicTitleCol);
                    singer = musicCursor.getString(singerCol);
                    /* Media IDøÕ Album ID∏¶ ∞¢∞¢¿« ∏ÆΩ∫∆Æø° ¿˙¿Â«ÿ µ–¥Ÿ
                     *
                     */
                    mMusicIDList.add(musicID);
                    mAlbumartIDList.add(albumID);
                    mMusiceTitleList.add(musicTitle);
                    mSingerList.add(singer);
                }while (musicCursor.moveToNext());
            }
            musicCursor.close();
            return;
        }
    }


    /* Album ID∑Œ ∫Œ≈Õ Bitmap¿ª ª˝º∫«ÿ ∏Æ≈œ«ÿ ¡÷¥¬ ∏ﬁº“µÂ */
    private static final BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

    // Get album art for specified album. This method will not try to
    // fall back to getting artwork directly from the file, nor will
    // it attempt to repair the database.
    private static Bitmap getArtworkQuick(Context context, int album_id, int w, int h) {
        // NOTE: There is in fact a 1 pixel frame in the ImageView used to
        // display this drawable. Take it into account now, so we don't have to
        // scale later.
        w -= 2;
        h -= 2;
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            ParcelFileDescriptor fd = null;
            try {
                fd = res.openFileDescriptor(uri, "r");
                int sampleSize = 1;

                // Compute the closest power-of-two scale factor
                // and pass that to sBitmapOptionsCache.inSampleSize, which will
                // result in faster decoding and better quality
                sBitmapOptionsCache.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, sBitmapOptionsCache);
                int nextWidth = sBitmapOptionsCache.outWidth >> 1;
                int nextHeight = sBitmapOptionsCache.outHeight >> 1;
                while (nextWidth>w && nextHeight>h) {
                    sampleSize <<= 1;
                    nextWidth >>= 1;
                    nextHeight >>= 1;
                }

                sBitmapOptionsCache.inSampleSize = sampleSize;
                sBitmapOptionsCache.inJustDecodeBounds = false;
                Bitmap b = BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, sBitmapOptionsCache);

                if (b != null) {
                    // finally rescale to exactly the size we need
                    if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                        Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
                        b.recycle();
                        b = tmp;
                    }
                }

                return b;
            } catch (FileNotFoundException e) {
            } finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
}