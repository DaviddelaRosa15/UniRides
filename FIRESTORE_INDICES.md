# Índices de Firestore - UniRides

## 📋 Índice Requerido: Mis Viajes Publicados

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
     - Campo: `date` → Orden: **Descending**
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
│ date             │ Descending  │
└──────────────────┴─────────────┘
```

**Nota:** Este índice es necesario porque Firestore requiere índices compuestos para queries que
combinan filtros (`where`) con ordenamiento (`orderBy`).
