package com.nexusai.data.repository

import com.nexusai.domain.model.InputPlaceholder
import com.nexusai.domain.model.ProviderType
import com.nexusai.domain.model.TaskTemplate
import com.nexusai.domain.model.TemplateCategory
import com.nexusai.domain.repository.TaskTemplateRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskTemplateRepositoryImpl @Inject constructor() : TaskTemplateRepository {

    private val templates = listOf(
        TaskTemplate(
            id = "yt_script",
            title = "Сценарий YouTube",
            description = "Генерация сценария для YouTube-видео с интро, основной частью и аутро",
            category = TemplateCategory.VIDEO,
            iconEmoji = "🎬",
            systemPrompt = "Ты опытный сценарист YouTube. Создай engaging сценарий для видео. Включи: интро (15-30 сек), основную часть с hook-ами каждые 60 сек, аутро с CTA. Используй разговорный стиль.",
            examplePrompt = "Создай сценарий для видео о том как начать программировать на Kotlin",
            inputPlaceholders = listOf(
                InputPlaceholder("topic", "Тема видео"),
                InputPlaceholder("duration", "Длительность (мин)", "10"),
                InputPlaceholder("style", "Стиль", "Обучающий")
            ),
            outputFormat = "markdown"
        ),
        TaskTemplate(
            id = "tiktok_script",
            title = "Сценарий TikTok/Reels",
            description = "Короткий вирусный скрипт для TikTok или Instagram Reels",
            category = TemplateCategory.VIDEO,
            iconEmoji = "📱",
            systemPrompt = "Ты эксперт по вирусному контенту. Создай скрипт для короткого видео (15-60 сек). Начни с hook в первые 3 секунды. Используй тренды и паттерны вирусных видео.",
            examplePrompt = "Скрипт для TikTok про 5 советов по продуктивности",
            inputPlaceholders = listOf(
                InputPlaceholder("topic", "Тема"),
                InputPlaceholder("duration", "Длительность (сек)", "30")
            ),
            outputFormat = "text"
        ),
        TaskTemplate(
            id = "thumbnail",
            title = "Thumbnail для видео",
            description = "Генерация описания и промпта для thumbnail",
            category = TemplateCategory.DESIGN,
            iconEmoji = "🖼️",
            systemPrompt = "Ты дизайнер YouTube thumbnail. Создай detailed промпт для генерации яркого, кликабельного thumbnail. Опиши композицию, цвета, текст и эмоцию.",
            examplePrompt = "Thumbnail для видео про заработок на ИИ",
            inputPlaceholders = listOf(
                InputPlaceholder("topic", "Тема видео"),
                InputPlaceholder("mood", "Настроение", "Яркое")
            ),
            outputFormat = "text"
        ),
        TaskTemplate(
            id = "landing_page",
            title = "Landing Page",
            description = "Генерация HTML/CSS кода для лендинга",
            category = TemplateCategory.WEB_DEVELOPMENT,
            iconEmoji = "🌐",
            systemPrompt = "Ты senior frontend-разработчик. Создай современный, адаптивный лендинг на HTML и CSS. Используй Tailwind CSS CDN, чистый код, semantic HTML. Включи hero секцию, features, CTA, footer.",
            examplePrompt = "Лендинг для SaaS продукта по автоматизации бизнес-процессов",
            inputPlaceholders = listOf(
                InputPlaceholder("product_name", "Название продукта"),
                InputPlaceholder("description", "Описание продукта"),
                InputPlaceholder("features", "Ключевые фичи (через запятую)")
            ),
            outputFormat = "html"
        ),
        TaskTemplate(
            id = "portfolio",
            title = "Portfolio сайт",
            description = "Персональный сайт-портфолио",
            category = TemplateCategory.WEB_DEVELOPMENT,
            iconEmoji = "💼",
            systemPrompt = "Ты frontend-разработчик. Создай элегантный portfolio сайт на HTML/CSS. Minimalist дизайн, плавные анимации, dark theme. Включи hero, about, projects, contact секции.",
            examplePrompt = "Portfolio для мобильного разработчика",
            inputPlaceholders = listOf(
                InputPlaceholder("name", "Имя"),
                InputPlaceholder("profession", "Профессия"),
                InputPlaceholder("skills", "Навыки (через запятую)")
            ),
            outputFormat = "html"
        ),
        TaskTemplate(
            id = "android_scaffold",
            title = "Android Scaffold",
            description = "Генерация архитектуры Android-приложения",
            category = TemplateCategory.APP_DEVELOPMENT,
            iconEmoji = "📱",
            systemPrompt = "Ты Android-разработчик. Создай полную архитектуру Android-приложения на Kotlin + Jetpack Compose. Включи: build.gradle, data layer, domain layer, DI (Hilt), navigation, UI компоненты. Следуй Clean Architecture.",
            examplePrompt = "Приложение для списков задач с SQLite",
            inputPlaceholders = listOf(
                InputPlaceholder("app_type", "Тип приложения"),
                InputPlaceholder("features", "Функционал (через запятую)")
            ),
            outputFormat = "code"
        ),
        TaskTemplate(
            id = "blog_post",
            title = "Blog Post",
            description = "Длинный SEO-оптимизированный пост для блога",
            category = TemplateCategory.CONTENT,
            iconEmoji = "✍️",
            systemPrompt = "Ты профессиональный копирайтер. Напиши engaging blog-пост (1500-2500 слов). Включи: цепляющий заголовок, введение с hook, структурированные секции с подзаголовками, выводы, CTA. Оптимизируй под SEO.",
            examplePrompt = "Статья о преимуществах ИИ в бизнесе",
            inputPlaceholders = listOf(
                InputPlaceholder("topic", "Тема"),
                InputPlaceholder("audience", "Целевая аудитория", "Широкая"),
                InputPlaceholder("tone", "Тон", "Профессиональный")
            ),
            outputFormat = "markdown"
        ),
        TaskTemplate(
            id = "email_sequence",
            title = "Email-последовательность",
            description = "Серия писем для email-рассылки",
            category = TemplateCategory.CONTENT,
            iconEmoji = "📧",
            systemPrompt = "Ты email-маркетолог. Создай последовательность из 5-7 писем для onboarding. Каждое письмо: тема, preview text, основной контент, CTA. Прогрессия: Welcome → Value → Social Proof → Feature → Offer.",
            examplePrompt = "Email-серия для SaaS продукта (пробный период 14 дней)",
            inputPlaceholders = listOf(
                InputPlaceholder("product", "Продукт"),
                InputPlaceholder("benefits", "Ключевые преимущества")
            ),
            outputFormat = "markdown"
        ),
        TaskTemplate(
            id = "ad_copy",
            title = "Рекламный текст",
            description = "Короткий рекламный копи для соцсетей",
            category = TemplateCategory.CONTENT,
            iconEmoji = "📢",
            systemPrompt = "Ты performance-маркетолог. Создай рекламный копи для соцсетей. Формула: Hook → Problem → Solution → Benefit → CTA. Используй power-слова, эмодзи, короткие предложения.",
            examplePrompt = "Реклама для онлайн-курса по Python",
            inputPlaceholders = listOf(
                InputPlaceholder("product", "Продукт/услуга"),
                InputPlaceholder("platform", "Платформа", "Instagram"),
                InputPlaceholder("goal", "Цель", "Продажи")
            ),
            outputFormat = "text"
        ),
        TaskTemplate(
            id = "business_plan",
            title = "Бизнес-план",
            description = "Структурированный бизнес-план стартапа",
            category = TemplateCategory.BUSINESS,
            iconEmoji = "💼",
            systemPrompt = "Ты бизнес-аналитик. Создай краткий бизнес-план (executive summary уровень). Включи: проблему, решение, рынок, конкурентов, бизнес-модель, юнит-экономику, roadmap, команду.",
            examplePrompt = "Бизнес-план для стартапа в сфере EdTech",
            inputPlaceholders = listOf(
                InputPlaceholder("idea", "Идея стартапа"),
                InputPlaceholder("market", "Целевой рынок"),
                InputPlaceholder("budget", "Бюджет", "Не указан")
            ),
            outputFormat = "markdown"
        ),
        TaskTemplate(
            id = "lesson_plan",
            title = "Урок/Курс",
            description = "Структура образовательного урока или модуля курса",
            category = TemplateCategory.EDUCATION,
            iconEmoji = "📚",
            systemPrompt = "Ты instructional designer. Создай структуру урока/модуля курса. Включи: учебные цели, prerequisites, теория, практика, домашнее задание, оценивание. Применяй principles spaced repetition.",
            examplePrompt = "Урок по основам React для начинающих",
            inputPlaceholders = listOf(
                InputPlaceholder("subject", "Предмет/тема"),
                InputPlaceholder("level", "Уровень", "Начинающий"),
                InputPlaceholder("duration", "Длительность", "60 мин")
            ),
            outputFormat = "markdown"
        )
    )

    override fun getAllTemplates(): List<TaskTemplate> = templates

    override fun getTemplatesByCategory(category: TemplateCategory): List<TaskTemplate> =
        templates.filter { it.category == category }

    override fun getTemplateById(id: String): TaskTemplate? =
        templates.firstOrNull { it.id == id }

    override fun searchTemplates(query: String): List<TaskTemplate> =
        templates.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true) ||
                    it.category.displayName.contains(query, ignoreCase = true)
        }
}
