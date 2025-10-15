# FinTrack - Arquitectura de Datos
## Guía Completa de Implementación Room + Firebase

---

## Índice

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Estructura de Paquetes](#estructura-de-paquetes)
3. [Entidades Completas](#entidades-completas)
4. [DAOs](#daos)
5. [Database](#database)
6. [Repositories](#repositories)
7. [Esquema Firebase](#esquema-firebase)
8. [Estrategia de Sincronización](#estrategia-de-sincronización)
9. [Migración desde SharedPreferences](#migración-desde-sharedpreferences)
10. [Diagramas de Flujo](#diagramas-de-flujo)

---

## Resumen Ejecutivo

Esta arquitectura implementa un sistema **offline-first** con persistencia local (Room) y sincronización remota (Firebase Firestore). El código está 100% en Java 11, compatible con el proyecto existente.

### Tecnologías

- **Room 2.6.1**: Persistencia local SQLite
- **Firebase BOM 33.7.0**: Firestore, Auth, Storage
- **Lifecycle 2.7.0**: LiveData, ViewModel
- **Gson 2.10.1**: Serialización JSON

### Principios Arquitectónicos

1. **Single Source of Truth**: Room es la fuente principal, Firebase es replica
2. **Offline-First**: La app funciona completamente sin conexión
3. **Lazy Sync**: Solo sincroniza cuando hay cambios pendientes
4. **Conflict Resolution**: Last-Write-Wins con timestamps
5. **Separation of Concerns**: Entity (Room) ↔ Model (UI) ↔ DTO (Firebase)

---

## Estructura de Paquetes

```
com.pascm.fintrack/
├── data/
│   ├── local/
│   │   ├── entity/           # Room entities (tablas)
│   │   │   ├── User.java               ✓ CREADO
│   │   │   ├── UserProfile.java        ✓ CREADO
│   │   │   ├── Account.java            ✓ CREADO
│   │   │   ├── CreditCardEntity.java   ✓ CREADO
│   │   │   ├── DebitCardEntity.java    ⚠ PENDIENTE
│   │   │   ├── Transaction.java        ⚠ PENDIENTE
│   │   │   ├── Category.java           ⚠ PENDIENTE
│   │   │   ├── Subcategory.java        ⚠ PENDIENTE
│   │   │   ├── Merchant.java           ⚠ PENDIENTE
│   │   │   ├── Budget.java             ⚠ PENDIENTE
│   │   │   ├── BudgetAlert.java        ⚠ PENDIENTE
│   │   │   ├── Reminder.java           ⚠ PENDIENTE
│   │   │   ├── NotificationLog.java    ⚠ PENDIENTE
│   │   │   ├── Trip.java               ⚠ PENDIENTE
│   │   │   ├── TripParticipant.java    ⚠ PENDIENTE
│   │   │   ├── TripExpense.java        ⚠ PENDIENTE
│   │   │   ├── TripPlace.java          ⚠ PENDIENTE
│   │   │   ├── Role.java               ⚠ PENDIENTE
│   │   │   ├── Permission.java         ⚠ PENDIENTE
│   │   │   ├── AuditLog.java           ⚠ PENDIENTE
│   │   │   ├── SyncState.java          ⚠ PENDIENTE
│   │   │   ├── PendingAction.java      ⚠ PENDIENTE
│   │   │   └── AttachmentLocal.java    ⚠ PENDIENTE
│   │   │
│   │   ├── dao/              # Data Access Objects
│   │   │   ├── UserDao.java            ⚠ PENDIENTE
│   │   │   ├── AccountDao.java         ⚠ PENDIENTE
│   │   │   ├── CreditCardDao.java      ⚠ PENDIENTE
│   │   │   ├── TransactionDao.java     ⚠ PENDIENTE
│   │   │   ├── TripDao.java            ⚠ PENDIENTE
│   │   │   └── SyncDao.java            ⚠ PENDIENTE
│   │   │
│   │   ├── converters/       # Room TypeConverters
│   │   │   └── Converters.java         ✓ CREADO
│   │   │
│   │   ├── relation/         # POJOs for @Relation queries
│   │   │   ├── UserWithProfile.java    ⚠ PENDIENTE
│   │   │   ├── AccountWithCards.java   ⚠ PENDIENTE
│   │   │   ├── TripWithDetails.java    ⚠ PENDIENTE
│   │   │   └── TransactionWithMerchant.java ⚠ PENDIENTE
│   │   │
│   │   └── FinTrackDatabase.java       ⚠ PENDIENTE (ver sección Database)
│   │
│   ├── remote/
│   │   ├── dto/              # Firebase DTOs
│   │   │   ├── UserDto.java            ⚠ PENDIENTE
│   │   │   ├── AccountDto.java         ⚠ PENDIENTE
│   │   │   ├── TransactionDto.java     ⚠ PENDIENTE
│   │   │   └── TripDto.java            ⚠ PENDIENTE
│   │   │
│   │   ├── mapper/           # Entity ↔ DTO mappers
│   │   │   ├── UserMapper.java         ⚠ PENDIENTE
│   │   │   ├── AccountMapper.java      ⚠ PENDIENTE
│   │   │   └── TransactionMapper.java  ⚠ PENDIENTE
│   │   │
│   │   └── FirebaseService.java        ⚠ PENDIENTE
│   │
│   ├── repository/
│   │   ├── UserRepository.java         ⚠ PENDIENTE
│   │   ├── AccountRepository.java      ⚠ PENDIENTE
│   │   ├── CardRepository.java         ⚠ PENDIENTE (reemplaza CardsManager)
│   │   ├── TripRepository.java         ⚠ PENDIENTE (reemplaza TripPrefs)
│   │   ├── PlaceRepository.java        ⚠ PENDIENTE (reemplaza PlacesManager)
│   │   └── SyncRepository.java         ⚠ PENDIENTE
│   │
│   └── TripPrefs.java        # EXISTENTE → Migrar a TripRepository
│
├── model/                    # UI models (no persistentes)
│   ├── CreditCard.java       # EXISTENTE → Mantener para UI
│   └── DebitCard.java        # EXISTENTE → Mantener para UI
│
└── util/
    ├── CardsManager.java     # EXISTENTE → Deprecar, usar CardRepository
    └── PlacesManager.java    # EXISTENTE → Deprecar, usar PlaceRepository
```

---

## Entidades Completas

### 1. Identidad y Control de Acceso

#### User.java ✓
Ver archivo creado en: `data/local/entity/User.java`

**Campos clave:**
- `userId` (PK, autoincrement)
- `email` (unique, indexed)
- `firebaseUid` (unique, indexed)
- `passwordHash` (nullable si usa solo Firebase Auth)
- `status` (ACTIVE, SUSPENDED, BLOCKED, DELETED)
- `roleId` (FK a Role)
- `createdAt`, `lastLoginAt`, `updatedAt`

#### UserProfile.java ✓
Ver archivo creado en: `data/local/entity/UserProfile.java`

**Campos clave:**
- `profileId` (PK)
- `userId` (FK a User, CASCADE)
- `fullName`, `alias`, `avatarUrl`, `phone`
- `language` (default "es")
- `theme` (LIGHT, DARK, SYSTEM)
- `defaultCurrency` (default "MXN")

#### Role.java ⚠ PENDIENTE

```java
@Entity(
    tableName = "roles",
    indices = @Index(value = "name", unique = true)
)
public class Role {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "role_id")
    private long roleId;

    @NonNull
    @ColumnInfo(name = "name")
    private String name; // "ADMIN", "USER", "GUEST"

    @ColumnInfo(name = "description")
    private String description;

    // List<Long> permissionIds stored as JSON via Converters
    @ColumnInfo(name = "permission_ids")
    private List<Long> permissionIds;

    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    // Getters/setters...
}
```

#### Permission.java ⚠ PENDIENTE

```java
@Entity(
    tableName = "permissions",
    indices = @Index(value = "key", unique = true)
)
public class Permission {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "permission_id")
    private long permissionId;

    @NonNull
    @ColumnInfo(name = "key")
    private String key; // "manage_users", "view_reports", etc.

    @ColumnInfo(name = "description")
    private String description;

    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    // Getters/setters...
}
```

#### AuditLog.java ⚠ PENDIENTE

```java
@Entity(
    tableName = "audit_logs",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "user_id",
        childColumns = "performed_by",
        onDelete = ForeignKey.SET_NULL
    ),
    indices = {
        @Index("performed_by"),
        @Index("entity_type"),
        @Index("performed_at")
    }
)
public class AuditLog {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "log_id")
    private long logId;

    @NonNull
    @ColumnInfo(name = "action_type")
    private String actionType; // "CREATE", "UPDATE", "DELETE", "LOGIN"

    @NonNull
    @ColumnInfo(name = "entity_type")
    private String entityType; // "USER", "TRANSACTION", "CARD"

    @ColumnInfo(name = "entity_id")
    private Long entityId;

    @ColumnInfo(name = "performed_by")
    private Long performedBy; // user_id

    @NonNull
    @ColumnInfo(name = "performed_at")
    private Instant performedAt;

    // Map<String, String> metadata stored as JSON
    @ColumnInfo(name = "metadata")
    private Map<String, String> metadata;

    // Getters/setters...
}
```

---

### 2. Finanzas Personales

#### Account.java ✓
Ver archivo creado en: `data/local/entity/Account.java`

**Campos clave:**
- `accountId` (PK)
- `userId` (FK a User)
- `name`, `type` (CASH, CHECKING, SAVINGS, DIGITAL_WALLET, INVESTMENT, OTHER)
- `currencyCode` (ISO 4217)
- `balance`, `available`
- `archived`

#### CreditCardEntity.java ✓
Ver archivo creado en: `data/local/entity/CreditCardEntity.java`

**Características especiales:**
- Incluye método `toModel()` para convertir a `CreditCard` (UI model)
- Constructor desde `CreditCard` existente (migración)
- Lógica de negocio: `getAvailableCredit()`, `getUsagePercentage()`, `getUsageLevel()`
- Campo `gradient` como string para persistencia

#### DebitCardEntity.java ⚠ PENDIENTE

```java
@Entity(
    tableName = "debit_cards",
    foreignKeys = {
        @ForeignKey(entity = Account.class, parentColumns = "account_id", childColumns = "account_id", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = User.class, parentColumns = "user_id", childColumns = "user_id", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Account.class, parentColumns = "account_id", childColumns = "linked_account_id", onDelete = ForeignKey.SET_NULL)
    },
    indices = {@Index("user_id"), @Index("account_id"), @Index("linked_account_id")}
)
public class DebitCardEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "card_id")
    private long cardId;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "account_id")
    private Long accountId;

    @NonNull
    @ColumnInfo(name = "issuer")
    private String issuer; // "BBVA", "Santander"

    @NonNull
    @ColumnInfo(name = "label")
    private String label;

    @NonNull
    @ColumnInfo(name = "brand")
    private String brand; // "visa", "mastercard"

    @NonNull
    @ColumnInfo(name = "pan_last_4")
    private String panLast4;

    @ColumnInfo(name = "linked_account_id")
    private Long linkedAccountId; // FK to Account for balance tracking

    @ColumnInfo(name = "is_default")
    private boolean isDefault = false;

    @NonNull
    @ColumnInfo(name = "gradient")
    private String gradient = "SILVER";

    @ColumnInfo(name = "archived")
    private boolean archived = false;

    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    @NonNull
    @ColumnInfo(name = "updated_at")
    private Instant updatedAt;

    // Getters/setters...
    // toModel() method similar to CreditCardEntity
}
```

#### Transaction.java ⚠ PENDIENTE

```java
@Entity(
    tableName = "transactions",
    foreignKeys = {
        @ForeignKey(entity = User.class, parentColumns = "user_id", childColumns = "user_id", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Account.class, parentColumns = "account_id", childColumns = "account_id", onDelete = ForeignKey.SET_NULL),
        @ForeignKey(entity = Category.class, parentColumns = "category_id", childColumns = "category_id", onDelete = ForeignKey.SET_NULL),
        @ForeignKey(entity = Merchant.class, parentColumns = "merchant_id", childColumns = "merchant_id", onDelete = ForeignKey.SET_NULL),
        @ForeignKey(entity = Trip.class, parentColumns = "trip_id", childColumns = "trip_id", onDelete = ForeignKey.SET_NULL)
    },
    indices = {
        @Index("user_id"),
        @Index("account_id"),
        @Index("category_id"),
        @Index("merchant_id"),
        @Index("trip_id"),
        @Index("created_at")
    }
)
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "transaction_id")
    private long transactionId;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "account_id")
    private Long accountId; // Puede ser null si es tarjeta directa

    @ColumnInfo(name = "card_id")
    private Long cardId; // FK implícita a CreditCard o DebitCard

    @ColumnInfo(name = "amount")
    private double amount;

    @NonNull
    @ColumnInfo(name = "currency_code")
    private String currencyCode = "MXN";

    @NonNull
    @ColumnInfo(name = "type")
    private TransactionType type; // INCOME, EXPENSE, TRANSFER

    @NonNull
    @ColumnInfo(name = "status")
    private TransactionStatus status; // PENDING, COMPLETED, CANCELLED

    @ColumnInfo(name = "category_id")
    private Long categoryId;

    @ColumnInfo(name = "subcategory_id")
    private Long subcategoryId;

    @ColumnInfo(name = "merchant_id")
    private Long merchantId;

    @ColumnInfo(name = "trip_id")
    private Long tripId;

    @ColumnInfo(name = "notes")
    private String notes;

    // List<String> attachments (URLs/paths) stored as JSON
    @ColumnInfo(name = "attachments")
    private List<String> attachments;

    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    @NonNull
    @ColumnInfo(name = "updated_at")
    private Instant updatedAt;

    @ColumnInfo(name = "synced_at")
    private Instant syncedAt;

    // Getters/setters...

    public enum TransactionType {
        INCOME, EXPENSE, TRANSFER
    }

    public enum TransactionStatus {
        PENDING, COMPLETED, CANCELLED
    }
}
```

#### Category.java ⚠ PENDIENTE

```java
@Entity(
    tableName = "categories",
    indices = @Index(value = "name", unique = true)
)
public class Category {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "category_id")
    private long categoryId;

    @NonNull
    @ColumnInfo(name = "name")
    private String name; // "Alimentación", "Transporte", etc.

    @ColumnInfo(name = "icon")
    private String icon; // Nombre del drawable o emoji

    @ColumnInfo(name = "color")
    private int color; // ARGB color

    @ColumnInfo(name = "is_income")
    private boolean isIncome = false;

    @ColumnInfo(name = "is_expense")
    private boolean isExpense = true;

    @ColumnInfo(name = "display_order")
    private int displayOrder = 0;

    @ColumnInfo(name = "active")
    private boolean active = true;

    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    // Getters/setters...
}
```

#### Subcategory.java ⚠ PENDIENTE

```java
@Entity(
    tableName = "subcategories",
    foreignKeys = @ForeignKey(
        entity = Category.class,
        parentColumns = "category_id",
        childColumns = "parent_category_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("parent_category_id")
)
public class Subcategory {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "subcategory_id")
    private long subcategoryId;

    @ColumnInfo(name = "parent_category_id")
    private long parentCategoryId;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "active")
    private boolean active = true;

    // Getters/setters...
}
```

#### Merchant.java (PlaceReference) ⚠ PENDIENTE

```java
@Entity(
    tableName = "merchants",
    indices = @Index("name")
)
public class Merchant {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "merchant_id")
    private long merchantId;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "address")
    private String address;

    @ColumnInfo(name = "latitude")
    private Double latitude;

    @ColumnInfo(name = "longitude")
    private Double longitude;

    // List<String> tags stored as JSON
    @ColumnInfo(name = "tags")
    private List<String> tags;

    @ColumnInfo(name = "hours")
    private String hours; // JSON o formato libre

    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    // Getters/setters...
}
```

#### Budget.java ⚠ PENDIENTE

```java
@Entity(
    tableName = "budgets",
    foreignKeys = {
        @ForeignKey(entity = User.class, parentColumns = "user_id", childColumns = "user_id", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Category.class, parentColumns = "category_id", childColumns = "category_id", onDelete = ForeignKey.SET_NULL)
    },
    indices = {
        @Index("user_id"),
        @Index("category_id"),
        @Index("period_start")
    }
)
public class Budget {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "budget_id")
    private long budgetId;

    @ColumnInfo(name = "user_id")
    private long userId;

    @NonNull
    @ColumnInfo(name = "period_start")
    private LocalDate periodStart;

    @NonNull
    @ColumnInfo(name = "period_end")
    private LocalDate periodEnd;

    @ColumnInfo(name = "target_amount")
    private double targetAmount;

    @NonNull
    @ColumnInfo(name = "currency_code")
    private String currencyCode = "MXN";

    @ColumnInfo(name = "category_id")
    private Long categoryId;

    @NonNull
    @ColumnInfo(name = "status")
    private BudgetStatus status;

    // Map<String, Double> alertThresholds (e.g., "warning": 0.8, "critical": 0.95)
    @ColumnInfo(name = "alert_thresholds")
    private Map<String, Object> alertThresholds;

    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    // Getters/setters...

    public enum BudgetStatus {
        ACTIVE, COMPLETED, CANCELLED
    }
}
```

#### BudgetAlert.java ⚠ PENDIENTE

```java
@Entity(
    tableName = "budget_alerts",
    foreignKeys = {
        @ForeignKey(entity = Budget.class, parentColumns = "budget_id", childColumns = "budget_id", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = NotificationLog.class, parentColumns = "notification_id", childColumns = "notification_id", onDelete = ForeignKey.SET_NULL)
    },
    indices = {
        @Index("budget_id"),
        @Index("triggered_at")
    }
)
public class BudgetAlert {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "alert_id")
    private long alertId;

    @ColumnInfo(name = "budget_id")
    private long budgetId;

    @NonNull
    @ColumnInfo(name = "triggered_at")
    private Instant triggeredAt;

    @ColumnInfo(name = "threshold")
    private double threshold; // 0.8, 0.95, etc.

    @ColumnInfo(name = "percent_used")
    private double percentUsed;

    @ColumnInfo(name = "notification_id")
    private Long notificationId;

    // Getters/setters...
}
```

---

### 3. Recordatorios y Notificaciones

#### Reminder.java ⚠ PENDIENTE

```java
@Entity(
    tableName = "reminders",
    foreignKeys = {
        @ForeignKey(entity = User.class, parentColumns = "user_id", childColumns = "user_id", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Transaction.class, parentColumns = "transaction_id", childColumns = "related_transaction_id", onDelete = ForeignKey.SET_NULL)
    },
    indices = {
        @Index("user_id"),
        @Index("trigger_at"),
        @Index("status")
    }
)
public class Reminder {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "reminder_id")
    private long reminderId;

    @ColumnInfo(name = "user_id")
    private long userId;

    @NonNull
    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @NonNull
    @ColumnInfo(name = "trigger_at")
    private Instant triggerAt;

    @ColumnInfo(name = "recurrence_rule")
    private String recurrenceRule; // RRULE format o custom

    @ColumnInfo(name = "related_transaction_id")
    private Long relatedTransactionId;

    @NonNull
    @ColumnInfo(name = "status")
    private ReminderStatus status;

    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    // Getters/setters...

    public enum ReminderStatus {
        PENDING, TRIGGERED, DISMISSED, SNOOZED
    }
}
```

#### NotificationLog.java ⚠ PENDIENTE

```java
@Entity(
    tableName = "notification_logs",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "user_id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index("user_id"),
        @Index("delivered_at")
    }
)
public class NotificationLog {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "notification_id")
    private long notificationId;

    @ColumnInfo(name = "user_id")
    private long userId;

    @NonNull
    @ColumnInfo(name = "type")
    private String type; // "REMINDER", "BUDGET_ALERT", "TRANSACTION", etc.

    // Map<String, Object> payload stored as JSON
    @ColumnInfo(name = "payload")
    private Map<String, Object> payload;

    @NonNull
    @ColumnInfo(name = "delivered_at")
    private Instant deliveredAt;

    @ColumnInfo(name = "read_at")
    private Instant readAt;

    @NonNull
    @ColumnInfo(name = "channel")
    private String channel; // "PUSH", "IN_APP", "EMAIL"

    // Getters/setters...
}
```

---

### 4. Modo Viaje

#### Trip.java ⚠ PENDIENTE

```java
@Entity(
    tableName = "trips",
    foreignKeys = @ForeignKey(
        entity = User.class,
        parentColumns = "user_id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index("user_id"),
        @Index("start_date"),
        @Index("status")
    }
)
public class Trip {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "trip_id")
    private long tripId;

    @ColumnInfo(name = "user_id")
    private long userId;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "budget_amount")
    private Double budgetAmount;

    @NonNull
    @ColumnInfo(name = "currency_code")
    private String currencyCode = "MXN";

    @ColumnInfo(name = "origin")
    private String origin;

    @ColumnInfo(name = "destination")
    private String destination;

    @NonNull
    @ColumnInfo(name = "start_date")
    private LocalDate startDate;

    @NonNull
    @ColumnInfo(name = "end_date")
    private LocalDate endDate;

    @NonNull
    @ColumnInfo(name = "status")
    private TripStatus status;

    @ColumnInfo(name = "notes")
    private String notes;

    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    @NonNull
    @ColumnInfo(name = "updated_at")
    private Instant updatedAt;

    // Getters/setters...

    public enum TripStatus {
        PLANNED, ACTIVE, COMPLETED, CANCELLED
    }
}
```

#### TripParticipant.java ⚠ PENDIENTE

```java
@Entity(
    tableName = "trip_participants",
    foreignKeys = @ForeignKey(
        entity = Trip.class,
        parentColumns = "trip_id",
        childColumns = "trip_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("trip_id")
)
public class TripParticipant {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "participant_id")
    private long participantId;

    @ColumnInfo(name = "trip_id")
    private long tripId;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "email")
    private String email;

    @NonNull
    @ColumnInfo(name = "role")
    private String role; // "ORGANIZER", "PARTICIPANT"

    @ColumnInfo(name = "share_ratio")
    private Double shareRatio; // 0.0-1.0 for expense splitting

    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    // Getters/setters...
}
```

#### TripExpense.java ⚠ PENDIENTE

```java
@Entity(
    tableName = "trip_expenses",
    foreignKeys = {
        @ForeignKey(entity = Trip.class, parentColumns = "trip_id", childColumns = "trip_id", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Transaction.class, parentColumns = "transaction_id", childColumns = "transaction_id", onDelete = ForeignKey.SET_NULL),
        @ForeignKey(entity = TripParticipant.class, parentColumns = "participant_id", childColumns = "paid_by_participant_id", onDelete = ForeignKey.SET_NULL)
    },
    indices = {
        @Index("trip_id"),
        @Index("transaction_id"),
        @Index("paid_by_participant_id")
    }
)
public class TripExpense {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "trip_expense_id")
    private long tripExpenseId;

    @ColumnInfo(name = "trip_id")
    private long tripId;

    @ColumnInfo(name = "transaction_id")
    private Long transactionId;

    @NonNull
    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "amount")
    private double amount;

    @NonNull
    @ColumnInfo(name = "currency_code")
    private String currencyCode = "MXN";

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "paid_by_participant_id")
    private Long paidByParticipantId;

    @NonNull
    @ColumnInfo(name = "split_strategy")
    private String splitStrategy; // "EQUAL", "CUSTOM", "PERCENTAGE"

    // List<String> attachments
    @ColumnInfo(name = "attachments")
    private List<String> attachments;

    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    // Getters/setters...
}
```

#### TripPlace.java ⚠ PENDIENTE

```java
@Entity(
    tableName = "trip_places",
    foreignKeys = {
        @ForeignKey(entity = Trip.class, parentColumns = "trip_id", childColumns = "trip_id", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Merchant.class, parentColumns = "merchant_id", childColumns = "place_id", onDelete = ForeignKey.SET_NULL)
    },
    indices = {
        @Index("trip_id"),
        @Index("place_id")
    }
)
public class TripPlace {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "trip_place_id")
    private long tripPlaceId;

    @ColumnInfo(name = "trip_id")
    private long tripId;

    @ColumnInfo(name = "place_id")
    private Long placeId; // FK to Merchant

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "latitude")
    private Double latitude;

    @ColumnInfo(name = "longitude")
    private Double longitude;

    @ColumnInfo(name = "address")
    private String address;

    @ColumnInfo(name = "notes")
    private String notes;

    @ColumnInfo(name = "position")
    private Integer position; // Order in itinerary

    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    // Getters/setters...
}
```

---

### 5. Administración y Sincronización

#### SyncState.java ⚠ PENDIENTE

```java
@Entity(
    tableName = "sync_state",
    indices = {
        @Index("entity_type"),
        @Index("entity_id"),
        @Index(value = {"entity_type", "entity_id"}, unique = true)
    }
)
public class SyncState {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "sync_id")
    private long syncId;

    @NonNull
    @ColumnInfo(name = "entity_type")
    private String entityType; // "USER", "TRANSACTION", "TRIP", etc.

    @ColumnInfo(name = "entity_id")
    private long entityId;

    @ColumnInfo(name = "firebase_id")
    private String firebaseId; // Document ID in Firestore

    @ColumnInfo(name = "last_synced_at")
    private Instant lastSyncedAt;

    @ColumnInfo(name = "dirty_flag")
    private boolean dirtyFlag = false; // true si hay cambios locales pendientes

    @ColumnInfo(name = "pending_operation")
    private String pendingOperation; // "CREATE", "UPDATE", "DELETE", null

    @NonNull
    @ColumnInfo(name = "updated_at")
    private Instant updatedAt;

    // Getters/setters...
}
```

#### PendingAction.java ⚠ PENDIENTE

```java
@Entity(
    tableName = "pending_actions",
    indices = {
        @Index("entity_type"),
        @Index("created_at")
    }
)
public class PendingAction {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pending_action_id")
    private long pendingActionId;

    @NonNull
    @ColumnInfo(name = "entity_type")
    private String entityType;

    @ColumnInfo(name = "entity_id")
    private long entityId;

    @NonNull
    @ColumnInfo(name = "action")
    private String action; // "CREATE", "UPDATE", "DELETE"

    @ColumnInfo(name = "payload_json")
    private String payloadJson; // JSON representation of entity

    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    @ColumnInfo(name = "retry_count")
    private int retryCount = 0;

    // Getters/setters...
}
```

#### AttachmentLocal.java ⚠ PENDIENTE

```java
@Entity(
    tableName = "attachments",
    indices = {
        @Index("entity_type"),
        @Index("entity_id"),
        @Index("status")
    }
)
public class AttachmentLocal {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "attachment_id")
    private long attachmentId;

    @NonNull
    @ColumnInfo(name = "local_uri")
    private String localUri; // file:// path

    @ColumnInfo(name = "remote_url")
    private String remoteUrl; // Firebase Storage URL

    @NonNull
    @ColumnInfo(name = "status")
    private AttachmentStatus status;

    @NonNull
    @ColumnInfo(name = "entity_type")
    private String entityType; // "TRANSACTION", "TRIP_EXPENSE"

    @ColumnInfo(name = "entity_id")
    private long entityId;

    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    // Getters/setters...

    public enum AttachmentStatus {
        LOCAL_ONLY, UPLOADING, SYNCED, FAILED
    }
}
```

---

## DAOs

### Ejemplo: CreditCardDao.java

```java
package com.pascm.fintrack.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.pascm.fintrack.data.local.entity.CreditCardEntity;

import java.util.List;

@Dao
public interface CreditCardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CreditCardEntity card);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<CreditCardEntity> cards);

    @Update
    int update(CreditCardEntity card);

    @Delete
    int delete(CreditCardEntity card);

    @Query("SELECT * FROM credit_cards WHERE card_id = :cardId")
    LiveData<CreditCardEntity> getById(long cardId);

    @Query("SELECT * FROM credit_cards WHERE card_id = :cardId")
    CreditCardEntity getByIdSync(long cardId);

    @Query("SELECT * FROM credit_cards WHERE user_id = :userId AND archived = 0 ORDER BY created_at DESC")
    LiveData<List<CreditCardEntity>> getAllByUser(long userId);

    @Query("SELECT * FROM credit_cards WHERE user_id = :userId AND archived = 0 ORDER BY created_at DESC")
    List<CreditCardEntity> getAllByUserSync(long userId);

    @Query("SELECT * FROM credit_cards WHERE user_id = :userId AND account_id = :accountId AND archived = 0")
    LiveData<List<CreditCardEntity>> getByAccount(long userId, long accountId);

    @Query("UPDATE credit_cards SET current_balance = :newBalance, updated_at = :updatedAt WHERE card_id = :cardId")
    int updateBalance(long cardId, double newBalance, long updatedAt);

    @Query("UPDATE credit_cards SET archived = 1, updated_at = :updatedAt WHERE card_id = :cardId")
    int archive(long cardId, long updatedAt);

    @Query("DELETE FROM credit_cards WHERE card_id = :cardId")
    int deleteById(long cardId);

    @Query("SELECT COUNT(*) FROM credit_cards WHERE user_id = :userId AND archived = 0")
    LiveData<Integer> getCardCount(long userId);
}
```

### Otros DAOs necesarios (estructura similar):

- `UserDao.java`
- `AccountDao.java`
- `TransactionDao.java`
- `TripDao.java`
- `SyncDao.java`
- `BudgetDao.java`
- `ReminderDao.java`

---

## Database

### FinTrackDatabase.java

```java
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
import com.pascm.fintrack.data.local.dao.*;
import com.pascm.fintrack.data.local.entity.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room database for FinTrack.
 *
 * This is the main database configuration with all entities, DAOs, and converters.
 * Version 1: Initial schema
 */
@Database(
        entities = {
                User.class,
                UserProfile.class,
                Account.class,
                CreditCardEntity.class,
                // DebitCardEntity.class,
                // Transaction.class,
                // Category.class,
                // Subcategory.class,
                // Merchant.class,
                // Budget.class,
                // BudgetAlert.class,
                // Reminder.class,
                // NotificationLog.class,
                // Trip.class,
                // TripParticipant.class,
                // TripExpense.class,
                // TripPlace.class,
                // Role.class,
                // Permission.class,
                // AuditLog.class,
                // SyncState.class,
                // PendingAction.class,
                // AttachmentLocal.class
        },
        version = 1,
        exportSchema = true
)
@TypeConverters({Converters.class})
public abstract class FinTrackDatabase extends RoomDatabase {

    // DAOs
    public abstract UserDao userDao();
    public abstract AccountDao accountDao();
    public abstract CreditCardDao creditCardDao();
    // public abstract TransactionDao transactionDao();
    // public abstract TripDao tripDao();
    // public abstract SyncDao syncDao();
    // ... otros DAOs

    // Singleton instance
    private static volatile FinTrackDatabase INSTANCE;

    // Thread pool for async operations
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Get database instance (singleton)
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
                            // .addMigrations(MIGRATION_1_2) // Add when schema changes
                            // .fallbackToDestructiveMigration() // Only during development!
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Example migration (for future schema changes)
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Example: adding a new table
            // database.execSQL("CREATE TABLE IF NOT EXISTS `new_table` (...)");
        }
    };
}
```

---

## Repositories

### Ejemplo: CardRepository.java (reemplaza CardsManager)

```java
package com.pascm.fintrack.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.pascm.fintrack.data.local.FinTrackDatabase;
import com.pascm.fintrack.data.local.dao.CreditCardDao;
import com.pascm.fintrack.data.local.entity.CreditCardEntity;
import com.pascm.fintrack.model.CreditCard;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository for credit/debit cards.
 *
 * Replaces the old CardsManager (SharedPreferences-based).
 * Provides both LiveData (reactive) and synchronous methods.
 */
public class CardRepository {

    private final CreditCardDao creditCardDao;
    private final FinTrackDatabase database;

    public CardRepository(Context context) {
        this.database = FinTrackDatabase.getDatabase(context);
        this.creditCardDao = database.creditCardDao();
    }

    // ========== Credit Cards ==========

    /**
     * Get all credit cards for a user (LiveData - reactive)
     */
    public LiveData<List<CreditCardEntity>> getAllCreditCards(long userId) {
        return creditCardDao.getAllByUser(userId);
    }

    /**
     * Get all credit cards for a user (synchronous)
     */
    public List<CreditCardEntity> getAllCreditCardsSync(long userId) {
        return creditCardDao.getAllByUserSync(userId);
    }

    /**
     * Get credit cards as UI models (for compatibility with existing UI code)
     */
    public List<CreditCard> getAllCreditCardsAsModels(long userId) {
        return creditCardDao.getAllByUserSync(userId).stream()
                .map(CreditCardEntity::toModel)
                .collect(Collectors.toList());
    }

    /**
     * Insert a new credit card
     */
    public void insertCreditCard(CreditCardEntity card) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            card.setCreatedAt(Instant.now());
            card.setUpdatedAt(Instant.now());
            long id = creditCardDao.insert(card);
            // TODO: Mark for sync (create PendingAction)
        });
    }

    /**
     * Update an existing credit card
     */
    public void updateCreditCard(CreditCardEntity card) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            card.setUpdatedAt(Instant.now());
            creditCardDao.update(card);
            // TODO: Mark for sync
        });
    }

    /**
     * Delete a credit card
     */
    public void deleteCreditCard(CreditCardEntity card) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            creditCardDao.delete(card);
            // TODO: Mark for sync
        });
    }

    /**
     * Archive a credit card (soft delete)
     */
    public void archiveCreditCard(long cardId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            creditCardDao.archive(cardId, Instant.now().toEpochMilli());
            // TODO: Mark for sync
        });
    }

    /**
     * Update card balance
     */
    public void updateCardBalance(long cardId, double newBalance) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            creditCardDao.updateBalance(cardId, newBalance, Instant.now().toEpochMilli());
            // TODO: Mark for sync
        });
    }

    // ========== Migration from CardsManager ==========

    /**
     * Migrate cards from old CardsManager format.
     * Call this once during app upgrade.
     */
    public void migrateFromCardsManager(Context context, long userId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            // TODO: Read from SharedPreferences via CardsManager
            // TODO: Parse and create CreditCardEntity objects
            // TODO: Insert into database
            // TODO: Clear old SharedPreferences
        });
    }
}
```

### Otros Repositories necesarios:

- `UserRepository.java` - gestión de usuarios y autenticación
- `AccountRepository.java` - cuentas y balances
- `TransactionRepository.java` - transacciones y movimientos
- `TripRepository.java` - reemplaza `TripPrefs`
- `PlaceRepository.java` - reemplaza `PlacesManager`
- `SyncRepository.java` - manejo de sincronización

---

## Esquema Firebase

### Colecciones Firestore

```
firestore/
├── users/                                    # Top-level collection
│   └── {userId}/                             # Document per user
│       ├── email: String
│       ├── firebaseUid: String
│       ├── status: String
│       ├── roleId: Number
│       ├── createdAt: Timestamp
│       ├── updatedAt: Timestamp
│       │
│       ├── profile/                          # Subcollection
│       │   └── {profileId}/
│       │       ├── fullName: String
│       │       ├── alias: String
│       │       ├── avatarUrl: String
│       │       ├── language: String
│       │       ├── theme: String
│       │       ├── defaultCurrency: String
│       │       └── updatedAt: Timestamp
│       │
│       ├── accounts/                         # Subcollection
│       │   └── {accountId}/
│       │       ├── name: String
│       │       ├── type: String
│       │       ├── currencyCode: String
│       │       ├── balance: Number
│       │       ├── available: Number
│       │       ├── archived: Boolean
│       │       ├── createdAt: Timestamp
│       │       └── updatedAt: Timestamp
│       │
│       ├── creditCards/                      # Subcollection
│       │   └── {cardId}/
│       │       ├── accountId: String (ref)
│       │       ├── issuer: String
│       │       ├── label: String
│       │       ├── brand: String
│       │       ├── panLast4: String
│       │       ├── creditLimit: Number
│       │       ├── currentBalance: Number
│       │       ├── statementDay: Number
│       │       ├── paymentDueDay: Number
│       │       ├── gradient: String
│       │       ├── archived: Boolean
│       │       ├── createdAt: Timestamp
│       │       └── updatedAt: Timestamp
│       │
│       ├── transactions/                     # Subcollection
│       │   └── {transactionId}/
│       │       ├── accountId: String (ref)
│       │       ├── cardId: String (ref)
│       │       ├── amount: Number
│       │       ├── currencyCode: String
│       │       ├── type: String
│       │       ├── status: String
│       │       ├── categoryId: String (ref)
│       │       ├── merchantId: String (ref)
│       │       ├── tripId: String (ref)
│       │       ├── notes: String
│       │       ├── attachments: Array<String>
│       │       ├── createdAt: Timestamp
│       │       └── updatedAt: Timestamp
│       │
│       └── trips/                            # Subcollection
│           └── {tripId}/
│               ├── name: String
│               ├── budgetAmount: Number
│               ├── currencyCode: String
│               ├── origin: String
│               ├── destination: String
│               ├── startDate: Timestamp
│               ├── endDate: Timestamp
│               ├── status: String
│               ├── notes: String
│               ├── createdAt: Timestamp
│               ├── updatedAt: Timestamp
│               │
│               ├── participants/             # Sub-subcollection
│               │   └── {participantId}/
│               │
│               └── expenses/                 # Sub-subcollection
│                   └── {expenseId}/
│
├── categories/                               # Global collection
│   └── {categoryId}/
│       ├── name: String
│       ├── icon: String
│       ├── color: Number
│       ├── isIncome: Boolean
│       ├── isExpense: Boolean
│       ├── displayOrder: Number
│       ├── active: Boolean
│       └── createdAt: Timestamp
│
└── merchants/                                # Global collection
    └── {merchantId}/
        ├── name: String
        ├── address: String
        ├── latitude: Number
        ├── longitude: Number
        ├── tags: Array<String>
        └── createdAt: Timestamp
```

### Índices Firestore Recomendados

```javascript
// users collection
users: [
  { fields: ["email"], order: "ASCENDING" },
  { fields: ["firebaseUid"], order: "ASCENDING" },
  { fields: ["status", "createdAt"], order: ["ASCENDING", "DESCENDING"] }
]

// users/{userId}/transactions subcollection
transactions: [
  { fields: ["createdAt"], order: "DESCENDING" },
  { fields: ["type", "createdAt"], order: ["ASCENDING", "DESCENDING"] },
  { fields: ["status", "createdAt"], order: ["ASCENDING", "DESCENDING"] },
  { fields: ["categoryId", "createdAt"], order: ["ASCENDING", "DESCENDING"] },
  { fields: ["tripId", "createdAt"], order: ["ASCENDING", "DESCENDING"] }
]

// users/{userId}/trips subcollection
trips: [
  { fields: ["status", "startDate"], order: ["ASCENDING", "DESCENDING"] },
  { fields: ["startDate"], order: "DESCENDING" }
]
```

### Reglas de Seguridad Firebase

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Helper functions
    function isAuthenticated() {
      return request.auth != null;
    }

    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }

    function isAdmin() {
      return isAuthenticated() &&
             get(/databases/$(database)/documents/users/$(request.auth.uid)).data.roleId == 1;
    }

    // Users collection
    match /users/{userId} {
      // Users can read/write their own data
      allow read, write: if isOwner(userId);

      // Admins can read all users
      allow read: if isAdmin();

      // Subcollections inherit parent rules
      match /profile/{profileId} {
        allow read, write: if isOwner(userId);
      }

      match /accounts/{accountId} {
        allow read, write: if isOwner(userId);
      }

      match /creditCards/{cardId} {
        allow read, write: if isOwner(userId);
      }

      match /transactions/{transactionId} {
        allow read, write: if isOwner(userId);
        // Prevent modifying transactions older than 30 days (optional)
        allow update: if isOwner(userId) &&
          resource.data.createdAt > request.time - duration.value(30, 'd');
      }

      match /trips/{tripId} {
        allow read, write: if isOwner(userId);

        match /participants/{participantId} {
          allow read, write: if isOwner(userId);
        }

        match /expenses/{expenseId} {
          allow read, write: if isOwner(userId);
        }
      }
    }

    // Global collections (read-only for users)
    match /categories/{categoryId} {
      allow read: if isAuthenticated();
      allow write: if isAdmin();
    }

    match /merchants/{merchantId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated();
      allow update, delete: if isAdmin();
    }
  }
}
```

### Cloud Functions (Triggers opcionales)

```javascript
// functions/index.js
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

// Trigger: Update account balance when transaction is created/updated
exports.updateAccountBalance = functions.firestore
  .document('users/{userId}/transactions/{transactionId}')
  .onWrite(async (change, context) => {
    const userId = context.params.userId;
    const transaction = change.after.exists ? change.after.data() : null;
    const oldTransaction = change.before.exists ? change.before.data() : null;

    // Calculate balance delta
    let delta = 0;
    if (transaction && !oldTransaction) {
      // New transaction
      delta = transaction.type === 'INCOME' ? transaction.amount : -transaction.amount;
    } else if (!transaction && oldTransaction) {
      // Deleted transaction
      delta = oldTransaction.type === 'INCOME' ? -oldTransaction.amount : oldTransaction.amount;
    } else if (transaction && oldTransaction) {
      // Updated transaction
      const oldDelta = oldTransaction.type === 'INCOME' ? oldTransaction.amount : -oldTransaction.amount;
      const newDelta = transaction.type === 'INCOME' ? transaction.amount : -transaction.amount;
      delta = newDelta - oldDelta;
    }

    if (delta !== 0 && transaction.accountId) {
      const accountRef = admin.firestore()
        .doc(`users/${userId}/accounts/${transaction.accountId}`);
      return accountRef.update({
        balance: admin.firestore.FieldValue.increment(delta),
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      });
    }

    return null;
  });

// Trigger: Create audit log for sensitive operations
exports.createAuditLog = functions.firestore
  .document('users/{userId}/{collection}/{docId}')
  .onWrite(async (change, context) => {
    const userId = context.params.userId;
    const collection = context.params.collection;
    const docId = context.params.docId;

    const actionType = !change.before.exists ? 'CREATE'
                     : !change.after.exists ? 'DELETE'
                     : 'UPDATE';

    // Only log certain collections
    const sensitiveCollections = ['accounts', 'creditCards', 'transactions'];
    if (!sensitiveCollections.includes(collection)) {
      return null;
    }

    return admin.firestore().collection('auditLogs').add({
      userId,
      actionType,
      entityType: collection,
      entityId: docId,
      performedAt: admin.firestore.FieldValue.serverTimestamp(),
      metadata: {
        before: change.before.exists ? change.before.data() : null,
        after: change.after.exists ? change.after.data() : null
      }
    });
  });
```

---

## Estrategia de Sincronización

### Arquitectura Offline-First

```
┌─────────────────────────────────────────────────────────────┐
│                          UI Layer                           │
│                  (Fragments, ViewModels)                    │
└────────────────────────┬────────────────────────────────────┘
                         │ LiveData/Callbacks
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    Repository Layer                         │
│  (UserRepository, AccountRepository, CardRepository, etc.)  │
└────────┬───────────────────────────────────────────┬────────┘
         │                                             │
         │ Read/Write                                  │ Sync
         ▼                                             ▼
┌────────────────────────┐            ┌────────────────────────┐
│    Room Database       │            │   Firebase Firestore   │
│  (Local SQLite)        │◄──────────►│   (Remote Cloud)       │
│  - Single source of    │   Sync     │   - Backup/Replica     │
│    truth               │            │   - Multi-device       │
│  - Offline-capable     │            │   - Collaboration      │
└────────────────────────┘            └────────────────────────┘
         │                                             │
         │ Tracks changes                              │ Polls/Listens
         ▼                                             ▼
┌────────────────────────┐            ┌────────────────────────┐
│  SyncState table       │            │   Firestore Listeners  │
│  - Dirty flags         │            │   - Real-time updates  │
│  - Last sync timestamp │            │                        │
└────────────────────────┘            └────────────────────────┘
         │
         │ Queues operations
         ▼
┌────────────────────────┐
│  PendingActions table  │
│  - CREATE/UPDATE/DEL   │
│  - Retry queue         │
└────────────────────────┘
```

### Flujo de Sincronización

#### 1. Escritura Local (Usuario crea/modifica datos)

```
1. Usuario crea una transacción en UI
   ↓
2. Repository.insertTransaction()
   ↓
3. Room: INSERT INTO transactions (...)
   ↓
4. Room: INSERT INTO sync_state (entity_type='TRANSACTION', entity_id=123, dirty_flag=true, pending_operation='CREATE')
   ↓
5. Room: INSERT INTO pending_actions (entity_type='TRANSACTION', entity_id=123, action='CREATE', payload_json='...')
   ↓
6. UI actualizada inmediatamente (LiveData emite cambio)
   ↓
7. SyncManager detecta pending_actions pendientes
   ↓
8. [Si hay conexión] Intenta sincronizar con Firebase
```

#### 2. Sincronización Ascendente (Local → Firebase)

```java
public class SyncRepository {

    public void syncPendingActions() {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            List<PendingAction> pending = syncDao.getAllPendingActions();

            for (PendingAction action : pending) {
                try {
                    switch (action.getEntityType()) {
                        case "TRANSACTION":
                            syncTransaction(action);
                            break;
                        case "CREDIT_CARD":
                            syncCreditCard(action);
                            break;
                        // ...
                    }

                    // Success: remove pending action and update sync state
                    syncDao.deletePendingAction(action.getPendingActionId());
                    syncDao.updateSyncState(
                        action.getEntityType(),
                        action.getEntityId(),
                        false, // dirty_flag = false
                        null,  // pending_operation = null
                        Instant.now()
                    );

                } catch (Exception e) {
                    // Failure: increment retry count
                    action.setRetryCount(action.getRetryCount() + 1);
                    syncDao.updatePendingAction(action);

                    // If too many retries, mark as failed
                    if (action.getRetryCount() > 5) {
                        Log.e("SyncRepository", "Failed to sync after 5 retries", e);
                        // TODO: Show user notification
                    }
                }
            }
        });
    }

    private void syncTransaction(PendingAction action) throws Exception {
        Transaction transaction = new Gson().fromJson(
            action.getPayloadJson(),
            Transaction.class
        );

        TransactionDto dto = TransactionMapper.toDto(transaction);

        String userId = String.valueOf(transaction.getUserId());
        String transactionId = String.valueOf(transaction.getTransactionId());

        switch (action.getAction()) {
            case "CREATE":
            case "UPDATE":
                // Upsert to Firestore
                FirebaseFirestore.getInstance()
                    .collection("users").document(userId)
                    .collection("transactions").document(transactionId)
                    .set(dto)
                    .get(); // Block until complete (or use Tasks.await in coroutine)
                break;

            case "DELETE":
                FirebaseFirestore.getInstance()
                    .collection("users").document(userId)
                    .collection("transactions").document(transactionId)
                    .delete()
                    .get();
                break;
        }
    }
}
```

#### 3. Sincronización Descendente (Firebase → Local)

```java
public class SyncRepository {

    private ListenerRegistration transactionListener;

    public void startListeningToFirebase(long userId) {
        String userIdStr = String.valueOf(userId);

        transactionListener = FirebaseFirestore.getInstance()
            .collection("users").document(userIdStr)
            .collection("transactions")
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    Log.e("SyncRepository", "Listen failed", error);
                    return;
                }

                if (snapshots == null) return;

                FinTrackDatabase.databaseWriteExecutor.execute(() -> {
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        TransactionDto dto = dc.getDocument().toObject(TransactionDto.class);
                        Transaction localTransaction = TransactionMapper.toEntity(dto, userId);

                        SyncState syncState = syncDao.getSyncState("TRANSACTION", localTransaction.getTransactionId());

                        // Conflict detection
                        if (syncState != null && syncState.isDirtyFlag()) {
                            // Local changes pending - apply conflict resolution
                            resolveConflict(localTransaction, dto, syncState);
                        } else {
                            // No conflict - apply remote changes
                            switch (dc.getType()) {
                                case ADDED:
                                case MODIFIED:
                                    transactionDao.upsert(localTransaction);
                                    syncDao.upsertSyncState(new SyncState(
                                        "TRANSACTION",
                                        localTransaction.getTransactionId(),
                                        dc.getDocument().getId(),
                                        Instant.now(),
                                        false,
                                        null
                                    ));
                                    break;

                                case REMOVED:
                                    transactionDao.deleteById(localTransaction.getTransactionId());
                                    syncDao.deleteSyncState("TRANSACTION", localTransaction.getTransactionId());
                                    break;
                            }
                        }
                    }
                });
            });
    }

    private void resolveConflict(Transaction local, TransactionDto remote, SyncState syncState) {
        // Strategy: Last-Write-Wins
        Instant localUpdated = local.getUpdatedAt();
        Instant remoteUpdated = remote.updatedAt; // Assuming Firebase Timestamp converted

        if (remoteUpdated.isAfter(localUpdated)) {
            // Remote wins - overwrite local
            transactionDao.update(TransactionMapper.toEntity(remote, local.getUserId()));
            syncDao.updateSyncState(
                "TRANSACTION",
                local.getTransactionId(),
                false,
                null,
                Instant.now()
            );
            // Remove pending action if exists
            syncDao.deletePendingActionForEntity("TRANSACTION", local.getTransactionId());

            Log.i("SyncRepository", "Conflict resolved: remote wins");
        } else {
            // Local wins - keep local, will sync up later
            Log.i("SyncRepository", "Conflict resolved: local wins, will sync up");
        }
    }

    public void stopListening() {
        if (transactionListener != null) {
            transactionListener.remove();
        }
    }
}
```

#### 4. Gestión de Archivos Adjuntos (Firebase Storage)

```java
public class AttachmentRepository {

    private final AttachmentDao attachmentDao;
    private final StorageReference storageRef;

    public AttachmentRepository(Context context) {
        this.attachmentDao = FinTrackDatabase.getDatabase(context).attachmentDao();
        this.storageRef = FirebaseStorage.getInstance().getReference();
    }

    public void uploadAttachment(long attachmentId, String localUri, String entityType, long entityId, long userId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            AttachmentLocal attachment = new AttachmentLocal();
            attachment.setAttachmentId(attachmentId);
            attachment.setLocalUri(localUri);
            attachment.setStatus(AttachmentLocal.AttachmentStatus.UPLOADING);
            attachment.setEntityType(entityType);
            attachment.setEntityId(entityId);
            attachment.setCreatedAt(Instant.now());

            attachmentDao.insert(attachment);

            // Upload to Firebase Storage
            Uri fileUri = Uri.parse(localUri);
            String remotePath = String.format("users/%d/%s/%d/%s",
                userId, entityType.toLowerCase(), entityId, fileUri.getLastPathSegment());

            StorageReference fileRef = storageRef.child(remotePath);

            fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        attachment.setRemoteUrl(uri.toString());
                        attachment.setStatus(AttachmentLocal.AttachmentStatus.SYNCED);
                        attachmentDao.update(attachment);
                    });
                })
                .addOnFailureListener(e -> {
                    attachment.setStatus(AttachmentLocal.AttachmentStatus.FAILED);
                    attachmentDao.update(attachment);
                    Log.e("AttachmentRepository", "Upload failed", e);
                });
        });
    }
}
```

---

## Migración desde SharedPreferences

### CardsManager → CardRepository

```java
public class MigrationHelper {

    /**
     * Migrate cards from old CardsManager to Room database.
     * Call this once on app upgrade (detected via version code).
     */
    public static void migrateCards(Context context, long userId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            CardRepository cardRepository = new CardRepository(context);

            // Read old credit cards from SharedPreferences
            List<String> oldCreditCards = CardsManager.getCards(context, CardsManager.TYPE_CREDIT);

            for (String cardLabel : oldCreditCards) {
                // Old format was just labels, create minimal entities
                CreditCardEntity card = new CreditCardEntity();
                card.setUserId(userId);
                card.setIssuer("Banco"); // Unknown
                card.setLabel(cardLabel);
                card.setBrand("visa"); // Default
                card.setPanLast4("0000"); // Unknown
                card.setCreditLimit(0);
                card.setCurrentBalance(0);
                card.setGradient("VIOLET");
                card.setCreatedAt(Instant.now());
                card.setUpdatedAt(Instant.now());

                cardRepository.insertCreditCard(card);
            }

            // Clear old SharedPreferences
            context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
                .edit()
                .remove("cards_credit")
                .apply();

            Log.i("Migration", "Migrated " + oldCreditCards.size() + " credit cards");
        });
    }

    /**
     * Migrate trip preferences
     */
    public static void migrateTripPrefs(Context context, long userId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            boolean hadActiveTrip = TripPrefs.isActiveTrip(context);

            if (hadActiveTrip) {
                // Create a default trip
                Trip trip = new Trip();
                trip.setUserId(userId);
                trip.setName("Viaje Migrado");
                trip.setStartDate(LocalDate.now());
                trip.setEndDate(LocalDate.now().plusDays(7));
                trip.setStatus(Trip.TripStatus.ACTIVE);
                trip.setCurrencyCode("MXN");
                trip.setCreatedAt(Instant.now());
                trip.setUpdatedAt(Instant.now());

                TripRepository tripRepository = new TripRepository(context);
                tripRepository.insertTrip(trip);
            }

            // Clear old preferences
            TripPrefs.clearAll(context);

            Log.i("Migration", "Migrated trip preferences");
        });
    }

    /**
     * Migrate places
     */
    public static void migratePlaces(Context context, long userId) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            boolean hadPlaces = PlacesManager.hasPlaces(context);

            // Old PlacesManager only stored a boolean flag, no actual places
            // Nothing to migrate, just clear the flag
            context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE)
                .edit()
                .remove("has_places")
                .apply();

            Log.i("Migration", "Cleared old places flag");
        });
    }

    /**
     * Main migration method - call on app startup if needed
     */
    public static void migrateAllIfNeeded(Context context, long userId) {
        SharedPreferences prefs = context.getSharedPreferences("FinTrackPrefs", Context.MODE_PRIVATE);
        int lastMigrationVersion = prefs.getInt("migration_version", 0);
        int currentVersion = 1; // Increment when adding new migrations

        if (lastMigrationVersion < currentVersion) {
            Log.i("Migration", "Starting migration from version " + lastMigrationVersion);

            migrateCards(context, userId);
            migrateTripPrefs(context, userId);
            migratePlaces(context, userId);

            prefs.edit().putInt("migration_version", currentVersion).apply();

            Log.i("Migration", "Migration completed to version " + currentVersion);
        }
    }
}
```

### Actualizar LoginFragment para migración

```java
// En LoginFragment.java:40-47
if ("user".equalsIgnoreCase(email) && "123".equals(pass)) {
    // Successful login

    // Check if migration is needed (first time login after update)
    long userId = 1; // TODO: Get real userId from User table
    MigrationHelper.migrateAllIfNeeded(requireContext(), userId);

    // Continue with normal login flow
    Navigation.findNavController(v).navigate(R.id.action_login_to_home);
}
```

---

## Diagramas de Flujo

### Flujo de Creación de Transacción

```
Usuario rellena formulario de transacción
              ↓
   [Guardar] → AgregarMovimientoFragment
              ↓
     TransactionRepository.insertTransaction()
              ↓
   ┌──────────┴──────────┐
   │                     │
   ▼                     ▼
Room INSERT         Adjuntar foto?
transacción            │
   │                   ▼
   │            AttachmentRepository
   │            .uploadAttachment()
   │                   │
   │                   ▼
   │            Firebase Storage
   │            .putFile()
   │                   │
   ├───────────────────┘
   │
   ▼
Room INSERT sync_state
(dirty=true, operation='CREATE')
   │
   ▼
Room INSERT pending_action
(action='CREATE', payload='...')
   │
   ▼
LiveData emite cambio
   │
   ▼
HomeFragment actualiza UI
(transacción visible inmediatamente)
   │
   ▼
SyncManager detecta pending_action
   │
   ├──[Sin conexión]──→ Queda pendiente
   │
   └──[Con conexión]──→ Firestore.set()
                            │
                            ▼
                     Sincronización exitosa
                            │
                            ▼
                  DELETE pending_action
                  UPDATE sync_state
                  (dirty=false)
```

### Flujo de Sincronización Bidireccional

```
          LOCAL (Room)                      FIREBASE (Firestore)
              │                                     │
              │                                     │
    Usuario crea transacción                       │
              │                                     │
              ▼                                     │
      INSERT Transaction                            │
              │                                     │
              ▼                                     │
      CREATE PendingAction                          │
              │                                     │
              ├────────[Sync UP]────────────────────►
              │                              Firestore.set()
              │                                     │
              │                              SnapshotListener
              │                               detecta cambio
              │                                     │
              │◄────────[Sync DOWN]─────────────────┤
              │                                     │
        Compara timestamps                          │
              │                                     │
      ¿Local más reciente?                          │
       │                │                           │
    [SÍ]            [NO]                            │
       │                │                           │
   Mantiene        Actualiza                        │
    local          con remoto                       │
       │                │                           │
       └────────┬───────┘                           │
                │                                   │
         DELETE PendingAction                       │
         UPDATE SyncState                           │
                │                                   │
                ▼                                   ▼
         Sincronización                        Consistente
            completa                           entre devices
```

---

## Resumen de Implementación

### Pasos para Implementar

1. **Completar Entidades** ⚠
   - Crear todas las entidades pendientes siguiendo los ejemplos
   - Agregar a `@Database` annotation en `FinTrackDatabase.java`

2. **Crear DAOs** ⚠
   - Implementar interfaces DAO para cada entidad
   - Incluir queries comunes (getAll, getById, insert, update, delete)

3. **Implementar Repositories** ⚠
   - Crear repositories que usen DAOs
   - Agregar lógica de sincronización
   - Reemplazar `CardsManager`, `PlacesManager`, `TripPrefs`

4. **Configurar Firebase** ⚠
   - Agregar `google-services.json` al proyecto
   - Crear proyecto Firebase en consola
   - Configurar Authentication
   - Crear colecciones y reglas de seguridad

5. **Implementar SyncRepository** ⚠
   - Lógica de sincronización ascendente/descendente
   - Manejo de conflictos
   - Queue de `PendingActions`

6. **Migración** ⚠
   - Ejecutar `MigrationHelper` en primer login post-update
   - Verificar datos migrados correctamente

7. **Actualizar UI** ⚠
   - Modificar Fragments para usar Repositories en lugar de Managers
   - Observar LiveData para actualizaciones reactivas
   - Manejar estados de sincronización (mostrar indicador de "sincronizando")

8. **Testing** ⚠
   - Tests unitarios para Repositories
   - Tests instrumentados para DAOs
   - Tests de integración para sincronización

---

## Próximos Pasos

1. Crear las entidades restantes basándose en los ejemplos
2. Implementar DAOs siguiendo el patrón de `CreditCardDao`
3. Completar `FinTrackDatabase` con todas las entidades y DAOs
4. Implementar todos los Repositories
5. Configurar Firebase y desplegar reglas de seguridad
6. Implementar `SyncRepository` completo
7. Actualizar UI para usar Repositories
8. Ejecutar migración y validar

---

**Autor**: Claude Code
**Fecha**: 2025-01-14
**Versión**: 1.0
**Estado**: Arquitectura base implementada, entidades y DAOs pendientes
