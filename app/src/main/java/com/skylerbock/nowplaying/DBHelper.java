package com.skylerbock.nowplaying;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.skylerbock.nowplaying.movie.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by sbock on 12/7/15.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static DBHelper singleton = null;
    private static Lock lock = new ReentrantLock();

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "NowPlaying.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String PRIMARY_KEY = " PRIMARY KEY";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_MOVIES =
            "CREATE TABLE " + MovieTable.TABLE_NAME + " (" +
                    MovieTable.COLUMN_IMDB_ID       + TEXT_TYPE + PRIMARY_KEY + COMMA_SEP +
                    MovieTable.COLUMN_TITLE         + TEXT_TYPE + COMMA_SEP +
                    MovieTable.COLUMN_POSTER        + TEXT_TYPE + COMMA_SEP +
                    MovieTable.COLUMN_TRAILER       + TEXT_TYPE + COMMA_SEP +
                    MovieTable.COLUMN_PLOT          + TEXT_TYPE + COMMA_SEP +
                    MovieTable.COLUMN_YEAR          + TEXT_TYPE + COMMA_SEP +
                    MovieTable.COLUMN_DIRECTOR      + TEXT_TYPE + COMMA_SEP +
                    MovieTable.COLUMN_CAST          + TEXT_TYPE + COMMA_SEP +
                    MovieTable.COLUMN_GENRE         + TEXT_TYPE + COMMA_SEP +
                    MovieTable.COLUMN_DURATION      + TEXT_TYPE + COMMA_SEP +
                    MovieTable.COLUMN_MPAA_RATING   + TEXT_TYPE + COMMA_SEP +
                    MovieTable.COLUMN_IMDB_RATING   + TEXT_TYPE +
            " )";

    private static final String SQL_DELETE_MOVIES = "DROP TABLE IF EXISTS " + MovieTable.TABLE_NAME;

    public class MovieTable {
        public static final String TABLE_NAME = "Movies";

        public static final String COLUMN_IMDB_ID = "imdbId";
        public static final String COLUMN_TITLE = "Title";
        public static final String COLUMN_POSTER = "Poster";
        public static final String COLUMN_TRAILER = "Trailer";
        public static final String COLUMN_YEAR = "Year";
        public static final String COLUMN_PLOT = "Plot";
        public static final String COLUMN_DIRECTOR = "Director";
        public static final String COLUMN_CAST = "Actors";
        public static final String COLUMN_GENRE = "Genre";
        public static final String COLUMN_DURATION = "Duration";
        public static final String COLUMN_MPAA_RATING = "mpaaRating";
        public static final String COLUMN_IMDB_RATING = "imdbRating";
    }

    public static DBHelper getInstance(Context context) {
        if (singleton == null)
            singleton = new DBHelper(context);

        return singleton;
    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_MOVIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_MOVIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public static List<Movie> getMovies(Context context) {

        lock.lock();
        SQLiteDatabase db = getInstance(context).getReadableDatabase();

        String[] projection = {
                MovieTable.COLUMN_IMDB_ID,
                MovieTable.COLUMN_TITLE,
                MovieTable.COLUMN_POSTER,
                MovieTable.COLUMN_TRAILER,
                MovieTable.COLUMN_YEAR,
                MovieTable.COLUMN_PLOT,
                MovieTable.COLUMN_DIRECTOR,
                MovieTable.COLUMN_CAST,
                MovieTable.COLUMN_GENRE,
                MovieTable.COLUMN_DURATION,
                MovieTable.COLUMN_MPAA_RATING,
                MovieTable.COLUMN_IMDB_RATING
        };

        Cursor c = db.query(
                MovieTable.TABLE_NAME,  // The table to query
                projection,             // The columns to return
                null,                   // The columns for the WHERE clause
                null,                   // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null                    // don't sort
        );

        List<Movie> movies = new ArrayList<>();

        if (c.moveToFirst())
        {
            do {
                String id = c.getString(c.getColumnIndex(MovieTable.COLUMN_IMDB_ID));
                String name = c.getString(c.getColumnIndex(MovieTable.COLUMN_TITLE));
                String poster = c.getString(c.getColumnIndex(MovieTable.COLUMN_POSTER));
                String trailer = c.getString(c.getColumnIndex(MovieTable.COLUMN_TRAILER));
                String plot = c.getString(c.getColumnIndex(MovieTable.COLUMN_PLOT));
                String year = c.getString(c.getColumnIndex(MovieTable.COLUMN_YEAR));
                String director = c.getString(c.getColumnIndex(MovieTable.COLUMN_DIRECTOR));
                String cast = c.getString(c.getColumnIndex(MovieTable.COLUMN_CAST));
                String genre = c.getString(c.getColumnIndex(MovieTable.COLUMN_GENRE));
                String duration = c.getString(c.getColumnIndex(MovieTable.COLUMN_DURATION));
                String mpaa = c.getString(c.getColumnIndex(MovieTable.COLUMN_MPAA_RATING));
                String rating = c.getString(c.getColumnIndex(MovieTable.COLUMN_IMDB_RATING));
                movies.add(new Movie(name, poster, trailer, id, year, plot, director, cast, genre, duration, mpaa, rating));
            } while (c.moveToNext());
        }

        c.close();
        db.close();
        lock.unlock();

        return movies;
    }

    public static boolean saveMovies(Context context, List<Movie> movies) {

        lock.lock();
        SQLiteDatabase db = getInstance(context).getWritableDatabase();
        boolean ret = false;

        try {
            db.beginTransaction();

            // Delete any of our current movies
            db.delete(MovieTable.TABLE_NAME, null, null);

            for (Movie m : movies) {
                ContentValues values = new ContentValues();
                values.put(MovieTable.COLUMN_IMDB_ID, m.getImdbId());
                values.put(MovieTable.COLUMN_TITLE, m.getTitle());
                values.put(MovieTable.COLUMN_POSTER, m.getPosterUrl());
                values.put(MovieTable.COLUMN_TRAILER, m.getTrailerUrl());
                values.put(MovieTable.COLUMN_YEAR, m.getYear());
                values.put(MovieTable.COLUMN_PLOT, m.getPlot());
                values.put(MovieTable.COLUMN_DIRECTOR, m.getDirector());
                values.put(MovieTable.COLUMN_CAST, m.getCast());
                values.put(MovieTable.COLUMN_GENRE, m.getGenre());
                values.put(MovieTable.COLUMN_DURATION, m.getDuration());
                values.put(MovieTable.COLUMN_MPAA_RATING, m.getMpaaRating());
                values.put(MovieTable.COLUMN_IMDB_RATING, m.getImdbRating());

                // Insert or throw (we want to throw an exception on error to correctly roll back our changes)
                db.insertOrThrow(MovieTable.TABLE_NAME, null, values);
            }

            db.setTransactionSuccessful();
            ret = true;
        }
        finally {
            db.endTransaction();
            db.close();
        }
        lock.unlock();

        return ret;
    }
}