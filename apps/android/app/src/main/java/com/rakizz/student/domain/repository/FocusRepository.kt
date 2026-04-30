package com.rakizz.student.domain.repository

import com.rakizz.student.domain.model.FocusPolicy

interface FocusRepository {
    suspend fun getPolicies(): Result<List<FocusPolicy>>

    suspend fun syncInstalledApps(): Result<Int>
}