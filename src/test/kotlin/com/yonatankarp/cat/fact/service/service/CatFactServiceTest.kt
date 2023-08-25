package com.yonatankarp.cat.fact.service.service

import com.yonatankarp.cat.fact.service.repository.CatFactRepository
import com.yonatankarpcat.fact.client.ports.Fact
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestConstructor

@OptIn(ExperimentalCoroutinesApi::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class CatFactServiceTest {

    private val repository: CatFactRepository = mockk<CatFactRepository>()
    private val service: CatFactService = CatFactService(repository)

    @Test
    fun `should store facts to repository`() = runTest {
        // Given
        val facts = setOf(Fact("fact1"), Fact("fact2"))
        coEvery { repository.storeFacts(any()) } returns true

        // When
        service.storeFacts(facts)

        // Then
        coVerify(exactly = 2) { repository.storeFacts(any()) }
    }
}
