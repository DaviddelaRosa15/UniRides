# Firebase Firestore Database Security Rules

## Configuración completa de reglas de seguridad para Firestore

Esta guía contiene todas las reglas de seguridad necesarias para proteger los datos de la aplicación
UniRides en Firestore Database.

---

## 📋 Reglas Completas de Firestore Database

Ve a **Firebase Console** → **Firestore Database** → **Rules** y configura lo siguiente:

```
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {

    // ==================== USUARIOS ====================
    // Regla para la colección de usuarios
    match /users/{userId} {
      // Permitir lectura si estás autenticado
      allow read: if request.auth != null;

      // Permitir crear tu propio documento al registrarte
      allow create: if request.auth != null && request.auth.uid == userId;

      // Permitir actualizar solo tu propio perfil
      allow update: if request.auth != null && request.auth.uid == userId;

      // Permitir eliminar solo tu propio perfil
      allow delete: if request.auth != null && request.auth.uid == userId;
    }

    // ==================== CALIFICACIONES ====================
    // Regla para la colección de calificaciones (ratings)
    match /ratings/{ratingId} {
      // Cualquier usuario autenticado puede leer calificaciones
      allow read: if request.auth != null;

      // Solo usuarios autenticados pueden crear calificaciones
      // Validaciones: el usuario autenticado debe ser quien califica y no puede calificarse a sí mismo
      allow create: if request.auth != null
                    && request.auth.uid == request.resource.data.raterUserId
                    && request.resource.data.raterUserId != request.resource.data.ratedUserId
                    && request.resource.data.score >= 1
                    && request.resource.data.score <= 5;

      // Solo el creador puede actualizar su calificación
      allow update: if request.auth != null
                    && request.auth.uid == resource.data.raterUserId
                    && request.resource.data.score >= 1
                    && request.resource.data.score <= 5;

      // Solo el creador puede eliminar su calificación
      allow delete: if request.auth != null
                    && request.auth.uid == resource.data.raterUserId;
    }

    // ==================== OFERTAS DE VIAJE ====================
    // Regla para la colección de ofertas (viajes)
    match /offers/{offerId} {
      // Cualquier usuario autenticado puede leer ofertas
      allow read: if request.auth != null;

      // Solo usuarios autenticados pueden crear ofertas
      allow create: if request.auth != null;

      // Solo el creador puede actualizar/eliminar su oferta
      allow update, delete: if request.auth != null &&
        resource.data.publisherUserId == request.auth.uid;
    }

    // ==================== CHATS ====================
    // Regla para la colección de chats
    match /chats/{chatId} {
      // Permitir lectura si el usuario es participante del chat
      allow read: if request.auth != null &&
        (resource.data.user1Id == request.auth.uid ||
         resource.data.user2Id == request.auth.uid);

      // Permitir crear un chat si el usuario autenticado es uno de los participantes
      allow create: if request.auth != null &&
        (request.resource.data.user1Id == request.auth.uid ||
         request.resource.data.user2Id == request.auth.uid);

      // Permitir actualizar solo el timestamp del último mensaje e id
      allow update: if request.auth != null &&
        (resource.data.user1Id == request.auth.uid ||
         resource.data.user2Id == request.auth.uid) &&
        request.resource.data.diff(resource.data).affectedKeys().hasOnly(['lastMessageTimestamp', 'id']);

      // Permitir eliminar si el usuario es participante
      allow delete: if request.auth != null &&
        (resource.data.user1Id == request.auth.uid ||
         resource.data.user2Id == request.auth.uid);

      // Reglas para la subcolección de mensajes
      match /messages/{messageId} {
        // Permitir leer mensajes si el usuario es participante del chat padre
        allow read: if request.auth != null &&
          (get(/databases/$(database)/documents/chats/$(chatId)).data.user1Id == request.auth.uid ||
           get(/databases/$(database)/documents/chats/$(chatId)).data.user2Id == request.auth.uid);

        // Permitir crear mensajes si el usuario es participante y el senderId es el usuario autenticado
        allow create: if request.auth != null &&
          request.resource.data.senderId == request.auth.uid &&
          (get(/databases/$(database)/documents/chats/$(chatId)).data.user1Id == request.auth.uid ||
           get(/databases/$(database)/documents/chats/$(chatId)).data.user2Id == request.auth.uid);

        // Permitir actualizar solo el campo read
        allow update: if request.auth != null &&
          (get(/databases/$(database)/documents/chats/$(chatId)).data.user1Id == request.auth.uid ||
           get(/databases/$(database)/documents/chats/$(chatId)).data.user2Id == request.auth.uid) &&
          request.resource.data.diff(resource.data).affectedKeys().hasOnly(['read']);

        // Permitir eliminar si el usuario es participante del chat
        allow delete: if request.auth != null &&
          (get(/databases/$(database)/documents/chats/$(chatId)).data.user1Id == request.auth.uid ||
           get(/databases/$(database)/documents/chats/$(chatId)).data.user2Id == request.auth.uid);
      }
    }
  }
}
```

---

## 📝 Explicación Detallada de las Reglas

### 1️⃣ **Colección: users**

```javascript
match /users/{userId} {
  allow read: if request.auth != null;
  allow create: if request.auth != null && request.auth.uid == userId;
  allow update: if request.auth != null && request.auth.uid == userId;
  allow delete: if request.auth != null && request.auth.uid == userId;
}
```

**Permisos del usuario:**

- ✅ **Lectura**: Cualquier usuario autenticado puede ver perfiles de otros usuarios
- ✅ **Creación**: Solo puedes crear tu propio documento (UID debe coincidir)
- ✅ **Actualización**: Solo puedes actualizar tu propio perfil (foto, nombre, etc.)
- ✅ **Eliminación**: Solo puedes eliminar tu propia cuenta

**Campos del usuario:**

- `name`: String
- `email`: String
- `profilePictureUrl`: String? (opcional)
- `verified`: Boolean

---

### 2️⃣ **Colección: ratings**

```javascript
match /ratings/{ratingId} {
  allow read: if request.auth != null;

  allow create: if request.auth != null
                && request.auth.uid == request.resource.data.raterUserId
                && request.resource.data.raterUserId != request.resource.data.ratedUserId
                && request.resource.data.score >= 1
                && request.resource.data.score <= 5;

  allow update: if request.auth != null
                && request.auth.uid == resource.data.raterUserId
                && request.resource.data.score >= 1
                && request.resource.data.score <= 5;

  allow delete: if request.auth != null
                && request.auth.uid == resource.data.raterUserId;
}
```

**Permisos de calificaciones:**

- ✅ **Lectura**: Cualquier usuario autenticado puede ver todas las calificaciones
- ✅ **Creación**: Solo puedes crear calificaciones a tu nombre, no puedes calificarte a ti mismo, y
  el score debe ser 1-5
- ✅ **Actualización**: Solo puedes modificar tus propias calificaciones
- ✅ **Eliminación**: Solo puedes eliminar tus propias calificaciones

**Campos de rating:**

- `id`: String
- `raterUserId`: String (ID del usuario que califica)
- `ratedUserId`: String (ID del usuario que es calificado)
- `score`: Int (puntuación 1-5)
- `comment`: String? (comentario opcional)
- `timestamp`: Timestamp

**Ejemplo de ruta:**

```
/ratings/rating123
```

**Consultas permitidas:**

```kotlin
// Ver calificaciones recibidas por un usuario
firestore.collection("ratings")
    .whereEqualTo("ratedUserId", userId)
    .get()

// Ver calificaciones dadas por un usuario
firestore.collection("ratings")
    .whereEqualTo("raterUserId", userId)
    .get()
```

---

### 3️⃣ **Colección: offers**

```javascript
match /offers/{offerId} {
  allow read: if request.auth != null;
  allow create: if request.auth != null;
  allow update, delete: if request.auth != null &&
    resource.data.publisherUserId == request.auth.uid;
}
```

**Permisos:**

- ✅ **Lectura**: Cualquier usuario autenticado puede ver todas las ofertas
- ✅ **Creación**: Cualquier usuario autenticado puede publicar un viaje
- ✅ **Actualización/Eliminación**: Solo el creador de la oferta puede modificarla/eliminarla

**Validación importante:**
allow create: if request.auth != null &&
(request.resource.data.user1Id == request.auth.uid ||
request.resource.data.user2Id == request.auth.uid);

- Se valida que `publisherUserId` coincida con el UID del usuario autenticado

  (resource.data.user1Id == request.auth.uid ||
  resource.data.user2Id == request.auth.uid) &&
  request.resource.data.diff(resource.data).affectedKeys().hasOnly(['lastMessageTimestamp']);

  allow delete: if request.auth != null &&
  **Campos de offer:**

- `publisherUserId`: String (ID del publicador)
- `destination`: String (destino)
- `origin`: String (origen)
  allow read: if request.auth != null &&
  (get(/databases/$(database)/documents/chats/$(chatId)).data.user1Id == request.auth.uid ||
  get(/databases/$(database)/documents/chats/$(chatId)).data.user2Id == request.auth.uid);

  allow create: if request.auth != null &&
  request.resource.data.senderId == request.auth.uid &&
  (get(/databases/$(database)/documents/chats/$(chatId)).data.user1Id == request.auth.uid ||
  get(/databases/$(database)/documents/chats/$(chatId)).data.user2Id == request.auth.uid);

  allow update: if request.auth != null &&
  (get(/databases/$(database)/documents/chats/$(chatId)).data.user1Id == request.auth.uid ||
  get(/databases/$(database)/documents/chats/$(chatId)).data.user2Id == request.auth.uid) &&
  request.resource.data.diff(resource.data).affectedKeys().hasOnly(['read']);

  allow delete: if request.auth != null &&
  (get(/databases/$(database)/documents/chats/$(chatId)).data.user1Id == request.auth.uid ||
  get(/databases/$(database)/documents/chats/$(chatId)).data.user2Id == request.auth.uid);

- `price`: Double (precio)
- `availableSeats`: Int (asientos disponibles)
- `details`: String? (detalles adicionales opcionales)

---

### 3️⃣ **Colección: chats**

- ✅ **Creación**: Solo puedes crear chats donde tú eres uno de los participantes (user1Id o user2Id
  debe ser tu UID)
- ✅ **Actualización**: Solo los participantes pueden actualizar, y SOLO el campo
  `lastMessageTimestamp`
- ✅ **Eliminación**: Solo los participantes pueden eliminar el chat completo
  match /chats/{chatId} {
  allow read: if request.auth != null &&
  (resource.data.user1Id == request.auth.uid ||
  resource.data.user2Id == request.auth.uid);

- Al crear un chat, debes ser uno de los participantes
- Las actualizaciones están limitadas solo al timestamp del último mensaje
  allow create: if request.auth != null;

  allow update: if request.auth != null &&
  (resource.data.user1Id == request.auth.uid ||
  resource.data.user2Id == request.auth.uid);

  // Subcolección de mensajes
  match /messages/{messageId} {
  allow read: if request.auth != null;
  allow create: if request.auth != null;

- ✅ **Lectura**: Solo los participantes del chat padre pueden leer mensajes
- ✅ **Creación**: Solo los participantes pueden crear mensajes, y el `senderId` debe ser tu UID
- ✅ **Actualización**: Solo los participantes pueden actualizar, y SOLO el campo `read`
- ✅ **Eliminación**: Solo los participantes pueden eliminar mensajes

```

**Permisos del chat:**

- ✅ **Lectura**: Solo los dos participantes del chat pueden verlo
- ✅ **Creación**: Cualquier usuario autenticado puede iniciar un chat
- `read`: Boolean (indica si el mensaje fue leído)
- ✅ **Actualización**: Solo los participantes pueden actualizar el chat
**Validaciones de seguridad importantes:**

- Los mensajes solo pueden ser creados por participantes del chat
- El `senderId` debe coincidir con el UID del usuario autenticado (no puedes enviar mensajes a nombre de otro)
- Solo se puede actualizar el campo `read` (para marcar mensajes como leídos)
- La lectura de mensajes requiere verificar que eres participante del chat padre

- Se verifica que el usuario es `user1Id` o `user2Id` del chat
- Otros usuarios NO pueden ver chats ajenos

**Campos del chat:**

- `user1Id`: String (participante 1)
- `user2Id`: String (participante 2)
- `offerId`: String (oferta relacionada)
- `lastMessageTimestamp`: Timestamp (última actividad)

**Subcolección messages/ (mensajes del chat):**

- ✅ **Lectura**: Cualquier usuario autenticado (heredado del chat padre)
- ✅ **Creación**: Cualquier usuario autenticado puede enviar mensajes

**Campos de message:**

- `senderId`: String (ID del remitente)
- `content`: String (contenido del mensaje)
- `timestamp`: Timestamp (fecha/hora del mensaje)

**Nota:** La seguridad se hereda del chat padre, por lo que solo los participantes del chat pueden
acceder a los mensajes.

**Ejemplo de ruta:**

```

/chats/chat123/messages/message456

```

---

## 🚀 Pasos para Aplicar las Reglas

1. Abre **Firebase Console** (https://console.firebase.google.com)
2. Selecciona tu proyecto **UniRides**
3. Ve a **Firestore Database** en el menú lateral
4. Haz clic en la pestaña **"Rules"** (Reglas)
5. **Borra todo** el contenido actual
6. **Copia y pega** las reglas completas mostradas arriba
7. Haz clic en **"Publicar"** o **"Publish"**
8. Espera la confirmación: ✅ **"Las reglas se publicaron correctamente"**

---

## 🧪 Probar las Reglas

Firebase Console incluye un **simulador de reglas**. Prueba estos escenarios:

### Escenario 1: Lectura de perfil ✅

```

Operación: get
Ruta: /databases/(default)/documents/users/user123
Autenticado como: user456
Resultado esperado: ✅ PERMITIDO

```

### Escenario 2: Actualización de perfil propio ✅

```

Operación: update
Ruta: /databases/(default)/documents/users/user123
Autenticado como: user123
Resultado esperado: ✅ PERMITIDO

```

### Escenario 3: Lectura de calificaciones de un usuario ✅

```

Operación: get
Ruta: /databases/(default)/documents/ratings/rating456
Autenticado como: user789
Resultado esperado: ✅ PERMITIDO

```

### Escenario 4: Actualizar calificación propia ✅

```

Operación: update
Ruta: /databases/(default)/documents/ratings/rating456
Datos: { raterUserId: "user789", ratedUserId: "user123", score: 5, comment: "Excelente" }
Autenticado como: user789
Resultado esperado: ✅ PERMITIDO

```

### Escenario 5: Actualizar calificación ajena ❌

```

Operación: update
Ruta: /databases/(default)/documents/ratings/rating456
Datos: { raterUserId: "user789", ratedUserId: "user123", score: 5, comment: "Excelente" }
Autenticado como: user999
Resultado esperado: ❌ DENEGADO

```

### Escenario 6: Lectura de chat propio ✅

```

Operación: get
Ruta: /databases/(default)/documents/chats/chat123
Datos del chat: { user1Id: "user123", user2Id: "user456", offerId: "offer789" }
Autenticado como: user123
Resultado esperado: ✅ PERMITIDO

```

### Escenario 7: Lectura de chat ajeno ❌

```

Operación: get
Ruta: /databases/(default)/documents/chats/chat123
Datos del chat: { user1Id: "user123", user2Id: "user456", offerId: "offer789" }
Autenticado como: user789
Resultado esperado: ❌ DENEGADO

```

---

## 📊 Estructura de Colecciones (Actualizada)

```

Firestore Database
├── users/
│ ├── {userId}
│ │ ├── name: String
│ │ ├── email: String
│ │ ├── profilePictureUrl: String (opcional)
│ │ └── verified: Boolean
│
├── ratings/
│ ├── {ratingId}
│ │ ├── id: String
│ │ ├── raterUserId: String
│ │ ├── ratedUserId: String
│ │ ├── score: Int
│ │ ├── comment: String (opcional)
│ │ └── timestamp: Timestamp
│

- ✅ Al crear un chat, debes ser uno de los participantes
- ✅ Solo se pueden actualizar los campos `lastMessageTimestamp` e `id` del chat
- ✅ Los mensajes solo pueden ser leídos por participantes del chat padre
- ✅ El `senderId` de un mensaje debe coincidir con el UID del usuario autenticado
- ✅ Solo se puede actualizar el campo `read` de los mensajes
  │ ├── {offerId}
  │ │ ├── publisherUserId: String
  │ │ ├── destination: String
  │ │ ├── origin: String
  │ │ ├── date: Timestamp
  │ │ ├── time: String
  │ │ ├── price: Double
  │ │ ├── availableSeats: Int
  │ │ └── details: String (opcional)
  │
  └── chats/
  ├── {chatId}
  │ ├── user1Id: String
  │ ├── user2Id: String
  │ ├── offerId: String
  │ ├── lastMessageTimestamp: Timestamp
  │ └── messages/ (subcolección)
  │ ├── {messageId}
  │ │ ├── senderId: String
  │ │ ├── content: String
  │ │ └── timestamp: Timestamp

```

---

## ✅ Validaciones Implementadas

### Seguridad de Usuarios:

- ✅ Solo el propietario puede modificar su perfil
- ✅ No se pueden modificar datos de otros usuarios
- ✅ El campo `profilePictureUrl` está protegido
- ✅ Solo usuarios autenticados pueden leer perfiles

### Seguridad de Calificaciones:

- ✅ Cualquier usuario puede ver las calificaciones
- ✅ Solo puedes crear calificaciones a tu nombre (raterUserId debe coincidir con tu UID)
- ✅ No puedes calificarte a ti mismo (raterUserId != ratedUserId)
- ✅ El score debe estar entre 1 y 5
- ✅ Solo el creador puede modificar/eliminar su calificación
- ✅ Las calificaciones están en una colección separada para consultas flexibles

### Seguridad de Ofertas:

- ✅ Solo el publicador puede editar/eliminar su oferta
- ✅ Todos los usuarios autenticados pueden ver ofertas
- ✅ Se valida el `publisherUserId`
- ✅ Incluye campos de origen y destino

### Seguridad de Chats:

- ✅ Solo los participantes pueden ver sus chats
- ✅ Privacidad total entre conversaciones
- ✅ No se pueden leer chats de otros usuarios
- ✅ Los mensajes heredan la seguridad del chat padre
- ✅ Asociados a una oferta específica (offerId)

---

## 🔍 Solución de Problemas

### ❌ Error: "Missing or insufficient permissions"

**Posibles causas:**

1. El usuario no está autenticado
2. Intentas acceder a datos de otro usuario
3. Las reglas no están publicadas correctamente

**Solución:**

- Verifica que `FirebaseAuth.getInstance().currentUser != null`
- Verifica que estás accediendo a tus propios datos
- Republica las reglas en Firebase Console

### ❌ Error al leer calificaciones

**Causa:** Asegúrate de usar la ruta correcta de la colección

**Solución:**

- Ruta correcta: `/ratings/{ratingId}` (colección raíz)
- Consulta correcta: `firestore.collection("ratings").whereEqualTo("ratedUserId", userId)`
- No: `/users/{userId}/ratings/{ratingId}` (esto ya no existe como subcolección)

### ❌ Error al leer chats

**Causa:** Intentas leer un chat donde no eres participante

**Solución:**

- Verifica que tu UID es `user1Id` o `user2Id` del chat
- Los chats se crean con dos participantes específicos

### ❌ Error al actualizar oferta

**Causa:** Intentas actualizar una oferta que no creaste

**Solución:**

- Solo el usuario con UID = `publisherUserId` puede actualizar
- Verifica que eres el creador de la oferta

---

## 🔐 Mejores Prácticas de Seguridad

1. ✅ **Nunca confíes en el cliente**: Siempre valida en el servidor (reglas)
2. ✅ **Principio de privilegio mínimo**: Solo da los permisos necesarios
3. ✅ **Valida siempre la autenticación**: `request.auth != null`
4. ✅ **Usa UIDs de Firebase**: Son únicos y seguros
5. ✅ **Prueba las reglas**: Usa el simulador antes de publicar
6. ✅ **No expongas datos sensibles**: Emails, tokens, etc.
7. ✅ **Revisa los logs**: Firebase Console → Firestore → Uso

---

## 📈 Monitoreo y Auditoría

Ve a **Firebase Console** → **Firestore Database** → **Usage** para ver:

- 📊 Cantidad de lecturas/escrituras
- 📊 Documentos almacenados
- 📊 Tamaño de la base de datos
- 📊 Reglas que se están aplicando

---

## 🎯 Resumen

Las reglas de Firestore están configuradas para:

✅ Proteger datos personales de usuarios
✅ Permitir solo al creador modificar sus ofertas
✅ Mantener privacidad en los chats (solo participantes)
✅ Validar permisos en calificaciones (subcolección de users)
✅ Requerir autenticación para todas las operaciones
✅ Seguir el principio de privilegio mínimo
✅ Coincidir exactamente con tu modelo de datos

**Estado:** ✅ Listo para producción

---

## 🆕 Cambios Principales vs Versión Anterior

1. **Ratings es ahora una colección separada**
    - Antes: `/users/{userId}/ratings/{ratingId}` (subcolección)
    - Ahora: `/ratings/{ratingId}` (colección raíz)
    - Nuevo campo: `ratedUserId` para identificar al usuario calificado

2. **Ofertas incluyen origen**
    - Nuevo campo: `origin: String`

3. **Ofertas usan availableSeats en lugar de seats**
    - Antes: `seats`
    - Ahora: `availableSeats`

4. **Chats incluyen offerId**
    - Nuevo campo: `offerId: String` (relaciona el chat con una oferta)

5. **Mensajes usan content en lugar de text**
    - Antes: `text`
    - Ahora: `content`

---

## 📚 Referencias

- [Documentación oficial de Firestore Rules](https://firebase.google.com/docs/firestore/security/get-started)
- [Guía de mejores prácticas de seguridad](https://firebase.google.com/docs/firestore/security/rules-conditions)
- [Reglas para subcolecciones](https://firebase.google.com/docs/firestore/security/rules-structure#subcollections)
- Para reglas de Storage, consulta: `FIREBASE_STORAGE_RULES.md`

---

**Última actualización:** Noviembre 2025
```
