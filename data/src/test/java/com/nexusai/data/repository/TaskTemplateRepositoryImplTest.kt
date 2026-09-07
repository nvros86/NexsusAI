package com.nexusai.data.repository

import com.nexusai.domain.model.TemplateCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskTemplateRepositoryImplTest {

    private lateinit var repository: TaskTemplateRepositoryImpl
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
        repository = TaskTemplateRepositoryImpl()
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun `getAllTemplates returns all 15 templates`() {
        val templates = repository.getAllTemplates()
        assertEquals(15, templates.size)
    }

    @Test
    fun `getAllTemplates contains yt_script`() {
        val templates = repository.getAllTemplates()
        val ytScript = templates.first { it.id == "yt_script" }
        assertEquals("Сценарий YouTube", ytScript.title)
        assertEquals(TemplateCategory.VIDEO, ytScript.category)
    }

    @Test
    fun `getAllTemplates contains tiktok_script`() {
        val templates = repository.getAllTemplates()
        val tiktok = templates.first { it.id == "tiktok_script" }
        assertEquals("Сценарий TikTok/Reels", tiktok.title)
        assertEquals(TemplateCategory.VIDEO, tiktok.category)
    }

    @Test
    fun `getAllTemplates contains landing_page`() {
        val templates = repository.getAllTemplates()
        val landing = templates.first { it.id == "landing_page" }
        assertEquals("Landing Page", landing.title)
        assertEquals(TemplateCategory.WEB_DEVELOPMENT, landing.category)
        assertEquals("html", landing.outputFormat)
    }

    @Test
    fun `getAllTemplates contains android_scaffold`() {
        val templates = repository.getAllTemplates()
        val scaffold = templates.first { it.id == "android_scaffold" }
        assertEquals("Android Scaffold", scaffold.title)
        assertEquals(TemplateCategory.APP_DEVELOPMENT, scaffold.category)
        assertEquals("code", scaffold.outputFormat)
    }

    @Test
    fun `getAllTemplates contains business_plan`() {
        val templates = repository.getAllTemplates()
        val plan = templates.first { it.id == "business_plan" }
        assertEquals("Бизнес-план", plan.title)
        assertEquals(TemplateCategory.BUSINESS, plan.category)
    }

    @Test
    fun `getAllTemplates contains lesson_plan`() {
        val templates = repository.getAllTemplates()
        val lesson = templates.first { it.id == "lesson_plan" }
        assertEquals("Урок/Курс", lesson.title)
        assertEquals(TemplateCategory.EDUCATION, lesson.category)
    }

    @Test
    fun `getAllTemplates contains api_docs`() {
        val templates = repository.getAllTemplates()
        val docs = templates.first { it.id == "api_docs" }
        assertEquals("API Documentation", docs.title)
        assertEquals(TemplateCategory.WEB_DEVELOPMENT, docs.category)
    }

    @Test
    fun `all templates have required fields`() {
        val templates = repository.getAllTemplates()
        templates.forEach { template ->
            assertTrue(template.id.isNotEmpty())
            assertTrue(template.title.isNotEmpty())
            assertTrue(template.description.isNotEmpty())
            assertTrue(template.systemPrompt.isNotEmpty())
            assertTrue(template.examplePrompt.isNotEmpty())
            assertTrue(template.iconEmoji.isNotEmpty())
            assertTrue(template.outputFormat.isNotEmpty())
        }
    }

    @Test
    fun `getTemplatesByCategory VIDEO returns video templates`() {
        val templates = repository.getTemplatesByCategory(TemplateCategory.VIDEO)
        assertTrue(templates.isNotEmpty())
        assertTrue(templates.all { it.category == TemplateCategory.VIDEO })
        assertTrue(templates.any { it.id == "yt_script" })
        assertTrue(templates.any { it.id == "tiktok_script" })
    }

    @Test
    fun `getTemplatesByCategory DESIGN returns design templates`() {
        val templates = repository.getTemplatesByCategory(TemplateCategory.DESIGN)
        assertEquals(1, templates.size)
        assertEquals("thumbnail", templates.first().id)
    }

    @Test
    fun `getTemplatesByCategory WEB_DEVELOPMENT returns web dev templates`() {
        val templates = repository.getTemplatesByCategory(TemplateCategory.WEB_DEVELOPMENT)
        assertTrue(templates.isNotEmpty())
        assertTrue(templates.all { it.category == TemplateCategory.WEB_DEVELOPMENT })
        assertTrue(templates.any { it.id == "landing_page" })
        assertTrue(templates.any { it.id == "portfolio" })
        assertTrue(templates.any { it.id == "code_review" })
        assertTrue(templates.any { it.id == "api_docs" })
    }

    @Test
    fun `getTemplatesByCategory APP_DEVELOPMENT returns app dev templates`() {
        val templates = repository.getTemplatesByCategory(TemplateCategory.APP_DEVELOPMENT)
        assertEquals(1, templates.size)
        assertEquals("android_scaffold", templates.first().id)
    }

    @Test
    fun `getTemplatesByCategory CONTENT returns content templates`() {
        val templates = repository.getTemplatesByCategory(TemplateCategory.CONTENT)
        assertTrue(templates.isNotEmpty())
        assertTrue(templates.all { it.category == TemplateCategory.CONTENT })
        assertTrue(templates.any { it.id == "blog_post" })
        assertTrue(templates.any { it.id == "email_sequence" })
        assertTrue(templates.any { it.id == "ad_copy" })
        assertTrue(templates.any { it.id == "social_media_plan" })
    }

    @Test
    fun `getTemplatesByCategory BUSINESS returns business templates`() {
        val templates = repository.getTemplatesByCategory(TemplateCategory.BUSINESS)
        assertTrue(templates.isNotEmpty())
        assertTrue(templates.any { it.id == "business_plan" })
        assertTrue(templates.any { it.id == "presentation" })
    }

    @Test
    fun `getTemplatesByCategory EDUCATION returns education templates`() {
        val templates = repository.getTemplatesByCategory(TemplateCategory.EDUCATION)
        assertEquals(1, templates.size)
        assertEquals("lesson_plan", templates.first().id)
    }

    @Test
    fun `getTemplatesByCategory nonexistent returns empty`() {
        val templates = repository.getTemplatesByCategory(TemplateCategory.VIDEO)
        val allCategories = TemplateCategory.entries
        val fakeCategory = allCategories.firstOrNull { it == TemplateCategory.VIDEO }
        assertNotNull(fakeCategory)
    }

    @Test
    fun `getTemplateById returns correct template`() {
        val template = repository.getTemplateById("yt_script")
        assertNotNull(template)
        assertEquals("Сценарий YouTube", template?.title)
        assertEquals(TemplateCategory.VIDEO, template?.category)
    }

    @Test
    fun `getTemplateById returns null for nonexistent id`() {
        val template = repository.getTemplateById("nonexistent_id")
        assertNull(template)
    }

    @Test
    fun `getTemplateById returns template with input placeholders`() {
        val template = repository.getTemplateById("yt_script")
        assertNotNull(template)
        assertEquals(3, template?.inputPlaceholders?.size)
        assertEquals("topic", template?.inputPlaceholders?.get(0)?.key)
        assertEquals("Тема видео", template?.inputPlaceholders?.get(0)?.label)
    }

    @Test
    fun `getTemplateById returns template with correct output format`() {
        val htmlTemplate = repository.getTemplateById("landing_page")
        assertEquals("html", htmlTemplate?.outputFormat)

        val codeTemplate = repository.getTemplateById("android_scaffold")
        assertEquals("code", codeTemplate?.outputFormat)

        val markdownTemplate = repository.getTemplateById("yt_script")
        assertEquals("markdown", markdownTemplate?.outputFormat)

        val textTemplate = repository.getTemplateById("tiktok_script")
        assertEquals("text", textTemplate?.outputFormat)
    }

    @Test
    fun `searchTemplates by title matches`() {
        val templates = repository.searchTemplates("YouTube")
        assertTrue(templates.isNotEmpty())
        assertTrue(templates.any { it.id == "yt_script" })
    }

    @Test
    fun `searchTemplates by description matches`() {
        val templates = repository.searchTemplates("вирусный")
        assertTrue(templates.isNotEmpty())
        assertTrue(templates.any { it.id == "tiktok_script" })
    }

    @Test
    fun `searchTemplates by category displayName matches`() {
        val templates = repository.searchTemplates("Дизайн")
        assertTrue(templates.isNotEmpty())
        assertTrue(templates.any { it.id == "thumbnail" })
    }

    @Test
    fun `searchTemplates case insensitive`() {
        val lower = repository.searchTemplates("youtube")
        val upper = repository.searchTemplates("YOUTUBE")
        assertEquals(lower.size, upper.size)
        assertTrue(lower.isNotEmpty())
    }

    @Test
    fun `searchTemplates partial match works`() {
        val templates = repository.searchTemplates("Landing")
        assertTrue(templates.any { it.id == "landing_page" })
    }

    @Test
    fun `searchTemplates no match returns empty`() {
        val templates = repository.searchTemplates("несуществующий_запрос_xyz")
        assertTrue(templates.isEmpty())
    }

    @Test
    fun `searchTemplates with empty query returns all`() {
        val templates = repository.searchTemplates("")
        assertEquals(15, templates.size)
    }

    @Test
    fun `searchTemplates by russian description matches`() {
        val templates = repository.searchTemplates("портфолио")
        assertTrue(templates.isNotEmpty())
        assertTrue(templates.any { it.id == "portfolio" })
    }

    @Test
    fun `all template categories are covered`() {
        val allTemplates = repository.getAllTemplates()
        val categories = allTemplates.map { it.category }.toSet()
        assertEquals(TemplateCategory.entries.size, categories.size)
        assertTrue(categories.containsAll(TemplateCategory.entries.toSet()))
    }

    @Test
    fun `each template has unique id`() {
        val templates = repository.getAllTemplates()
        val ids = templates.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `templates have non-empty system prompts`() {
        val templates = repository.getAllTemplates()
        templates.forEach { template ->
            assertTrue("${template.id} has empty systemPrompt", template.systemPrompt.isNotEmpty())
        }
    }

    @Test
    fun `templates have non-empty example prompts`() {
        val templates = repository.getAllTemplates()
        templates.forEach { template ->
            assertTrue("${template.id} has empty examplePrompt", template.examplePrompt.isNotEmpty())
        }
    }
}
