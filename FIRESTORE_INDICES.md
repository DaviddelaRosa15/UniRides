# Índices de Firestore - UniRides

## 📋 Índice 1: Mis Viajes Publicados

Para que funcione la consulta de "Mis Viajes", necesitas crear un índice compuesto en Firestore.

### Como crear el índice

1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Selecciona tu proyecto: **compose-firebase-codelab-6ab52**
3. En el menú lateral: **Firestore Database** → **Índices**
4. Click en **"Crear índice"**
5. Configura así:
    - **Colección:** `offers`
    - **Campos a indexar:**
        - Campo: `publisherUserId` → Orden: **Ascending**
        - Campo: `dateTime` → Orden: **Descending**
    - **Ámbito de consulta:** Collection
6. Click en **"Crear"**
7. Espera 2-5 minutos hasta que el estado sea "Habilitado"

## 📊 Configuración del Índice

```
Colección: offers
Índice: Compuesto

Campos indexados:
┌──────────────────┬─────────────┐
│ Campo            │ Orden       │
├──────────────────┼─────────────┤
│ publisherUserId  │ Ascending   │
│ dateTime         │ Descending  │
└──────────────────┴─────────────┘
```

---

## 📋 Índice 2: Búsqueda de Chats (user1Id + user2Id + offerId)

Para buscar chats existentes entre dos usuarios para una oferta específica.

### Como crear el índice

1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Selecciona tu proyecto: **compose-firebase-codelab-6ab52**
3. En el menú lateral: **Firestore Database** → **Índices**
4. Click en **"Crear índice"**
5. Configura así:
    - **Colección:** `chats`
    - **Campos a indexar:**
        - Campo: `user1Id` → Orden: **Ascending**
        - Campo: `user2Id` → Orden: **Ascending**
        - Campo: `offerId` → Orden: **Ascending**
    - **Ámbito de consulta:** Collection
6. Click en **"Crear"**
7. Espera 2-5 minutos hasta que el estado sea "Habilitado"

## 📊 Configuración del Índice

```
Colección: chats
Índice: Compuesto

Campos indexados:
┌──────────────┬─────────────┐
│ Campo        │ Orden       │
├──────────────┼─────────────┤
│ user1Id      │ Ascending   │
│ user2Id      │ Ascending   │
│ offerId      │ Ascending   │
└──────────────┴─────────────┘
```

---

## 🔗 Crear Índices Automáticamente

Alternativamente, puedes dejar que Firebase te sugiera crear los índices:

1. Ejecuta la app y prueba las funcionalidades
2. Cuando encuentres un error de "requires an index", copia el enlace de la consola
3. El enlace te llevará directamente a crear el índice correcto
4. Click en **"Crear índice"**

**Nota:** Los índices compuestos son necesarios porque Firestore requiere índices para queries que
combinan múltiples filtros `whereEqualTo` o filtros con `orderBy`.
