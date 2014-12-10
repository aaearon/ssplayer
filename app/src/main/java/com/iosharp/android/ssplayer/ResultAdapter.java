package com.iosharp.android.ssplayer;

import android.content.Context;
import android.database.Cursor;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class ResultAdapter extends CursorAdapter {
    public static final String TIME_FORMAT = "EEE MMM dd HH:mm";

    public ResultAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View retView = inflater.inflate(R.layout.search_result_row, parent, false);
        return retView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String quality = cursor.getString(SearchableActivity.COL_EVENT_QUALITY);
        String language = cursor.getString(SearchableActivity.COL_EVENT_LANGUAGE);
        String title = cursor.getString(SearchableActivity.COL_EVENT_NAME);
        String category = cursor.getString(SearchableActivity.COL_EVENT_CATEGORY);
        long startDate = cursor.getLong(SearchableActivity.COL_EVENT_START_DATE);
        String channel = String.format("%02d", cursor.getInt(SearchableActivity.COL_EVENT_CHANNEL));


        SpannableString qualitySpannableString = new SpannableString("");
        SpannableString languageSpannableString = new SpannableString("");

        if (!language.equals("")) {
            languageSpannableString = Utils.getLanguageImg(context, language);
        }
        if (quality.equalsIgnoreCase("720p")) {
            qualitySpannableString = Utils.getHighDefBadge();
        }

        TextView eventTitle = (TextView) view.findViewById(R.id.result_name);
        eventTitle.setText(TextUtils.concat(title, languageSpannableString, qualitySpannableString));

        TextView eventChannel = (TextView) view.findViewById(R.id.result_channel);
        eventChannel.setText("CH: " + channel);

        TextView eventDate = (TextView) view.findViewById(R.id.result_time);
        eventDate.setText(Utils.formatLongToString(startDate, TIME_FORMAT));

        TextView eventCategory = (TextView) view.findViewById(R.id.result_category);
        eventCategory.setText(category);
    }
}
