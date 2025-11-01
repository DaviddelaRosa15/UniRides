package com.ddl.unirides.data.repository

import com.ddl.unirides.data.model.User
import com.ddl.unirides.domain.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    /**
     * ID del usuario actual autenticado
     */
    val currentUserId: String? get() = auth.currentUser?.uid

    /**
     * Usuario actual autenticado
     */
    val currentUser get() = auth.currentUser

    /**
     * Verifica si hay un usuario autenticado
     */
    val isUserAuthenticated: Boolean get() = auth.currentUser != null

    /**
     * Flujo reactivo del usuario actual
     * Se actualiza automáticamente cuando cambia el estado de autenticación
     */
    fun getCurrentUser(): Flow<Resource<User?>> = callbackFlow {
        trySend(Resource.Loading)

        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                // Obtener datos adicionales de Firestore
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val user = document.toObject(User::class.java)
                            trySend(Resource.Success(user))
                        } else {
                            // Crear usuario en Firestore si no existe
                            val newUser = User(
                                id = firebaseUser.uid,
                                name = firebaseUser.displayName ?: "",
                                email = firebaseUser.email ?: "",
                                verified = firebaseUser.isEmailVerified
                            )
                            firestore.collection("users")
                                .document(firebaseUser.uid)
                                .set(newUser)
                            trySend(Resource.Success(newUser))
                        }
                    }
                    .addOnFailureListener { e ->
                        trySend(Resource.Error(e.message ?: "Error al obtener usuario"))
                    }
            } else {
                trySend(Resource.Success(null))
            }
        }

        auth.addAuthStateListener(authStateListener)

        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    /**
     * Registrar nuevo usuario con email y contraseña
     * Solo permite emails .edu
     */
    suspend fun signUp(
        email: String,
        password: String,
        name: String,
        profilePictureUrl: String? = null
    ): Resource<User> {
        return try {
            // Crear usuario en Firebase Auth
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Resource.Error("Error al crear usuario")

            // Enviar email de verificación
            firebaseUser.sendEmailVerification().await()

            // Crear documento de usuario en Firestore
            val user = User(
                id = firebaseUser.uid,
                name = name,
                email = email,
                verified = false,
                profilePictureUrl = profilePictureUrl
            )

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(user)
                .await()

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al registrar usuario")
        }
    }

    /**
     * Iniciar sesión con email y contraseña
     */
    suspend fun signIn(email: String, password: String): Resource<User> {
        return try {
            // Autenticar con Firebase Auth
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Resource.Error("Error al iniciar sesión")

            // Obtener datos del usuario de Firestore
            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            if (!userDoc.exists()) {
                // Si el usuario no existe en Firestore, crearlo
                val user = User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: email,
                    verified = firebaseUser.isEmailVerified,
                    profilePictureUrl = firebaseUser.photoUrl?.toString()
                )
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .set(user)
                    .await()
                return Resource.Success(user)
            }

            val user = userDoc.toObject(User::class.java)
                ?: return Resource.Error("Error al obtener datos del usuario")

            // Actualizar el estado de verificación si ha cambiado
            if (user.verified != firebaseUser.isEmailVerified) {
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .update("verified", firebaseUser.isEmailVerified)
                    .await()
            }

            Resource.Success(user.copy(verified = firebaseUser.isEmailVerified))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al iniciar sesión")
        }
    }

    /**
     * Cerrar sesión del usuario actual
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Recargar información del usuario actual desde Firebase
     */
    suspend fun reloadUser(): Resource<Unit> {
        return try {
            auth.currentUser?.reload()?.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al recargar usuario")
        }
    }

    /**
     * Enviar email de verificación al usuario actual
     */
    suspend fun sendEmailVerification(): Resource<Unit> {
        return try {
            val user = auth.currentUser ?: return Resource.Error("Usuario no autenticado")
            user.sendEmailVerification().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al enviar email de verificación")
        }
    }

    /**
     * Verificar si el correo del usuario actual está verificado
     */
    suspend fun checkEmailVerified(): Resource<Boolean> {
        return try {
            val user = auth.currentUser ?: return Resource.Error("Usuario no autenticado")
            user.reload().await()

            // Actualizar en Firestore si cambió
            if (user.isEmailVerified) {
                firestore.collection("users")
                    .document(user.uid)
                    .update("verified", true)
                    .await()
            }

            Resource.Success(user.isEmailVerified)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al verificar email")
        }
    }

    /**
     * Enviar email para recuperar contraseña
     */
    suspend fun sendPasswordResetEmail(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al enviar email de recuperación")
        }
    }

    /**
     * Actualizar perfil del usuario en Firestore
     */
    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Resource<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update(updates)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al actualizar perfil")
        }
    }

    /**
     * Obtener usuario por ID
     */
    suspend fun getUserById(userId: String): Resource<User> {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val user = doc.toObject(User::class.java)
                ?: return Resource.Error("Usuario no encontrado")

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener usuario")
        }
    }

    /**
     * Actualizar nombre del usuario
     */
    suspend fun updateDisplayName(name: String): Resource<Unit> {
        return try {
            val userId = currentUserId ?: return Resource.Error("Usuario no autenticado")

            // Actualizar en Firestore
            firestore.collection("users")
                .document(userId)
                .update("name", name)
                .await()

            // Actualizar en Firebase Auth
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            auth.currentUser?.updateProfile(profileUpdates)?.await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al actualizar nombre")
        }
    }

    /**
     * Actualizar foto de perfil
     */
    suspend fun updateProfilePicture(photoUrl: String): Resource<Unit> {
        return try {
            val userId = currentUserId ?: return Resource.Error("Usuario no autenticado")

            // Actualizar en Firestore
            firestore.collection("users")
                .document(userId)
                .update("profilePictureUrl", photoUrl)
                .await()

            // Actualizar en Firebase Auth
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setPhotoUri(android.net.Uri.parse(photoUrl))
                .build()
            auth.currentUser?.updateProfile(profileUpdates)?.await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al actualizar foto de perfil")
        }
    }

    /**
     * Eliminar cuenta de usuario
     */
    suspend fun deleteAccount(): Resource<Unit> {
        return try {
            val userId = currentUserId ?: return Resource.Error("Usuario no autenticado")

            // Eliminar documento de Firestore
            firestore.collection("users")
                .document(userId)
                .delete()
                .await()

            // Eliminar cuenta de Firebase Auth
            auth.currentUser?.delete()?.await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al eliminar cuenta")
        }
    }

    /**
     * Cambiar contraseña
     */
    suspend fun changePassword(newPassword: String): Resource<Unit> {
        return try {
            val user = auth.currentUser ?: return Resource.Error("Usuario no autenticado")
            user.updatePassword(newPassword).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al cambiar contraseña")
        }
    }
}
