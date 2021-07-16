package com.xylink.sdk.sample.gen;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.xylink.sdk.sample.bean.MeettingInfoData;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "meeting_data".
*/
public class MeettingInfoDataDao extends AbstractDao<MeettingInfoData, Long> {

    public static final String TABLENAME = "meeting_data";

    /**
     * Properties of entity MeettingInfoData.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "Id");
        public final static Property MeettingName = new Property(1, String.class, "meettingName", false, "MEETTING_NAME");
        public final static Property MeettingNum = new Property(2, String.class, "meettingNum", false, "MEETTING_NUM");
        public final static Property ReMark = new Property(3, String.class, "reMark", false, "RE_MARK");
        public final static Property Time = new Property(4, long.class, "time", false, "TIME");
    }


    public MeettingInfoDataDao(DaoConfig config) {
        super(config);
    }
    
    public MeettingInfoDataDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"meeting_data\" (" + //
                "\"Id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"MEETTING_NAME\" TEXT," + // 1: meettingName
                "\"MEETTING_NUM\" TEXT," + // 2: meettingNum
                "\"RE_MARK\" TEXT," + // 3: reMark
                "\"TIME\" INTEGER NOT NULL );"); // 4: time
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"meeting_data\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, MeettingInfoData entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String meettingName = entity.getMeettingName();
        if (meettingName != null) {
            stmt.bindString(2, meettingName);
        }
 
        String meettingNum = entity.getMeettingNum();
        if (meettingNum != null) {
            stmt.bindString(3, meettingNum);
        }
 
        String reMark = entity.getReMark();
        if (reMark != null) {
            stmt.bindString(4, reMark);
        }
        stmt.bindLong(5, entity.getTime());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, MeettingInfoData entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String meettingName = entity.getMeettingName();
        if (meettingName != null) {
            stmt.bindString(2, meettingName);
        }
 
        String meettingNum = entity.getMeettingNum();
        if (meettingNum != null) {
            stmt.bindString(3, meettingNum);
        }
 
        String reMark = entity.getReMark();
        if (reMark != null) {
            stmt.bindString(4, reMark);
        }
        stmt.bindLong(5, entity.getTime());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public MeettingInfoData readEntity(Cursor cursor, int offset) {
        MeettingInfoData entity = new MeettingInfoData( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // meettingName
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // meettingNum
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // reMark
            cursor.getLong(offset + 4) // time
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, MeettingInfoData entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setMeettingName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setMeettingNum(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setReMark(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setTime(cursor.getLong(offset + 4));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(MeettingInfoData entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(MeettingInfoData entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(MeettingInfoData entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
