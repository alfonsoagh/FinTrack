package com.pascm.fintrack.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.pascm.fintrack.data.local.converters.Converters;
import com.pascm.fintrack.data.local.dao.AccountDao;
import com.pascm.fintrack.data.local.dao.CategoryDao;
import com.pascm.fintrack.data.local.dao.CreditCardDao;
import com.pascm.fintrack.data.local.dao.DebitCardDao;
import com.pascm.fintrack.data.local.dao.MerchantDao;
import com.pascm.fintrack.data.local.dao.SyncDao;
import com.pascm.fintrack.data.local.dao.TransactionDao;
import com.pascm.fintrack.data.local.dao.TripDao;
import com.pascm.fintrack.data.local.dao.UserDao;
import com.pascm.fintrack.data.local.entity.Account;
import com.pascm.fintrack.data.local.entity.Category;
import com.pascm.fintrack.data.local.entity.CreditCardEntity;
import com.pascm.fintrack.data.local.entity.DebitCardEntity;
import com.pascm.fintrack.data.local.entity.Merchant;
import com.pascm.fintrack.data.local.entity.PendingAction;
import com.pascm.fintrack.data.local.entity.SyncState;
import com.pascm.fintrack.data.local.entity.Transaction;
import com.pascm.fintrack.data.local.entity.Trip;
import com.pascm.fintrack.data.local.entity.User;
import com.pascm.fintrack.data.local.entity.UserProfile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room database for FinTrack application.
 *
 * This is the main database configuration containing:
 * - All entity declarations
 * - All DAO declarations
 * - Type converters
 * - Migration strategies
 *
 * Singleton pattern ensures only one instance exists at runtime.
 *
 * @version 2 - Added DebitCardEntity
 *
 * Entities included:
 *  ✓ User, UserProfile, Account, CreditCardEntity, DebitCardEntity
 *  ✓ Transaction, Category, Merchant
 *  ✓ Trip
 *  ✓ SyncState, PendingAction
 *
 * TODO: Add remaining entities in future versions:
 *  - Subcategory
 *  - Budget, BudgetAlert
 *  - Reminder, NotificationLog
 *  - TripParticipant, TripExpense, TripPlace
 *  - Role, Permission
 *  - AuditLog
 *  - AttachmentLocal
 */
@Database(
        entities = {
                // Identity and user management
                User.class,
                UserProfile.class,

                // Financial accounts and cards
                Account.class,
                CreditCardEntity.class,
                DebitCardEntity.class,

                // Transactions and categorization
                Transaction.class,
                Category.class,
                Merchant.class,

                // Trip management
                Trip.class,

                // Sync infrastructure
                SyncState.class,
                PendingAction.class

                // TODO: Add remaining entities in future versions
                // Subcategory.class,
                // Budget.class,
                // BudgetAlert.class,
                // Reminder.class,
                // NotificationLog.class,
                // TripParticipant.class,
                // TripExpense.class,
                // TripPlace.class,
                // Role.class,
                // Permission.class,
                // AuditLog.class,
                // AttachmentLocal.class
        },
        version = 5,
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class FinTrackDatabase extends RoomDatabase {

    // ========== Abstract DAO Methods ==========

    /**
     * DAO for User table
     */
    public abstract UserDao userDao();

    /**
     * DAO for Account table
     */
    public abstract AccountDao accountDao();

    /**
     * DAO for CreditCard table
     */
    public abstract CreditCardDao creditCardDao();

    /**
     * DAO for Transaction table
     */
    public abstract TransactionDao transactionDao();

    /**
     * DAO for Trip table
     */
    public abstract TripDao tripDao();

    /**
     * DAO for Sync management (SyncState and PendingAction)
     */
    public abstract SyncDao syncDao();

    /**
     * DAO for Category table
     */
    public abstract CategoryDao categoryDao();

    /**
     * DAO for Merchant table
     */
    public abstract MerchantDao merchantDao();

    /**
     * DAO for DebitCard table
     */
    public abstract DebitCardDao debitCardDao();

    // TODO: Add remaining DAOs as they are created
    // public abstract BudgetDao budgetDao();
    // public abstract ReminderDao reminderDao();

    // ========== Singleton Instance ==========

    private static volatile FinTrackDatabase INSTANCE;

    /**
     * Thread pool for async database operations.
     * Use this executor for all write operations to avoid blocking the main thread.
     *
     * Example:
     * FinTrackDatabase.databaseWriteExecutor.execute(() -> {
     *     userDao().insert(user);
     * });
     */
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    /**
     * Get the singleton instance of the database.
     *
     * Thread-safe double-check locking pattern.
     *
     * @param context Application context
     * @return Database instance
     */
    public static FinTrackDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FinTrackDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    FinTrackDatabase.class,
                                    "fintrack_database"
                            )
                            // Add migrations when schema changes
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)

                            // CAUTION: fallbackToDestructiveMigration() will DELETE ALL DATA
                            // Only use during development! Remove for production.
                            // Uncomment for development: .fallbackToDestructiveMigration()

                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // ========== Database Migrations ==========

    /**
     * Migration from version 1 to 2: Add DebitCardEntity table
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Create debit_cards table
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `debit_cards` (" +
                "`card_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`user_id` INTEGER NOT NULL, " +
                "`account_id` INTEGER NOT NULL, " +
                "`issuer` TEXT NOT NULL, " +
                "`label` TEXT NOT NULL, " +
                "`brand` TEXT, " +
                "`pan_last4` TEXT, " +
                "`card_type` TEXT NOT NULL DEFAULT 'PHYSICAL', " +
                "`expiry_date` INTEGER, " +
                "`daily_limit` REAL, " +
                "`gradient` TEXT NOT NULL DEFAULT 'GRADIENT_BLUE', " +
                "`is_primary` INTEGER NOT NULL DEFAULT 0, " +
                "`is_active` INTEGER NOT NULL DEFAULT 1, " +
                "`archived` INTEGER NOT NULL DEFAULT 0, " +
                "`created_at` INTEGER NOT NULL, " +
                "`updated_at` INTEGER NOT NULL, " +
                "`firebase_id` TEXT, " +
                "FOREIGN KEY(`user_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE, " +
                "FOREIGN KEY(`account_id`) REFERENCES `accounts`(`account_id`) ON DELETE CASCADE)"
            );

            // Create indices for debit_cards
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_debit_cards_user_id` " +
                "ON `debit_cards` (`user_id`)"
            );

            database.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_debit_cards_account_id` " +
                "ON `debit_cards` (`account_id`)"
            );
        }
    };

    /**
     * Migration from version 2 to 3: Add notification and location fields to user_profiles
     */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add notifications_enabled column
            database.execSQL(
                "ALTER TABLE user_profiles ADD COLUMN notifications_enabled INTEGER NOT NULL DEFAULT 1"
            );

            // Add location_enabled column
            database.execSQL(
                "ALTER TABLE user_profiles ADD COLUMN location_enabled INTEGER NOT NULL DEFAULT 0"
            );
        }
    };

    /**
     * Migration from version 3 to 4: Add photo_url field to merchants table
     */
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add photo_url column to merchants table
            database.execSQL(
                "ALTER TABLE merchants ADD COLUMN photo_url TEXT"
            );
        }
    };

    /**
     * Migration from version 4 to 5: Add expiry_date field to credit_cards table
     */
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add expiry_date column to credit_cards table
            database.execSQL(
                "ALTER TABLE credit_cards ADD COLUMN expiry_date INTEGER"
            );
        }
    };

    // ========== Database Callbacks ==========

    /**
     * Optional: Callback for database creation.
     * Can be used to pre-populate database with default data.
     */
    /*
    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // Pre-populate database with default categories, etc.
            databaseWriteExecutor.execute(() -> {
                // Example: Insert default categories
                CategoryDao categoryDao = INSTANCE.categoryDao();

                Category food = new Category();
                food.setName("Alimentación");
                food.setIcon("ic_food");
                food.setColor(0xFF4CAF50);
                food.setIsExpense(true);
                categoryDao.insert(food);

                // ... more default data
            });
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // Database has been opened, perform any necessary initialization
        }
    };
    */
}
