package com.example.android.psgameinventory;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.psgameinventory.data.GameContract.GameEntry;

import static com.example.android.psgameinventory.R.id.console;
import static com.example.android.psgameinventory.R.id.price_view;

public class GameCursorAdapter extends CursorAdapter {

    public GameCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);
        TextView genreTextView = (TextView) view.findViewById(R.id.genre);
        TextView consoleTextView= (TextView)view.findViewById(console);
        TextView priceTextView= (TextView)view.findViewById(price_view);


        // Find the columns of pet attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_NAME);
        int gameColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_STOCK);
        int priceColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_PRICE);
        int genreColumnIndex=cursor.getColumnIndex(GameEntry.COLUMN_GAME_GENRE);
        int consoleColumnIndex=cursor.getColumnIndex(GameEntry.COLUMN_GAME_CONSOLE);



        // Read the pet attributes from the Cursor for the current pet
        String gameName = cursor.getString(nameColumnIndex);
        String gameStock = cursor.getString(gameColumnIndex);
        String gamePrice = cursor.getString(priceColumnIndex);
        String genre = cursor.getString(genreColumnIndex);
        String console = cursor.getString(consoleColumnIndex);

        String GenreString="";
        String ConsoleString="";


        switch (Integer.parseInt(genre)) {
            case 1: GenreString = context.getString(R.string.SCI_FI);break;
            case 2: GenreString=context.getString(R.string.ACTION);break;
            case 3: GenreString=context.getString(R.string.SPORT);break;
            case 4: GenreString=context.getString(R.string.ADVENTURE);break;
            case 5: GenreString=context.getString(R.string.SHOOTING);break;
        }

        switch (Integer.parseInt(console)) {
            case 1: GenreString = context.getString(R.string.PS);break;
            case 2: GenreString=context.getString(R.string.PS1);break;
            case 3: GenreString=context.getString(R.string.PS2);break;
            case 4: GenreString=context.getString(R.string.PS3);break;
            case 5: GenreString=context.getString(R.string.PS4);break;
        }

        if (TextUtils.isEmpty(gameStock)) {
            gameStock = context.getString(R.string.unknown_stock);
            gamePrice = context.getString(R.string.unknown_Price);}
        // Update the TextViews with the attributes for the current pet
        nameTextView.setText(gameName);
        summaryTextView.setText(gameStock);
        priceTextView.setText(gamePrice);
        genreTextView.setText(GenreString);
        consoleTextView.setText(ConsoleString);
    }}