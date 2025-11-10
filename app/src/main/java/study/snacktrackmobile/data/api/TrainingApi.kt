package study.snacktrackmobile.data.api

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import study.snacktrackmobile.data.model.dto.TrainingDetailsResponseDTO
import study.snacktrackmobile.data.model.dto.TrainingInfoDTO

import retrofit2.http.*
import retrofit2.Response


interface TrainingApi {

    @GET("trainings/my")
    suspend fun getUserTraining(@Header("Authorization") token: String): TrainingInfoDTO

    @GET("trainings/my/details")
    suspend fun getUserTrainingDetails(@Header("Authorization") token: String): TrainingDetailsResponseDTO

    @GET("trainings")
    suspend fun getAllTrainings(@Header("Authorization") token: String): List<TrainingInfoDTO>

    @POST("trainings/assign/{trainingId}")
    suspend fun assignTraining(
        @Path("trainingId") trainingId: Int,
        @Header("Authorization") token: String
    ): Response<Unit>

    @DELETE("trainings/my/deprive")
    suspend fun depriveTraining(@Header("Authorization") token: String): Response<Unit>
}
