package net.kimleo.rec.repository

import net.kimleo.rec.rule.RuleLoader
import net.kimleo.rec.rule.impl.Unique
import org.junit.Assert.*
import org.junit.Test
import java.io.BufferedReader
import java.io.InputStreamReader

class RecRepositoryTest {
    val records = linesOfRes("person_test.txt")
    val rec = linesOfRes("person_test.txt.rec")

    fun linesOfRes(file: String): List<String> {
        val stream = RecRepositoryTest::class.java.classLoader.getResourceAsStream(file)
        val reader = BufferedReader(InputStreamReader(stream))

        val lines = reader.readLines()
        reader.close()
        return lines
    }
    @Test
    fun testRepository() {
        val type = DefaultRecConfig.makeTypeFrom(rec)
        val collect = RecordSet.loadData(records.stream(), type)
        val repo = RecRepository(listOf(collect))

        assertNotNull(repo)

        assertTrue(repo.from("Person").where("first name") { it.contains("Kimmy") }.records.count() == 1L)

        val (unique1) = Unique().verify(listOf(collect.select("first name")))
        assertTrue(unique1)

        val (unique2, result) = Unique().verify(listOf(collect.select("comment")))

        assertFalse(unique2)
        assertTrue(result.size == 1)

        val names = repo["Person[first name], Person[comment] as Comment"]

        assertTrue(names.size == 2)
        assertTrue(names[1].records.count() == 5L)

        val ruleResult = RuleLoader()
                .load(listOf("unique: Person[first name]", "unique: Person[comment]")).map {
            it.runOn(repo)
        }.toList()

        assert(ruleResult.size == 2)

        assert(ruleResult[1].second.size == 1)
    }
}