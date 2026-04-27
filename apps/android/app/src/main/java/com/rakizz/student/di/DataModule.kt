package com.rakizz.student.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.rakizz.student.data.local.TokenManager
import com.rakizz.student.data.repository.AssignmentRepositoryImpl
import com.rakizz.student.data.repository.AuthRepositoryImpl
import com.rakizz.student.data.repository.FocusRepositoryImpl
import com.rakizz.student.data.repository.MaterialRepositoryImpl
import com.rakizz.student.data.repository.QuizRepositoryImpl
import com.rakizz.student.domain.repository.AssignmentRepository
import com.rakizz.student.domain.repository.AuthRepository
import com.rakizz.student.domain.repository.FocusRepository
import com.rakizz.student.domain.repository.MaterialRepository
import com.rakizz.student.domain.repository.QuizRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rakizz_prefs")

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideTokenManager(dataStore: DataStore<Preferences>): TokenManager {
        return TokenManager(dataStore)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMaterialRepository(
        materialRepositoryImpl: MaterialRepositoryImpl
    ): MaterialRepository

    @Binds
    @Singleton
    abstract fun bindAssignmentRepository(
        assignmentRepositoryImpl: AssignmentRepositoryImpl
    ): AssignmentRepository

    @Binds
    @Singleton
    abstract fun bindQuizRepository(
        quizRepositoryImpl: QuizRepositoryImpl
    ): QuizRepository

    @Binds
    @Singleton
    abstract fun bindFocusRepository(
        focusRepositoryImpl: FocusRepositoryImpl
    ): FocusRepository
}