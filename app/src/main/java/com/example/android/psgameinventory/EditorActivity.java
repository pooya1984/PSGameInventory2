package com.example.android.psgameinventory;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.psgameinventory.data.GameContract.GameEntry;

import java.io.FileDescriptor;
import java.io.IOException;
import java.text.NumberFormat;


public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    int quantity=0;

    private static final String LOG_TAG = CatalogActivity.class.getSimpleName();
    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int MY_PERMISSIONS_REQUEST = 2;
    private ImageView mImageView;
    private Uri myUri;
    private Bitmap mBitmap;
    private String mUri = "noImages";



    private static final int EXISTING_GAME_LOADER = 0;

    /** Content URI for the existing game (null if it's a new game) */
    private Uri mCurrentGAMEUri;

    /** EditText field to enter the game's name */
    private EditText mNameEditText;

    /** EditText field to enter the game's console */
    private Spinner mConsoleSpinner;

    /** TextView field to enter the game's quantity */
    private TextView mQuantityEditText;

    /** TextView field to enter the game's quantity */
    private TextView mPriceTextView;

    /** EditText field to enter the game's genre */
    private Spinner mGenreSpinner;

    private int mConsole = GameEntry.CONSOLE_UNKNOWN;

    private int mGenre = GameEntry.GENRE_UNKNOWN;

    /** Boolean flag that keeps track of whether the game has been edited (true) or not (false) */
    private boolean mGameHasChanged = false;


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mGameHasChanged = true;
            return false;
        }
    };

    private boolean isGalleryPicture = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mImageView = (ImageView) findViewById(R.id.product_photo);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {openImageSelector(view);}
        });
        ViewTreeObserver viewTreeObserver = mImageView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });


        Intent intent = getIntent();
        mCurrentGAMEUri = intent.getData();

        if (mCurrentGAMEUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_game));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_game));
            getLoaderManager().initLoader(EXISTING_GAME_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.edit_game_name);
        mConsoleSpinner = (Spinner) findViewById(R.id.spinner_console);
        mQuantityEditText = (TextView) findViewById(R.id.edit_game_quantity);
        mPriceTextView = (TextView) findViewById(R.id.order_summary_text_view);
        mGenreSpinner = (Spinner) findViewById(R.id.spinner_genre);

        mNameEditText.setOnTouchListener(mTouchListener);
        mConsoleSpinner.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceTextView.setOnTouchListener(mTouchListener);
        mGenreSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    /**
     * This method is called when the plus button is clicked.
     */
    public void increment(View view) {
        quantity = quantity + 1;
        if (quantity==100){
            return;
        }
        displayquantity(quantity);
        displayPrice(quantity * 5);
    }

    /**
     * This method is called when the minus button is clicked.
     */
    public void decrement(View view) {

        if (quantity < 1) {
            // Show an error message as a toast
            Toast.makeText(this, "You cannot have less than 1 coffee", Toast.LENGTH_SHORT).show();
            // Exit this method early because there's nothing left to do
            return;
        }
        quantity = quantity - 1;
        displayquantity(quantity);
        displayPrice(quantity * 5);
    }

    private void setupSpinner() {
        ArrayAdapter genreSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_genre_options, android.R.layout.simple_spinner_item);
        ArrayAdapter consoleSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_console_options, android.R.layout.simple_spinner_item);

        genreSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        consoleSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mGenreSpinner.setAdapter(genreSpinnerAdapter);
        mGenreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.SCI_FI))) {
                        mGenre = GameEntry.GENRE_SCI_FI;
                    } else if (selection.equals(getString(R.string.ACTION))) {
                        mGenre = GameEntry.GENRE_ACTION;
                    } else if (selection.equals(getString(R.string.SPORT))) {
                        mGenre = GameEntry.GENRE_SPORT;
                    } else if (selection.equals(getString(R.string.ADVENTURE))) {
                        mGenre = GameEntry.GENRE_ADVENTURE;
                    } else if (selection.equals(getString(R.string.SHOOTING))) {
                        mGenre = GameEntry.GENRE_SHOOTING;
                    } else {
                        mGenre = GameEntry.GENRE_UNKNOWN;
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGenre = GameEntry.GENRE_UNKNOWN;
            }
        });

        mConsoleSpinner.setAdapter(consoleSpinnerAdapter);
        mConsoleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.PS))) {
                        mConsole = GameEntry.CONSOLE_PS;
                    } else if (selection.equals(getString(R.string.PS1))) {
                        mConsole = GameEntry.CONSOLE_PS1;
                    } else if (selection.equals(getString(R.string.PS2))) {
                        mConsole = GameEntry.CONSOLE_PS2;
                    } else if (selection.equals(getString(R.string.PS3))) {
                        mConsole = GameEntry.CONSOLE_PS3;
                    } else if (selection.equals(getString(R.string.PS4))) {
                        mConsole = GameEntry.CONSOLE_PS4;
                    } else {
                        mConsole = GameEntry.CONSOLE_UNKNOWN;
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mConsole = GameEntry.CONSOLE_UNKNOWN;
            }
        });
    }

    private void saveGame() {
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceTextView.getText().toString().trim();
        mUri = String.valueOf(myUri);


        if (mCurrentGAMEUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(quantityString)&&TextUtils.isEmpty(priceString)&&
                mGenre == GameEntry.GENRE_UNKNOWN  && mConsole == GameEntry.CONSOLE_UNKNOWN) {
            return;
        }
        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(GameEntry.COLUMN_GAME_NAME, nameString);
        values.put(GameEntry.COLUMN_GAME_GENRE, mGenre);
        values.put(GameEntry.COLUMN_GAME_CONSOLE, mConsole);
        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(GameEntry.COLUMN_GAME_STOCK, quantity);

        // integer value. Use 0 by default.
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(GameEntry.COLUMN_GAME_PRICE, price);

        // Determine if this is a new or existing pet by checking if mCurrentGAMEUri is null or not
        if (mCurrentGAMEUri == null) {
            Uri newUri = getContentResolver().insert(GameEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_game_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_game_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentGAMEUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_game_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_game_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void requestPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            mImageView.setEnabled(true);
        }
    }

    public void openImageSelector(View view) {
        Intent intent;
        Log.e(LOG_TAG, "While is set and the ifs are worked through.");

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        // Show only images, no videos or anything else
        Log.e(LOG_TAG, "Check write to external permissions");

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // Save the activity state when it's going to stop.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("picUri", myUri);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Log.i(LOG_TAG, "Received an \"Activity Result\"");
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                myUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + myUri.toString());

                mBitmap = getBitmapFromUri(myUri);
                mImageView.setImageBitmap(mBitmap);

                isGalleryPicture = true;
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error closing ParcelFile Descriptor");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }
    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentGAMEUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveGame();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:

                if (!mGameHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;}
        return super.onOptionsItemSelected(item);}

    @Override
    public void onBackPressed() {
        if (!mGameHasChanged) {
            super.onBackPressed();
            return;}

    DialogInterface.OnClickListener discardButtonClickListener =
     new DialogInterface.OnClickListener() {
       @Override
       public void onClick(DialogInterface dialogInterface, int i) {finish();}};
        showUnsavedChangesDialog(discardButtonClickListener);}

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                GameEntry._ID,
                GameEntry.COLUMN_GAME_NAME,
                GameEntry.COLUMN_GAME_GENRE,
                GameEntry.COLUMN_GAME_CONSOLE,
                GameEntry.COLUMN_GAME_PRICE,
                GameEntry.COLUMN_GAME_STOCK,
                GameEntry.COLUMN_GAME_NAME};

        return new CursorLoader(this,   // Parent activity context
                mCurrentGAMEUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {

            int nameColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_NAME);
            int genreColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_GENRE);
            int consoleColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_CONSOLE);
            int quantityColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_STOCK);
            int priceColumnIndex = cursor.getColumnIndex (GameEntry.COLUMN_GAME_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            int genre = cursor.getInt(genreColumnIndex);
            int console = cursor.getInt(consoleColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            mUri = cursor.getString(imageColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceTextView.setText(Integer.toString(price));
            mImageView.setImageURI(Uri.parse(mUri));



            switch (console) {
                case GameEntry.CONSOLE_PS:
                    mConsoleSpinner.setSelection(1);
                    break;
                case GameEntry.CONSOLE_PS1:
                    mConsoleSpinner.setSelection(2);
                    break;
                case GameEntry.CONSOLE_PS2:
                    mConsoleSpinner.setSelection(3);
                case GameEntry.CONSOLE_PS3:
                    mConsoleSpinner.setSelection(4);
                    break;
                case GameEntry.CONSOLE_PS4:
                    mConsoleSpinner.setSelection(5);
                default:
                    mConsoleSpinner.setSelection(0);
                    break;
            }
            switch (genre) {
                case GameEntry.GENRE_SCI_FI:
                    mGenreSpinner.setSelection(1);
                    break;
                case GameEntry.GENRE_ACTION:
                    mGenreSpinner.setSelection(2);
                    break;
                case GameEntry.GENRE_SPORT:
                    mGenreSpinner.setSelection(3);
                case GameEntry.GENRE_ADVENTURE:
                    mGenreSpinner.setSelection(4);
                    break;
                case GameEntry.GENRE_SHOOTING:
                    mGenreSpinner.setSelection(5);
                default:
                    mGenreSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mNameEditText.setText("");
        mGenreSpinner.setSelection(0); //select "unknown" genre
        mQuantityEditText.setText("");
        mPriceTextView.setText("");
        mConsoleSpinner.setSelection(0); // Select "Unknown" console
        mImageView.setImageURI(myUri);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteGame();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void deleteGame() {
        if (mCurrentGAMEUri != null) {

            int rowsDeleted = getContentResolver().delete(mCurrentGAMEUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_game_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_game_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    /**
     * This method displays the given quantity value on the screen.
     */
    private void displayquantity(int effect) {
        TextView quantityTextView = (TextView) findViewById(
                R.id.edit_game_quantity);
        quantityTextView.setText("" + effect);
    }

    private void displayPrice(int number) {
        TextView priceTextView = (TextView) findViewById(R.id.order_summary_text_view);
        priceTextView.setText(NumberFormat.getCurrencyInstance().format(number));
    }
}