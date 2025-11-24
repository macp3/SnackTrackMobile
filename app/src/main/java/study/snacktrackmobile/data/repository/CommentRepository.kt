package study.snacktrackmobile.data.repository

import study.snacktrackmobile.data.api.Request
import study.snacktrackmobile.data.model.dto.CommentRequest
import study.snacktrackmobile.data.model.dto.CommentResponse
import study.snacktrackmobile.data.model.dto.ReportedCommentRequest

class CommentRepository {
    private val api = Request.commentApi

    suspend fun getCommentsForMeal(token: String, mealId: Int): Result<List<CommentResponse>> { // <--- Dodaj token
        return try {
            val comments = api.getCommentsForMeal("Bearer $token", mealId) // <--- Przekaż token
            Result.success(comments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addComment(token: String, mealId: Int, content: String): Result<CommentResponse> {
        return try {
            val response = api.addComment("Bearer $token", CommentRequest(mealId, content))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editComment(token: String, mealId: Int, content: String?): Result<CommentResponse> {
        return try {
            val response = api.editComment("Bearer $token", CommentRequest(mealId, content))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteComment(token: String, mealId: Int): Result<Unit> {
        return try {
            val response = api.deleteComment("Bearer $token", mealId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Delete failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reportComment(token: String, commentId: Int, reason: String): Result<Unit> {
        return try {
            val response = api.reportComment("Bearer $token", ReportedCommentRequest(commentId, reason))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Report failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}