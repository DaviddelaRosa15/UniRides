# Firebase Storage Security Rules

## Configuración de reglas de seguridad para las imágenes de perfil

Para proteger las imágenes de perfil y asegurar que solo el usuario propietario pueda modificar su
foto, debes configurar las siguientes reglas en Firebase Storage.

---

## 📋 Reglas de Firebase Storage

Ve a **Firebase Console** → **Storage** → **Rules** y configura lo siguiente:

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Regla para imágenes de perfil
    match /profile_images/{userId} {
      // Permitir lectura a todos los usuarios autenticados
      allow read: if request.auth != null;
      
      // Permitir escritura solo al propietario de la imagen
      // Validar tamaño (máximo 5MB) y tipo de archivo (solo imágenes)
      allow write: if request.auth != null 
                   && request.auth.uid == userId
                   && request.resource.size < 5 * 1024 * 1024
                   && request.resource.contentType.matches('image/.*');
    }
    
    // Regla general para otras rutas (denegar por defecto)
    match /{allPaths=**} {
      allow read, write: if false;
    }
  }
}
```

### 📝 Explicación de las reglas:

1. **Lectura (read)**: Cualquier usuario autenticado puede ver las imágenes de perfil
2. **Escritura (write)**: Solo el usuario cuyo UID coincide con `{userId}` puede subir/actualizar su
   imagen
3. **Validaciones de seguridad**:
    - ✅ Solo usuarios autenticados
    - ✅ Solo el propietario puede modificar
    - ✅ Tamaño máximo: 5MB
    - ✅ Solo archivos de tipo imagen (`image/*`)
4. **Otras rutas**: Denegadas por defecto (seguridad adicional)

---

## 🚀 Pasos para aplicar las reglas

1. Abre **Firebase Console**
2. Ve a **Storage** en el menú lateral
3. Haz clic en la pestaña **"Rules"** (Reglas)
4. **Borra todo** el contenido actual
5. **Copia y pega** las reglas de Storage mostradas arriba
6. Haz clic en **"Publicar"** o **"Publish"**
7. Espera la confirmación de que se publicaron correctamente ✅

---

## 📁 Estructura de almacenamiento

Las imágenes se guardan con la siguiente estructura:

```
Storage (gs://tu-proyecto.appspot.com)
└── profile_images/
    ├── profile_userId1.jpg
    ├── profile_userId2.jpg
    └── profile_userId3.jpg
```

**Formato del nombre**: `profile_{userId}.jpg`

Donde `{userId}` es el UID único de Firebase Authentication.

---

## ✅ Validaciones implementadas

- ✅ Solo usuarios autenticados pueden leer imágenes
- ✅ Solo el propietario (UID coincide) puede escribir su imagen
- ✅ Tamaño máximo: **5 MB**
- ✅ Solo archivos de tipo **imagen** (`image/*`)

---

## 🔍 Solución de problemas

### ❌ Error: "Permission denied" al subir imagen

**Posibles causas:**

1. Las reglas no están publicadas correctamente
2. El usuario no está autenticado
3. El UID del usuario no coincide con el nombre del archivo

**Solución:**

- Verifica que publicaste las reglas
- Verifica que `FirebaseAuth.getInstance().currentUser != null`
- Verifica que el nombre del archivo es `profile_{userId}.jpg`

### ❌ Error: "File too large"

**Causa:** La imagen supera los 5MB

**Solución:**

- La app comprime automáticamente las imágenes a 800x800px con 85% de calidad
- Si aún así es muy grande, reduce la calidad en `ImageCompressor.kt`

---

## 📊 Monitoreo

Puedes monitorear el uso de Storage en:

**Firebase Console** → **Storage** → **Usage**

---

## 🔐 Notas de seguridad importantes

1. ⚠️ **Nunca permitas `allow read, write: if true;`** - Esto permite acceso público sin
   autenticación
2. ✅ **Siempre valida `request.auth != null`** - Asegura que el usuario está autenticado
3. ✅ **Valida el tamaño de archivo** - Evita que se suban archivos enormes
4. ✅ **Valida el tipo de archivo** - Solo permite los tipos que necesitas
5. ✅ **Usa el UID del usuario** - No confíes en datos que el cliente puede manipular

---

## 🎯 Resumen

Las reglas están configuradas para:

✅ Permitir que usuarios autenticados vean fotos de perfil
✅ Permitir que solo el propietario modifique su foto
✅ Validar tamaño (máximo 5MB)
✅ Validar tipo de archivo (solo imágenes)
✅ Denegar acceso no autorizado

**Estado:** ✅ Listo para producción

---

**Nota:** Para las reglas de Firestore Database, consulta el archivo `FIRESTORE_DATABASE_RULES.md`
