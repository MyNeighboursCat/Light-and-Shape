/*
 * Copyright (c) 2023 Colin Walters.  All rights reserved.
 */
package com.mycompany.lightandshape.model;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;

import androidx.annotation.Nullable;

import com.mycompany.lightandshape.controller.SettingsActivity;

/**
 * @author Colin Walters
 * @version 1.0, 24/05/2023
 */
public final class HighScoreDatabaseHelper extends SQLiteOpenHelper {
    // NOTE: This value is only used as a dummy; master settings records will
    // have a
    // HIGH_SCORE_TABLE_NAME_ID of NULL
    public static final long SETTINGS_NO_HIGH_SCORE_VALUE = -1L;
    public static final String LIGHT_NO = "Lights";
    public static final String ANGLE = "Angle";
    public static final String TARGET_SHAPES_NO = "Target Shapes";
    public static final String FIND_SHAPES_NO = "Find Shapes";
    public static final String MISSES_NO = "Misses";
    public static final String SPEED = "Speed";
    public static final String SOUND = "Sound";
    public static final int NUMBER_OF_SETTINGS = 7;
    private static final int DATA_BASE_VERSION = 1;
    private static final String DATABASE_NAME = "lightandshape.db";
    private static final String _ID = "_id";
    private static final int HIGH_SCORE_COUNT = 10;
    private static final String HIGH_SCORE_TABLE_NAME = "high_score";
    private static final String SCORE = "score";
    private static final String TEXT_SCORE = "text_score";
    private static final String INITIALS = "initials";
    private static final String SETTINGS_TABLE_NAME = "settings";
    private static final String SETTINGS_NAME = "settings_name";
    private static final String SETTINGS_VALUE = "settings_value";
    private final String TAG = this.getClass().getSimpleName();

    @SuppressWarnings("unused")
    public HighScoreDatabaseHelper(@Nullable final Context context, @Nullable final String name,
                                   @Nullable final CursorFactory factory, final int version) {
        super(context, HighScoreDatabaseHelper.DATABASE_NAME, null,
                HighScoreDatabaseHelper.DATA_BASE_VERSION);
    }

    private static void createTables(final SQLiteDatabase sqLiteDatabase) {
        // NOTE: INTEGER value in database can store long values

        String qs;

        qs = "CREATE TABLE " + HighScoreDatabaseHelper.HIGH_SCORE_TABLE_NAME
                + " (\n" + HighScoreDatabaseHelper._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + HighScoreDatabaseHelper.SCORE + " INTEGER,\n"
                + HighScoreDatabaseHelper.TEXT_SCORE + " TEXT,\n"
                + HighScoreDatabaseHelper.INITIALS + " TEXT" + ");";
        sqLiteDatabase.execSQL(qs);

        qs = "CREATE TABLE " + HighScoreDatabaseHelper.SETTINGS_TABLE_NAME
                + " (\n" + HighScoreDatabaseHelper._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + HighScoreDatabaseHelper.SETTINGS_NAME + " TEXT,\n"
                + HighScoreDatabaseHelper.SETTINGS_VALUE + " INTEGER,\n"
                + HighScoreDatabaseHelper.HIGH_SCORE_TABLE_NAME
                + HighScoreDatabaseHelper._ID + " REFERENCES "
                + HighScoreDatabaseHelper.HIGH_SCORE_TABLE_NAME + "("
                + HighScoreDatabaseHelper._ID + ")" + ");";
        sqLiteDatabase.execSQL(qs);

        // NOTE: can't do this here because tables haven't been finished until
        // onCreate() has finished
        // addSettings(true, SETTINGS_NO_HIGH_SCORE_VALUE);
    }

    public static void removeData(final SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "
                + HighScoreDatabaseHelper.HIGH_SCORE_TABLE_NAME + ";");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "
                + HighScoreDatabaseHelper.SETTINGS_TABLE_NAME + ";");
        HighScoreDatabaseHelper.createTables(sqLiteDatabase);
    }

    public void addScore(final long score, final String[] strs) {
        if (score >= 0) {
            long highScoreID;
            SQLiteDatabase db = this.getWritableDatabase();

            final ContentValues map = new ContentValues();
            map.put(HighScoreDatabaseHelper.SCORE, score);
            map.put(HighScoreDatabaseHelper.TEXT_SCORE, strs[0]);
            map.put(HighScoreDatabaseHelper.INITIALS, strs[1]);

            try {
                highScoreID = db.insert(
                        HighScoreDatabaseHelper.HIGH_SCORE_TABLE_NAME, null,
                        map);
            } catch (final Exception e) {
                Log.e(this.TAG, "addScore() error 1 " + e);
                throw new RuntimeException();
            }

            // Need to shut database because findScores() uses the database
            db.close();

            // Remove last high score if more than maximum high score number
            // stored
            HighScoreCursor dbHighScoreCursor = this.findScores();
            // Need to reopen the database because findScores() uses the
            // database
            db = this.getWritableDatabase();
            if (dbHighScoreCursor.getCount() > HighScoreDatabaseHelper.HIGH_SCORE_COUNT) {
                for (dbHighScoreCursor
                             .moveToPosition(HighScoreDatabaseHelper.HIGH_SCORE_COUNT);
                     !dbHighScoreCursor
                             .isAfterLast(); dbHighScoreCursor.moveToNext()) {
                    final String[] whereArgs1 = new String[]{Long
                            .toString(dbHighScoreCursor.getColumnHighScoreId())};
                    try {
                        db.delete(
                                HighScoreDatabaseHelper.HIGH_SCORE_TABLE_NAME,
                                HighScoreDatabaseHelper._ID + " = ?",
                                whereArgs1);

                        // NOTE: can't just do this - may have to use primary
                        // key
                        // - not foreign key - for deletion
                        /*
                         * db.delete(HighScoreDatabaseHelper.SETTINGS_TABLE_NAME,
                         * HighScoreDatabaseHelper.HIGH_SCORE_TABLE_NAME +
                         * HighScoreDatabaseHelper._ID + " = ?", whereArgs);
                         */

                        // Need to shut database because getSettingsRecords()
                        // uses the database
                        db.close();

                        SettingsCursor dbSettingsCursor = this
                                .getSettingsRecords(null, false,
                                        dbHighScoreCursor
                                                .getColumnHighScoreId());
                        // Need to reopen the database because
                        // getSettingsRecords() uses the
                        // database
                        db = this.getWritableDatabase();
                        if (dbSettingsCursor.getCount() == HighScoreDatabaseHelper
                                .NUMBER_OF_SETTINGS) {
                            for (dbSettingsCursor.moveToFirst(); !dbSettingsCursor
                                    .isAfterLast(); dbSettingsCursor
                                         .moveToNext()) {
                                final String[] whereArgs2 = new String[]{Long
                                        .toString(dbSettingsCursor
                                        .getColumnSettingsId())};

                                try {
                                    db.delete(
                                            HighScoreDatabaseHelper.SETTINGS_TABLE_NAME,
                                            HighScoreDatabaseHelper._ID
                                                    + " = ?", whereArgs2);
                                } catch (final Exception e) {
                                    Log.e(this.TAG,
                                            "addScore() error 2 "
                                                    + e);
                                    throw new RuntimeException();
                                }
                            }
                        }

                        dbSettingsCursor.close();
                    } catch (final Exception e) {
                        Log.e(this.TAG, "addScore() error 3 " + e);
                        throw new RuntimeException();
                    }
                }
            }

            // Add settings records
            this.addSettings(false, highScoreID);

            dbHighScoreCursor.close();

            if (db != null) {
                db.close();
            }
        } else {
            Log.e(this.TAG, "addScore() error 4");
            throw new RuntimeException();
        }
    }

    private void addSettings(final boolean settingsRecords,
                             final long highScoreID) {
        if (settingsRecords) {
            this.insertSettingsRecord(HighScoreDatabaseHelper.LIGHT_NO,
                    SettingsActivity.DEFAULT_LIGHTS, true,
                    HighScoreDatabaseHelper.SETTINGS_NO_HIGH_SCORE_VALUE);
            this.insertSettingsRecord(HighScoreDatabaseHelper.ANGLE,
                    SettingsActivity.DEFAULT_ANGLES, true,
                    HighScoreDatabaseHelper.SETTINGS_NO_HIGH_SCORE_VALUE);
            this.insertSettingsRecord(HighScoreDatabaseHelper.TARGET_SHAPES_NO,
                    SettingsActivity.DEFAULT_TARGET_SHAPES, true,
                    HighScoreDatabaseHelper.SETTINGS_NO_HIGH_SCORE_VALUE);
            this.insertSettingsRecord(HighScoreDatabaseHelper.FIND_SHAPES_NO,
                    SettingsActivity.DEFAULT_FIND_SHAPES, true,
                    HighScoreDatabaseHelper.SETTINGS_NO_HIGH_SCORE_VALUE);
            this.insertSettingsRecord(HighScoreDatabaseHelper.MISSES_NO,
                    SettingsActivity.DEFAULT_MISSES, true,
                    HighScoreDatabaseHelper.SETTINGS_NO_HIGH_SCORE_VALUE);
            this.insertSettingsRecord(HighScoreDatabaseHelper.SPEED,
                    SettingsActivity.DEFAULT_SPEED, true,
                    HighScoreDatabaseHelper.SETTINGS_NO_HIGH_SCORE_VALUE);
            this.insertSettingsRecord(HighScoreDatabaseHelper.SOUND,
                    SettingsActivity.DEFAULT_SOUND, true,
                    HighScoreDatabaseHelper.SETTINGS_NO_HIGH_SCORE_VALUE);
        } else {
            SettingsCursor dbSettingsCursor = this.getSettingsRecords(null,
                    true, HighScoreDatabaseHelper.SETTINGS_NO_HIGH_SCORE_VALUE);
            if (dbSettingsCursor.getCount() == HighScoreDatabaseHelper.NUMBER_OF_SETTINGS) {
                for (dbSettingsCursor.moveToFirst(); !dbSettingsCursor
                        .isAfterLast(); dbSettingsCursor.moveToNext()) {
                    this.insertSettingsRecord(
                            dbSettingsCursor.getColumnSettingsName(),
                            dbSettingsCursor.getColumnSettingsValue(),
                            false, highScoreID);
                }
            }

            dbSettingsCursor.close();
        }
    }

    public void editSettings(final String settingsName, final int value) {
        final ContentValues map = new ContentValues();
        map.put(HighScoreDatabaseHelper.SETTINGS_NAME, settingsName);
        map.put(HighScoreDatabaseHelper.SETTINGS_VALUE, value);
        final String whereString = HighScoreDatabaseHelper.SETTINGS_NAME
                + " = \"" + settingsName + "\" AND "
                + HighScoreDatabaseHelper.HIGH_SCORE_TABLE_NAME
                + HighScoreDatabaseHelper._ID + " IS NULL";
        final String[] whereArgs = new String[]{};

        try {
            this.getWritableDatabase().update(
                    HighScoreDatabaseHelper.SETTINGS_TABLE_NAME, map,
                    whereString, whereArgs);
        } catch (final Exception e) {
            Log.e(this.TAG, "editSettings() error 1 " + e);
            throw new RuntimeException();
        }
    }

    public HighScoreCursor findScores() {
        final String sql = "SELECT * " + "FROM "
                + HighScoreDatabaseHelper.HIGH_SCORE_TABLE_NAME + " "
                + "ORDER BY " + HighScoreDatabaseHelper.SCORE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") final HighScoreCursor dbHighScoreCursor = (HighScoreCursor) db
                .rawQueryWithFactory(
                new HighScoreCursor.Factory(), sql, null, null);
        dbHighScoreCursor.moveToFirst();

        db.close();

        return dbHighScoreCursor;
    }

    private boolean findSettings() {
        boolean settingsExist = false;

        final String sql = "SELECT * " + "FROM "
                + HighScoreDatabaseHelper.SETTINGS_TABLE_NAME + " " + "WHERE "
                + HighScoreDatabaseHelper.HIGH_SCORE_TABLE_NAME
                + HighScoreDatabaseHelper._ID + " IS NULL ";

        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle")
        SettingsCursor dbSettingsCursor = (SettingsCursor) db.rawQueryWithFactory(
                new SettingsCursor.Factory(), sql, null, null);
        dbSettingsCursor.moveToFirst();

        if (dbSettingsCursor.getCount() != 0) {
            settingsExist = true;
        }

        dbSettingsCursor.close();

        db.close();

        return settingsExist;
    }

    public SettingsCursor getSettingsRecords(final String settingsName,
                                             final boolean settingsRecords, final long
                                                     highScoreID) {
        if (!this.findSettings()) {
            this.addSettings(true,
                    HighScoreDatabaseHelper.SETTINGS_NO_HIGH_SCORE_VALUE);
        }

        String sql = "SELECT * " + "FROM "
                + HighScoreDatabaseHelper.SETTINGS_TABLE_NAME + " " + "WHERE "
                + HighScoreDatabaseHelper.HIGH_SCORE_TABLE_NAME
                + HighScoreDatabaseHelper._ID + " ";

        if (settingsRecords) {
            // NOTE: In SELECTs NEVER use = NULL but IS NULL instead
            sql = sql + "IS NULL ";
        } else {
            sql = sql + "= " + highScoreID + " ";
        }

        if (settingsName != null) {
            // NOTE: It is important to include quotes around strings in
            // conditions
            // e.g. SETTINGS_NAME + " = \"" + settingsName + "\" "
            sql = sql + "AND " + HighScoreDatabaseHelper.SETTINGS_NAME
                    + " = \"" + settingsName + "\" ";
        }

        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") final SettingsCursor dbSettingsCursor = (SettingsCursor) db
                .rawQueryWithFactory(
                new SettingsCursor.Factory(), sql, null, null);
        dbSettingsCursor.moveToFirst();

        // Can't do this
        // if (settingsName.compareTo(null) == 0) {
        if (settingsName == null) {
            if (dbSettingsCursor.getCount() != HighScoreDatabaseHelper.NUMBER_OF_SETTINGS) {
                Log.e(this.TAG, "getSettingsRecords() error 1");
                throw new RuntimeException();
            }
        } else {
            if (dbSettingsCursor.getCount() != 1) {
                Log.e(this.TAG, "getSettingsRecords() error 2");
                throw new RuntimeException();
            }
        }

        db.close();

        return dbSettingsCursor;
    }

    private void insertSettingsRecord(final String name, final int value,
                                      final boolean settingsRecords, final long highScoreID) {
        final ContentValues map = new ContentValues();

        map.put(HighScoreDatabaseHelper.SETTINGS_NAME, name);
        map.put(HighScoreDatabaseHelper.SETTINGS_VALUE, value);

        // NOTE: Not including a field in the map means it will be NULL in the
        // table
        // if not an ID AUTOINCREMENT field
        if (!settingsRecords) {
            map.put(HighScoreDatabaseHelper.HIGH_SCORE_TABLE_NAME
                    + HighScoreDatabaseHelper._ID, highScoreID);
        }

        try {
            this.getWritableDatabase().insert(
                    HighScoreDatabaseHelper.SETTINGS_TABLE_NAME, null, map);
        } catch (final Exception e) {
            Log.e(this.TAG, "insertSettingsRecord() error 1 " + e);
            throw new RuntimeException();
        }
    }

    public boolean isInHighScores(final long score) {
        boolean highScore = false;

        HighScoreCursor dbHighScoreCursor = this.findScores();
        if (dbHighScoreCursor.getCount() < HighScoreDatabaseHelper.HIGH_SCORE_COUNT) {
            highScore = true;
        } else {
            dbHighScoreCursor.moveToLast();
            if (score > dbHighScoreCursor.getColumnScore()) {
                highScore = true;
            }
        }

        dbHighScoreCursor.close();

        return highScore;
    }

    @Override
    public void onCreate(final SQLiteDatabase sqLiteDatabase) {
        // abstract method so no super call
        HighScoreDatabaseHelper.createTables(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase sqLiteDatabase, final int oldv,
                          final int newv) {
        // abstract method so no super call
        HighScoreDatabaseHelper.removeData(sqLiteDatabase);
    }

    @SuppressWarnings("WeakerAccess")
    public static final class HighScoreCursor extends SQLiteCursor {
        public HighScoreCursor(final SQLiteDatabase db,
                               final SQLiteCursorDriver driver, final String editTable,
                               final SQLiteQuery query) {
            super(db, driver, editTable, query);
        }

        public long getColumnHighScoreId() {
            return this.getLong(this
                    .getColumnIndexOrThrow(HighScoreDatabaseHelper._ID));
        }

        // NOTE: IDs need to be long and not int because high scores can be
        // deleted along
        // with their corresponding setting records; new records can then be
        // created and
        // IDs are incremented no matter if there are gaps in the ID e.g. 0, 1,
        // 2, 3, 4, 5
        // and 2 id is deleted, but next ID created is still 6 and so on.

        public String getColumnInitials() {
            return this.getString(this
                    .getColumnIndexOrThrow(HighScoreDatabaseHelper.INITIALS));
        }

        public long getColumnScore() {
            return this.getLong(this
                    .getColumnIndexOrThrow(HighScoreDatabaseHelper.SCORE));
        }

        public String getColumnTextScore() {
            return this.getString(this
                    .getColumnIndexOrThrow(HighScoreDatabaseHelper.TEXT_SCORE));
        }

        public static final class Factory implements CursorFactory {
            @Override
            public Cursor newCursor(final SQLiteDatabase db,
                                    final SQLiteCursorDriver driver, final String editTable,
                                    final SQLiteQuery query) {
                // abstract method so no super call
                return new HighScoreCursor(db, driver, editTable, query);
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static final class SettingsCursor extends SQLiteCursor {
        public SettingsCursor(final SQLiteDatabase db,
                              final SQLiteCursorDriver driver, final String editTable,
                              final SQLiteQuery query) {
            super(db, driver, editTable, query);
        }

        @SuppressWarnings("unused")
        public long getColumnHighScoreId() {
            return this
                    .getLong(this
                            .getColumnIndexOrThrow(HighScoreDatabaseHelper.HIGH_SCORE_TABLE_NAME
                                    + HighScoreDatabaseHelper._ID));
        }

        public long getColumnSettingsId() {
            return this.getLong(this
                    .getColumnIndexOrThrow(HighScoreDatabaseHelper._ID));
        }

        public String getColumnSettingsName() {
            return this
                    .getString(this
                            .getColumnIndexOrThrow(HighScoreDatabaseHelper.SETTINGS_NAME));
        }

        public int getColumnSettingsValue() {
            return this
                    .getInt(this
                            .getColumnIndexOrThrow(HighScoreDatabaseHelper.SETTINGS_VALUE));
        }

        public static final class Factory implements CursorFactory {
            @Override
            public Cursor newCursor(final SQLiteDatabase db,
                                    final SQLiteCursorDriver driver, final String editTable,
                                    final SQLiteQuery query) {
                // abstract method so no super call
                return new SettingsCursor(db, driver, editTable, query);
            }
        }
    }
}
