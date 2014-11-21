package com.iosharp.android.ssplayer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.widgets.MiniController;
import com.iosharp.android.ssplayer.db.ChannelContract;
import com.iosharp.android.ssplayer.db.DbHelper;
import com.iosharp.android.ssplayer.videoplayer.VideoActivity;

import static com.iosharp.android.ssplayer.db.ChannelContract.*;
import static com.iosharp.android.ssplayer.db.ChannelContract.ChannelEntry;

public class ChannelListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int CURSOR_LOADER_ID = 0;

    private static final String[] CHANNEL_EVENT_COLUMNS = {
            ChannelEntry.TABLE_NAME + "." + ChannelEntry._ID,
            ChannelEntry.TABLE_NAME + "." + ChannelEntry.COLUMN_NAME,
            ChannelEntry.TABLE_NAME + "." + ChannelEntry.COLUMN_ICON,
            EventEntry.TABLE_NAME + "." + EventEntry._ID,
            EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_NAME,
            "MIN(" + EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_START_DATE +") AS " +
                    EventEntry.COLUMN_START_DATE,
            EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_END_DATE,
            EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_QUALITY,
    };

    // Indices tied to CHANNEL_COLUMNS
    public static final int COL_CHANNEL_ID = 0;
    public static final int COL_CHANNEL_NAME = 1;
    public static final int COL_CHANNEL_ICON = 2;
    public static final int COL_EVENT_ID = 3;
    public static final int COL_EVENT_NAME = 4;
    public static final int COL_EVENT_START_DATE = 5;
    public static final int COL_EVENT_END_DATE = 6;
    public static final int COL_EVENT_QUALITY = 7;

    private ChannelAdapter mAdapter;
    private MiniController mMini;
    private VideoCastManager mCastManager;
    private int mChannelId;

    public ChannelListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCastManager = PlayerApplication.getCastManager(getActivity());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
    }

    public void handleNavigation(Context c, MediaInfo info) {
        if (mCastManager != null) {
            if (mCastManager.isConnected()) {
                mCastManager.startCastControllerActivity(c, info, 0, true);
            }
        } else {
            Intent intent = new Intent(c, VideoActivity.class);
            intent.putExtra("media", com.google.sample.castcompanionlibrary.utils.Utils.fromMediaInfo(info));
            intent.putExtra("channel", mChannelId);
            c.startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mCastManager != null) {
            mCastManager.incrementUiCounter();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mCastManager != null) {
            mCastManager.decrementUiCounter();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // MiniController
        if (mCastManager != null) {
            mMini = (MiniController) rootView.findViewById(R.id.miniController1);
            mCastManager.addMiniController(mMini);
        }

        ListView listView = (ListView) rootView.findViewById(R.id.listview);
        mAdapter = new ChannelAdapter(getActivity(), null);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor c = (Cursor) mAdapter.getItem(position);
                c.moveToPosition(position);

                // Get channel details
                mChannelId = c.getInt(c.getColumnIndex(ChannelEntry._ID));
                String channelName = c.getString(c.getColumnIndex(ChannelEntry.COLUMN_NAME));
                String channelIcon = c.getString(c.getColumnIndex(ChannelEntry.COLUMN_ICON));

                if (Utils.checkForSetServiceCredentials(getActivity())) {
                    // Create MediaInfo based off channel
                    String url = Utils.getStreamUrl(getActivity(), mChannelId);
                    MediaInfo mediaInfo = Utils.buildMediaInfo(channelName, "SmoothStreams", url, channelIcon);

                    // Pass to handleNavigation
                    handleNavigation(getActivity(), mediaInfo);
                } else {
                    // Launch settings
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    getActivity().startActivity(intent);
                    Toast.makeText(getActivity(),
                            "ERROR: No login credentials found! Set your login and password first.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortOrder =  ChannelEntry.TABLE_NAME + "." + ChannelEntry._ID +
                ", " + EventEntry.TABLE_NAME + "." + EventEntry.COLUMN_START_DATE;

        return new CursorLoader(getActivity(), ChannelEntry.CONTENT_URI, CHANNEL_EVENT_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }
}


