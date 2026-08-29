package com.nexusai.data.repository

import com.nexusai.domain.model.Prompt
import com.nexusai.domain.model.PromptCategory
import com.nexusai.domain.repository.PromptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptRepositoryImpl @Inject constructor() : PromptRepository {

    private val favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    private val usageCounts = MutableStateFlow<Map<String, Int>>(emptyMap())

    private val prompts = listOf(
        Prompt(
            id = "write_email",
            title = "Профессиональное письмо",
            description = "Написать формальное деловое письмо",
            content = "Напиши профессиональное деловое письмо по следующему описанию: [ОПИСАНИЕ]. Используй вежливый тон, структурируй текст с приветствием, основной частью и заключением.",
            category = PromptCategory.WRITING,
            tags = listOf("письмо", "бизнес", "формальное")
        ),
        Prompt(
            id = "write_blog",
            title = "Статья для блога",
            description = "Написать увлекательную статью для блога",
            content = "Напиши увлекательную статью для блога на тему: [ТЕМА]. Статья должна быть объёмом 1000-1500 слов, с заголовками, вводной частью, основным контентом и заключением. Используй разговорный стиль.",
            category = PromptCategory.WRITING,
            tags = listOf("блог", "статья", "контент")
        ),
        Prompt(
            id = "write_resume",
            title = "Резюме",
            description = "Составить профессиональное резюме",
            content = "Составь профессиональное резюме на основе следующей информации: [ИНФОРМАЦИЯ]. Включи разделы: контактные данные, опыт работы, образование, навыки, достижения. Формат — структурированный и лаконичный.",
            category = PromptCategory.BUSINESS,
            tags = listOf("резюме", "карьера", "вакансия")
        ),
        Prompt(
            id = "code_review",
            title = "Ревью кода",
            description = "Провести код-ревью и найти проблемы",
            content = "Проведи ревью следующего кода. Найди баги, проблемы с производительностью, нарушения best practices. Предложи конкретные улучшения.\n\n```\n[КОД]\n```",
            category = PromptCategory.CODING,
            tags = listOf("код", "ревью", "оптимизация")
        ),
        Prompt(
            id = "explain_code",
            title = "Объяснить код",
            description = "Пошагово объяснить как работает код",
            content = "Пошагово объясни как работает следующий код. Опиши каждую строку и общую логику.\n\n```\n[КОД]\n```",
            category = PromptCategory.CODING,
            tags = listOf("код", "объяснение", "обучение")
        ),
        Prompt(
            id = "debug_code",
            title = "Найти баг",
            description = "Помочь найти и исправить ошибку в коде",
            content = "Помоги найти ошибку в следующем коде. Опиши проблему и предложи исправление.\n\n```\n[КОД]\n```\n\nОжидаемое поведение: [ОЖИДАНИЕ]\nФактическое поведение: [РЕАЛЬНОСТЬ]",
            category = PromptCategory.CODING,
            tags = listOf("дебаг", "ошибка", "исправление")
        ),
        Prompt(
            id = "marketing_ad",
            title = "Рекламный текст",
            description = "Написать продающий рекламный текст",
            content = "Напиши продающий рекламный текст для продукта/услуги: [ОПИСАНИЕ]. Целевая аудитория: [АУДИТОРИЯ]. Текст должен быть коротким, цепляющим, с чётким CTA.",
            category = PromptCategory.MARKETING,
            tags = listOf("реклама", "продажи", "текст")
        ),
        Prompt(
            id = "social_media",
            title = "Пост для соцсетей",
            description = "Создать пост для Instagram/Twitter/Telegram",
            content = "Создай пост для соцсетей на тему: [ТЕМА]. Платформа: [ПЛАТФОРМА]. Добавь эмодзи, хэштеги, привлекательное начало. Объём: [ОБЪЁМ] символов.",
            category = PromptCategory.MARKETING,
            tags = listOf("соцсети", "пост", "контент")
        ),
        Prompt(
            id = "translate",
            title = "Перевод текста",
            description = "Перевести текст на другой язык с сохранением стиля",
            content = "Переведи следующий текст на [ЯЗЫК]. Сохрани стиль и тон оригинала. Если есть культурные особенности — адаптируй.\n\n[ТЕКСТ]",
            category = PromptCategory.TRANSLATION,
            tags = listOf("перевод", "язык", "локализация")
        ),
        Prompt(
            id = "brainstorm",
            title = "Брейнсторм",
            description = "Генерация идей по заданной теме",
            content = "Проведи брейнсторм по теме: [ТЕМА]. Сгенерируй минимум 10 идей, от самых обычных до самых креативных. Для каждой идеи кратко опиши суть и как её реализовать.",
            category = PromptCategory.CREATIVE,
            tags = listOf("идеи", "креатив", "генерация")
        ),
        Prompt(
            id = "summarize",
            title = "Резюмировать",
            description = "Кратко изложить основные тезисы",
            content = "Резюмируй следующий текст. Выдели 5-7 ключевых тезисов. Сохрани только самое важное.\n\n[ТЕКСТ]",
            category = PromptCategory.ANALYSIS,
            tags = listOf("резюме", "анализ", "сжатие")
        ),
        Prompt(
            id = "study_plan",
            title = "План обучения",
            description = "Составить структурированный план обучения",
            content = "Составь подробный план обучения по теме: [ТЕМА]. Уровень: [УРОВЕНЬ]. Продолжительность: [СРОК]. Включи теорию, практику, проекты и проверочные задания.",
            category = PromptCategory.EDUCATION,
            tags = listOf("обучение", "план", "курс")
        ),
        Prompt(
            id = "weekly_plan",
            title = "План на неделю",
            description = "Составить план задач на неделю",
            content = "Составь план на неделю. Приоритетные задачи: [ЗАДАЧИ]. Дедлайны: [ДЕДЛАЙНЫ]. Структурируй по дням с учётом приоритетов и реалистичной загрузки.",
            category = PromptCategory.DAILY,
            tags = listOf("план", "организация", "задачи")
        ),
        Prompt(
            id = "explain_concept",
            title = "Объяснить концепцию",
            description = "Простым языком объяснить сложную тему",
            content = "Объясни концепцию [КОНЦЕПЦИЯ] простым языком. Используй аналогии из повседневной жизни. Объясни так, чтобы понял пятиклассник.",
            category = PromptCategory.EDUCATION,
            tags = listOf("объяснение", "аналогия", "простой язык")
        ),
        Prompt(
            id = "compare_options",
            title = "Сравнение вариантов",
            description = "Сравнить два или более варианта",
            content = "Сравни следующие варианты:\n1. [ВАРИАНТ 1]\n2. [ВАРИАНТ 2]\n\nСравни по критериям: цена, качество, удобство, сроки. Составь таблицу и дай рекомендацию.",
            category = PromptCategory.ANALYSIS,
            tags = listOf("сравнение", "анализ", "выбор")
        ),
        Prompt(
            id = "story_writing",
            title = "Написать рассказ",
            description = "Написать короткий рассказ по сюжету",
            content = "Напиши короткий рассказ (1000-2000 слов) по сюжету: [СЮЖЕТ]. Стиль: [СТИЛЬ]. Главный герой: [ГЕРОЙ]. Добавь неожиданный поворот в конце.",
            category = PromptCategory.CREATIVE,
            tags = listOf("рассказ", "художественный", "сюжет")
        )
    )

    override fun getAllPrompts(): Flow<List<Prompt>> {
        return combine(favoriteIds, usageCounts) { favs, usage ->
            prompts.map { p ->
                p.copy(
                    isFavorite = favs.contains(p.id),
                    usageCount = usage[p.id] ?: 0
                )
            }
        }
    }

    override fun getPromptsByCategory(category: String): Flow<List<Prompt>> {
        return combine(favoriteIds, usageCounts) { favs, usage ->
            prompts.filter { it.category.name == category }
                .map { p ->
                    p.copy(
                        isFavorite = favs.contains(p.id),
                        usageCount = usage[p.id] ?: 0
                    )
                }
        }
    }

    override fun searchPrompts(query: String): Flow<List<Prompt>> {
        return combine(favoriteIds, usageCounts) { favs, usage ->
            prompts.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true) ||
                        it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
            }.map { p ->
                p.copy(
                    isFavorite = favs.contains(p.id),
                    usageCount = usage[p.id] ?: 0
                )
            }
        }
    }

    override fun getFavoritePrompts(): Flow<List<Prompt>> {
        return combine(favoriteIds, usageCounts) { favs, usage ->
            prompts.filter { favs.contains(it.id) }
                .map { p ->
                    p.copy(
                        isFavorite = true,
                        usageCount = usage[p.id] ?: 0
                    )
                }
        }
    }

    override suspend fun toggleFavorite(id: String) {
        favoriteIds.value = if (favoriteIds.value.contains(id)) {
            favoriteIds.value - id
        } else {
            favoriteIds.value + id
        }
    }

    override suspend fun incrementUsage(id: String) {
        val current = usageCounts.value
        usageCounts.value = current + (id to ((current[id] ?: 0) + 1))
    }
}
