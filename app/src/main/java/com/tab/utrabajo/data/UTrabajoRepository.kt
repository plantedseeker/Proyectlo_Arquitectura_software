package com.tab.utrabajo.data

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.tab.utrabajo.BuildConfig
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

fun interface Cancellable {
    fun remove()
}

class UTrabajoRepository private constructor(private val context: Context) {
    companion object {
        @Volatile private var instance: UTrabajoRepository? = null

        fun initialize(context: Context) {
            if (instance == null) synchronized(this) {
                if (instance == null) instance = UTrabajoRepository(context.applicationContext)
            }
        }

        fun getInstance(): UTrabajoRepository = instance
            ?: error("UTrabajoRepository no está inicializado. Llame initialize() desde MainActivity.")
    }

    private val gson = Gson()
    private val preferences = context.getSharedPreferences("utrabajo_session", Context.MODE_PRIVATE)
    private val handler = Handler(Looper.getMainLooper())

    private val api: ApiService by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                preferences.getString("token", null)?.let { builder.header("Authorization", "Bearer $it") }
                chain.proceed(builder.build())
            }
            .build()

        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }

    fun getCurrentUser(): SessionUser? = preferences.getString("user", null)
        ?.let { runCatching { gson.fromJson(it, SessionUser::class.java) }.getOrNull() }

    private fun saveSession(response: AuthResponse) {
        preferences.edit()
            .putString("token", response.token)
            .putString("user", gson.toJson(response.user))
            .apply()
    }

    private fun clearSession() = preferences.edit().clear().apply()

    private fun errorMessage(response: Response<*>): String {
        val fallback = "Error ${response.code()} al comunicarse con el servidor"
        return runCatching {
            gson.fromJson(response.errorBody()?.string(), ApiError::class.java)?.message
        }.getOrNull().orEmpty().ifBlank { fallback }
    }

    private fun <T> execute(call: Call<T>, onSuccess: (T?) -> Unit, onError: (String) -> Unit) {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) onSuccess(response.body()) else onError(errorMessage(response))
            }

            override fun onFailure(call: Call<T>, throwable: Throwable) {
                onError(throwable.message ?: "No fue posible conectar con la API local")
            }
        })
    }

    fun registerStudent(email: String, password: String, fullName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        execute(api.registerStudent(RegisterStudentRequest(email, password, fullName)), { result ->
            result?.let(::saveSession)
            if (result != null) onSuccess() else onError("El servidor devolvió una respuesta vacía")
        }, onError)
    }

    fun registerCompany(nit: String, phone: String, email: String, workers: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        execute(api.registerCompany(RegisterCompanyRequest(nit, phone, email, workers.toIntOrNull() ?: 0, password)), { result ->
            result?.let(::saveSession)
            if (result != null) onSuccess() else onError("El servidor devolvió una respuesta vacía")
        }, onError)
    }

    fun loginUser(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        execute(api.login(LoginRequest(email, password)), { result ->
            result?.let(::saveSession)
            if (result != null) onSuccess() else onError("El servidor devolvió una respuesta vacía")
        }, onError)
    }

    fun logout() {
        api.logout().enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) = clearSession()
            override fun onFailure(call: Call<Unit>, throwable: Throwable) = clearSession()
        })
    }

    fun getUserProfile(userId: String, onSuccess: (Map<String, Any?>) -> Unit, onError: (String) -> Unit) {
        execute(api.profile(), { onSuccess(it ?: emptyMap()) }, onError)
    }

    fun updateUserProfile(userId: String, phone: String?, address: String?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        execute(api.updateProfile(UpdateProfileRequest(phone = phone, address = address)), { onSuccess() }, onError)
    }

    fun saveStudentWorkInfo(userId: String, worksNow: Boolean, companyName: String = "", role: String = "", onSuccess: () -> Unit, onError: (String) -> Unit) {
        execute(api.saveWorkInfo(WorkInfoRequest(worksNow, companyName, role)), { onSuccess() }, onError)
    }

    fun saveStudentSkills(userId: String, skills: List<String>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        execute(api.saveSkills(SkillsRequest(skills)), { onSuccess() }, onError)
    }

    private fun uriPart(uri: Uri, partName: String, fallbackName: String): MultipartBody.Part {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri) ?: "application/octet-stream"
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalArgumentException("No fue posible leer el archivo")
        require(bytes.size <= 10 * 1024 * 1024) { "El archivo supera el límite de 10 MB" }
        return MultipartBody.Part.createFormData(partName, fallbackName, bytes.toRequestBody(mime.toMediaTypeOrNull()))
    }

    private fun textPart(value: String): RequestBody = value.toRequestBody("text/plain".toMediaTypeOrNull())

    fun uploadAvatar(userId: String, imageUri: Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit) = runCatching {
        api.uploadAvatar(uriPart(imageUri, "file", "avatar.jpg"))
    }.fold(
        onSuccess = { call -> execute(call, { result -> result?.url?.let(onSuccess) ?: onError("Respuesta de archivo vacía") }, onError) },
        onFailure = { onError(it.message ?: "No fue posible leer la imagen") },
    )

    fun uploadCV(fileUri: Uri, userId: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) = runCatching {
        api.uploadCv(uriPart(fileUri, "file", "cv.pdf"))
    }.fold(
        onSuccess = { call -> execute(call, { result -> result?.url?.let(onSuccess) ?: onError("Respuesta de archivo vacía") }, onError) },
        onFailure = { onError(it.message ?: "No fue posible leer el CV") },
    )

    fun saveCompanyRepresentative(userId: String, repName: String, docType: String, docNumber: String, docUri: Uri?, onSuccess: () -> Unit, onError: (String) -> Unit) = runCatching {
        api.saveRepresentative(textPart(repName), textPart(docType), textPart(docNumber), docUri?.let { uriPart(it, "file", "representante.pdf") })
    }.fold(
        onSuccess = { execute(it, { onSuccess() }, onError) },
        onFailure = { onError(it.message ?: "No fue posible leer el documento") },
    )

    fun uploadCompanyDocuments(userId: String, rutUri: Uri?, camaraComercioUri: Uri?, onSuccess: () -> Unit, onError: (String) -> Unit) = runCatching {
        api.uploadCompanyDocuments(
            rutUri?.let { uriPart(it, "rut", "rut.pdf") },
            camaraComercioUri?.let { uriPart(it, "chamber", "camara_comercio.pdf") },
        )
    }.fold(
        onSuccess = { execute(it, { onSuccess() }, onError) },
        onFailure = { onError(it.message ?: "No fue posible leer los documentos") },
    )

    fun createJobOffer(companyId: String, title: String, description: String, requirements: List<String>, salary: String?, location: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        execute(api.createJob(JobRequest(title, description, requirements, salary, location)), { onSuccess() }, onError)
    }

    fun updateJobOffer(jobId: String, title: String?, salary: String?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        execute(api.updateJob(jobId, JobUpdateRequest(title, salary)), { onSuccess() }, onError)
    }

    fun deleteJobOffer(jobId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        execute(api.deleteJob(jobId), { onSuccess() }, onError)
    }

    private fun nonNullMaps(items: List<Map<String, Any?>>?): List<Map<String, Any>> = items.orEmpty().map { map ->
        map.mapValues { (_, value) -> value ?: "" }
    }

    fun getActiveJobOffers(onSuccess: (List<Map<String, Any>>) -> Unit, onError: (String) -> Unit) {
        execute(api.activeJobs(), { onSuccess(nonNullMaps(it)) }, onError)
    }

    private fun poll(periodMs: Long = 4_000, load: () -> Unit): Cancellable {
        var active = true
        lateinit var task: Runnable
        task = Runnable {
            if (!active) return@Runnable
            load()
            handler.postDelayed(task, periodMs)
        }
        handler.post(task)
        return Cancellable { active = false; handler.removeCallbacks(task) }
    }

    fun listenToActiveJobOffers(onUpdate: (List<Map<String, Any>>) -> Unit, onError: (String) -> Unit): Cancellable =
        poll { getActiveJobOffers(onUpdate, onError) }

    fun applyToJob(jobId: String, studentId: String, companyId: String, jobTitle: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        execute(api.apply(ApplicationRequest(jobId)), { onSuccess() }, onError)
    }

    fun getStudentApplications(studentId: String, onSuccess: (List<Map<String, Any>>) -> Unit, onError: (String) -> Unit) {
        execute(api.applications(), { onSuccess(nonNullMaps(it)) }, onError)
    }

    fun cancelApplication(applicationId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        execute(api.cancelApplication(applicationId), { onSuccess() }, onError)
    }

    fun createOrGetChat(studentId: String, companyId: String, jobId: String, jobTitle: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        execute(api.createChat(ChatRequest(jobId)), { result -> result?.id?.let(onSuccess) ?: onError("Respuesta de chat vacía") }, onError)
    }

    fun sendMessage(chatId: String, senderId: String, messageText: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        execute(api.sendMessage(chatId, MessageRequest(messageText)), { onSuccess() }, onError)
    }

    fun getChatsForUser(userId: String, userType: String, onSuccess: (List<Map<String, Any>>) -> Unit, onError: (String) -> Unit) {
        execute(api.chats(), { onSuccess(nonNullMaps(it)) }, onError)
    }

    fun listenToChats(onUpdate: (List<Map<String, Any>>) -> Unit, onError: (String) -> Unit): Cancellable =
        poll { getChatsForUser("", "", onUpdate, onError) }

    fun getMessages(chatId: String, onSuccess: (List<Map<String, Any>>) -> Unit, onError: (String) -> Unit) {
        execute(api.messages(chatId), { onSuccess(nonNullMaps(it)) }, onError)
    }

    fun listenToMessages(chatId: String, onUpdate: (List<Map<String, Any>>) -> Unit, onError: (String) -> Unit): Cancellable =
        poll { getMessages(chatId, onUpdate, onError) }
}
