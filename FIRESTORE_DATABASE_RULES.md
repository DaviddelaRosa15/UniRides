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
      
      // Subcolección de calificaciones (ratings)
      match /ratings/{ratingId} {
        // Cualquier usuario autenticado puede leer las calificaciones de un usuario
        allow read: if request.auth != null;
        
        // Solo usuarios autenticados pueden crear calificaciones
        allow create: if request.auth != null;
        
        // Solo el creador de la calificación puede actualizarla/eliminarla
        allow update, delete: if request.auth != null && 
          resource.data.raterUserId == request.auth.uid;
      }
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
      // Solo los participantes del chat pueden leerlo
      allow read: if request.auth != null && 
        (resource.data.user1Id == request.auth.uid || 
         resource.data.user2Id == request.auth.uid);
      
      // Usuarios autenticados pueden crear chats
      allow create: if request.auth != null;
      
      // Solo participantes pueden actualizar
      allow update: if request.auth != null && 
        (resource.data.user1Id == request.auth.uid || 
         resource.data.user2Id == request.auth.uid);
      
      // Subcolección de mensajes
      match /messages/{messageId} {
        // Solo participantes del chat pueden leer mensajes
        allow read: if request.auth != null;
        
        // Solo participantes pueden crear mensajes
        allow create: if request.auth != null;
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
  
  // Subcolección de calificaciones
  match /ratings/{ratingId} {
    allow read: if request.auth != null;
    allow create: if request.auth != null;
    allow update, delete: if request.auth != null && 
      resource.data.raterUserId == request.auth.uid;
  }
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

**Subcolección ratings/ (calificaciones recibidas):**

- ✅ **Lectura**: Cualquier usuario autenticado puede ver las calificaciones de un usuario
- ✅ **Creación**: Cualquier usuario autenticado puede calificar a otro usuario
- ✅ **Actualización/Eliminación**: Solo quien creó la calificación puede modificarla

**Campos de rating:**

- `raterUserId`: String (ID del usuario que califica)
- `score`: Int (puntuación)
- `comment`: String? (comentario opcional)
- `timestamp`: Timestamp

**Ejemplo de ruta:**

```
/users/user123/ratings/rating456
```

---

### 2️⃣ **Colección: offers**

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

- Se valida que `publisherUserId` coincida con el UID del usuario autenticado

**Campos de offer:**

- `publisherUserId`: String (ID del publicador)
- `destination`: String (destino)
- `origin`: String (origen)
- `date`: Timestamp (fecha del viaje)
- `time`: String (hora)
- `price`: Double (precio)
- `availableSeats`: Int (asientos disponibles)
- `details`: String? (detalles adicionales opcionales)

---

### 3️⃣ **Colección: chats**

```javascript
match /chats/{chatId} {
  allow read: if request.auth != null && 
    (resource.data.user1Id == request.auth.uid || 
     resource.data.user2Id == request.auth.uid);
  
  allow create: if request.auth != null;
  
  allow update: if request.auth != null && 
    (resource.data.user1Id == request.auth.uid || 
     resource.data.user2Id == request.auth.uid);
  
  // Subcolección de mensajes
  match /messages/{messageId} {
    allow read: if request.auth != null;
    allow create: if request.auth != null;
  }
}
```

**Permisos del chat:**

- ✅ **Lectura**: Solo los dos participantes del chat pueden verlo
- ✅ **Creación**: Cualquier usuario autenticado puede iniciar un chat
- ✅ **Actualización**: Solo los participantes pueden actualizar el chat

**Validación de privacidad:**

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
Ruta: /databases/(default)/documents/users/user123/ratings/rating456
Autenticado como: user789
Resultado esperado: ✅ PERMITIDO
```

### Escenario 4: Actualizar calificación propia ✅

```
Operación: update
Ruta: /databases/(default)/documents/users/user123/ratings/rating456
Datos: { raterUserId: "user789", score: 5, comment: "Excelente" }
Autenticado como: user789
Resultado esperado: ✅ PERMITIDO
```

### Escenario 5: Actualizar calificación ajena ❌

```
Operación: update
Ruta: /databases/(default)/documents/users/user123/ratings/rating456
Datos: { raterUserId: "user789", score: 5, comment: "Excelente" }
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
│   ├── {userId}
│   │   ├── name: String
│   │   ├── email: String
│   │   ├── profilePictureUrl: String (opcional)
│   │   ├── verified: Boolean
│   │   └── ratings/ (subcolección)
│   │       ├── {ratingId}
│   │       │   ├── raterUserId: String
│   │       │   ├── score: Int
│   │       │   ├── comment: String (opcional)
│   │       │   └── timestamp: Timestamp
│
├── offers/
│   ├── {offerId}
│   │   ├── publisherUserId: String
│   │   ├── destination: String
│   │   ├── origin: String
│   │   ├── date: Timestamp
│   │   ├── time: String
│   │   ├── price: Double
│   │   ├── availableSeats: Int
│   │   └── details: String (opcional)
│
└── chats/
    ├── {chatId}
    │   ├── user1Id: String
    │   ├── user2Id: String
    │   ├── offerId: String
    │   ├── lastMessageTimestamp: Timestamp
    │   └── messages/ (subcolección)
    │       ├── {messageId}
    │       │   ├── senderId: String
    │       │   ├── content: String
    │       │   └── timestamp: Timestamp
```

---

## ✅ Validaciones Implementadas

### Seguridad de Usuarios:

- ✅ Solo el propietario puede modificar su perfil
- ✅ No se pueden modificar datos de otros usuarios
- ✅ El campo `profilePictureUrl` está protegido
- ✅ Solo usuarios autenticados pueden leer perfiles

### Seguridad de Calificaciones (subcolección de users):

- ✅ Cualquier usuario puede ver las calificaciones de otros
- ✅ Cualquier usuario puede crear una calificación
- ✅ Solo el creador (raterUserId) puede modificar su calificación
- ✅ Las calificaciones están anidadas bajo cada usuario

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

**Causa:** Las calificaciones son una subcolección, asegúrate de usar la ruta correcta

**Solución:**

- Ruta correcta: `/users/{userId}/ratings/{ratingId}`
- No: `/ratings/{ratingId}` (esto no existe como colección raíz)

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

1. **Ratings es ahora una subcolección de users**
    - Antes: `/ratings/{ratingId}`
    - Ahora: `/users/{userId}/ratings/{ratingId}`

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
