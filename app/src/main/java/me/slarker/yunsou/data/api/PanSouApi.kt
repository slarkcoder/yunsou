package me.slarker.yunsou.data.api

import me.slarker.yunsou.data.api.dto.ApiResponse
import me.slarker.yunsou.data.api.dto.CheckLinksRequest
import me.slarker.yunsou.data.api.dto.CheckLinksResponse
import me.slarker.yunsou.data.api.dto.SearchRequest
import me.slarker.yunsou.data.api.dto.SearchResponse
import me.slarker.yunsou.data.api.dto.HealthResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PanSouApi {

    @POST("api/search")
    suspend fun search(@Body request: SearchRequest): ApiResponse<SearchResponse>

    @POST("api/check/links")
    suspend fun checkLinks(@Body request: CheckLinksRequest): CheckLinksResponse

    @GET("api/health")
    suspend fun health(): HealthResponse
}
