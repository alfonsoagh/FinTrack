# FinTrack - Guía de Implementación Rápida
## Room + Firebase - Migración desde SharedPreferences

---

## Estado Actual de la Implementación

### ✅ Completado

1. **Dependencias Gradle**
   - Room 2.6.1
   - Firebase BOM 33.7.0 (Firestore, Auth, Storage, Analytics)
   - Lifecycle 2.7.0 (LiveData, ViewModel)
   - Gson 2.10.1

2. **Type Converters** (`Converters.java`)
   - Instant ↔ Long
   - LocalDate ↔ Long
   - List<String> ↔ JSON
   - List<Long> ↔ JSON
   - Map<String, String> ↔ JSON
   - Map<String, Object> ↔ JSON

3. **Entidades Room**
   - ✅ User
   - ✅ UserProfile
   - ✅ Account
   - ✅ CreditCardEntity

4. **DAOs**
   - ✅ UserDao
   - ✅ AccountDao
   - ✅ CreditCardDao

5. **Database**
   - ✅ FinTrackDatabase (configuración base con singleton)

6. **Repositories**
   - ✅ CardRepository (completo con migración desde CardsManager)

7. **Documentación**
   - ✅ ARCHITECTURE.md - Arquitectura completa con todos los modelos pendientes
   - ✅ IMPLEMENTATION_GUIDE.md (este archivo)

### ⚠️ Pendiente

**Entidades:**
- DebitCardEntity
- Transaction
- Category, Subcategory
- Merchant
- Budget, BudgetAlert
- Reminder, NotificationLog
- Trip, TripParticipant, TripExpense, TripPlace
- Role, Permission
- AuditLog
- SyncState, PendingAction
- AttachmentLocal

**DAOs:** Para todas las entidades pendientes

**Repositories:**
- UserRepository
- AccountRepository
- TransactionRepository
- TripRepository (reemplaza```` TripPrefs)
- PlaceRepository (reemplaza PlacesManager)
- SyncRepository

**Firebase:**
- Configuración inicial (google-services.json)
- DTOs y Mappers
- FirebaseService
- Reglas de seguridad
- Cloud Functions (opcional)

---

## Cómo Usar lo que ya Existe

### 1. Migrar un Fragment que usa CardsManager

**Antes (con CardsManager):**

```java
// CreditCardsFragment.java - ANTIGUO
public class CreditCardsFragment extends Fragment {
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Leer tarjetas desde SharedPreferences
        List<String> cardLabels = CardsManager.getCards(
            requireContext(),
            CardsManager.TYPE_CREDIT
        );

        // Mostrar en UI (solo labels, sin datos reales)
        // ...
    }
}
```

**Después (con CardRepository + Room):**

```java
// CreditCardsFragment.java - NUEVO
public class CreditCardsFragment extends Fragment {

    private CardRepository cardRepository;
    private long userId = 1; // TODO: Get from session

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cardRepository = new CardRepository(requireContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observar cambios de forma reactiva
        cardRepository.getAllCreditCards(userId).observe(getViewLifecycleOwner(), cards -> {
            // Actualizar UI con tarjetas completas (límite, balance, etc.)
            updateUI(cards);
        });
    }

    private void updateUI(List<CreditCardEntity> cards) {
        // Convertir a UI models si es necesario
        List<CreditCard> uiModels = cards.stream()
            .map(CreditCardEntity::toModel)
            .collect(Collectors.toList());

        // Actualizar RecyclerView o lo que uses
        adapter.submitList(uiModels);
    }
}
```

### 2. Guardar una Nueva Tarjeta

**En AddCreditCardFragment.java:292** (actualmente tiene TODO):

```java
private void saveCard() {
    // Validaciones existentes...
    if (edtBankName.getText() == null || edtBankName.getText().toString().trim().isEmpty()) {
        Toast.makeText(requireContext(), "Ingresa el nombre del banco", Toast.LENGTH_SHORT).show();
        return;
    }

    // ===== NUEVO CÓDIGO =====

    // Crear entidad
    CreditCardEntity card = new CreditCardEntity();
    card.setUserId(getCurrentUserId()); // TODO: Implementar
    card.setIssuer(edtBankName.getText().toString());
    card.setLabel(edtCardAlias.getText().toString());

    // Obtener brand del RadioGroup
    int checkedId = rgBrand.getCheckedRadioButtonId();
    String brand = "visa";
    if (checkedId == R.id.rbMastercard) brand = "mastercard";
    else if (checkedId == R.id.rbAmex) brand = "amex";
    card.setBrand(brand);

    // Número de tarjeta (últimos 4 dígitos)
    String cardNumber = edtCardNumber.getText().toString().replaceAll("\\s", "");
    String last4 = cardNumber.length() >= 4
        ? cardNumber.substring(cardNumber.length() - 4)
        : "0000";
    card.setPanLast4(last4);

    // Límite y balance
    try {
        double limit = Double.parseDouble(
            edtCreditLimit.getText().toString().replaceAll("[^0-9.]", "")
        );
        double balance = Double.parseDouble(
            edtCurrentBalance.getText().toString().replaceAll("[^0-9.]", "")
        );
        card.setCreditLimit(limit);
        card.setCurrentBalance(balance);
    } catch (NumberFormatException e) {
        card.setCreditLimit(0);
        card.setCurrentBalance(0);
    }

    // Días de corte y pago
    try {
        int statementDay = Integer.parseInt(edtStatementDay.getText().toString());
        int dueDay = Integer.parseInt(edtDueDay.getText().toString());
        card.setStatementDay(statementDay);
        card.setPaymentDueDay(dueDay);
    } catch (NumberFormatException e) {
        // Opcionales
    }

    // Gradiente seleccionado
    card.setGradient(selectedGradient.name());

    // Guardar en Room
    CardRepository repository = new CardRepository(requireContext());
    repository.insertCreditCard(card);

    Toast.makeText(requireContext(), "Tarjeta registrada exitosamente", Toast.LENGTH_SHORT).show();
    Navigation.findNavController(requireView()).navigateUp();
}

private long getCurrentUserId() {
    // TODO: Implementar sistema de sesión
    // Por ahora retornar hardcoded
    return 1L;
}
```

### 3. Ejecutar Migración (Primera Vez)

**En LoginFragment.java:40-47** (después del login exitoso):

```java
if ("user".equalsIgnoreCase(email) && "123".equals(pass)) {
    // Login exitoso

    // ===== AGREGAR MIGRACIÓN =====

    long userId = 1L; // TODO: Obtener userId real del login

    // Migrar tarjetas desde CardsManager a Room
    CardRepository cardRepository = new CardRepository(requireContext());
    cardRepository.migrateFromCardsManager(requireContext(), userId);

    // Migrar preferencias de viaje
    // TripRepository tripRepository = new TripRepository(requireContext());
    // tripRepository.migrateFromTripPrefs(requireContext(), userId);

    // ===== FIN MIGRACIÓN =====

    TripPrefs.setActiveTrip(requireContext(), false);
    PlacesManager.setHasPlaces(requireContext(), false);
    Navigation.findNavController(v).navigate(R.id.action_login_to_home);
}
```

### 4. Ejemplo de ViewModel (Opcional pero Recomendado)

Si quieres usar arquitectura MVVM correcta:

```java
// CreditCardsViewModel.java - NUEVO
public class CreditCardsViewModel extends ViewModel {

    private final CardRepository repository;
    private final LiveData<List<CreditCardEntity>> allCards;
    private final LiveData<Double> totalAvailableCredit;

    public CreditCardsViewModel(Application application, long userId) {
        repository = new CardRepository(application);
        allCards = repository.getAllCreditCards(userId);
        totalAvailableCredit = repository.getTotalAvailableCredit(userId);
    }

    public LiveData<List<CreditCardEntity>> getAllCards() {
        return allCards;
    }

    public LiveData<Double> getTotalAvailableCredit() {
        return totalAvailableCredit;
    }

    public void insertCard(CreditCardEntity card) {
        repository.insertCreditCard(card);
    }

    public void updateCard(CreditCardEntity card) {
        repository.updateCreditCard(card);
    }

    public void archiveCard(long cardId) {
        repository.archiveCreditCard(cardId);
    }
}
```

```java
// CreditCardsFragment.java - usando ViewModel
public class CreditCardsFragment extends Fragment {

    private CreditCardsViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long userId = 1L; // TODO: Get from session

        ViewModelProvider.Factory factory = new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new CreditCardsViewModel(requireActivity().getApplication(), userId);
            }
        };

        viewModel = new ViewModelProvider(this, factory).get(CreditCardsViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel.getAllCards().observe(getViewLifecycleOwner(), cards -> {
            updateUI(cards);
        });

        viewModel.getTotalAvailableCredit().observe(getViewLifecycleOwner(), total -> {
            binding.tvTotalCredit.setText(String.format("$%.2f", total));
        });
    }
}
```

---

## Próximos Pasos para Completar la Implementación

### Prioridad Alta

1. **Crear entidades restantes** (ver ARCHITECTURE.md sección "Entidades Completas")
   - Copiar los esquemas de ARCHITECTURE.md
   - Crear archivos .java en `data/local/entity/`
   - Agregar a `@Database` en FinTrackDatabase.java

2. **Crear DAOs correspondientes**
   - Seguir el patrón de CreditCardDao.java
   - Queries básicas: insert, update, delete, getById, getAll

3. **Implementar Transaction y TransactionDao**
   - Entidad más importante para la app de finanzas
   - Relacionar con Account, Category, Merchant, Trip

4. **Crear TripRepository** que reemplace TripPrefs
   - Similar estructura a CardRepository
   - Métodos: getActiveTrip(), createTrip(), endTrip()

5. **Configurar Firebase**
   - Crear proyecto en Firebase Console
   - Descargar google-services.json → app/
   - Habilitar Authentication (Email/Password)
   - Crear base de datos Firestore
   - Aplicar reglas de seguridad (ver ARCHITECTURE.md)

### Prioridad Media

6. **Implementar SyncRepository**
   - Lógica de sincronización ascendente (Local → Firebase)
   - Lógica de sincronización descendente (Firebase → Local)
   - Manejo de conflictos (Last-Write-Wins)

7. **Crear SyncState y PendingAction**
   - Tracking de cambios locales pendientes
   - Queue de operaciones de sincronización

8. **Implementar Firebase DTOs y Mappers**
   - TransactionDto, AccountDto, etc.
   - Mapper classes para Entity ↔ DTO

9. **Sistema de Autenticación Real**
   - Integrar Firebase Auth
   - UserRepository con login/logout/register
   - Sesión persistente
   - Reemplazar hardcoded credentials

### Prioridad Baja

10. **Tests Unitarios**
    - Tests para DAOs (AndroidJUnit)
    - Tests para Repositories (Mockito)
    - Tests de integración para sync

11. **Optimizaciones**
    - Paginación con Paging 3 para listas grandes
    - Caché de imágenes (avatares, attachments)
    - Compresión de attachments antes de upload

12. **Features Avanzados**
    - Exportar datos a CSV/PDF
    - Backup/Restore manual
    - Soporte multi-idioma completo
    - Modo oscuro respetando UserProfile.theme

---

## Comandos Útiles

### Ver esquema de la base de datos (debug)

```java
// En cualquier Fragment
FinTrackDatabase db = FinTrackDatabase.getDatabase(requireContext());

// Ejecutar en background thread
FinTrackDatabase.databaseWriteExecutor.execute(() -> {
    // Queries directas SQL para debug
    db.getOpenHelper().getWritableDatabase().query("SELECT * FROM users");
});
```

### Limpiar base de datos (desarrollo)

```java
// CAUTION: Esto borra TODOS los datos
requireContext().deleteDatabase("fintrack_database");
```

### Ver archivo SQLite directamente

1. Conectar dispositivo/emulador
2. Android Studio → View → Tool Windows → App Inspection
3. Database Inspector → Seleccionar app
4. Explorar tablas y ejecutar queries SQL

---

## Troubleshooting Común

### Error: "Cannot access database on the main thread"

**Causa:** Llamaste a un método síncrono (_Sync) desde el main thread.

**Solución:**
```java
// MAL - bloquea UI
List<CreditCardEntity> cards = cardRepository.getAllCreditCardsSync(userId);

// BIEN - usa LiveData (reactivo)
cardRepository.getAllCreditCards(userId).observe(this, cards -> {
    // Actualizar UI
});

// O ejecuta en background thread
FinTrackDatabase.databaseWriteExecutor.execute(() -> {
    List<CreditCardEntity> cards = cardRepository.getAllCreditCardsSync(userId);
    // Procesar en background
});
```

### Error: "SQLiteConstraintException: FOREIGN KEY constraint failed"

**Causa:** Intentas insertar una entidad con FK a otra que no existe.

**Solución:** Inserta primero las entidades padre (User, Account) antes de las hijas (CreditCard, Transaction).

### Error: "Migration didn't properly handle ..."

**Causa:** Cambiaste el esquema sin crear una Migration.

**Solución durante desarrollo:**
```java
// En FinTrackDatabase.java
.fallbackToDestructiveMigration() // BORRA TODOS LOS DATOS - solo dev!
```

**Solución en producción:** Crear Migration apropiada (ver ARCHITECTURE.md).

### LiveData no emite cambios

**Causa:** No estás usando `getViewLifecycleOwner()` correctamente.

**Solución:**
```java
// MAL - se suscribe múltiples veces
repository.getCards(userId).observe(this, cards -> ...);

// BIEN - respeta lifecycle del View
repository.getCards(userId).observe(getViewLifecycleOwner(), cards -> ...);
```

---

## Recursos Adicionales

### Documentos del Proyecto

- `ARCHITECTURE.md` - Documentación completa de arquitectura con todos los esquemas
- `app/src/main/java/com/pascm/fintrack/data/` - Código de persistencia

### Links Externos

- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [LiveData Overview](https://developer.android.com/topic/libraries/architecture/livedata)
- [Firebase Firestore](https://firebase.google.com/docs/firestore)
- [Firebase Storage](https://firebase.google.com/docs/storage)

---

## Contacto y Soporte

Para dudas sobre la implementación:

1. Revisar ARCHITECTURE.md para esquemas completos
2. Ver archivos de ejemplo creados (User, CreditCardEntity, CardRepository)
3. Seguir los patrones establecidos para crear entidades nuevas

**Recuerda:** Todos los archivos Java siguen el mismo patrón. Usa los existentes como plantilla.

---

**Última actualización:** 2025-01-14
**Versión:** 1.0
