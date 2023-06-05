package com.wafflestudio.ggzz.global.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import org.springframework.stereotype.Service
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest
import java.io.ByteArrayInputStream

interface FirebaseConfig {
    fun verifyId(id: String): String
    fun getIdByToken(token: String): String
}

@Service
class FirebaseConfigImpl : FirebaseConfig {
    private val firebaseAuth: FirebaseAuth by lazy {
        try {
            // AWS Secrets Manager에 접근하기 위한 클라이언트 생성
            val secretsManager = SecretsManagerClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .build()

            val secretRequest = GetSecretValueRequest.builder()
                .secretId("dev/ggzz-server")
                .build()

            val secretResponseString = secretsManager.getSecretValue(secretRequest).secretString()

            // ObjectMapper instance 생성하여 secretResponseString 파싱
            val objectMapper = jacksonObjectMapper()
            val secrets: Map<String, Any> = objectMapper.readValue(secretResponseString)

            val googleServicesJsonString = secrets["google-services.json"] as? String
                ?: throw IllegalStateException("google-services.json not found in secrets")

            val googleCredentials =
                GoogleCredentials.fromStream(ByteArrayInputStream(googleServicesJsonString.toByteArray()))

            val options = FirebaseOptions.builder()
                .setCredentials(googleCredentials)
                .build()

            FirebaseApp.initializeApp(options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        FirebaseAuth.getInstance()
    }

    override fun verifyId(id: String): String =
        firebaseAuth.getUser(id).uid

    override fun getIdByToken(token: String): String =
        firebaseAuth.verifyIdToken(token).uid
}
