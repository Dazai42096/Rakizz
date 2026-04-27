package com.rakizz.student.domain.repository

import com.rakizz.student.domain.model.FocusPolicy
import com.rakizz.student.domain.model.UsageSummary

interface FocusRepository {
    suspend fun getPolicies(): Result<List<FocusPolicy>>

    suspend fun getUsageSummary(days: Int): Result<UsageSummary>

    suspend fun syncDemoUsage(): Result<Int>
}