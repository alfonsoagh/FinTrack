package com.pascm.fintrack.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pascm.fintrack.data.local.dao.AccountDao;
import com.pascm.fintrack.data.local.dao.CreditCardDao;
import com.pascm.fintrack.data.local.dao.DebitCardDao;
import com.pascm.fintrack.data.local.FinTrackDatabase;
import com.pascm.fintrack.data.local.entity.Account;
import com.pascm.fintrack.data.local.entity.CreditCardEntity;
import com.pascm.fintrack.data.local.entity.DebitCardEntity;
import com.pascm.fintrack.util.SessionManager;

import java.util.List;

public class HomeRepository {

    private final AccountDao accountDao;
    private final CreditCardDao creditCardDao;
    private final DebitCardDao debitCardDao;
    private final Context context;

    public HomeRepository(Context context) {
        FinTrackDatabase database = FinTrackDatabase.getDatabase(context);
        accountDao = database.accountDao();
        creditCardDao = database.creditCardDao();
        debitCardDao = database.debitCardDao();
        this.context = context;
    }

    /**
     * Calcula el balance total de todas las cuentas y tarjetas
     */
    public LiveData<Double> getTotalBalance() {
        MutableLiveData<Double> totalBalance = new MutableLiveData<>();
        long userId = SessionManager.getUserId(context);

        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            double total = 0.0;

            // Sumar balance de cuentas (efectivo, cheques, etc.)
            List<Account> accounts = accountDao.getAllByUserSync(userId);
            for (Account account : accounts) {
                total += account.getBalance();
            }

            // Sumar crédito disponible de tarjetas de crédito
            List<CreditCardEntity> creditCards = creditCardDao.getAllByUserSync(userId);
            for (CreditCardEntity card : creditCards) {
                total += (card.getCreditLimit() - card.getCurrentBalance());
            }

            // Note: Debit card balance is already included in Account balance
            // (debit cards are linked to accounts, so we don't double-count)

            totalBalance.postValue(total);
        });

        return totalBalance;
    }

    /**
     * Obtiene el balance total de efectivo
     */
    public LiveData<BalanceInfo> getCashBalance() {
        MutableLiveData<BalanceInfo> cashBalance = new MutableLiveData<>();
        long userId = SessionManager.getUserId(context);

        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            List<Account> accounts = accountDao.getAllByUserSync(userId);
            double total = 0.0;
            int count = 0;

            for (Account account : accounts) {
                if (account.getType() == Account.AccountType.CASH) {
                    total += account.getBalance();
                    count++;
                }
            }

            cashBalance.postValue(new BalanceInfo(total, count));
        });

        return cashBalance;
    }

    /**
     * Obtiene el balance total de tarjetas de crédito (crédito disponible)
     */
    public LiveData<BalanceInfo> getCreditBalance() {
        MutableLiveData<BalanceInfo> creditBalance = new MutableLiveData<>();
        long userId = SessionManager.getUserId(context);

        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            List<CreditCardEntity> creditCards = creditCardDao.getAllByUserSync(userId);
            double total = 0.0;
            int count = creditCards.size();

            for (CreditCardEntity card : creditCards) {
                // Mostrar el crédito disponible (límite - balance actual)
                total += (card.getCreditLimit() - card.getCurrentBalance());
            }

            creditBalance.postValue(new BalanceInfo(total, count));
        });

        return creditBalance;
    }

    /**
     * Obtiene el balance total de tarjetas de débito
     */
    public LiveData<BalanceInfo> getDebitBalance() {
        MutableLiveData<BalanceInfo> debitBalance = new MutableLiveData<>();
        long userId = SessionManager.getUserId(context);

        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            List<DebitCardEntity> debitCards = debitCardDao.getAllByUserSync(userId);
            List<Account> accounts = accountDao.getAllByUserSync(userId);
            double total = 0.0;
            int count = debitCards.size();

            // Para tarjetas de débito, el balance viene de la cuenta asociada
            for (DebitCardEntity card : debitCards) {
                // Buscar la cuenta asociada
                for (Account account : accounts) {
                    if (account.getAccountId() == card.getAccountId()) {
                        total += account.getBalance();
                        break;
                    }
                }
            }

            debitBalance.postValue(new BalanceInfo(total, count));
        });

        return debitBalance;
    }

    /**
     * Clase auxiliar para almacenar balance y cantidad de cuentas
     */
    public static class BalanceInfo {
        public final double balance;
        public final int count;

        public BalanceInfo(double balance, int count) {
            this.balance = balance;
            this.count = count;
        }

        public String getCountText() {
            if (count == 0) {
                return "Sin cuentas";
            } else if (count == 1) {
                return "1 cuenta";
            } else {
                return count + " cuentas";
            }
        }
    }
}
