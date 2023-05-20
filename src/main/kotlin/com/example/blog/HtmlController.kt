package com.example.blog

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Controller
class HtmlController(
    private val articleRepository: ArticleRepository,
    private val userRepository: UserRepository,
    private val publicationsRepository: PublicationsRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val feedbackRepository: FeedbackRepository,
    @Value("\${admin.session.id}") private val sessionId: String
) {
    @GetMapping("/")
    fun blog(model: Model): String {
        model["title"] = "SERL IIIT Allahabad"
        return "home"
    }

    @GetMapping("/article/{slug}")
    fun article(model: Model, @PathVariable slug: String): String {
        val article = articleRepository.findBySlug(slug = slug)?.render() ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No such article!"
        )
        model["article"] = article
        model["title"] = article.title
        return "article"
    }

    @GetMapping("/faculty")
    fun faculty(model: Model): String {
        val faculties = userRepository.findAllByDesignation("faculty")
        model["title"] = "Faculty - SERL IIITA"
        model["faculties"] = faculties.map { it.render() }
        return "faculty"
    }

    @GetMapping("/publications")
    fun publications(model: Model): String {
        val publications = publicationsRepository.findAll()
        model["title"] = "Publications - SERL IIITA"
        model["publications"] = publications.mapIndexed { index, publication -> publication.render(index + 1) }
        return "publications"
    }

    @GetMapping("/publications/{authorUsername}")
    fun publicationsByAuthor(model: Model, @PathVariable authorUsername: String): String {
        val publications = publicationsRepository.findAllByAuthorUsername(authorUsername)
        val author = userRepository.findByUsername(authorUsername)
        model["title"] = "Publications by ${author?.firstName} ${author?.lastName}"
        model["publications"] = publications.mapIndexed { index, publication -> publication.render(index + 1) }
        return "publications"
    }

    @GetMapping("/researchers")
    fun researchers(model: Model): String {
        val researchers = userRepository.findAllByDesignation("researcher")
        model["title"] = "Researchers - SERL IIITA"
        model["researchers"] = researchers.map { it.render() }
        return "researchers"
    }

    @GetMapping("/resources")
    fun resources(model: Model): String {
        model["title"] = "Resources - SERL IIITA"
        return "resources"
    }

    @GetMapping("/contact")
    fun contact(model: Model): String {
        model["title"] = "Contact Us - SERL IIITA"
        return "contact"
    }

    @GetMapping("/admin/console/{sessionId}")
    fun admin(model: Model, @PathVariable sessionId: String): String {
        return if (sessionId == this.sessionId) {
            model["title"] = "Admin Console"
            model["faculties"] = userRepository.findAllByDesignation("faculty").map { it.render() }
            model["researchers"] = userRepository.findAllByDesignation("researcher").map { it.render() }
            model["publications"] = publicationsRepository.findAll().mapIndexed { index, publication -> publication.render(index + 1) }
            model["subscriptions"] = subscriptionRepository.findAll()
            model["feedback"] = feedbackRepository.findAll()
            "admin-console";
        } else {
            "admin-error";
        }
    }
}

private fun Publication.render(index: Int = 0): PublicationHtmlRenderDto = PublicationHtmlRenderDto(
    index = index,
    title = this.title,
    url = this.url ?: defaultUrl(),
    journal = this.journal ?: defaultJournal(),
    author = this.author.renderForPublication()
)

private fun User?.renderForPublication(): AuthorHtmlRenderDto =
    if (this == null) AuthorHtmlRenderDto(
        firstName = "null",
        lastName = "null",
        username = "null"
    ) else AuthorHtmlRenderDto(firstName, lastName, username)

fun defaultJournal(): String = metaDefault(element = "Journal", kind = "Publication")

fun defaultUrl(): String = metaDefault(element = "URL", kind = "Publication")

private fun User.render(): UserHtmlRenderDto = UserHtmlRenderDto(
    firstName = firstName,
    lastName = lastName,
    designation = designation ?: defaultDesignation(),
    description = description ?: defaultDescription(),
    address = address ?: defaultAddress(),
    contactNumber = contactNumber ?: defaultContactNumber(),
    website = website ?: defaultWebsite(),
    imageUrl = imageUrl ?: defaultImageUrl(),
    mails = mails ?: defaultMails()
)

fun defaultDescription(): String = metaDefault("Description")

fun defaultDesignation(): String = metaDefault("Designation")

fun defaultMails(): String = metaDefault("EMails")

fun defaultImageUrl(): String =
    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSvP6ebHtO8_ghJf0RYy-8l_kmSEDdl-fPvknMoPrl8&s"

fun metaDefault(element: String, kind: String = "user"): String = "[No entry for $element on this $kind]"

fun defaultWebsite(): String = metaDefault("Website")

fun defaultContactNumber(): String = metaDefault("Contact Number")

fun defaultAddress(): String = metaDefault("Address")

data class UserHtmlRenderDto(
    var firstName: String,
    var lastName: String,
    var designation: String? = null,
    var address: String? = null,
    var contactNumber: String? = null,
    var website: String? = null,
    var imageUrl: String? = null,
    var mails: String? = null,
    var description: String? = null
)

private fun Article.render() = RenderedArticle(
    slug,
    title,
    headline,
    content,
    author,
    addedAt.format()
)

private fun LocalDateTime.format() = this.format(DateTimeFormatter.ISO_DATE)

data class RenderedArticle(
    val slug: String,
    val title: String,
    val headline: String,
    val content: String,
    val author: User,
    val addedAt: String
)