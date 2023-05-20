package com.example.blog

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.view.RedirectView


@RestController
@RequestMapping("/api/article")
class ArticleController(private val repository: ArticleRepository) {
    @GetMapping("/")
    fun findAll() = repository.findAllByOrderByAddedAtDesc()

    @GetMapping("/{slug}")
    fun findOne(@PathVariable slug: String) =
        repository.findBySlug(slug) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "DNE")
}

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userRepository: UserRepository,
    private val publicationsRepository: PublicationsRepository,
    @Value("\${api.key}") private val apiKey: String,
    @Value("\${admin.session.id}") private val adminSessionId : String,
) {
    @GetMapping("/")
    fun findAll(): Iterable<User> = userRepository.findAll()

    @PostMapping(path = ["/"], consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun postUser(userDto: UserHttpPostRequestDto): RedirectView {
        val publications = userDto.publications.map { publicationsRepository.findByIdOrNull(it) }
        userRepository.save(fromHttpPostDto(userDto, publications.toMutableList()))
        return when (userDto.designation) {
            "faculty" -> RedirectView("/admin/console/${adminSessionId}")
            "researcher" -> RedirectView("/admin/console/${adminSessionId}")
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Designation should be \"faculty\" or \"researcher\"")
        }
    }

    @PostMapping(path = ["/login"], consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun loginAdmin(adminLoginDto: AdminLoginDto): RedirectView {
        if (adminLoginDto.username == "admin" && (adminLoginDto.password == null || adminLoginDto.password == "admin" )) {
            return RedirectView("/admin/console/${adminSessionId}")
        }
        return RedirectView("/admin/console/error")
    }

    private fun fromHttpPostDto(userDto: UserHttpPostRequestDto, publications: MutableList<Publication?>): User = User(
        username = userDto.username,
        firstName = userDto.firstName,
        lastName = userDto.lastName,
        designation = userDto.designation,
        description = userDto.description,
        publications = publications,
        address = userDto.address,
        contactNumber = userDto.contactNumber,
        website = userDto.website,
        imageUrl = userDto.imageUrl,
        mails = userDto.mails
    )

    @GetMapping("/{login}")
    fun findOne(@PathVariable login: String) =
        userRepository.findByUsername(login) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No")

    @GetMapping("/faculty/all")
    fun findFaculties() = userRepository.findAllByDesignation("faculty").map { it.toHttpGetResponseDto() }
}

class AdminLoginDto(
    var username: String,
    var password: String? = null,
)

@RestController
@RequestMapping("/api/publications")
class PublicationsController(
    private val publicationsRepository: PublicationsRepository,
    private val userRepository: UserRepository,
    @Value("\${admin.session.id}") private val adminSessionId : String,
) {
    @GetMapping("/{authorUsername}")
    fun findByAuthorName(@PathVariable authorUsername: String): List<PublicationHttpGetResponseDto> =
        publicationsRepository.findAllByAuthorUsername(authorUsername)
            .map { it.toHttpGetResponseDto() }


    @PostMapping("/", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun postByAuthorName(
        requestDto: PublicationHttpPostRequestDto
    ): RedirectView {
        val authorUsername = requestDto.authorUsername
        val author = userRepository.findByUsername(authorUsername)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid author username")
        val publication = requestDto.toEntity(author)
        author.publications.add(publication)
        publicationsRepository.save(publication)
        return RedirectView("/admin/console/${adminSessionId}")
    }
}

@RestController
@RequestMapping("/api/subscriptions")
class SubscriptionController(private val subscriptionRepository: SubscriptionRepository) {
    @GetMapping("/")
    fun findAll() = subscriptionRepository.findAll()

    @PostMapping("/")
    fun postSubscription(requestDto: SubscriptionHttpPostRequestDto): RedirectView {
        subscriptionRepository.save(requestDto.toEntity())
        return RedirectView("/")
    }
}

@RestController
@RequestMapping("/api/feedback/")
class FeedbackController(private val feedbackRepository: FeedbackRepository) {
    @PostMapping("/", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun postFeedback(requestDto: FeedbackHttpPostRequestDto): RedirectView {
        feedbackRepository.save(requestDto.toEntity())
        return RedirectView("/contact")
    }
}

// Utility functions - wrappers and type aliases

private fun FeedbackHttpPostRequestDto.toEntity(): Feedback = Feedback(id = null, name = this.name, feedback = this.feedback)

private fun SubscriptionHttpPostRequestDto.toEntity(): Subscription = Subscription(mail = this.mail)

private fun Publication.toHttpPostResponseDto(): PublicationHttpPostResponseDto {
    return this.toHttpGetResponseDto()
}

typealias PublicationHttpPostResponseDto = PublicationHttpGetResponseDto;
typealias UserHttpPostResponseDto = User
typealias SubscriptionHttpPostResponseDto = Subscription

private fun PublicationHttpPostRequestDto.toEntity(author: User): Publication =
    Publication(title = this.title, journal = this.journal, id = null, url = this.url, author = author)

private fun Publication.toHttpGetResponseDto(): PublicationHttpGetResponseDto = PublicationHttpGetResponseDto(
    id = this.id,
    title = this.title,
    authorId = this.author?.id,
    journal = this.journal,
    url = this.url
)

private fun User.toHttpGetResponseDto() =
    UserHttpGetDto(
        username, firstName, lastName, designation, address, contactNumber, website, imageUrl, mails, id
    )