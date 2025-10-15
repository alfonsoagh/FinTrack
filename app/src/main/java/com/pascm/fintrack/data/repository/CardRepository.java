package com.pascm.fintrack.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.pascm.fintrack.data.local.FinTrackDatabase;
import com.pascm.fintrack.data.local.dao.CreditCardDao;
import com.pascm.fintrack.data.local.dao.DebitCardDao;
import com.pascm.fintrack.data.local.entity.CreditCardEntity;
import com.pascm.fintrack.data.local.entity.DebitCardEntity;
import com.pascm.fintrack.model.CreditCard;
import com.pascm.fintrack.util.CardsManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository for managing credit and debit cards.
 *
 * This repository replaces the old CardsManager (SharedPreferences-based)
 * with proper Room database persistence.
 *
 * Provides both LiveData (reactive) and synchronous methods for flexibility.
 * All write operations are executed asynchronously on a background thread.
 *
 * Usage in Fragment/ViewModel:
 * <pre>
 * CardRepository repository = new CardRepository(requireContext());
 *
 * // Observe changes reactively
 * repository.getAllCreditCards(userId).observe(getViewLifecycleOwner(), cards -> {
 *     // Update UI with cards list
 * });
 *
 * // Insert a new card
 * repository.insertCreditCard(newCard);
 * </pre>
 */
public class CardRepository {

    private final CreditCardDao creditCardDao;
    private final DebitCardDao debitCardDao;
    private final FinTrackDatabase database;

    /**
     * Constructor - initializes database and DAOs.
     *
     * @param context Application context
     */
    public CardRepository(Context context) {
        this.database = FinTrackDatabase.getDatabase(context);
        this.creditCardDao = database.creditCardDao();
        this.debitCardDao = database.debitCardDao();
    }

    // ========== Credit Cards - Read Operations ==========

    /**
     * Get all credit cards for a user (LiveData - reactive).
     *
     * The returned LiveData will automatically emit updates when the database changes.
     * Use this in Fragments with observe() for automatic UI updates.
     *
     * @param userId User ID
     * @return LiveData list of credit card entities
     */
    public LiveData<List<CreditCardEntity>> getAllCreditCards(long userId) {
        return creditCardDao.getAllByUser(userId);
    }

    /**
     * Get all credit cards for a user (synchronous).
     *
     * WARNING: This blocks the calling thread. Don't call on main thread!
     * Use this only when you need immediate results in a background thread.
     *
     * @param userId User ID
     * @return List of credit card entities
     */
    public List<CreditCardEntity> getAllCreditCardsSync(long userId) {
        return creditCardDao.getAllByUserSync(userId);
    }

    /**
     * Get credit cards as UI models (CreditCard from model package).
     *
     * This method converts Room entities to the existing UI model for
     * compatibility with current Fragment code that expects CreditCard objects.
     *
     * @param userId User ID
     * @return List of CreditCard UI models
     */
    public List<CreditCard> getAllCreditCardsAsModels(long userId) {
        List<CreditCardEntity> entities = creditCardDao.getAllByUserSync(userId);
        return entities.stream()
                .map(CreditCardEntity::toModel)
                .collect(Collectors.toList());
    }

    /**
     * Get a single credit card by ID (LiveData).
     *
     * @param cardId Card ID
     * @return LiveData of credit card entity
     */
    public LiveData<CreditCardEntity> getCreditCardById(long cardId) {
        return creditCardDao.getById(cardId);
    }

    /**
     * Get total credit limit across all cards (LiveData).
     *
     * @param userId User ID
     * @return LiveData of total credit limit
     */
    public LiveData<Double> getTotalCreditLimit(long userId) {
        return creditCardDao.getTotalCreditLimit(userId);
    }

    /**
     * Get total balance across all cards (LiveData).
     *
     * @param userId User ID
     * @return LiveData of total balance
     */
    public LiveData<Double> getTotalBalance(long userId) {
        return creditCardDao.getTotalBalance(userId);
    }

    /**
     * Get total available credit across all cards (LiveData).
     *
     * @param userId User ID
     * @return LiveData of total available credit
     */
    public LiveData<Double> getTotalAvailableCredit(long userId) {
        return creditCardDao.getTotalAvailableCredit(userId);
    }

    /**
     * Get card count for a user (LiveData).
     *
     * @param userId User ID
     * @return LiveData of card count
     */
    public LiveData<Integer> getCardCount(long userId) {
        return creditCardDao.getCardCount(userId);
    }

    // ========== Credit Cards - Write Operations ==========

    /**
     * Insert a new credit card.
     *
     * Executes asynchronously on a background thread.
     * Sets timestamps automatically.
     *
     * @param card Credit card entity to insert
     */
    public void insertCreditCard(CreditCardEntity card) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            Instant now = Instant.now();
            card.setCreatedAt(now);
            card.setUpdatedAt(now);

            long id = creditCardDao.insert(card);

            // TODO: Mark for Firebase sync
            // SyncRepository.markForSync("CREDIT_CARD", id, "CREATE");
        });
    }

    /**
     * Insert a credit card from the old UI model (CreditCard).
     *
     * Helper method for migrating existing code.
     *
     * @param creditCard Old CreditCard model
     * @param userId     User ID
     */
    public void insertCreditCardFromModel(CreditCard creditCard, long userId) {
        CreditCardEntity entity = new CreditCardEntity(creditCard, userId);
        insertCreditCard(entity);
    }

    /**
     * Update an existing credit card.
     *
     * Updates the `updated_at` timestamp automatically.
     *
     * @param card Credit card entity to update
     */
    public void updateCreditCard(CreditCardEntity card) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            card.setUpdatedAt(Instant.now());
            creditCardDao.update(card);

            // TODO: Mark for Firebase sync
            // SyncRepository.markForSync("CREDIT_CARD", card.getCardId(), "UPDATE");
        });
    }

    /**
     * Update only the balance of a credit card.
     *
     * More efficient than updating the entire entity when only the balance changes.
     *
     * @param cardId     Card ID
     * @param newBalance New balance value
     */
    public void updateCardBalance(long cardId, double newBalance) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            creditCardDao.updateBalance(cardId, newBalance, Instant.now().toEpochMilli());

            // TODO: Mark for Firebase sync
            // SyncRepository.markForSync("CREDIT_CARD", cardId, "UPDATE");
        });
    }

    /**
     * Archive a credit card (soft delete).
     *
     * The card is not deleted from the database, but marked as archived
     * and will not appear in normal queries.
     *
     * @param cardId Card ID
     */
    public void archiveCreditCard(long cardId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            creditCardDao.archive(cardId, Instant.now().toEpochMilli());

            // TODO: Mark for Firebase sync
            // SyncRepository.markForSync("CREDIT_CARD", cardId, "UPDATE");
        });
    }

    /**
     * Delete a credit card permanently.
     *
     * This is a hard delete. The card will be removed from the database.
     * Consider using archiveCreditCard() instead for soft delete.
     *
     * @param card Credit card entity to delete
     */
    public void deleteCreditCard(CreditCardEntity card) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            creditCardDao.delete(card);

            // TODO: Mark for Firebase sync
            // SyncRepository.markForSync("CREDIT_CARD", card.getCardId(), "DELETE");
        });
    }

    // ========== Migration from CardsManager ==========

    /**
     * Migrate credit cards from old CardsManager (SharedPreferences) to Room.
     *
     * This method should be called once during app upgrade to migrate existing data.
     * It reads cards from SharedPreferences and inserts them into the Room database.
     *
     * Call this from a migration helper or during first login after update.
     *
     * @param context Application context
     * @param userId  User ID to assign to migrated cards
     */
    public void migrateFromCardsManager(Context context, long userId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            // Read old credit cards from SharedPreferences
            List<String> oldCreditCardLabels = CardsManager.getCards(context, CardsManager.TYPE_CREDIT);

            if (oldCreditCardLabels == null || oldCreditCardLabels.isEmpty()) {
                return; // Nothing to migrate
            }

            List<CreditCardEntity> entities = new ArrayList<>();

            for (String label : oldCreditCardLabels) {
                // Old format only stored labels, create minimal entities
                CreditCardEntity card = new CreditCardEntity();
                card.setUserId(userId);
                card.setIssuer("Banco"); // Unknown, use default
                card.setLabel(label);
                card.setBrand("visa"); // Default brand
                card.setPanLast4("0000"); // Unknown
                card.setCreditLimit(0);
                card.setCurrentBalance(0);
                card.setGradient("VIOLET");
                card.setCreatedAt(Instant.now());
                card.setUpdatedAt(Instant.now());

                entities.add(card);
            }

            // Bulk insert
            creditCardDao.insertAll(entities);

            // Clear old SharedPreferences
            context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .remove("cards_credit")
                    .apply();

            android.util.Log.i("CardRepository", "Migrated " + entities.size() + " credit cards from CardsManager");
        });
    }

    // ========== Debit Cards - Read Operations ==========

    /**
     * Get all debit cards for a user (LiveData - reactive).
     *
     * @param userId User ID
     * @return LiveData list of debit card entities
     */
    public LiveData<List<DebitCardEntity>> getAllDebitCards(long userId) {
        return debitCardDao.getAllByUser(userId);
    }

    /**
     * Get all debit cards for a user (synchronous).
     *
     * WARNING: Don't call on main thread!
     *
     * @param userId User ID
     * @return List of debit card entities
     */
    public List<DebitCardEntity> getAllDebitCardsSync(long userId) {
        return debitCardDao.getAllByUserSync(userId);
    }

    /**
     * Get active debit cards only (is_active = true, archived = false).
     *
     * @param userId User ID
     * @return LiveData list of active debit cards
     */
    public LiveData<List<DebitCardEntity>> getActiveDebitCards(long userId) {
        return debitCardDao.getActiveCards(userId);
    }

    /**
     * Get debit cards linked to a specific account.
     *
     * @param accountId Account ID
     * @return LiveData list of debit cards
     */
    public LiveData<List<DebitCardEntity>> getDebitCardsByAccount(long accountId) {
        return debitCardDao.getByAccount(accountId);
    }

    /**
     * Get primary debit card for user.
     *
     * @param userId User ID
     * @return LiveData of primary debit card (or null)
     */
    public LiveData<DebitCardEntity> getPrimaryDebitCard(long userId) {
        return debitCardDao.getPrimaryCard(userId);
    }

    /**
     * Get a single debit card by ID (LiveData).
     *
     * @param cardId Card ID
     * @return LiveData of debit card entity
     */
    public LiveData<DebitCardEntity> getDebitCardById(long cardId) {
        return debitCardDao.getById(cardId);
    }

    /**
     * Get debit card count for a user (LiveData).
     *
     * @param userId User ID
     * @return LiveData of debit card count
     */
    public LiveData<Integer> getDebitCardCount(long userId) {
        return debitCardDao.getCardCount(userId);
    }

    /**
     * Get active debit card count.
     *
     * @param userId User ID
     * @return LiveData of active card count
     */
    public LiveData<Integer> getActiveDebitCardCount(long userId) {
        return debitCardDao.getActiveCardCount(userId);
    }

    /**
     * Get total daily limit across all active debit cards.
     *
     * @param userId User ID
     * @return LiveData of total daily limit
     */
    public LiveData<Double> getTotalDailyLimit(long userId) {
        return debitCardDao.getTotalDailyLimit(userId);
    }

    // ========== Debit Cards - Write Operations ==========

    /**
     * Insert a new debit card.
     *
     * Executes asynchronously on a background thread.
     * Sets timestamps automatically.
     *
     * @param card Debit card entity to insert
     */
    public void insertDebitCard(DebitCardEntity card) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            Instant now = Instant.now();
            card.setCreatedAt(now);
            card.setUpdatedAt(now);

            long id = debitCardDao.insert(card);

            android.util.Log.i("CardRepository", "Inserted debit card ID: " + id);

            // TODO: Mark for Firebase sync
            // SyncRepository.markForSync("DEBIT_CARD", id, "CREATE");
        });
    }

    /**
     * Update an existing debit card.
     *
     * Updates the `updated_at` timestamp automatically.
     *
     * @param card Debit card entity to update
     */
    public void updateDebitCard(DebitCardEntity card) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            card.setUpdatedAt(Instant.now());
            debitCardDao.update(card);

            // TODO: Mark for Firebase sync
            // SyncRepository.markForSync("DEBIT_CARD", card.getCardId(), "UPDATE");
        });
    }

    /**
     * Set a debit card as primary (and unset all others for the user).
     *
     * @param userId User ID
     * @param cardId Card ID to set as primary
     */
    public void setPrimaryDebitCard(long userId, long cardId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            debitCardDao.setPrimaryCard(userId, cardId, Instant.now().toEpochMilli());

            // TODO: Mark for Firebase sync
        });
    }

    /**
     * Update debit card status (active/inactive).
     *
     * @param cardId   Card ID
     * @param isActive true to activate, false to deactivate
     */
    public void updateDebitCardStatus(long cardId, boolean isActive) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            debitCardDao.updateStatus(cardId, isActive, Instant.now().toEpochMilli());

            // TODO: Mark for Firebase sync
        });
    }

    /**
     * Update daily spending limit for a debit card.
     *
     * @param cardId     Card ID
     * @param dailyLimit New daily limit
     */
    public void updateDailyLimit(long cardId, double dailyLimit) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            debitCardDao.updateDailyLimit(cardId, dailyLimit, Instant.now().toEpochMilli());

            // TODO: Mark for Firebase sync
        });
    }

    /**
     * Archive a debit card (soft delete).
     *
     * The card is not deleted from the database, but marked as archived
     * and will not appear in normal queries.
     *
     * @param cardId Card ID
     */
    public void archiveDebitCard(long cardId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            debitCardDao.archive(cardId, Instant.now().toEpochMilli());

            // TODO: Mark for Firebase sync
            // SyncRepository.markForSync("DEBIT_CARD", cardId, "UPDATE");
        });
    }

    /**
     * Delete a debit card permanently.
     *
     * This is a hard delete. The card will be removed from the database.
     * Consider using archiveDebitCard() instead for soft delete.
     *
     * @param card Debit card entity to delete
     */
    public void deleteDebitCard(DebitCardEntity card) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            debitCardDao.delete(card);

            // TODO: Mark for Firebase sync
            // SyncRepository.markForSync("DEBIT_CARD", card.getCardId(), "DELETE");
        });
    }

    // ========== Migration from CardsManager (Debit Cards) ==========

    /**
     * Migrate debit cards from old CardsManager (SharedPreferences) to Room.
     *
     * Similar to credit card migration, but for debit cards.
     * Note: Old CardsManager didn't store account associations, so we'll need
     * to create a default account or let user link later.
     *
     * @param context   Application context
     * @param userId    User ID to assign to migrated cards
     * @param accountId Default account ID to link debit cards to
     */
    public void migrateDebitCardsFromCardsManager(Context context, long userId, long accountId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            // Read old debit cards from SharedPreferences
            List<String> oldDebitCardLabels = CardsManager.getCards(context, CardsManager.TYPE_DEBIT);

            if (oldDebitCardLabels == null || oldDebitCardLabels.isEmpty()) {
                return; // Nothing to migrate
            }

            List<DebitCardEntity> entities = new ArrayList<>();

            for (String label : oldDebitCardLabels) {
                // Old format only stored labels, create minimal entities
                DebitCardEntity card = new DebitCardEntity();
                card.setUserId(userId);
                card.setAccountId(accountId); // Link to default account
                card.setIssuer("Banco"); // Unknown, use default
                card.setLabel(label);
                card.setBrand("visa"); // Default brand
                card.setPanLast4("0000"); // Unknown
                card.setCardType(DebitCardEntity.CardType.PHYSICAL);
                card.setGradient("GRADIENT_BLUE");
                card.setCreatedAt(Instant.now());
                card.setUpdatedAt(Instant.now());

                entities.add(card);
            }

            // Bulk insert
            debitCardDao.insertAll(entities);

            // Clear old SharedPreferences
            context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .remove("cards_debit")
                    .apply();

            android.util.Log.i("CardRepository", "Migrated " + entities.size() + " debit cards from CardsManager");
        });
    }
}
