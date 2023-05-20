package com.example.blog

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import java.time.LocalDateTime
import java.util.*

fun String.toSlug() = lowercase(Locale.getDefault())
    .replace("\n", " ")
    .replace("[^a-z\\d\\s]".toRegex(), " ")
    .split(" ")
    .joinToString("-")
    .replace("-+".toRegex(), "-")

@Entity
open class Article(
    var title: String,
    var headline: String,
    var content: String,
    @ManyToOne var author: User,
    var slug: String = title.toSlug(),
    var addedAt: LocalDateTime = LocalDateTime.now(),
    @Id @GeneratedValue var id: Long? = null
)

@Entity
open class User(
    var username: String,
    var firstName: String,
    var lastName: String,
    var designation: String? = null,
    var description: String? = null,
    @OneToMany(mappedBy = "author", cascade = arrayOf(CascadeType.ALL))
    var publications: MutableList<Publication?> = mutableListOf(),
    var address: String? = null,
    var contactNumber: String? = null,
    var website: String? = null,
    var imageUrl: String? = null,
    var mails: String? = null,
    @Id @GeneratedValue var id: Long? = null
)

@Entity
class Faculty(
    @Id @GeneratedValue var id: Long? = null,
    var name: String,
    var designation: String,
    var address: String,
    var contactNumber: String,
    var website: String,
    var mails: String
)

@Entity
class Resource(
    @Id @GeneratedValue var id: Long? = null,
    var name: String,
    var imageUrl: String,
    var purchaseUrl: String
)

@Entity
class Publication(
    @Id @GeneratedValue var id: Long? = null,
    var title: String,
    @ManyToOne
    var author: User? = null,
    var journal: String? = null,
    var url: String? = null
)

@Entity
class Subscription(
    @Id @GeneratedValue var id: Long? = null,
    var mail: String? = null
)

@Entity
class Feedback(
    @Id @GeneratedValue var id: Long? = null,
    var name: String? = null,
    var feedback: String? = null,
)

data class SubscriptionHttpPostRequestDto(
    var mail: String? = null
)

data class PublicationHtmlRenderDto(
    val title: String,
    val journal: String,
    val url: String,
    val author: AuthorHtmlRenderDto,
    val index: Int
)

data class AuthorHtmlRenderDto (
    val firstName: String,
    val lastName: String,
    val username: String
)

data class PublicationHttpGetResponseDto(
    var id: Long?,
    var title: String,
    var authorId: Long?,
    var journal: String? = null,
    var url: String? = null
)

data class PublicationHttpPostRequestDto(
    var title: String,
    var authorUsername: String,
    var journal: String? = null,
    var url: String? = null
)

data class UserHttpGetDto(
    var username: String,
    var firstName: String,
    var lastName: String,
    var designation: String? = null,
    var address: String? = null,
    var contactNumber: String? = null,
    var website: String? = null,
    var imageUrl: String? = null,
    var mails: String? = null,
    var id: Long? = null
)

data class UserHttpPostRequestDto(
    var username: String,
    var firstName: String,
    var lastName: String,
    var designation: String,
    var publications: List<Long> = emptyList(),
    var address: String? = null,
    var description: String? = null,
    var contactNumber: String? = null,
    var website: String? = null,
    var imageUrl: String? = null,
    var mails: String? = null
)

data class FeedbackHttpPostRequestDto(
    var name: String? = null,
    var feedback: String? = null
)